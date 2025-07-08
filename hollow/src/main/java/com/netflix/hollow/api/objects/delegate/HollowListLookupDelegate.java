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
package com.netflix.hollow.api.objects.delegate;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import com.netflix.hollow.api.custom.HollowListTypeAPI;
import com.netflix.hollow.api.objects.HollowList;
import com.netflix.hollow.core.read.dataaccess.HollowListTypeDataAccess;
import com.netflix.hollow.core.schema.HollowListSchema;

/**
 * This is the extension of the {@link HollowRecordDelegate} interface for lookup LIST type records.
 * 
 * @see HollowRecordDelegate
 */
public class HollowListLookupDelegate<T> implements HollowListDelegate<T> {

    private final HollowListTypeDataAccess dataAccess;
    protected final HollowListTypeAPI typeAPI;

    @SideEffectFree
    @Impure
    public HollowListLookupDelegate(HollowListTypeDataAccess dataAccess) {
        this(dataAccess, null);
    }

    @SideEffectFree
    @Impure
    public HollowListLookupDelegate(HollowListTypeAPI typeAPI) {
        this(typeAPI.getTypeDataAccess(), typeAPI);
    }

    @SideEffectFree
    private HollowListLookupDelegate(HollowListTypeDataAccess dataAccess, HollowListTypeAPI typeAPI) {
        this.dataAccess = dataAccess;
        this.typeAPI = typeAPI;
    }

    @Impure
    @Override
    public int size(int ordinal) {
        return dataAccess.size(ordinal);
    }

    @Impure
    @Override
    public T get(HollowList<T> list, int ordinal, int index) {
        int elementOrdinal = dataAccess.getElementOrdinal(ordinal, index);
        return list.instantiateElement(elementOrdinal);
    }

    @Impure
    @Override
    public final boolean contains(HollowList<T> list, int ordinal, Object o) {
        return indexOf(list, ordinal, o) != -1;
    }

    @Impure
    @Override
    public final int indexOf(HollowList<T> list, int ordinal, Object o) {
        int size = size(ordinal);
        for(int i=0;i<size;i++) {
            int elementOrdinal = dataAccess.getElementOrdinal(ordinal, i);
            if(list.equalsElement(elementOrdinal, o))
                return i;
        }
        return -1;
    }

    @Impure
    @Override
    public final int lastIndexOf(HollowList<T> list, int ordinal, Object o) {
        int size = size(ordinal);
        for(int i=size - 1; i>=0; i--) {
            int elementOrdinal = dataAccess.getElementOrdinal(ordinal, i);
            if(list.equalsElement(elementOrdinal, o))
                return i;
        }
        return -1;
    }

    @Impure
    @Override
    public HollowListSchema getSchema() {
        return dataAccess.getSchema();
    }

    @Pure
    @Override
    public HollowListTypeDataAccess getTypeDataAccess() {
        return dataAccess;
    }

    @Pure
    @Override
    public HollowListTypeAPI getTypeAPI() {
        return typeAPI;
    }
}
