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
package com.netflix.hollow.core.schema;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.core.read.engine.HollowTypeReadState;

/**
 * A schema for a Collection record type -- parent class of both {@link HollowListSchema} or a {@link HollowSetSchema} 
 * 
 * @see HollowSchema
 * 
 * @author dkoszewnik
 *
 */
public abstract class HollowCollectionSchema extends HollowSchema {

    @SideEffectFree
    @Impure
    public HollowCollectionSchema(String name) {
        super(name);
    }

    @Pure
    public abstract String getElementType();

    @Pure
    public abstract HollowTypeReadState getElementTypeState();

}
