package com.netflix.hollow.core.index;

import org.checkerframework.dataflow.qual.Impure;
import java.util.Collection;

/**
 * This package is for internal use. Do not depend on it.
 *
 * This interface allows us to re-use tests for two very similar classes. If we
 * merge {@link HollowPrimaryKeyIndex} and {@link HollowUniqueKeyIndex}, then this
 * interface won't be necessary.
 */
@SuppressWarnings({"DeprecatedIsStillUsed", "override"})
@Deprecated
interface TestableUniqueKeyIndex {
    @Impure
    void listenForDeltaUpdates();

    @Impure
    int getMatchingOrdinal(Object key);
    @Impure
    int getMatchingOrdinal(Object key1, Object key2);
    @Impure
    int getMatchingOrdinal(Object key1, Object key2, Object key3);

    @Impure
    Object[] getRecordKey(int ordinal);

    @Impure
    boolean containsDuplicates();

    @Impure
    Collection<Object[]> getDuplicateKeys();
}
