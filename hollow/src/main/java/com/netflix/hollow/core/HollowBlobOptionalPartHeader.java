/*
 *  Copyright 2021 Netflix, Inc.
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
package com.netflix.hollow.core;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import com.netflix.hollow.core.schema.HollowSchema;
import java.util.Collections;
import java.util.List;

public class HollowBlobOptionalPartHeader {

    public static final int HOLLOW_BLOB_PART_VERSION_HEADER = 1031;
    
    private final String partName;
    private List<HollowSchema> schemas = Collections.emptyList();
    private long originRandomizedTag;
    private long destinationRandomizedTag;

    @SideEffectFree
    public HollowBlobOptionalPartHeader(String partName) {
        this.partName = partName;
    }

    @Pure
    public List<HollowSchema> getSchemas() {
        return schemas;
    }

    @Impure
    public void setSchemas(List<HollowSchema> schemas) {
        this.schemas = schemas;
    }

    @Pure
    public long getOriginRandomizedTag() {
        return originRandomizedTag;
    }

    @Impure
    public void setOriginRandomizedTag(long originRandomizedTag) {
        this.originRandomizedTag = originRandomizedTag;
    }

    @Pure
    public long getDestinationRandomizedTag() {
        return destinationRandomizedTag;
    }

    @Impure
    public void setDestinationRandomizedTag(long destinationRandomizedTag) {
        this.destinationRandomizedTag = destinationRandomizedTag;
    }

    @Pure
    public String getPartName() {
        return partName;
    }
    
}
