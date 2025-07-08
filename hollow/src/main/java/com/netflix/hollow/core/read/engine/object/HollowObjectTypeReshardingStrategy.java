package com.netflix.hollow.core.read.engine.object;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.core.read.engine.HollowTypeDataElements;
import com.netflix.hollow.core.read.engine.HollowTypeDataElementsJoiner;
import com.netflix.hollow.core.read.engine.HollowTypeDataElementsSplitter;
import com.netflix.hollow.core.read.engine.HollowTypeReshardingStrategy;

public class HollowObjectTypeReshardingStrategy extends HollowTypeReshardingStrategy {
    @SideEffectFree
    @Impure
    @Override
    public HollowTypeDataElementsSplitter createDataElementsSplitter(HollowTypeDataElements from, int shardingFactor) {
        return new HollowObjectTypeDataElementsSplitter((HollowObjectTypeDataElements) from, shardingFactor);
    }

    @SideEffectFree
    @Impure
    @Override
    public HollowTypeDataElementsJoiner createDataElementsJoiner(HollowTypeDataElements[] from) {
        return new HollowObjectTypeDataElementsJoiner((HollowObjectTypeDataElements[]) from);
    }
}
