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
package com.netflix.hollow.tools.history;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.api.client.StackTraceRecorder;
import com.netflix.hollow.api.sampling.HollowSampler;
import com.netflix.hollow.api.sampling.HollowSamplingDirector;
import com.netflix.hollow.core.read.dataaccess.HollowTypeDataAccess;
import com.netflix.hollow.core.read.engine.HollowTypeReadState;
import com.netflix.hollow.core.read.filter.HollowFilterConfig;
import com.netflix.hollow.core.schema.HollowSchema;
import com.netflix.hollow.core.util.IntMap;

public abstract class HollowHistoricalTypeDataAccess implements HollowTypeDataAccess {

    protected final HollowHistoricalStateDataAccess dataAccess;
    protected final HollowTypeReadState removedRecords;
    protected final IntMap ordinalRemap;

    protected final HollowSampler sampler;

    @Impure
    public HollowHistoricalTypeDataAccess(HollowHistoricalStateDataAccess dataAccess, HollowTypeReadState removedRecords, HollowSampler sampler) {
        IntMap ordinalRemap = null;
        if(dataAccess.getOrdinalMapping() instanceof IntMapOrdinalRemapper) {
            ordinalRemap = ((IntMapOrdinalRemapper)dataAccess.getOrdinalMapping()).getOrdinalRemapping(removedRecords.getSchema().getName());
        }
        this.dataAccess = dataAccess;
        this.ordinalRemap = ordinalRemap;
        this.removedRecords = removedRecords;
        this.sampler = sampler;
    }

    @Pure
    @Override
    public HollowHistoricalStateDataAccess getDataAccess() {
        return dataAccess;
    }

    @Impure
    @Override
    public HollowSchema getSchema() {
        return removedRecords.getSchema();
    }

    @Impure
    protected boolean ordinalIsPresent(int ordinal) {
        return ordinalRemap == null || ordinalRemap.get(ordinal) != -1;
    }

    @Impure
    protected int getMappedOrdinal(int ordinal) {
        return ordinalRemap == null ? ordinal : ordinalRemap.get(ordinal);
    }

    @Pure
    @Override
    public HollowTypeReadState getTypeState() {
        throw new UnsupportedOperationException();
    }

    @Impure
    @Override
    public void setSamplingDirector(HollowSamplingDirector director) {
        sampler.setSamplingDirector(director);
    }

    @Impure
    @Override
    public void setFieldSpecificSamplingDirector(HollowFilterConfig fieldSpec, HollowSamplingDirector director) {
        sampler.setFieldSpecificSamplingDirector(fieldSpec, director);
    }
    
    @Impure
    @Override
    public void ignoreUpdateThreadForSampling(Thread t) {
        sampler.setUpdateThread(t);
    }

    @Pure
    @Override
    public HollowSampler getSampler() {
        return sampler;
    }

    @Impure
    protected void recordStackTrace() {
        StackTraceRecorder recorder = dataAccess.getStackTraceRecorder();
        if(recorder != null)
            recorder.recordStackTrace(2);
    }

    @Pure
    HollowTypeReadState getRemovedRecords() {
        return removedRecords;
    }

    @Pure
    IntMap getOrdinalRemap() {
        return ordinalRemap;
    }

}
