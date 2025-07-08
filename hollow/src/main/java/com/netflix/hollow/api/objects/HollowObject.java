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
package com.netflix.hollow.api.objects;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import com.netflix.hollow.api.objects.delegate.HollowObjectDelegate;
import com.netflix.hollow.api.objects.delegate.HollowRecordDelegate;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;

/**
 * A HollowObject provides an interface for accessing data from an OBJECT record in
 * a hollow dataset.
 */
public abstract class HollowObject implements HollowRecord {

    protected final int ordinal;
    protected final HollowObjectDelegate delegate;

    @SideEffectFree
    public HollowObject(HollowObjectDelegate delegate, int ordinal) {
        this.ordinal = ordinal;
        this.delegate = delegate;
    }

    @Pure
    @Override
    public final int getOrdinal() {
        return ordinal;
    }

    @Impure
    public final boolean isNull(String fieldName) {
        return delegate.isNull(ordinal, fieldName);
    }

    @Impure
    public final boolean getBoolean(String fieldName) {
        return delegate.getBoolean(ordinal, fieldName);
    }

    @Impure
    public final int getOrdinal(String fieldName) {
        return delegate.getOrdinal(ordinal, fieldName);
    }

    @Impure
    public final int getInt(String fieldName) {
        return delegate.getInt(ordinal, fieldName);
    }

    @Impure
    public final long getLong(String fieldName) {
        return delegate.getLong(ordinal, fieldName);
    }

    @Impure
    public final float getFloat(String fieldName) {
        return delegate.getFloat(ordinal, fieldName);
    }

    @Impure
    public final double getDouble(String fieldName) {
        return delegate.getDouble(ordinal, fieldName);
    }

    @Impure
    public final String getString(String fieldName) {
        return delegate.getString(ordinal, fieldName);
    }

    @Impure
    public final boolean isStringFieldEqual(String fieldName, String testValue) {
        return delegate.isStringFieldEqual(ordinal, fieldName, testValue);
    }

    @Impure
    public final byte[] getBytes(String fieldName) {
        return delegate.getBytes(ordinal, fieldName);
    }

    @Impure
    @Override
    public HollowObjectSchema getSchema() {
        return delegate.getSchema();
    }

    @Impure
    @Override
    public HollowObjectTypeDataAccess getTypeDataAccess() {
        return delegate.getTypeDataAccess();
    }

    @Pure
    @Override
    public int hashCode() {
        return ordinal;
    }

    @Impure
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof HollowObject) {
            HollowObject hollowObj = (HollowObject)obj;

            if(ordinal == hollowObj.getOrdinal()) {
                String otherType = hollowObj.getSchema().getName();
                String myType = getSchema().getName();

                return myType.equals(otherType);
            }
        }
        return false;
    }

    @Impure
    @Override
    public String toString() {
        return "Hollow Object: " + getSchema().getName() + " (" + ordinal + ")";
    }

    @Pure
    @Override
    public HollowRecordDelegate getDelegate() {
        return delegate;
    }

}