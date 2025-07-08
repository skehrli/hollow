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
package com.netflix.hollow.api.perfapi;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

public abstract class HollowRef {
    protected final long ref;

    @SideEffectFree
    protected HollowRef(long ref) {
        this.ref = ref;
    }

    @Pure
    public long ref() {
        return ref;
    }

    @Pure
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HollowRef)) {
            return false;
        }

        HollowRef hollowRef = (HollowRef) o;
        return ref == hollowRef.ref;
    }

    @Pure
    @Override
    public int hashCode() {
        return Long.hashCode(ref);
    }
}
