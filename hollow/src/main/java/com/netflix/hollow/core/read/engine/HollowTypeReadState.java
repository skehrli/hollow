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
package com.netflix.hollow.core.read.engine;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import com.netflix.hollow.api.sampling.HollowSampler;
import com.netflix.hollow.core.memory.MemoryMode;
import com.netflix.hollow.core.memory.encoding.GapEncodedVariableLengthIntegerReader;
import com.netflix.hollow.core.memory.pool.ArraySegmentRecycler;
import com.netflix.hollow.core.read.HollowBlobInput;
import com.netflix.hollow.core.read.dataaccess.HollowDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowTypeDataAccess;
import com.netflix.hollow.core.schema.HollowSchema;
import com.netflix.hollow.tools.checksum.HollowChecksum;
import java.io.IOException;
import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.Stream;

/**
 * A HollowTypeReadState contains and is the root handle to all the records of a specific type in
 * a {@link HollowReadStateEngine}.
 */
public abstract class HollowTypeReadState implements HollowTypeDataAccess {

    protected static final HollowTypeStateListener[] EMPTY_LISTENERS = new HollowTypeStateListener[0];

    protected final HollowReadStateEngine stateEngine;
    protected final MemoryMode memoryMode;
    protected final HollowSchema schema;
    protected HollowTypeStateListener[] stateListeners;

    @SideEffectFree
    public HollowTypeReadState(HollowReadStateEngine stateEngine, MemoryMode memoryMode, HollowSchema schema) {
        this.stateEngine = stateEngine;
        this.memoryMode = memoryMode;
        this.schema = schema;
        this.stateListeners = EMPTY_LISTENERS;
    }

    /**
     * Add a {@link HollowTypeStateListener} to this type.
     * @param listener the listener to add
     */
    @Impure
    public void addListener(HollowTypeStateListener listener) {
        HollowTypeStateListener[] newListeners = Arrays.copyOf(stateListeners, stateListeners.length + 1);
        newListeners[newListeners.length - 1] = listener;
        stateListeners = newListeners;
    }

    /**
     * Remove a specific {@link HollowTypeStateListener} from this type.
     * @param listener the listener to remove
     */
    @Impure
    public void removeListener(HollowTypeStateListener listener) {
        if (stateListeners.length == 0)
            return;

        stateListeners = Stream.of(stateListeners)
                .filter(l -> l != listener)
                .toArray(HollowTypeStateListener[]::new);
    }

    /**
     * @return all {@link HollowTypeStateListener}s currently associated with this type.
     */
    @Pure
    public HollowTypeStateListener[] getListeners() {
        return stateListeners;
    }

    /**
     * @param listenerClazz the listener class
     * @return a {@link HollowTypeStateListener} of the specified class currently associated with this type, or
     * null if none is currently attached.
     * @param <T> the type of the listener
     */
    @Pure
    @SuppressWarnings("unchecked")
    public <T extends HollowTypeStateListener> T getListener(Class<T> listenerClazz) {
        for (HollowTypeStateListener listener : stateListeners) {
            if (listenerClazz.isAssignableFrom(listener.getClass())) {
                return (T) listener;
            }
        }
        return null;
    }
    
    /**
     * Returns the BitSet containing the currently populated ordinals in this type state.
     * <p>
     * WARNING: Do not modify the returned BitSet.
     * @return the bit containing the currently populated ordinals
     */
    @Pure
    @Impure
    public BitSet getPopulatedOrdinals() {
        return getListener(PopulatedOrdinalListener.class).getPopulatedOrdinals();
    }
    
    /**
     * Returns the BitSet containing the populated ordinals in this type state prior to the previous delta transition.
     * <p>
     * WARNING: Do not modify the returned BitSet.
     * @return the bit containing the previously populated ordinals
     */
    @Pure
    @Impure
    public BitSet getPreviousOrdinals() {
        return getListener(PopulatedOrdinalListener.class).getPreviousOrdinals();
    }

    /**
     * @return The maximum ordinal currently populated in this type state.
     */
    @Pure
    public abstract int maxOrdinal();

    @Impure
    public abstract void readSnapshot(HollowBlobInput in, ArraySegmentRecycler recycler, int numShards) throws IOException;

    @Impure
    public abstract void applyDelta(HollowBlobInput in, HollowSchema deltaSchema, ArraySegmentRecycler memoryRecycler, int deltaNumShards) throws IOException;

    @Pure
    public HollowSchema getSchema() {
        return schema;
    }

    @Pure
    @Override
    public HollowDataAccess getDataAccess() {
        return stateEngine;
    }

    /**
     * @return the {@link HollowReadStateEngine} which this type state belongs to.
     */
    @Pure
    public HollowReadStateEngine getStateEngine() {
        return stateEngine;
    }

    @Impure
    protected void notifyListenerAboutDeltaChanges(GapEncodedVariableLengthIntegerReader removals, GapEncodedVariableLengthIntegerReader additions, int shardNumber, int numShards) {
        for(HollowTypeStateListener stateListener : stateListeners) {
            removals.reset();
            int removedOrdinal = removals.nextElement();
            while(removedOrdinal < Integer.MAX_VALUE) {
                stateListener.removedOrdinal((removedOrdinal * numShards) + shardNumber);
                removals.advance();
                removedOrdinal = removals.nextElement();
            }

            additions.reset();
            int addedOrdinal = additions.nextElement();
            while(addedOrdinal < Integer.MAX_VALUE) {
                stateListener.addedOrdinal((addedOrdinal * numShards) + shardNumber);
                additions.advance();
                addedOrdinal = additions.nextElement();
            }
        }
    }

    @Pure
    public abstract HollowSampler getSampler();

    @Impure
    protected abstract void invalidate();

    @Impure
    public HollowChecksum getChecksum(HollowSchema withSchema) {
        HollowChecksum cksum = new HollowChecksum();
        applyToChecksum(cksum, withSchema);
        return cksum;
    }

    @Impure
    protected abstract void applyToChecksum(HollowChecksum checksum, HollowSchema withSchema);

    @Pure
    @Override
    public HollowTypeReadState getTypeState() {
        return this;
    }
    
    /**
     * @return an approximate accounting of the current heap footprint occupied by this type state.
     */
    @Pure
    @Impure
    public abstract long getApproximateHeapFootprintInBytes();
    
    /**
     * @return an approximate accounting of the current cost of the "ordinal holes" in this type state.
     */
    @Pure
    @Impure
    public abstract long getApproximateHoleCostInBytes();

    /**
     * @return an approximate accounting of the current heap footprint occupied by each shard of this type state.
     */
    @Pure
    @Impure
    public long getApproximateShardSizeInBytes() {
        return getApproximateHeapFootprintInBytes() / numShards();
    }

    /**
     * @return The number of shards into which this type is split.  Sharding is transparent, so this has no effect on normal usage.
     */
    @Pure
    public abstract int numShards();

    @Pure
    public abstract ShardsHolder getShardsVolatile();

    @Impure
    public abstract void updateShardsVolatile(HollowTypeReadStateShard[] shards);

    @Pure
    public abstract HollowTypeDataElements[] createTypeDataElements(int len);

    @SideEffectFree
    @Impure
    public abstract HollowTypeReadStateShard createTypeReadStateShard(HollowSchema schema, HollowTypeDataElements dataElements, int shardOrdinalShift);

    @Impure
    public void destroyOriginalDataElements(HollowTypeDataElements dataElements) {
        dataElements.destroy();
        if (dataElements.encodedRemovals != null) {
            dataElements.encodedRemovals.destroy();
        }
    }
}
