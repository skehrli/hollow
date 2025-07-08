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
package com.netflix.hollow.tools.patch.record;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.util.ArrayList;
import java.util.List;

public class TypeMatchSpec {

    private final String typeName;
    private final String keyPaths[];
    private final List<Object[]> keyMatchingValues;

    @SideEffectFree
    public TypeMatchSpec(String typeName, String... keyPaths) {
        this.typeName = typeName;
        this.keyPaths = keyPaths;
        this.keyMatchingValues = new ArrayList<Object[]>();
    }

    @Impure
    public void addMatchingValue(Object... matchValues) {
        this.keyMatchingValues.add(matchValues);
    }

    @Pure
    public String getTypeName() {
        return typeName;
    }

    @Pure
    public String[] getKeyPaths() {
        return keyPaths;
    }

    @Pure
    public List<Object[]> getKeyMatchingValues() {
        return keyMatchingValues;
    }

}
