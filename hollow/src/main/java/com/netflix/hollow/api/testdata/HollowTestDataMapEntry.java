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
package com.netflix.hollow.api.testdata;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.checker.mustcall.qual.NotOwning;
import org.checkerframework.dataflow.qual.SideEffectFree;

@SuppressWarnings("rawtypes")
public class HollowTestDataMapEntry<K extends HollowTestRecord, V extends HollowTestRecord> {

    private final K key;
    private final V value;
    
    @SideEffectFree
    public HollowTestDataMapEntry(K key, V value) {
        this.key = key;
        this.value = value;
    }
    
    @NotOwning
    @Pure
    public K key() {
        return key;
    }
    
    @NotOwning
    @Pure
    public V value() {
        return value;
    }
    
    @SideEffectFree
    @Impure
    public static <K extends HollowTestRecord, V extends HollowTestRecord> HollowTestDataMapEntry<K, V> entry(K key, V value) {
        return new HollowTestDataMapEntry<>(key, value);
    }
    
}
