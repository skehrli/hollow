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
package com.netflix.hollow.tools.history.keyindex;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import com.netflix.hollow.core.read.engine.HollowTypeReadState;
import com.netflix.hollow.core.read.engine.object.HollowObjectTypeReadState;
import com.netflix.hollow.core.util.IntMap;
import com.netflix.hollow.core.util.IntMap.IntMapEntryIterator;
import com.netflix.hollow.tools.combine.OrdinalRemapper;

public class HollowHistoricalStateTypeKeyOrdinalMapping {

    private final String typeName;
    private final HollowHistoryTypeKeyIndex keyIndex;

    private IntMap addedOrdinalMap;
    private IntMap removedOrdinalMap;

    private int numberOfNewRecords;
    private int numberOfRemovedRecords;
    private int numberOfModifiedRecords;

    @SideEffectFree
    public HollowHistoricalStateTypeKeyOrdinalMapping(String typeName, HollowHistoryTypeKeyIndex keyIndex) {
        this.typeName = typeName;
        this.keyIndex = keyIndex;
    }

    // this is only invoked for double snapshots
    @Impure
    private HollowHistoricalStateTypeKeyOrdinalMapping(String typeName, HollowHistoryTypeKeyIndex keyIndex, IntMap addedOrdinalMap, IntMap removedOrdinalMap) {
        this.typeName = typeName;
        this.keyIndex = keyIndex;
        this.addedOrdinalMap = addedOrdinalMap;
        this.removedOrdinalMap = removedOrdinalMap;
        finish();
    }

    @Impure
    public void prepare(int numAdditions, int numRemovals) {
        this.addedOrdinalMap = new IntMap(numAdditions);
        this.removedOrdinalMap = new IntMap(numRemovals);
    }
    @Impure
    public void added(HollowTypeReadState typeState, int ordinal) {
        int recordKeyOrdinal = keyIndex.findKeyIndexOrdinal((HollowObjectTypeReadState)typeState, ordinal);
        addedOrdinalMap.put(recordKeyOrdinal, ordinal);
    }

    @Impure
    public void removed(HollowTypeReadState typeState, int ordinal) {
        removed(typeState, ordinal, ordinal);
    }

    @Impure
    public void removed(HollowTypeReadState typeState, int stateEngineOrdinal, int mappedOrdinal) {
        int recordKeyOrdinal = keyIndex.findKeyIndexOrdinal((HollowObjectTypeReadState)typeState, stateEngineOrdinal);
        removedOrdinalMap.put(recordKeyOrdinal, mappedOrdinal);
    }

    // this is only invoked for double snapshots
    @Impure
    public HollowHistoricalStateTypeKeyOrdinalMapping remap(OrdinalRemapper remapper) {
        IntMap newAddedOrdinalMap = new IntMap(addedOrdinalMap.size());
        IntMapEntryIterator addedIter = addedOrdinalMap.iterator();
        while(addedIter.next())
            newAddedOrdinalMap.put(addedIter.getKey(), remapper.getMappedOrdinal(typeName, addedIter.getValue()));

        IntMap newRemovedOrdinalMap = new IntMap(removedOrdinalMap.size());
        IntMapEntryIterator removedIter = removedOrdinalMap.iterator();
        while(removedIter.next())
            newRemovedOrdinalMap.put(removedIter.getKey(), remapper.getMappedOrdinal(typeName, removedIter.getValue()));

        return new HollowHistoricalStateTypeKeyOrdinalMapping(typeName, keyIndex, newAddedOrdinalMap, newRemovedOrdinalMap);
    }

    @Impure
    public void finish() {
        IntMapEntryIterator iter = addedOrdinalMap.iterator();

        while(iter.next()) {
            if(removedOrdinalMap.get(iter.getKey()) != -1)
                numberOfModifiedRecords++;
        }

        numberOfNewRecords = addedOrdinalMap.size() - numberOfModifiedRecords;
        numberOfRemovedRecords = removedOrdinalMap.size() - numberOfModifiedRecords;
    }

    @Impure
    public IntMapEntryIterator removedOrdinalMappingIterator() {
        return removedOrdinalMap.iterator();
    }

    @Impure
    public IntMapEntryIterator addedOrdinalMappingIterator() {
        return addedOrdinalMap.iterator();
    }

    @Impure
    public int findRemovedOrdinal(int keyOrdinal) {
        return removedOrdinalMap.get(keyOrdinal);
    }

    @Impure
    public int findAddedOrdinal(int keyOrdinal) {
        return addedOrdinalMap.get(keyOrdinal);
    }

    @Pure
    public HollowHistoryTypeKeyIndex getKeyIndex() {
        return keyIndex;
    }

    @Pure
    public int getNumberOfNewRecords() {
        return numberOfNewRecords;
    }

    @Pure
    public int getNumberOfRemovedRecords() {
        return numberOfRemovedRecords;
    }

    @Pure
    public int getNumberOfModifiedRecords() {
        return numberOfModifiedRecords;
    }

}
