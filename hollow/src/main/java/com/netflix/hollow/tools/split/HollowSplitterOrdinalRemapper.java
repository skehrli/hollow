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
package com.netflix.hollow.tools.split;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.core.read.engine.HollowReadStateEngine;
import com.netflix.hollow.core.read.engine.HollowTypeReadState;
import com.netflix.hollow.tools.combine.OrdinalRemapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HollowSplitterOrdinalRemapper implements OrdinalRemapper {

    private final HollowSplitterShardCopier shardCopier;
    private final Map<String, int[]> typeMappings = new HashMap<String, int[]>();

    @Impure
    public HollowSplitterOrdinalRemapper(HollowReadStateEngine stateEngine, HollowSplitterShardCopier shardCopier) {
        this.shardCopier = shardCopier;

        for(HollowTypeReadState typeState : stateEngine.getTypeStates()) {
            String typeName = typeState.getSchema().getName();
            int ordinalRemapping[] = new int[typeState.maxOrdinal() + 1];
            Arrays.fill(ordinalRemapping, -1);
            typeMappings.put(typeName, ordinalRemapping);
        }
    }

    @Impure
    @Override
    public int getMappedOrdinal(String type, int originalOrdinal) {
        int[] ordinalRemapping = typeMappings.get(type);

        if(ordinalRemapping[originalOrdinal] == -1) {
            ordinalRemapping[originalOrdinal] = shardCopier.copyRecord(type, originalOrdinal);
        }

        return ordinalRemapping[originalOrdinal];
    }

    @Impure
    @Override
    public void remapOrdinal(String type, int originalOrdinal, int mappedOrdinal) {
        typeMappings.get(type)[originalOrdinal] = mappedOrdinal;
    }

    @Pure
    @Override
    public boolean ordinalIsMapped(String type, int originalOrdinal) {
        return typeMappings.get(type)[originalOrdinal] != -1;
    }

}
