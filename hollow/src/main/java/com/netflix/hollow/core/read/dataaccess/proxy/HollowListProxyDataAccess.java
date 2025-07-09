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
import com.netflix.hollow.core.read.dataaccess.HollowListTypeDataAccess;
import com.netflix.hollow.core.read.iterator.HollowListOrdinalIterator;
import com.netflix.hollow.core.read.iterator.HollowOrdinalIterator;
import com.netflix.hollow.core.schema.HollowListSchema;

/**
 * A {@link HollowTypeProxyDataAccess} for a LIST type.
 * 
 * @see HollowProxyDataAccess
 */
public class HollowListProxyDataAccess extends HollowTypeProxyDataAccess implements HollowListTypeDataAccess {

    @SideEffectFree
    @Impure
    public HollowListProxyDataAccess(HollowProxyDataAccess dataAccess) {
        super(dataAccess);
    }

    @Impure
    @Override
    public int size(int ordinal) {
        return currentDataAccess().size(ordinal);
    }

    @Impure
    @Override
    public HollowOrdinalIterator ordinalIterator(int ordinal) {
        return new HollowListOrdinalIterator(ordinal, this);
    }

    @Impure
    @Override
    public HollowListSchema getSchema() {
        return currentDataAccess().getSchema();
    }

    @Impure
    @Override
    public int getElementOrdinal(int ordinal, int listIndex) {
        return currentDataAccess().getElementOrdinal(ordinal, listIndex);
    }

    @Pure
    private HollowListTypeDataAccess currentDataAccess() {
        return (HollowListTypeDataAccess)currentDataAccess;
    }
}
