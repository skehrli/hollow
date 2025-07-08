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
package com.netflix.hollow.core.read.dataaccess.disabled;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import com.netflix.hollow.api.sampling.HollowSampler;
import com.netflix.hollow.api.sampling.HollowSamplingDirector;
import com.netflix.hollow.core.read.dataaccess.HollowDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.read.engine.HollowTypeReadState;
import com.netflix.hollow.core.read.filter.HollowFilterConfig;
import com.netflix.hollow.core.schema.HollowObjectSchema;

public class HollowObjectDisabledDataAccess implements HollowObjectTypeDataAccess {

    public static final HollowObjectDisabledDataAccess INSTANCE = new HollowObjectDisabledDataAccess();

    @SideEffectFree
    private HollowObjectDisabledDataAccess() { }

    @Pure
    @Override
    public HollowDataAccess getDataAccess() {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @SideEffectFree
    @Override
    public void setSamplingDirector(HollowSamplingDirector director) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @SideEffectFree
    @Override
    public void setFieldSpecificSamplingDirector(HollowFilterConfig fieldSpec, HollowSamplingDirector director) {
        throw new IllegalStateException("Data Access is Disabled");
    }
    
    @SideEffectFree
    @Override
    public void ignoreUpdateThreadForSampling(Thread t) {
        throw new IllegalStateException("DataAccess is Disabled");
    }

    @Pure
    @Override
    public HollowSampler getSampler() {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public HollowTypeReadState getTypeState() {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public HollowObjectSchema getSchema() {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public boolean isNull(int ordinal, int fieldIndex) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public int readOrdinal(int ordinal, int fieldIndex) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public int readInt(int ordinal, int fieldIndex) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public float readFloat(int ordinal, int fieldIndex) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public double readDouble(int ordinal, int fieldIndex) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public long readLong(int ordinal, int fieldIndex) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public Boolean readBoolean(int ordinal, int fieldIndex) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public byte[] readBytes(int ordinal, int fieldIndex) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public String readString(int ordinal, int fieldIndex) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public boolean isStringFieldEqual(int ordinal, int fieldIndex, String testValue) {
        throw new IllegalStateException("Data Access is Disabled");
    }

    @Pure
    @Override
    public int findVarLengthFieldHashCode(int ordinal, int fieldIndex) {
        throw new IllegalStateException("Data Access is Disabled");
    }

}
