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
import com.netflix.hollow.api.custom.HollowListTypeAPI;
import com.netflix.hollow.api.objects.HollowList;
import com.netflix.hollow.core.read.dataaccess.HollowListTypeDataAccess;
import com.netflix.hollow.core.schema.HollowListSchema;

/**
 * This is the extension of the {@link HollowRecordDelegate} interface for LIST type records.
 * 
 * @see HollowRecordDelegate
 */
public interface HollowListDelegate<T> extends HollowRecordDelegate {

    @Impure
    public int size(int ordinal);

    @Impure
    public T get(@NotOwningCollection HollowList<T> list, int ordinal, int index);

    @Impure
    public boolean contains(@NotOwningCollection HollowList<T> list, int ordinal, Object o);

    @Impure
    public int indexOf(@NotOwningCollection HollowList<T> list, int ordinal, Object o);

    @Impure
    public int lastIndexOf(@NotOwningCollection HollowList<T> list, int ordinal, Object o);

    @Impure
    public HollowListSchema getSchema();

    @Pure
    public HollowListTypeDataAccess getTypeDataAccess();

    @Pure
    public HollowListTypeAPI getTypeAPI();

}
