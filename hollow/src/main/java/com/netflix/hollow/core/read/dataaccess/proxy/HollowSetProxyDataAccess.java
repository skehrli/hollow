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
import com.netflix.hollow.core.read.dataaccess.HollowSetTypeDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowTypeDataAccess;
import com.netflix.hollow.core.read.engine.set.PotentialMatchHollowSetOrdinalIterator;
import com.netflix.hollow.core.read.iterator.HollowOrdinalIterator;
import com.netflix.hollow.core.read.iterator.HollowSetOrdinalIterator;
import com.netflix.hollow.core.schema.HollowSetSchema;

/**
 * A {@link HollowTypeProxyDataAccess} for a SET type.
 * 
 * @see HollowProxyDataAccess
 */
public class HollowSetProxyDataAccess extends HollowTypeProxyDataAccess implements HollowSetTypeDataAccess{

    @SideEffectFree
    @Impure
    public HollowSetProxyDataAccess(HollowProxyDataAccess dataAccess) {
        super(dataAccess);
    }

    @Impure
    public void setCurrentDataAccess(HollowTypeDataAccess currentDataAccess) {
        this.currentDataAccess = (HollowSetTypeDataAccess) currentDataAccess;
    }

    @Impure
    @Override
    public int size(int ordinal) {
        return currentDataAccess().size(ordinal);
    }

    @Impure
    @Override
    public HollowOrdinalIterator ordinalIterator(int ordinal) {
        return new HollowSetOrdinalIterator(ordinal, this);
    }

    @Impure
    @Override
    public HollowSetSchema getSchema() {
        return currentDataAccess().getSchema();
    }

    @Impure
    @Override
    public boolean contains(int ordinal, int value) {
        return currentDataAccess().contains(ordinal, value);
    }

    @Impure
    @Override
    public boolean contains(int ordinal, int value, int hashCode) {
        return currentDataAccess().contains(ordinal, value, hashCode);
    }

    @Impure
    @Override
    public int findElement(int ordinal, Object... hashKey) {
        return currentDataAccess().findElement(ordinal, hashKey);
    }
    
    @Impure
    @Override
    public int relativeBucketValue(int ordinal, int bucketIndex) {
        return currentDataAccess().relativeBucketValue(ordinal, bucketIndex);
    }

    @Impure
    @Override
    public HollowOrdinalIterator potentialMatchOrdinalIterator(int ordinal, int hashCode) {
        return new PotentialMatchHollowSetOrdinalIterator(ordinal, this, hashCode);
    }

    @Pure
    private HollowSetTypeDataAccess currentDataAccess() {
        return (HollowSetTypeDataAccess)currentDataAccess;
    }

}
