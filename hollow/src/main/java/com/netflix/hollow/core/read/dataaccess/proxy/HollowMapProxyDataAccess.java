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
package com.netflix.hollow.core.read.dataaccess.proxy;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.core.read.dataaccess.HollowMapTypeDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowTypeDataAccess;
import com.netflix.hollow.core.read.engine.map.PotentialMatchHollowMapEntryOrdinalIteratorImpl;
import com.netflix.hollow.core.read.iterator.HollowMapEntryOrdinalIterator;
import com.netflix.hollow.core.read.iterator.HollowMapEntryOrdinalIteratorImpl;
import com.netflix.hollow.core.schema.HollowMapSchema;

/**
 * A {@link HollowTypeProxyDataAccess} for a MAP type.
 * 
 * @see HollowProxyDataAccess
 */
public class HollowMapProxyDataAccess extends HollowTypeProxyDataAccess implements HollowMapTypeDataAccess{

    @SideEffectFree
    @Impure
    public HollowMapProxyDataAccess(HollowProxyDataAccess dataAccess) {
        super(dataAccess);
    }

    @Impure
    public void setCurrentDataAccess(HollowTypeDataAccess currentDataAccess) {
        this.currentDataAccess = (HollowMapTypeDataAccess) currentDataAccess;
    }

    @Impure
    @Override
    public HollowMapSchema getSchema() {
        return currentDataAccess().getSchema();
    }

    @Impure
    @Override
    public int size(int ordinal) {
        return currentDataAccess().size(ordinal);
    }

    @Impure
    @Override
    public int get(int ordinal, int keyOrdinal) {
        return currentDataAccess().get(ordinal, keyOrdinal);
    }

    @Impure
    @Override
    public int get(int ordinal, int keyOrdinal, int hashCode) {
        return currentDataAccess().get(ordinal, keyOrdinal, hashCode);
    }
    
    @Impure
    @Override
    public int findKey(int ordinal, Object... hashKey) {
        return currentDataAccess().findKey(ordinal, hashKey);
    }

    @Impure
    @Override
    public int findValue(int ordinal, Object... hashKey) {
        return currentDataAccess().findValue(ordinal, hashKey);
    }

    @Impure
    @Override
    public long findEntry(int ordinal, Object... hashKey) {
        return currentDataAccess().findEntry(ordinal, hashKey);
    }

    @Impure
    @Override
    public HollowMapEntryOrdinalIterator potentialMatchOrdinalIterator(int ordinal, int hashCode) {
        return new PotentialMatchHollowMapEntryOrdinalIteratorImpl(ordinal, this, hashCode);
    }

    @Impure
    @Override
    public HollowMapEntryOrdinalIterator ordinalIterator(int ordinal) {
        return new HollowMapEntryOrdinalIteratorImpl(ordinal, this);
    }

    @Impure
    @Override
    public long relativeBucket(int ordinal, int bucketIndex) {
        return currentDataAccess().relativeBucket(ordinal, bucketIndex);
    }

    @Pure
    private HollowMapTypeDataAccess currentDataAccess() {
        return (HollowMapTypeDataAccess)currentDataAccess;
    }

}
