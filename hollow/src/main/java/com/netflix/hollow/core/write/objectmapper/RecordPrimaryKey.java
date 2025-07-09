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
package com.netflix.hollow.core.write.objectmapper;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import java.util.Arrays;

public class RecordPrimaryKey {
    private final String type;
    private final Object[] key;
    
    @SideEffectFree
    public RecordPrimaryKey(String type, Object[] key) {
        this.type = type;
        this.key = key;
    }
    
    @Pure
    public String getType() {
        return type;
    }
    
    @Pure
    public Object[] getKey() {
        return key;
    }

    @Pure
    @Override
    public int hashCode() {
        return 31 * type.hashCode() + Arrays.hashCode(key);
    }

    @Pure
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RecordPrimaryKey) {
            return type.equals(((RecordPrimaryKey) obj).type) 
                    && Arrays.equals(key, ((RecordPrimaryKey) obj).key);
        }
        return false;
    }

    @SideEffectFree
    @Override
    public String toString() {
        return type + ": " + Arrays.toString(key);
    }
}
