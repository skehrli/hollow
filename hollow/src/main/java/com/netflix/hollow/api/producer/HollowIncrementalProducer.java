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
package com.netflix.hollow.api.producer;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import static com.netflix.hollow.api.producer.HollowIncrementalCyclePopulator.AddIfAbsent;
import static com.netflix.hollow.api.producer.HollowIncrementalCyclePopulator.DELETE_RECORD;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.HollowConsumer.BlobRetriever;
import com.netflix.hollow.api.consumer.fs.HollowFilesystemAnnouncementWatcher;
import com.netflix.hollow.core.util.SimultaneousExecutor;
import com.netflix.hollow.core.write.objectmapper.RecordPrimaryKey;
import com.netflix.hollow.core.write.objectmapper.flatrecords.FlatRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Warning: This is a BETA API and is subject to breaking changes.
 * @deprecated see {@link HollowProducer.Incremental}
 * @see HollowProducer.Incremental
 */
@Deprecated
public class HollowIncrementalProducer {

    private static final long FAILED_VERSION = Long.MIN_VALUE;

    private final HollowProducer producer;
    private final ConcurrentHashMap<RecordPrimaryKey, Object> mutations;
    private final HollowProducer.Populator populator;
    private final ProducerListenerSupport listeners;
    private final Map<String, Object> cycleMetadata;
    private final Class<?>[] dataModel;
    private final HollowConsumer.AnnouncementWatcher announcementWatcher;
    private final HollowConsumer.BlobRetriever blobRetriever;
    private final double threadsPerCpu;
    private long lastSucessfulCycle;

    @Impure
    public HollowIncrementalProducer(HollowProducer producer) {
        this(producer, 1.0d, null, null, new ArrayList<IncrementalCycleListener>());
    }

    //For backwards compatible. TODO: @Deprecated ??
    @Impure
    public HollowIncrementalProducer(HollowProducer producer, double threadsPerCpu) {
        this(producer, threadsPerCpu, null, null, new ArrayList<IncrementalCycleListener>());
    }

    @Impure
    protected HollowIncrementalProducer(HollowProducer producer, double threadsPerCpu, HollowConsumer.AnnouncementWatcher announcementWatcher, HollowConsumer.BlobRetriever blobRetriever, List<IncrementalCycleListener> listeners, Class<?>... classes) {
        this.producer = producer;
        this.mutations = new ConcurrentHashMap<RecordPrimaryKey, Object>();
        this.populator = new HollowIncrementalCyclePopulator(mutations, threadsPerCpu);
        this.dataModel = classes;
        this.announcementWatcher = announcementWatcher;
        this.blobRetriever = blobRetriever;
        this.listeners = new ProducerListenerSupport();
        this.cycleMetadata = new HashMap<String, Object>();
        this.threadsPerCpu = threadsPerCpu;

        for (IncrementalCycleListener listener : listeners)
            this.listeners.add(listener);
    }

    /**
     * Initializes the data model and restores from existing state.
     */
    @Impure
    public void restoreFromLastState() {
        producer.initializeDataModel(dataModel);
        long latestAnnouncedVersion = announcementWatcher.getLatestVersion();
        if (latestAnnouncedVersion == HollowFilesystemAnnouncementWatcher.NO_ANNOUNCEMENT_AVAILABLE || latestAnnouncedVersion < 0) {
            return;
        }

        restore(latestAnnouncedVersion, blobRetriever);
    }

    @Impure
    public void restore(long versionDesired, BlobRetriever blobRetriever) {
        producer.hardRestore(versionDesired, blobRetriever);
    }

    @Impure
    public void addOrModify(Object obj) {
        RecordPrimaryKey pk = extractRecordPrimaryKey(obj);
        mutations.put(pk, obj);
    }
    
    @Impure
    public void addIfAbsent(Object obj) {
        RecordPrimaryKey pk = extractRecordPrimaryKey(obj);
        mutations.putIfAbsent(pk, new AddIfAbsent(obj));
    }

    @Impure
    public void addOrModify(Collection<Object> objList) {
        for(Object obj : objList) {
            addOrModify(obj);
        }
    }
    
    @Impure
    public void addOrModify(FlatRecord flatRecord) {
        RecordPrimaryKey pk = flatRecord.getRecordPrimaryKey();
        mutations.put(pk, flatRecord);
    }
    
    @Impure
    public void addIfAbsent(FlatRecord flatRecord) {
        RecordPrimaryKey pk = flatRecord.getRecordPrimaryKey();
        mutations.putIfAbsent(pk, new AddIfAbsent(flatRecord));
    }

    @Impure
    public void addOrModifyInParallel(Collection<Object> objList) {
        executeInParallel(objList, "add-or-modify", this::addOrModify);
    }

    @Impure
    public void delete(Object obj) {
        RecordPrimaryKey pk = extractRecordPrimaryKey(obj);
        delete(pk);
    }

    @Impure
    public void delete(Collection<Object> objList) {
        for(Object obj : objList) {
            delete(obj);
        }
    }

    @Impure
    public void deleteInParallel(Collection<Object> objList) {
        executeInParallel(objList, "delete", this::delete);
    }

    @Impure
    public void discard(Object obj) {
        RecordPrimaryKey pk = extractRecordPrimaryKey(obj);
        discard(pk);
    }

    @Impure
    public void discard(Collection<Object> objList) {
        for(Object obj : objList) {
            discard(obj);
        }
    }

    @Impure
    public void discardInParallel(Collection<Object> objList) {
        executeInParallel(objList, "discard", this::discard);
    }

    @Impure
    public void delete(RecordPrimaryKey key) {
        mutations.put(key, DELETE_RECORD);
    }

    @Impure
    public void discard(RecordPrimaryKey key) {
        mutations.remove(key);
    }

    @Impure
    public void clearChanges() {
        this.mutations.clear();
    }

    @Pure
    public boolean hasChanges() {
        return this.mutations.size() > 0;
    }

    @Impure
    public void addCycleMetadata(String key, Object value) {
        this.cycleMetadata.put(key, value);
    }

    @Impure
    public void addAllCycleMetadata(Map<String, Object> metadata) {
        this.cycleMetadata.putAll(metadata);
    }

    @Impure
    public void removeFromCycleMetadata(String key) {
        this.cycleMetadata.remove(key);
    }

    @Impure
    public void clearCycleMetadata() {
        this.cycleMetadata.clear();
    }

    @Pure
    public boolean hasMetadata() {
        return !this.cycleMetadata.isEmpty();
    }

    @Impure
    public void addListener(IncrementalCycleListener listener) {
        this.listeners.add(listener);
    }

    @Impure
    public void removeListener(IncrementalCycleListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Runs a Hollow Cycle, if successful, cleans the mutations map.
     *
     * @return the version of the cycle if successful, otherwise the {@link #FAILED_VERSION}
     * @since 2.9.9
     */
    @Impure
    public long runCycle() {
        long recordsRemoved = countRecordsToRemove();
        long recordsAddedOrModified = this.mutations.values().size() - recordsRemoved;
        try {
            long version = producer.runCycle(populator);
            if(version == lastSucessfulCycle) {
                return version;
            }
            listeners.fireIncrementalCycleComplete(version, recordsAddedOrModified, recordsRemoved, new HashMap<String, Object>(cycleMetadata));
            //Only clean changes when the version is new.
            clearChanges();
            lastSucessfulCycle = version;
            return version;
        } catch (Exception e) {
            listeners.fireIncrementalCycleFail(e, recordsAddedOrModified, recordsRemoved, new HashMap<String, Object>(cycleMetadata));
            return FAILED_VERSION;
        } finally {
            clearCycleMetadata();
        }
    }

    @SideEffectFree
    private long countRecordsToRemove() {
        long recordsToRemove = 0L;
        Collection<Object> records = mutations.values();
        for (Object record : records) {
            if (record == HollowIncrementalCyclePopulator.DELETE_RECORD) recordsToRemove++;
        }
        return recordsToRemove;
    }

    @Impure
    private RecordPrimaryKey extractRecordPrimaryKey(Object obj) {
        return producer.getObjectMapper().extractPrimaryKey(obj);
    }


    @Impure
    public static HollowIncrementalProducer.Builder withProducer(HollowProducer hollowProducer) {
        Builder builder = new Builder();
        return builder.withProducer(hollowProducer);
    }


    public static class Builder<B extends HollowIncrementalProducer.Builder<B>> {
        protected HollowProducer producer;
        protected double threadsPerCpu = 1.0d;
        protected HollowConsumer.AnnouncementWatcher announcementWatcher;
        protected HollowConsumer.BlobRetriever blobRetriever;
        protected Class<?>[] dataModel;
        protected List<IncrementalCycleListener> listeners = new ArrayList<IncrementalCycleListener>();

        @Impure
        public B withProducer(HollowProducer producer) {
            this.producer = producer;
            return (B) this;
        }

        @Impure
        public B withThreadsPerCpu(double threadsPerCpu) {
            this.threadsPerCpu = threadsPerCpu;
            return (B) this;
        }

        @Impure
        public B withAnnouncementWatcher(HollowConsumer.AnnouncementWatcher announcementWatcher) {
            this.announcementWatcher = announcementWatcher;
            return (B) this;
        }

        @Impure
        public B withBlobRetriever(HollowConsumer.BlobRetriever blobRetriever) {
            this.blobRetriever = blobRetriever;
            return (B) this;
        }

        @Impure
        public B withDataModel(Class<?>... classes) {
            this.dataModel = classes;
            return (B) this;
        }

        @Impure
        public B withListener(IncrementalCycleListener listener) {
            this.listeners.add(listener);
            return (B) this;
        }

        @Impure
        public B withListeners(IncrementalCycleListener... listeners) {
            for (IncrementalCycleListener listener : listeners)
                this.listeners.add(listener);
            return (B) this;
        }

        @SideEffectFree
        protected void checkArguments() {
            if (producer == null)
                throw new IllegalArgumentException("HollowProducer must be specified.");
        }

        @Impure
        public HollowIncrementalProducer build() {
            checkArguments();
            return new HollowIncrementalProducer(producer, threadsPerCpu, announcementWatcher, blobRetriever, listeners, dataModel);
        }
    }

    /**
     * Parallel execution. Modifies the mutation ConcurrentHashMap in parallel based on a Callback.
     * <p>
     * Note: This could be replaced with Java 8 parallelStream and lambadas instead of Callback interface
     * </p>
     * @param objList
     * @param callback
     */
    @Impure
    private void executeInParallel(Collection<Object> objList, String description, final Callback callback) {
        SimultaneousExecutor executor = new SimultaneousExecutor(threadsPerCpu, getClass(), description);
        for(final Object obj : objList) {
            executor.execute(() -> callback.call(obj));
        }

        try {
            executor.awaitSuccessfulCompletion();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private interface Callback {
        @SideEffectFree
        void call(Object obj);
    }
}
