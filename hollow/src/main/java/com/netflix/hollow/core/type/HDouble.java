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
package com.netflix.hollow.core.type;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.api.custom.HollowAPI;
import com.netflix.hollow.api.objects.HollowObject;
import com.netflix.hollow.core.type.delegate.DoubleDelegate;

public class HDouble extends HollowObject {

    @Impure
    public HDouble(DoubleDelegate delegate, int ordinal) {
        super(delegate, ordinal);
    }

    @Impure
    public double getValue() {
        return delegate().getValue(ordinal);
    }

    @Impure
    public Double getValueBoxed() {
        return delegate().getValueBoxed(ordinal);
    }

    @Impure
    public HollowAPI api() {
        return typeApi().getAPI();
    }

    @Pure
    @Impure
    public DoubleTypeAPI typeApi() {
        return delegate().getTypeAPI();
    }

    @Pure
    protected DoubleDelegate delegate() {
        return (DoubleDelegate)delegate;
    }

}