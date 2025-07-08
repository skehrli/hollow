/*
 *  Copyright 2016-2019 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.hollow.api.client;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import static com.netflix.hollow.core.util.Threads.daemonThread;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.custom.HollowAPI;
import com.netflix.hollow.api.sampling.EnabledSamplingDirector;
import com.netflix.hollow.core.read.dataaccess.HollowDataAccess;
import com.netflix.hollow.core.read.dataaccess.proxy.HollowProxyDataAccess;
import com.netflix.hollow.core.read.engine.HollowReadStateEngine;
import com.netflix.hollow.tools.history.HollowHistoricalStateDataAccess;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Detect stale Hollow references and USAGE of stale hollow references.
 *
 * When obtaining a reference to a hollow object, this reference is not intended to be held on to indefinitely.  This
 * class detects whether references are held and/or used beyond some expected lifetime.
 *
 * If objects are detected as held beyond some grace period but not used beyond that period, then they will be detached,
 * so they do not hang on to the entire historical data store beyond some length of time.
 *
 * This class is also responsible for notifying the HollowUpdateListener if stale references or usage is detected.
 */
public class StaleHollowReferenceDetector {

    /// Every HOUSEKEEPING_INTERVAL, in milliseconds, check to see whether
    /// a) We should transition from the GRACE_PERIOD to the DISABLE_TEST_PERIOD
    /// b) any object in the DISABLE_TEST_PERIOD has been accessed
    /// c) any hollow objects are referenced which we expect to be unreferenced
    /// and do the appropriate disabling / send the appropriate signals to the update listener.
    private static final long HOUSEKEEPING_INTERVAL = 30000L;
    private static final EnabledSamplingDirector ENABLED_SAMPLING_DIRECTOR = new EnabledSamplingDirector();

    private final List<HollowWeakReferenceHandle> handles;

    private final HollowConsumer.ObjectLongevityConfig config;

    private final HollowConsumer.ObjectLongevityDetector detector;

    private final StackTraceRecorder stackTraceRecorder;

    private Thread monitor;

    @Impure
    public StaleHollowReferenceDetector(HollowConsumer.ObjectLongevityConfig config, HollowConsumer.ObjectLongevityDetector detector) {
        this.handles = new ArrayList<HollowWeakReferenceHandle>();
        this.config = config;
        this.detector = detector;
        this.stackTraceRecorder = new StackTraceRecorder(25);
    }

    @SideEffectFree
    @Impure
    synchronized boolean isKnownAPIHandle(HollowAPI api) {
        for(HollowWeakReferenceHandle handle : handles)
            if(handle.isAPIHandled(api))
                return true;
        return false;
    }

    @Impure
    synchronized void newAPIHandle(HollowAPI api) {
        for(HollowWeakReferenceHandle handle : handles)
            handle.newAPIAvailable(api);
        handles.add(new HollowWeakReferenceHandle(api));
    }

    @Impure
    private synchronized int countStaleReferenceExistenceSignals() {
        int signals = 0;

        for(HollowWeakReferenceHandle handle : handles) {
            if(handle.isExistingStaleReferenceHint())
                signals++;
        }

        return signals;
    }

    @Impure
    private synchronized int countStaleReferenceUsageSignals() {
        int signals = 0;

        for(HollowWeakReferenceHandle handle : handles) {
            if(handle.hasBeenUsedSinceReset())
                signals++;
        }

        return signals;
    }

    @Impure
    private synchronized void housekeeping() {
        Iterator<HollowWeakReferenceHandle> iter = handles.iterator();
        while(iter.hasNext()) {
            HollowWeakReferenceHandle handle = iter.next();
            handle.housekeeping();
            if(handle.isFinished())
                iter.remove();
        }
    }

    @Impure
    public void startMonitoring() {
        if (monitor == null) {
            daemonThread(new Monitor(this), getClass(), "monitor")
                    .start();
        }
    }

    @Pure
    public StackTraceRecorder getStaleReferenceStackTraceRecorder() {
        return stackTraceRecorder;
    }

    private static class Monitor implements Runnable {

        private final WeakReference<StaleHollowReferenceDetector> ref;

        @Impure
        Monitor(StaleHollowReferenceDetector parent) {
            this.ref = new WeakReference<>(parent);
        }

        @Impure
        public void run() {
            while (ref.get() != null) {

                try {
                    Thread.sleep(HOUSEKEEPING_INTERVAL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                StaleHollowReferenceDetector parent = ref.get();
                if (parent != null) {
                    parent.housekeeping();

                    parent.detector.staleReferenceExistenceDetected(parent.countStaleReferenceExistenceSignals());
                    parent.detector.staleReferenceUsageDetected(parent.countStaleReferenceUsageSignals());
                }
            }
        }
    }

    private class HollowWeakReferenceHandle {
        private final WeakReference<HollowAPI> apiHandle;
        private final WeakReference<Object> siblingHandle;
        private long gracePeriodBeginTimestamp = Long.MAX_VALUE;

        private Object sibling;

        private boolean usageDetected;
        private boolean detached;

        @Impure
        private HollowWeakReferenceHandle(HollowAPI stateEngine) {
            this.apiHandle = new WeakReference<HollowAPI>(stateEngine);
            this.sibling = new Object();
            this.siblingHandle = new WeakReference<Object>(sibling);
        }

        @SideEffectFree
        @Impure
        private boolean isFinished() {
            return !stateEngineIsReachable();
        }

        @SideEffectFree
        @Impure
        private boolean isExistingStaleReferenceHint() {
            return stateEngineIsReachable() && !siblingIsReachable();
        }

        @Impure
        private boolean hasBeenUsedSinceReset() {
            if(sibling == null) {
                HollowAPI myAPI = apiHandle.get();
                if(myAPI != null)
                    return myAPI.getDataAccess().hasSampleResults();
            }
            return false;
        }

        @Impure
        private void housekeeping() {
            if(gracePeriodBeginTimestamp != Long.MAX_VALUE) {
                if(shouldBeginUsageDetectionPeriod())
                    beginUsageDetectionPeriod();
                if(shouldDetach())
                    detach();
                setUpStackTraceRecording();
            }
        }

        @Impure
        private boolean shouldDetach() {
            if(!detached && System.currentTimeMillis() > (gracePeriodBeginTimestamp + config.gracePeriodMillis() + config.usageDetectionPeriodMillis())) {
                if(config.forceDropData()) {
                    return true;
                } else if(config.dropDataAutomatically()) {
                    if(usageDetected)
                        return false;

                    HollowAPI api = apiHandle.get();

                    if(api != null) {
                        HollowDataAccess dataAccess = api.getDataAccess();

                        if(dataAccess.hasSampleResults()) {
                            usageDetected = true;
                            return false;
                        }

                        return true;
                    }
                }
            }

            return false;
        }

        @Impure
        private void detach() {
            HollowAPI api = apiHandle.get();
            if(api != null) {
                HollowDataAccess dataAccess = api.getDataAccess();

                if (dataAccess instanceof HollowProxyDataAccess)
                    ((HollowProxyDataAccess) dataAccess).disableDataAccess();
                else if (dataAccess instanceof HollowReadStateEngine)
                    ((HollowReadStateEngine) dataAccess).invalidate();

                api.detachCaches();
            }
            detached = true;
        }

        @Impure
        private boolean shouldBeginUsageDetectionPeriod() {
            return sibling != null && System.currentTimeMillis() > (gracePeriodBeginTimestamp + config.gracePeriodMillis());
        }

        @Impure
        private void beginUsageDetectionPeriod() {
            sibling = null;
            HollowAPI hollowAPI = apiHandle.get();
            if(hollowAPI != null) {
                hollowAPI.getDataAccess().resetSampling();
                hollowAPI.setSamplingDirector(ENABLED_SAMPLING_DIRECTOR);
            }
        }

        @Impure
        private void setUpStackTraceRecording() {
            HollowAPI api = apiHandle.get();
            if(api != null) {
                HollowDataAccess dataAccess = api.getDataAccess();
                if(dataAccess instanceof HollowProxyDataAccess) {
                    HollowDataAccess proxiedDataAccess = ((HollowProxyDataAccess) dataAccess).getProxiedDataAccess();
                    if(proxiedDataAccess instanceof HollowHistoricalStateDataAccess)
                        ((HollowHistoricalStateDataAccess)proxiedDataAccess).setStackTraceRecorder(config.enableExpiredUsageStackTraces() ? stackTraceRecorder : null);
                }
            }
        }

        @SideEffectFree
        private boolean stateEngineIsReachable() {
            return apiHandle.get() != null;
        }

        @SideEffectFree
        private boolean siblingIsReachable() {
            return siblingHandle.get() != null;
        }

        @SideEffectFree
        private boolean isAPIHandled(HollowAPI api) {
            return apiHandle.get() == api;
        }

        @Impure
        private void newAPIAvailable(HollowAPI api) {
            if(shouldBeginGracePeriod(api)) {
                gracePeriodBeginTimestamp = System.currentTimeMillis();
            }
        }

        @Impure
        private boolean shouldBeginGracePeriod(HollowAPI newAPI) {
            if(gracePeriodBeginTimestamp != Long.MAX_VALUE)
                return false;
            HollowAPI myAPI = apiHandle.get();
            if(myAPI == null)
                return false;
            if(myAPI == newAPI)
                return false;
            if(myAPI.getDataAccess() == newAPI.getDataAccess())
                return false;
            if(newAPI.getDataAccess() instanceof HollowProxyDataAccess && ((HollowProxyDataAccess)newAPI.getDataAccess()).getProxiedDataAccess() == myAPI.getDataAccess())
                return false;
            if(myAPI.getDataAccess() instanceof HollowProxyDataAccess && ((HollowProxyDataAccess)myAPI.getDataAccess()).getProxiedDataAccess() == newAPI.getDataAccess())
                return false;
            if(myAPI.getDataAccess() instanceof HollowProxyDataAccess
              && newAPI.getDataAccess() instanceof HollowProxyDataAccess
              && ((HollowProxyDataAccess)myAPI.getDataAccess()).getProxiedDataAccess() == ((HollowProxyDataAccess)newAPI.getDataAccess()).getProxiedDataAccess())
                return false;

            return true;
        }
    }

}
