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
package com.netflix.hollow.core.read.dataaccess.missing;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.api.sampling.HollowSampler;
import com.netflix.hollow.api.sampling.HollowSamplingDirector;
import com.netflix.hollow.api.sampling.HollowSetSampler;
import com.netflix.hollow.core.read.dataaccess.HollowDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowSetTypeDataAccess;
import com.netflix.hollow.core.read.engine.HollowTypeReadState;
import com.netflix.hollow.core.read.filter.HollowFilterConfig;
import com.netflix.hollow.core.read.iterator.HollowOrdinalIterator;
import com.netflix.hollow.core.read.missing.MissingDataHandler;
import com.netflix.hollow.core.schema.HollowSetSchema;

/**
 * Used when an entire SET type, which is expected by a Generated Hollow API, is missing from the actual data.  
 */
public class HollowSetMissingDataAccess implements HollowSetTypeDataAccess {

    private final HollowDataAccess dataAccess;
    private final String typeName;

    @SideEffectFree
    public HollowSetMissingDataAccess(HollowDataAccess dataAccess, String typeName) {
        this.dataAccess = dataAccess;
        this.typeName = typeName;
    }

    @Pure
    @Override
    public HollowDataAccess getDataAccess() {
        return dataAccess;
    }

    @Impure
    @Override
    public HollowSetSchema getSchema() {
        return (HollowSetSchema) missingDataHandler().handleSchema(typeName);
    }

    @Impure
    @Override
    public int size(int ordinal) {
        return missingDataHandler().handleSetSize(typeName, ordinal);
    }

    @Impure
    @Override
    public boolean contains(int ordinal, int elementOrdinal) {
        return missingDataHandler().handleSetContainsElement(typeName, ordinal, elementOrdinal, elementOrdinal);
    }

    @Impure
    @Override
    public boolean contains(int ordinal, int elementOrdinal, int hashCode) {
        return missingDataHandler().handleSetContainsElement(typeName, ordinal, elementOrdinal, hashCode);
    }

    @Impure
    @Override
    public int findElement(int ordinal, Object... hashKey) {
        return missingDataHandler().handleSetFindElement(typeName, ordinal, hashKey);
    }

    @Pure
    @Override
    public int relativeBucketValue(int ordinal, int bucketIndex) {
        throw new UnsupportedOperationException("Set type " + typeName + " is missing, but an attempt was made to access relative bucket values");
    }

    @Impure
    @Override
    public HollowOrdinalIterator potentialMatchOrdinalIterator(int ordinal, int hashCode) {
        return missingDataHandler().handleSetPotentialMatchIterator(typeName, ordinal, hashCode);
    }

    @Impure
    @Override
    public HollowOrdinalIterator ordinalIterator(int ordinal) {
        return missingDataHandler().handleSetIterator(typeName, ordinal);
    }

    @Impure
    MissingDataHandler missingDataHandler() {
        return dataAccess.getMissingDataHandler();
    }

    @Pure
    @Override
    public HollowTypeReadState getTypeState() {
        throw new UnsupportedOperationException("No HollowTypeReadState exists for " + typeName);
    }

    @SideEffectFree
    @Override
    public void setSamplingDirector(HollowSamplingDirector director) {
    }
    
    @SideEffectFree
    @Override
    public void setFieldSpecificSamplingDirector(HollowFilterConfig fieldSpec, HollowSamplingDirector director) {
    }

    @SideEffectFree
    @Override
    public void ignoreUpdateThreadForSampling(Thread t) {
    }

    @Pure
    @Override
    public HollowSampler getSampler() {
        return HollowSetSampler.NULL_SAMPLER;
    }

}
