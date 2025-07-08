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
package com.netflix.hollow.api.consumer.metrics;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import com.netflix.hollow.api.consumer.HollowConsumer.Blob.BlobType;
import java.util.List;
import java.util.OptionalLong;

public class ConsumerRefreshMetrics {

    private long durationMillis;
    private boolean isRefreshSuccess;               // true if refresh was successful, false if refresh failed
    private boolean isInitialLoad;                  // true if initial load, false if subsequent refresh
    private BlobType overallRefreshType;            // snapshot, delta, or reverse delta
    private UpdatePlanDetails updatePlanDetails;    // details about the update plan such as no. and types of transitions and no. of successful transitions
    private long consecutiveFailures;
    private OptionalLong refreshSuccessAgeMillisOptional; // time elapsed since the previous successful refresh
    private long refreshEndTimeNano;                // monotonic system time when refresh ended
    private OptionalLong cycleStartTimestamp;       // timestamp in millis of when cycle started for the loaded data version
    private OptionalLong announcementTimestamp; // timestamp in milliseconds to mark announcement for the loaded data version
    private OptionalLong deltaChainVersionCounter;  // the sequence number of a version in a delta chain

    /**
     * A class that contains details of the consumer refresh update plan that may be useful to report as metrics or logs.
     * These details are computed in {@code AbstractRefreshMetricsListener} during execution of the update plan.
     */
    public static class UpdatePlanDetails {
        long beforeVersion;
        long desiredVersion;
        List<BlobType> transitionSequence;
        int numSuccessfulTransitions;

        @Pure
        public long getBeforeVersion() {
            return beforeVersion;
        }
        @Pure
        public long getDesiredVersion() {
            return desiredVersion;
        }
        @Pure
        public List<BlobType> getTransitionSequence() {
            return transitionSequence;
        }
        @Pure
        public int getNumSuccessfulTransitions() {
            return numSuccessfulTransitions;
        }
    }

    @Pure
    public long getDurationMillis() {
        return durationMillis;
    }
    @Pure
    public boolean getIsRefreshSuccess() {
        return isRefreshSuccess;
    }
    @Pure
    public boolean getIsInitialLoad() {
        return isInitialLoad;
    }
    @Pure
    public BlobType getOverallRefreshType() {
        return overallRefreshType;
    }
    @Pure
    public UpdatePlanDetails getUpdatePlanDetails() {
        return updatePlanDetails;
    }
    @Pure
    public long getConsecutiveFailures() {
        return consecutiveFailures;
    }
    @Pure
    public OptionalLong getRefreshSuccessAgeMillisOptional() {
        return refreshSuccessAgeMillisOptional;
    }
    @Pure
    public long getRefreshEndTimeNano() {
        return refreshEndTimeNano;
    }
    @Pure
    public OptionalLong getCycleStartTimestamp() {
        return cycleStartTimestamp;
    }
    @Pure
    public OptionalLong getAnnouncementTimestamp() {
        return announcementTimestamp;
    }
    @Pure
    public OptionalLong getDeltaChainVersionCounter() {
        return deltaChainVersionCounter;
    }

    @SideEffectFree
    private ConsumerRefreshMetrics(Builder builder) {
        this.durationMillis = builder.durationMillis;
        this.isRefreshSuccess = builder.isRefreshSuccess;
        this.isInitialLoad = builder.isInitialLoad;
        this.overallRefreshType = builder.overallRefreshType;
        this.updatePlanDetails = builder.updatePlanDetails;
        this.consecutiveFailures = builder.consecutiveFailures;
        this.refreshSuccessAgeMillisOptional = builder.refreshSuccessAgeMillisOptional;
        this.refreshEndTimeNano = builder.refreshEndTimeNano;
        this.cycleStartTimestamp = builder.cycleStartTimestamp;
        this.announcementTimestamp = builder.announcementTimestamp;
        this.deltaChainVersionCounter = builder.deltaChainVersionCounter;
    }

    public static final class Builder {
        private long durationMillis;
        private boolean isRefreshSuccess;
        private boolean isInitialLoad;
        private BlobType overallRefreshType;
        private UpdatePlanDetails updatePlanDetails;
        private long consecutiveFailures;
        private OptionalLong refreshSuccessAgeMillisOptional;
        private long refreshEndTimeNano;
        private OptionalLong cycleStartTimestamp;
        private OptionalLong announcementTimestamp;
        private OptionalLong deltaChainVersionCounter;

        @SideEffectFree
        public Builder() {
            refreshSuccessAgeMillisOptional = OptionalLong.empty();
            cycleStartTimestamp = OptionalLong.empty();
            announcementTimestamp = OptionalLong.empty();
            deltaChainVersionCounter = OptionalLong.empty();
        }

        @Impure
        public Builder setDurationMillis(long durationMillis) {
            this.durationMillis = durationMillis;
            return this;
        }
        @Impure
        public Builder setIsRefreshSuccess(boolean isRefreshSuccess) {
            this.isRefreshSuccess = isRefreshSuccess;
            return this;
        }
        @Impure
        public Builder setIsInitialLoad(boolean isInitialLoad) {
            this.isInitialLoad = isInitialLoad;
            return this;
        }
        @Impure
        public Builder setOverallRefreshType(BlobType overallRefreshType) {
            this.overallRefreshType = overallRefreshType;
            return this;
        }
        @Impure
        public Builder setUpdatePlanDetails(
                UpdatePlanDetails updatePlanDetails) {
            this.updatePlanDetails = updatePlanDetails;
            return this;
        }
        @Impure
        public Builder setConsecutiveFailures(long consecutiveFailures) {
            this.consecutiveFailures = consecutiveFailures;
            return this;
        }
        @Impure
        public Builder setRefreshSuccessAgeMillisOptional(long refreshSuccessAgeMillis) {
            this.refreshSuccessAgeMillisOptional = OptionalLong.of(refreshSuccessAgeMillis);
            return this;
        }
        @Impure
        public Builder setRefreshEndTimeNano(long refreshEndTimeNano) {
            this.refreshEndTimeNano = refreshEndTimeNano;
            return this;
        }
        @Impure
        public Builder setCycleStartTimestamp(long cycleStartTimestamp) {
            this.cycleStartTimestamp = OptionalLong.of(cycleStartTimestamp);
            return this;
        }

        @Impure
        public Builder setAnnouncementTimestamp(long announcementTimestamp) {
            this.announcementTimestamp = OptionalLong.of(announcementTimestamp);
            return this;
        }
        @Impure
        public Builder setDeltaChainVersionCounter(long deltaChainVersionCounter) {
            this.deltaChainVersionCounter = OptionalLong.of(deltaChainVersionCounter);
            return this;
        }

        @SideEffectFree
        @Impure
        public ConsumerRefreshMetrics build() {
            return new ConsumerRefreshMetrics(this);
        }
    }
}