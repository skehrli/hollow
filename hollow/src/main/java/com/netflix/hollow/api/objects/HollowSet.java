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
package com.netflix.hollow.api.objects;

import org.checkerframework.framework.qual.EnsuresQualifier;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.checker.collectionownership.qual.NotOwningCollection;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.collectionownership.qual.PolyOwningCollection;
import com.netflix.hollow.api.objects.delegate.HollowRecordDelegate;
import com.netflix.hollow.api.objects.delegate.HollowSetDelegate;
import com.netflix.hollow.core.read.dataaccess.HollowSetTypeDataAccess;
import com.netflix.hollow.core.read.iterator.HollowOrdinalIterator;
import com.netflix.hollow.core.schema.HollowSetSchema;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A HollowSet provides an implementation of the {@link java.util.Set} interface over
 * a SET record in a Hollow dataset.
 * 
 * Also provides the findElement() method, which allows for searching the set for elements with matching hash keys.
 */
public abstract class HollowSet<T> extends AbstractSet<T> implements HollowRecord {

    protected final int ordinal;
    protected final HollowSetDelegate<T> delegate;

    @Impure
    public HollowSet(HollowSetDelegate<T> delegate, int ordinal) {
        this.ordinal = ordinal;
        this.delegate = delegate;
    }

    @Pure
    @Override
    public final int getOrdinal() {
        return ordinal;
    }

    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.NotOwningCollection.class)
    @Impure
    @Override
    public final int size(@NotOwningCollection HollowSet<T> this) {
        return delegate.size(ordinal);
    }

    @Impure
    @Override
    public boolean contains(@NotOwningCollection HollowSet<T> this, Object o) {
        return delegate.contains(this, ordinal, o);
    }
    
    /**
     * Find an element with the specified hash key. 
     * 
     * @param hashKey The hash key to match.
     * @return The element if discovered, null otherwise.
     */
    @Impure
    public T findElement(Object... hashKey) {
        return delegate.findElement(this, ordinal, hashKey);
    }

    @Impure
    public abstract T instantiateElement(int elementOrdinal);
    @Impure
    public abstract boolean equalsElement(int elementOrdinal, Object testObject);

    @Impure
    @Override
    public HollowSetSchema getSchema() {
        return delegate.getSchema();
    }

    @Pure
    @Impure
    @Override
    public HollowSetTypeDataAccess getTypeDataAccess() {
        return delegate.getTypeDataAccess();
    }

    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.PolyOwningCollection.class)
    @Impure
    @Override
    public final Iterator<T> iterator(@PolyOwningCollection HollowSet<T> this) {
        return new Itr();
    }

    @Pure
    @Override
    public HollowRecordDelegate getDelegate() {
        return delegate;
    }

    @Pure
    @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.NotOwningCollection.class)
    @Impure
    @Override
    public boolean equals(@NotOwningCollection HollowSet<T> this, Object o) {
        // Note: hashCode is computed from the set's contents, see AbstractSet.hashCode

        if (this == o) {
            return true;
        }

        // If type state is the same then compare ordinals
        if (o instanceof HollowSet) {
            HollowSet<?> that = (HollowSet<?>) o;
            if (delegate.getTypeDataAccess() == that.delegate.getTypeDataAccess()) {
                return ordinal == that.ordinal;
            }
        }

        // Otherwise, compare the contents
        return super.equals(o);
    }

    private final class Itr implements Iterator<T> {

        private final HollowOrdinalIterator ordinalIterator;
        private T next;

        @Impure
        Itr() {
            this.ordinalIterator = delegate.iterator(ordinal);
            positionNext();
        }

        @Pure
        @Override
        public boolean hasNext(@NotOwningCollection HollowSet<T>.Itr this) {
            return next != null;
        }

        @Impure
        @Override
        public T next(@NotOwningCollection HollowSet<T>.Itr this) {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            T current = next;
            positionNext();
            return current;
        }

        @Impure
        private void positionNext() {
            int currentOrdinal = ordinalIterator.next();

            if(currentOrdinal != HollowOrdinalIterator.NO_MORE_ORDINALS)
                next = instantiateElement(currentOrdinal);
            else
                next = null;
        }
    }
}
