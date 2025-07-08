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
import java.util.concurrent.TimeUnit;

/**
 * A trivial implementation of {@link HollowProducerListener} which does nothing.
 * Implementations of HollowProducerListenerV2 should subclass this class for convenience.
 *
 * @author Tim Taylor {@literal<tim@toolbear.io>}
 */
public class AbstractHollowProducerListener implements HollowProducerListener {
    // DataModelInitializationListener
    @SideEffectFree
    @Override public void onProducerInit(long elapsed, TimeUnit unit) {}

    // RestoreListener
    @SideEffectFree
    @Override public void onProducerRestoreStart(long restoreVersion) {}
    @SideEffectFree
    @Override public void onProducerRestoreComplete(RestoreStatus status, long elapsed, TimeUnit unit) {}

    // CycleListener
    @SideEffectFree
    @Override public void onNewDeltaChain(long version) {}
    @Impure
    @Override public void onCycleSkip(CycleSkipReason reason) {}
    @Impure
    @Override public void onCycleStart(long version) {}
    @Impure
    @Override public void onCycleComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}

    // PopulateListener
    @SideEffectFree
    @Override public void onPopulateStart(long version) {}
    @SideEffectFree
    @Override public void onPopulateComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}

    // PublishListener
    @SideEffectFree
    @Override public void onNoDeltaAvailable(long version) {}
    @SideEffectFree
    @Override public void onPublishStart(long version) {}
    @SideEffectFree
    @Override public void onArtifactPublish(PublishStatus publishStatus, long elapsed, TimeUnit unit) {}
    @SideEffectFree
    @Override public void onPublishComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}

    // IntegrityCheckListener
    @SideEffectFree
    @Override public void onIntegrityCheckStart(long version) {}
    @SideEffectFree
    @Override public void onIntegrityCheckComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}

    // ValidationListener
    @SideEffectFree
    @Override public void onValidationStart(long version) {}
    @SideEffectFree
    @Override public void onValidationComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}

    // AnnouncementListener
    @Impure
    @Override public void onAnnouncementStart(long version) {}

    @SideEffectFree
    @Override
    public void onAnnouncementStart(HollowProducer.ReadState readState) {}

    @SideEffectFree
    @Override public void onAnnouncementComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}
}
