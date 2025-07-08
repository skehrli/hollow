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
package com.netflix.hollow.api.producer;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import com.netflix.hollow.api.producer.HollowProducer.ReadState;
import com.netflix.hollow.core.HollowConstants;
import com.netflix.hollow.core.read.engine.HollowReadStateEngine;

/**
 * Beta API subject to change.
 *
 * Helper for {@link HollowProducer} to manage current and pending read states and the
 * transition that occurs within a cycle.
 *
 * @author Tim Taylor {@literal<tim@toolbear.io>}
 *
 */
final class ReadStateHelper {
    @SideEffectFree
    @Impure
    static ReadStateHelper newDeltaChain() {
        return new ReadStateHelper(null, null);
    }

    @SideEffectFree
    @Impure
    static ReadStateHelper restored(ReadState state) {
        return new ReadStateHelper(state, null);
    }
    
    @Impure
    static ReadState newReadState(final long version, final HollowReadStateEngine stateEngine) {
        return new HollowProducer.ReadState() {
            @Pure
            @Override
            public long getVersion() {
                return version;
            }

            @Pure
            @Override
            public HollowReadStateEngine getStateEngine() {
                return stateEngine;
            }
        };
    }

    private final ReadState current;
    private final ReadState pending;

    @SideEffectFree
    private ReadStateHelper(ReadState current, ReadState pending) {
        this.current = current;
        this.pending = pending;
    }

    @Impure
    ReadStateHelper roundtrip(long version) {
        if(pending != null) throw new IllegalStateException();
        return new ReadStateHelper(this.current, newReadState(version, new HollowReadStateEngine()));
    }

    /**
     * Swap underlying state engines between current and pending while keeping the versions consistent;
     * used after delta integrity checks have altered the underlying state engines.
     *
     * @return
     */
    @Impure
    ReadStateHelper swap() {
        return new ReadStateHelper(newReadState(current.getVersion(), pending.getStateEngine()),
                newReadState(pending.getVersion(), current.getStateEngine()));
    }

    @SideEffectFree
    @Impure
    ReadStateHelper commit() {
        if(pending == null) throw new IllegalStateException();
        return new ReadStateHelper(this.pending, null);
    }

    @Impure
    ReadStateHelper rollback() {
        if(pending == null) throw new IllegalStateException();
        return new ReadStateHelper(newReadState(current.getVersion(), pending.getStateEngine()), null);
    }

    @Pure
    ReadState current() {
        return current;
    }

    @Pure
    boolean hasCurrent() {
        return current != null;
    }

    @Pure
    ReadState pending() {
        return pending;
    }

    @Pure
    @Impure
    long pendingVersion() {
        return pending != null ? pending.getVersion() : HollowConstants.VERSION_NONE;
    }
}
