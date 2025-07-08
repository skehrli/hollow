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
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;

/**
 * A {@link HollowTypeProxyDataAccess} for an OBJECT type.
 * 
 * @see HollowProxyDataAccess
 */
public class HollowObjectProxyDataAccess extends HollowTypeProxyDataAccess implements HollowObjectTypeDataAccess {

    @SideEffectFree
    @Impure
    public HollowObjectProxyDataAccess(HollowProxyDataAccess dataAccess) {
        super(dataAccess);
    }

    @Impure
    public void setCurrentDataAccess(HollowTypeDataAccess currentDataAccess) {
        this.currentDataAccess = (HollowObjectTypeDataAccess) currentDataAccess;
    }

    @Impure
    @Override
    public HollowObjectSchema getSchema() {
        return ((HollowObjectTypeDataAccess) currentDataAccess).getSchema();
    }

    @Impure
    @Override
    public boolean isNull(int ordinal, int fieldIndex) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).isNull(ordinal, fieldIndex);
    }

    @Impure
    @Override
    public int readOrdinal(int ordinal, int fieldIndex) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).readOrdinal(ordinal, fieldIndex);
    }

    @Impure
    @Override
    public int readInt(int ordinal, int fieldIndex) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).readInt(ordinal, fieldIndex);
    }

    @Impure
    @Override
    public float readFloat(int ordinal, int fieldIndex) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).readFloat(ordinal, fieldIndex);
    }

    @Impure
    @Override
    public double readDouble(int ordinal, int fieldIndex) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).readDouble(ordinal, fieldIndex);
    }

    @Impure
    @Override
    public long readLong(int ordinal, int fieldIndex) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).readLong(ordinal, fieldIndex);
    }

    @Impure
    @Override
    public Boolean readBoolean(int ordinal, int fieldIndex) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).readBoolean(ordinal, fieldIndex);
    }

    @Impure
    @Override
    public byte[] readBytes(int ordinal, int fieldIndex) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).readBytes(ordinal, fieldIndex);
    }

    @Impure
    @Override
    public String readString(int ordinal, int fieldIndex) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).readString(ordinal, fieldIndex);
    }

    @Impure
    @Override
    public boolean isStringFieldEqual(int ordinal, int fieldIndex, String testValue) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).isStringFieldEqual(ordinal, fieldIndex, testValue);
    }

    @Impure
    @Override
    public int findVarLengthFieldHashCode(int ordinal, int fieldIndex) {
        return ((HollowObjectTypeDataAccess) currentDataAccess).findVarLengthFieldHashCode(ordinal, fieldIndex);
    }

}
