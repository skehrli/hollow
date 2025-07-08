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
package com.netflix.hollow.core.util;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.collectionownership.qual.NotOwningCollection;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.checker.collectionownership.qual.PolyOwningCollection;
import java.util.AbstractCollection;
import java.util.BitSet;
import java.util.Iterator;

public abstract class HollowRecordCollection<T> extends AbstractCollection<T> {

    private final BitSet populatedOrdinals;

    @SideEffectFree
    public HollowRecordCollection(BitSet populatedOrdinals) {
        this.populatedOrdinals = populatedOrdinals;
    }

    @Impure
    @Override
    public Iterator<T> iterator(@PolyOwningCollection HollowRecordCollection<T> this) {
        return new Iterator<T>() {
            private int ordinal = populatedOrdinals.nextSetBit(0);

            @Pure
            public boolean hasNext() {
                return ordinal != -1;
            }

            @Impure
            @Override
            public T next() {
                T t = getForOrdinal(ordinal);
                ordinal = populatedOrdinals.nextSetBit(ordinal + 1);
                return t;
            }

            @SideEffectFree
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Pure
    @Override
    public int size(@NotOwningCollection HollowRecordCollection<T> this) {
        return populatedOrdinals.cardinality();
    }
    
    @Pure
    protected abstract T getForOrdinal(int ordinal);

}
