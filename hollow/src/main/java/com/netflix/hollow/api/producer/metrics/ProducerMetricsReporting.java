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
package com.netflix.hollow.api.producer.metrics;
import org.checkerframework.dataflow.qual.SideEffectFree;

/**
 * Allows implementations to plug in custom reporting of producer metrics, while not enforcing that any or all metrics
 * are reported. For example, an implementation might only be insterested in cycle metrics but not announcement metrics, etc.
 */
public interface ProducerMetricsReporting {

    @SideEffectFree
    default void cycleMetricsReporting(CycleMetrics cycleMetrics) {
        // no-op
    }

    @SideEffectFree
    default void announcementMetricsReporting(AnnouncementMetrics announcementMetrics) {
        // no-op
    }
}
