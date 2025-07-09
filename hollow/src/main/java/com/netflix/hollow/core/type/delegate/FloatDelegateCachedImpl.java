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
package com.netflix.hollow.core.type.delegate;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import com.netflix.hollow.api.custom.HollowTypeAPI;
import com.netflix.hollow.api.objects.delegate.HollowCachedDelegate;
import com.netflix.hollow.api.objects.delegate.HollowObjectAbstractDelegate;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;
import com.netflix.hollow.core.type.FloatTypeAPI;

public class FloatDelegateCachedImpl extends HollowObjectAbstractDelegate implements HollowCachedDelegate, FloatDelegate {

    private final Float value;
    private FloatTypeAPI typeAPI;

    @Impure
    public FloatDelegateCachedImpl(FloatTypeAPI typeAPI, int ordinal) {
        this.value = typeAPI.getValueBoxed(ordinal);
        this.typeAPI = typeAPI;
    }

    @Pure
    @Override
    public float getValue(int ordinal) {
        if(value == null)
            return Float.NaN;
        return value.floatValue();
    }

    @Pure
    @Override
    public Float getValueBoxed(int ordinal) {
        return value;
    }

    @Impure
    @Override
    public HollowObjectSchema getSchema() {
        return typeAPI.getTypeDataAccess().getSchema();
    }

    @Impure
    @Override
    public HollowObjectTypeDataAccess getTypeDataAccess() {
        return typeAPI.getTypeDataAccess();
    }

    @Pure
    @Override
    public FloatTypeAPI getTypeAPI() {
        return typeAPI;
    }

    @Impure
    @Override
    public void updateTypeAPI(HollowTypeAPI typeAPI) {
        this.typeAPI = (FloatTypeAPI) typeAPI;
    }

}