package com.netflix.hollow.core.read.engine;
import org.checkerframework.dataflow.qual.Pure;

public interface ShardsHolder {

    @Pure
    HollowTypeReadStateShard[] getShards();
}
