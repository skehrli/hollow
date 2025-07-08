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
package com.netflix.hollow.tools.diff.count;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.core.util.IntList;
import com.netflix.hollow.tools.diff.HollowDiff;
import com.netflix.hollow.tools.diff.HollowDiffNodeIdentifier;
import com.netflix.hollow.tools.diff.HollowTypeDiff;
import java.util.Collections;
import java.util.List;

/**
 * Counting nodes are used by the HollowDiff to count and aggregate changes for specific record types in a data model.
 * 
 * This type of counting node is applicable to types which are missing.
 * 
 * Not intended for external consumption.
 */
public class HollowDiffMissingCountingNode extends HollowDiffCountingNode {

    public static final HollowDiffMissingCountingNode INSTANCE = new HollowDiffMissingCountingNode(null, null, null);

    @Impure
    public HollowDiffMissingCountingNode(HollowDiff diff, HollowTypeDiff topLevelTypeDiff, HollowDiffNodeIdentifier nodeId) {
        super(diff, topLevelTypeDiff, nodeId);
    }

    @SideEffectFree
    @Override
    public void prepare(int topLevelFromOrdinal, int topLevelToOrdinal) { }

    @Pure
    @Override
    public int traverseDiffs(IntList fromOrdinals, IntList toOrdinals) { return 0; }

    @Pure
    @Override
    public int traverseMissingFields(IntList fromOrdinals, IntList toOrdinals) { return 0; }

    @SideEffectFree
    @Override
    public List<HollowFieldDiff> getFieldDiffs() {
        return Collections.emptyList();
    }

}
