package com.netflix.hollow.core.read.engine;
import org.checkerframework.dataflow.qual.Pure;

public interface HollowTypeReadStateShard {

    @Pure
    HollowTypeDataElements getDataElements();

    @Pure
    int getShardOrdinalShift();
}
