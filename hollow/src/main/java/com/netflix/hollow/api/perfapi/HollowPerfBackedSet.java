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

import org.checkerframework.checker.collectionownership.qual.NotOwningCollection;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.collectionownership.qual.PolyOwningCollection;
import com.netflix.hollow.core.read.dataaccess.HollowSetTypeDataAccess;
import com.netflix.hollow.core.read.iterator.HollowOrdinalIterator;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class HollowPerfBackedSet<T> extends AbstractSet<T> {
    private final int ordinal;
    private final HollowSetTypeDataAccess dataAccess;
    private final long elementMaskedTypeIdx;
    private final POJOInstantiator<T> instantiator;
    private final HashKeyExtractor hashKeyExtractor;

    @Impure
    public HollowPerfBackedSet(
            HollowSetTypePerfAPI typeApi, 
            long ref,
            POJOInstantiator<T> instantiator,
            HashKeyExtractor hashKeyExtractor) {
        this.dataAccess = typeApi.typeAccess();
        this.ordinal = typeApi.ordinal(ref);
        this.instantiator = instantiator;
        this.elementMaskedTypeIdx = typeApi.elementMaskedTypeIdx;
        this.hashKeyExtractor = hashKeyExtractor;
    }

    @Impure
    @Override
    public Iterator<T> iterator(@PolyOwningCollection HollowPerfBackedSet<T> this) {
        HollowOrdinalIterator oi = dataAccess.ordinalIterator(ordinal);

        return new Iterator<T>() {
            int eo = oi.next();

            @Pure
            @Override public boolean hasNext() {
                return eo != HollowOrdinalIterator.NO_MORE_ORDINALS;
            }

            @Impure
            @Override public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int o = eo;
                eo = oi.next();
                return instantiator.instantiate(elementMaskedTypeIdx | o);
            }
        };
    }

    @Impure
    @Override
    public boolean contains(@NotOwningCollection HollowPerfBackedSet<T> this, Object o) {
        if(hashKeyExtractor == null)
            throw new UnsupportedOperationException();
        
        Object[] key = hashKeyExtractor.extractArray(o);
        if(key == null)
            return false;
        return dataAccess.findElement(ordinal, key) != -1;
    }

    @Impure
    @Override
    public int size(@NotOwningCollection HollowPerfBackedSet<T> this) {
        return dataAccess.size(ordinal);
    }

}
