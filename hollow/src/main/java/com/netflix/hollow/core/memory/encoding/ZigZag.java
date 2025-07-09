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
package com.netflix.hollow.core.memory.encoding;

import org.checkerframework.dataflow.qual.Pure;
import com.netflix.hollow.core.schema.HollowObjectSchema.FieldType;

/**
 * Zig-zag encoding. Used to encode {@link FieldType#INT} and {@link FieldType#LONG} because smaller absolute 
 * values can be encoded using fewer bits.
 */
public class ZigZag {

    @Pure
    public static long encodeLong(long l) {
        return (l << 1) ^ (l >> 63);
    }

    @Pure
    public static long decodeLong(long l) {
        return (l >>> 1) ^ ((l << 63) >> 63);
    }

    @Pure
    public static int encodeInt(int i) {
        return (i << 1) ^ (i >> 31);
    }

    @Pure
    public static int decodeInt(int i) {
        return (i >>> 1) ^ ((i << 31) >> 31);
    }
}
