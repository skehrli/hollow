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
 */
package com.netflix.hollow.api.consumer.data;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.collectionownership.qual.PolyOwningCollection;
import com.netflix.hollow.core.read.iterator.HollowOrdinalIterator;
import java.util.Iterator;

// @@@ AbstractHollowOrdinalIterable is incorrect, it's a one shot iterable that
//     behaves incorrectly on second and subsequent iterations
public abstract class AbstractHollowOrdinalIterable<T> implements Iterable<T> {
    private final HollowOrdinalIterator iter;
    private final int firstOrdinal;

    @Impure
    public AbstractHollowOrdinalIterable(final HollowOrdinalIterator iter) {
        this.iter = iter;
        this.firstOrdinal = iter.next();
    }

    @Pure
    protected abstract T getData(int ordinal);

    @Impure
    @Override
    public Iterator<T> iterator(@PolyOwningCollection AbstractHollowOrdinalIterable<T> this) {
        return new Iterator<T>() {
            private int next = firstOrdinal;

            @Pure
            @Override
            public boolean hasNext() {
                return next != HollowOrdinalIterator.NO_MORE_ORDINALS;
            }

            @Impure
            @Override
            public T next() {
                T obj = getData(next);
                next = iter.next();
                return obj;
            }

            @SideEffectFree
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}