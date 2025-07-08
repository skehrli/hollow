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
import org.checkerframework.dataflow.qual.SideEffectFree;
import com.netflix.hollow.core.write.HollowWriteStateEngine;
import com.netflix.hollow.core.write.objectmapper.HollowObjectMapper;

final class CloseableWriteState implements HollowProducer.WriteState, AutoCloseable {
    private final long version;
    private final HollowObjectMapper objectMapper;
    private final HollowProducer.ReadState priorReadState;
    private volatile boolean closed;

    @SideEffectFree
    CloseableWriteState(long version, HollowObjectMapper objectMapper, HollowProducer.ReadState priorReadState) {
        this.version = version;
        this.objectMapper = objectMapper;
        this.priorReadState = priorReadState;
    }

    @Impure
    @Override
    public int add(Object o) throws IllegalStateException {
        ensureNotClosed();

        return objectMapper.add(o);
    }

    @SideEffectFree
    @Impure
    @Override
    public HollowObjectMapper getObjectMapper() throws IllegalStateException {
        ensureNotClosed();

        return objectMapper;
    }

    @Impure
    @Override
    public HollowWriteStateEngine getStateEngine() throws IllegalStateException {
        ensureNotClosed();

        return objectMapper.getStateEngine();
    }

    @SideEffectFree
    @Impure
    @Override
    public long getVersion() throws IllegalStateException {
        ensureNotClosed();

        return version;
    }

    @SideEffectFree
    @Impure
    @Override
    public HollowProducer.ReadState getPriorState() throws IllegalStateException {
        ensureNotClosed();
        return priorReadState;
    }

    @SideEffectFree
    private void ensureNotClosed() {
        if (closed) {
            throw new IllegalStateException(
                    String.format("Write state operated on after the population stage of a cycle; version=%d",
                            version));
        }
    }

    @Impure
    @Override
    public void close() {
        closed = true;
    }
}
