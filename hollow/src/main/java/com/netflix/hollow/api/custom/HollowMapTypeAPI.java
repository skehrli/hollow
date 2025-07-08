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
package com.netflix.hollow.api.custom;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.core.read.dataaccess.HollowMapTypeDataAccess;
import com.netflix.hollow.core.read.iterator.HollowMapEntryOrdinalIterator;

/**
 * This is the Hollow Type API interface for MAP type records. 
 * 
 * @see HollowTypeAPI
 */
public class HollowMapTypeAPI extends HollowTypeAPI {

    @SideEffectFree
    @Impure
    public HollowMapTypeAPI(HollowAPI api, HollowMapTypeDataAccess typeDataAccess) {
        super(api, typeDataAccess);
    }
    
    @Impure
    public int size(int ordinal) {
        return getTypeDataAccess().size(ordinal);
    }
    
    @Impure
    public int get(int ordinal, int keyOrdinal) {
        return getTypeDataAccess().get(ordinal, keyOrdinal);
    }

    @Impure
    public int get(int ordinal, int keyOrdinal, int hashCode) {
        return getTypeDataAccess().get(ordinal, keyOrdinal, hashCode);
    }
    
    @Impure
    public int findKey(int ordinal, Object... hashKey) {
        return getTypeDataAccess().findKey(ordinal, hashKey);
    }
    
    @Impure
    public int findValue(int ordinal, Object... hashKey) {
        return getTypeDataAccess().findValue(ordinal, hashKey);
    }
    
    @Impure
    public long findEntry(int ordinal, Object... hashKey) {
        return getTypeDataAccess().findEntry(ordinal, hashKey);
    }

    @Impure
    public HollowMapEntryOrdinalIterator getOrdinalIterator(int ordinal) {
        return getTypeDataAccess().ordinalIterator(ordinal);
    }

    @Impure
    public HollowMapEntryOrdinalIterator potentialMatchOrdinalIterator(int ordinal, int hashCode) {
        return getTypeDataAccess().potentialMatchOrdinalIterator(ordinal, hashCode);
    }
    
    @Pure
    @Override
    public HollowMapTypeDataAccess getTypeDataAccess() {
        return (HollowMapTypeDataAccess) typeDataAccess;
    }

}
