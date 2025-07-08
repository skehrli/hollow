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
import com.netflix.hollow.api.objects.generic.GenericHollowObject;
import com.netflix.hollow.core.read.dataaccess.HollowObjectTypeDataAccess;
import com.netflix.hollow.core.schema.HollowObjectSchema;

/**
 * A delegate used by the {@link GenericHollowObject} of the Generic Hollow Object API.
 * 
 * @see HollowRecordDelegate
 */
public class HollowObjectGenericDelegate extends HollowObjectAbstractDelegate {

    private final HollowObjectTypeDataAccess dataAccess;

    @Impure
    public HollowObjectGenericDelegate(HollowObjectTypeDataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Impure
    @Override
    public HollowObjectSchema getSchema() {
        return dataAccess.getSchema();
    }

    @Pure
    @Override
    public HollowObjectTypeDataAccess getTypeDataAccess() {
        return dataAccess;
    }

    @Pure
    @Override
    public HollowObjectTypeAPI getTypeAPI() {
        return null;
    }
}
