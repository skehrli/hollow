/*
 *  Copyright 2021 Netflix, Inc.
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
package com.netflix.hollow.api.testdata;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.checker.mustcall.qual.Owning;
import com.netflix.hollow.core.write.HollowSetWriteRecord;
import com.netflix.hollow.core.write.HollowWriteRecord;
import com.netflix.hollow.core.write.HollowWriteStateEngine;
import java.util.HashSet;
import java.util.Set;

public abstract class HollowTestSetRecord<T> extends HollowTestRecord<T> {

    private final Set<HollowTestRecord<?>> elements = new HashSet<>();

    @SideEffectFree
    @Impure
    protected HollowTestSetRecord(@Owning T parent) {
        super(parent);
    }

    @Impure
    protected void addElement(HollowTestRecord<?> element) {
        elements.add(element);
    }

    @Impure
    public HollowWriteRecord toWriteRecord(HollowWriteStateEngine writeEngine) {
        HollowSetWriteRecord rec = new HollowSetWriteRecord();
        for(HollowTestRecord<?> e : elements) {
            rec.addElement(e.addTo(writeEngine));
        }
        return rec;
    }
}
