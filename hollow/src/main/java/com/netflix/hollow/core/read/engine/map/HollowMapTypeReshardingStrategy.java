package com.netflix.hollow.core.read.engine.map;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.core.read.engine.HollowTypeDataElements;
import com.netflix.hollow.core.read.engine.HollowTypeDataElementsJoiner;
import com.netflix.hollow.core.read.engine.HollowTypeDataElementsSplitter;
import com.netflix.hollow.core.read.engine.HollowTypeReshardingStrategy;

public class HollowMapTypeReshardingStrategy extends HollowTypeReshardingStrategy {
    @SideEffectFree
    @Impure
    @Override
    public HollowTypeDataElementsSplitter createDataElementsSplitter(HollowTypeDataElements from, int shardingFactor) {
        return new HollowMapTypeDataElementsSplitter((HollowMapTypeDataElements) from, shardingFactor);
    }

    @SideEffectFree
    @Impure
    @Override
    public HollowTypeDataElementsJoiner createDataElementsJoiner(HollowTypeDataElements[] from) {
        return new HollowMapTypeDataElementsJoiner((HollowMapTypeDataElements[]) from);
    }
}
