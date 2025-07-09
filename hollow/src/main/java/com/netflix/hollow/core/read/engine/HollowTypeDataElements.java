package com.netflix.hollow.core.read.engine;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import com.netflix.hollow.core.memory.MemoryMode;
import com.netflix.hollow.core.memory.encoding.GapEncodedVariableLengthIntegerReader;
import com.netflix.hollow.core.memory.pool.ArraySegmentRecycler;

public abstract class HollowTypeDataElements {

    public int maxOrdinal;

    public GapEncodedVariableLengthIntegerReader encodedAdditions;
    public GapEncodedVariableLengthIntegerReader encodedRemovals;

    public final ArraySegmentRecycler memoryRecycler;
    public final MemoryMode memoryMode;

    @SideEffectFree
    public HollowTypeDataElements(MemoryMode memoryMode, ArraySegmentRecycler memoryRecycler) {
        this.memoryMode = memoryMode;
        this.memoryRecycler = memoryRecycler;
    }

    @Impure
    public abstract void destroy();
}
