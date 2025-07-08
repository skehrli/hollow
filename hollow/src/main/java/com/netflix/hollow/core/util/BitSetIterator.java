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
package com.netflix.hollow.core.util;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.checker.collectionownership.qual.NotOwningCollection;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import java.util.BitSet;
import java.util.Iterator;

/**
 * A utility to iterate over range of ordinals within a BitSet 
 */
public class BitSetIterator implements Iterator<Integer> {
    private final BitSet bitset;
    private final Integer limit;
    private int next = -1;
    private int count = 0;

    @SideEffectFree
    @Impure
    public BitSetIterator(BitSet bitSet) {
        this(bitSet, null, null);
    }

    @SideEffectFree
    public BitSetIterator(BitSet bitSet, Integer start, Integer limit) {
        this.bitset = bitSet;
        this.limit = limit == null ? Integer.MAX_VALUE : limit.intValue();

        // advance next to start
        if (start == null || start.intValue() <= 1) {
            next = bitset.nextSetBit(0);
        } else {
            for (int i = 0; i < start; i++) {
                next = bitset.nextSetBit(next + 1);
                if (next == -1)
                    break;
            }
        }
    }

    @Pure
    @Override
    public boolean hasNext(@NotOwningCollection BitSetIterator this) {
        return next != -1;
    }

    @Impure
    @Override
    public Integer next(@NotOwningCollection BitSetIterator this) {
        if (!hasNext())
            return null;

        int returnValue = next;
        next = bitset.nextSetBit(next + 1);
        if (++count >= limit)
            next = -1;

        return returnValue;
    }

    @SideEffectFree
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove");
    }
}
