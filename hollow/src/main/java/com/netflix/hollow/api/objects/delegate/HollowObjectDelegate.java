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
import com.netflix.hollow.api.custom.HollowObjectTypeAPI;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;

/**
 * This is the extension of the {@link HollowRecordDelegate} interface for OBJECT type records.
 * 
 * @see HollowRecordDelegate
 */
public interface HollowObjectDelegate extends HollowRecordDelegate {

    @Impure
    public boolean isNull(int ordinal, String fieldName);

    @Impure
    public boolean getBoolean(int ordinal, String fieldName);

    @Impure
    public int getOrdinal(int ordinal, String fieldName);

    @Impure
    public int getInt(int ordinal, String fieldName);

    @Impure
    public long getLong(int ordinal, String fieldName);

    @Impure
    public float getFloat(int ordinal, String fieldName);

    @Impure
    public double getDouble(int ordinal, String fieldName);

    @Impure
    public String getString(int ordinal, String fieldName);

    @Impure
    public boolean isStringFieldEqual(int ordinal, String fieldName, String testValue);

    @Impure
    public byte[] getBytes(int ordinal, String fieldName);

    @Impure
    public HollowObjectSchema getSchema();

    @Pure
    @Impure
    public HollowObjectTypeDataAccess getTypeDataAccess();

    @Pure
    public HollowObjectTypeAPI getTypeAPI();

}
