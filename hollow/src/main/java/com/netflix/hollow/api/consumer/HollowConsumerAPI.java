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
 */
package com.netflix.hollow.api.consumer;

import org.checkerframework.dataflow.qual.Pure;
import com.netflix.hollow.core.type.HBoolean;
import com.netflix.hollow.core.type.HDouble;
import com.netflix.hollow.core.type.HFloat;
import com.netflix.hollow.core.type.HInteger;
import com.netflix.hollow.core.type.HLong;
import com.netflix.hollow.core.type.HString;
import java.util.Collection;

public interface HollowConsumerAPI {

    public interface BooleanRetriever {
        @Pure
        public Collection<HBoolean> getAllHBoolean();

        @Pure
        public HBoolean getHBoolean(int ordinal);
    }

    public interface DoubleRetriever {
        @Pure
        public Collection<HDouble> getAllHDouble();

        @Pure
        public HDouble getHDouble(int ordinal);
    }

    public interface FloatRetriever {
        @Pure
        public Collection<HFloat> getAllHFloat();

        @Pure
        public HFloat getHFloat(int ordinal);
    }

    public interface IntegerRetriever {
        @Pure
        public Collection<HInteger> getAllHInteger();

        @Pure
        public HInteger getHInteger(int ordinal);
    }

    public interface LongRetriever {
        @Pure
        public Collection<HLong> getAllHLong();

        @Pure
        public HLong getHLong(int ordinal);
    }

    public interface StringRetriever {
        @Pure
        public Collection<HString> getAllHString();

        @Pure
        public HString getHString(int ordinal);
    }
}
