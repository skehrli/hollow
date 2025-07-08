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

import org.checkerframework.checker.collectionownership.qual.NotOwningCollection;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.api.custom.HollowSetTypeAPI;
import com.netflix.hollow.api.objects.HollowSet;
import com.netflix.hollow.core.read.dataaccess.HollowSetTypeDataAccess;
import com.netflix.hollow.core.read.iterator.HollowOrdinalIterator;
import com.netflix.hollow.core.schema.HollowSetSchema;

/**
 * This is the extension of the {@link HollowRecordDelegate} interface for SET type records.
 * 
 * @see HollowRecordDelegate
 */
public interface HollowSetDelegate<T> extends HollowRecordDelegate {

    @Impure
    public int size(int ordinal);

    @Impure
    public boolean contains(@NotOwningCollection HollowSet<T> set, int ordinal, Object o);
    
    @Impure
    public T findElement(HollowSet<T> set, int ordinal, Object... keys);

    @Impure
    public HollowOrdinalIterator iterator(int ordinal);

    @Impure
    public HollowSetSchema getSchema();

    @Pure
    public HollowSetTypeDataAccess getTypeDataAccess();

    @Pure
    public HollowSetTypeAPI getTypeAPI();
}
