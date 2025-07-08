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
package com.netflix.hollow.tools.history;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import com.netflix.hollow.core.util.IntMap;
import com.netflix.hollow.tools.combine.OrdinalRemapper;
import com.netflix.hollow.tools.diff.exact.DiffEqualityMapping;
import java.util.HashMap;

public class DiffEqualityMappingOrdinalRemapper implements OrdinalRemapper {

    private final HashMap<String, IntMap> unmatchedOrdinalRemapping;
    private final DiffEqualityMapping equalityMapping;

    @Impure
    DiffEqualityMappingOrdinalRemapper(DiffEqualityMapping mapping) {
        this.equalityMapping = mapping;
        this.unmatchedOrdinalRemapping = new HashMap<String, IntMap>();
    }

    @Impure
    @Override
    public int getMappedOrdinal(String type, int originalOrdinal) {
        IntMap remapping = unmatchedOrdinalRemapping.get(type);

        if(remapping != null) {
            int remappedOrdinal = remapping.get(originalOrdinal);
            if(remappedOrdinal != -1)
                return remappedOrdinal;
        }

        int matchedOrdinal = equalityMapping.getEqualOrdinalMap(type).getIdentityFromOrdinal(originalOrdinal);
        return matchedOrdinal == -1 ? originalOrdinal : matchedOrdinal;
    }

    @Impure
    public void hintUnmatchedOrdinalCount(String type, int numOrdinals) {
        unmatchedOrdinalRemapping.put(type, new IntMap(numOrdinals));
    }

    @Impure
    @Override
    public void remapOrdinal(String type, int originalOrdinal, int mappedOrdinal) {
        IntMap remap = unmatchedOrdinalRemapping.get(type);
        if(remap == null)
            throw new IllegalStateException("Must call hintUnmatchedOrdinalCount for type " + type + " before attempting to remap unmatched ordinals");
        remap.put(originalOrdinal, mappedOrdinal);
    }

    @Pure
    @Override
    public boolean ordinalIsMapped(String type, int originalOrdinal) {
        throw new UnsupportedOperationException();
    }

    @Pure
    public DiffEqualityMapping getDiffEqualityMapping() {
        return equalityMapping;
    }

    @Pure
    public IntMap getUnmatchedOrdinalMapping(String type) {
        return unmatchedOrdinalRemapping.get(type);
    }

}
