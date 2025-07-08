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
import com.netflix.hollow.api.error.SchemaNotFoundException;
import com.netflix.hollow.core.HollowStateEngine;
import com.netflix.hollow.core.memory.pool.ArraySegmentRecycler;
import com.netflix.hollow.core.memory.pool.GarbageCollectorAwareRecycler;
import com.netflix.hollow.core.read.dataaccess.HollowDataAccess;
import com.netflix.hollow.core.read.dataaccess.HollowTypeDataAccess;
import com.netflix.hollow.core.read.engine.map.HollowMapTypeReadState;
import com.netflix.hollow.core.read.engine.set.HollowSetTypeReadState;
import com.netflix.hollow.core.read.missing.DefaultMissingDataHandler;
import com.netflix.hollow.core.read.missing.MissingDataHandler;
import com.netflix.hollow.core.schema.HollowListSchema;
import com.netflix.hollow.core.schema.HollowMapSchema;
import com.netflix.hollow.core.schema.HollowObjectSchema;
import com.netflix.hollow.core.schema.HollowSchema;
import com.netflix.hollow.core.schema.HollowSetSchema;
import com.netflix.hollow.core.util.DefaultHashCodeFinder;
import com.netflix.hollow.core.util.HollowObjectHashCodeFinder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A HollowReadStateEngine is our main handle to the current state of a Hollow dataset as a data consumer.
 * <p>
 * A dataset changes over time.  A core concept in Hollow is that the timeline for a changing dataset can be 
 * broken down into discrete data states, each of which is a complete snapshot of the data at a particular point in time.
 * Data consumers handle data states with a HollowReadStateEngine.
 */
public class HollowReadStateEngine implements HollowStateEngine, HollowDataAccess {

    private final Map<String, HollowTypeReadState> typeStates;
    private final Map<String, List<HollowTypeStateListener>> listeners;
    private final HollowObjectHashCodeFinder hashCodeFinder;
    private final boolean listenToAllPopulatedOrdinals;
    private boolean skipTypeShardUpdateWithNoAdditions;
    private ArraySegmentRecycler memoryRecycler;
    private Map<String,String> headerTags;
    private Set<String> typesWithDefinedHashCodes = new HashSet<String>();

    private long currentRandomizedTag;
    private long originRandomizedTag;

    private MissingDataHandler missingDataHandler = new DefaultMissingDataHandler();

    @Impure
    public HollowReadStateEngine() {
        this(DefaultHashCodeFinder.INSTANCE, true, new GarbageCollectorAwareRecycler());
    }

    @Impure
    public HollowReadStateEngine(boolean listenToAllPopulatedOrdinals) {
        this(DefaultHashCodeFinder.INSTANCE, listenToAllPopulatedOrdinals, new GarbageCollectorAwareRecycler());
    }

    @Impure
    public HollowReadStateEngine(ArraySegmentRecycler recycler) {
        this(DefaultHashCodeFinder.INSTANCE, true, recycler);
    }

    @Impure
    public HollowReadStateEngine(boolean listenToAllPopulatedOrdinals, ArraySegmentRecycler recycler) {
        this(DefaultHashCodeFinder.INSTANCE, listenToAllPopulatedOrdinals, recycler);
    }

    @Impure
    @Deprecated
    public HollowReadStateEngine(HollowObjectHashCodeFinder hashCodeFinder) {
        this(hashCodeFinder, true, new GarbageCollectorAwareRecycler());
    }

    @Impure
    @Deprecated
    public HollowReadStateEngine(HollowObjectHashCodeFinder hashCodeFinder, boolean listenToAllPopulatedOrdinals, ArraySegmentRecycler recycler) {
        this.typeStates = new HashMap<String, HollowTypeReadState>();
        this.listeners = new HashMap<String, List<HollowTypeStateListener>>();
        this.hashCodeFinder = hashCodeFinder;
        this.memoryRecycler = recycler;
        this.listenToAllPopulatedOrdinals = listenToAllPopulatedOrdinals;
    }

    @Pure
    @Override
    public HollowObjectHashCodeFinder getHashCodeFinder() {
        return hashCodeFinder;
    }

    @Impure
    protected void addTypeState(HollowTypeReadState typeState) {
        typeStates.put(typeState.getSchema().getName(), typeState);

        if(listenToAllPopulatedOrdinals) {
            typeState.addListener(new PopulatedOrdinalListener());
        }

        List<HollowTypeStateListener> list = listeners.get(typeState.getSchema().getName());
        if(list != null) {
            for(HollowTypeStateListener listener : list)
                typeState.addListener(listener);
        }
    }

    /**
     * Add a {@link HollowTypeStateListener} to a type.
     *
     * @param typeName the type name
     * @param listener the listener to add
     */
    @Impure
    public void addTypeListener(String typeName, HollowTypeStateListener listener) {
        List<HollowTypeStateListener> list = listeners.get(typeName);
        if(list == null) {
            list = new ArrayList<HollowTypeStateListener>();
            listeners.put(typeName, list);
        }

        list.add(listener);

        HollowTypeReadState typeState = typeStates.get(typeName);
        if(typeState != null)
            typeState.addListener(listener);
    }

    @Impure
    void wireTypeStatesToSchemas() {
        for(HollowTypeReadState state : typeStates.values()) {
            switch(state.getSchema().getSchemaType()) {
            case OBJECT:
                HollowObjectSchema objSchema = (HollowObjectSchema)state.getSchema();
                for(int i=0;i<objSchema.numFields();i++) {
                    if(objSchema.getReferencedType(i) != null)
                        objSchema.setReferencedTypeState(i, typeStates.get(objSchema.getReferencedType(i)));
                }
                break;
            case LIST:
                HollowListSchema listSchema = (HollowListSchema)state.getSchema();
                listSchema.setElementTypeState(typeStates.get(listSchema.getElementType()));
                break;
            case SET:
                HollowSetSchema setSchema = (HollowSetSchema)state.getSchema();
                setSchema.setElementTypeState(typeStates.get(setSchema.getElementType()));
                ((HollowSetTypeReadState)state).buildKeyDeriver();
                break;
            case MAP:
                HollowMapSchema mapSchema = (HollowMapSchema)state.getSchema();
                mapSchema.setKeyTypeState(typeStates.get(mapSchema.getKeyType()));
                mapSchema.setValueTypeState(typeStates.get(mapSchema.getValueType()));
                ((HollowMapTypeReadState)state).buildKeyDeriver();
                break;
            }
        }
    }

    /**
     * Calculates the data size of a read state engine which is defined as the approximate heap footprint by iterating
     * over the read state shards in each type state
     * @return the heap footprint of the read state engine
     */
    @Impure
    public long calcApproxDataSize() {
        return this.getAllTypes()
                .stream()
                .map(this::getTypeState)
                .mapToLong(HollowTypeReadState::getApproximateHeapFootprintInBytes)
                .sum();
    }

    /**
     * @return the no. of shards for each type in the read state
     */
    @Impure
    public Map<String, Integer> numShardsPerType() {
        Map<String, Integer> typeShards = new HashMap<>();
        for (String type : this.getAllTypes()) {
            HollowTypeReadState typeState = this.getTypeState(type);
            typeShards.put(type, typeState.numShards());
        }
        return typeShards;
    }

    /**
     * @return the approx heap footprint of a single shard in bytes, for each type in the read state
     */
    @Impure
    public Map<String, Long> calcApproxShardSizePerType() {
        Map<String, Long> typeShardSizes = new HashMap<>();
        for (String type : this.getAllTypes()) {
            HollowTypeReadState typeState = this.getTypeState(type);
            typeShardSizes.put(type, typeState.getApproximateShardSizeInBytes());
        }
        return typeShardSizes;
    }

    @Pure
    @Override
    public HollowTypeDataAccess getTypeDataAccess(String type) {
        return typeStates.get(type);
    }

    @Pure
    @Override
    public HollowTypeDataAccess getTypeDataAccess(String type, int ordinal) {
        return typeStates.get(type);
    }

    @SideEffectFree
    @Override
    public Collection<String> getAllTypes() {
        return typeStates.keySet();
    }

    @Pure
    public HollowTypeReadState getTypeState(String type) {
        return typeStates.get(type);
    }

    @SideEffectFree
    public Collection<HollowTypeReadState> getTypeStates() {
        return typeStates.values();
    }

    @Pure
    public ArraySegmentRecycler getMemoryRecycler() {
        return memoryRecycler;
    }

    @Pure
    public boolean isListenToAllPopulatedOrdinals() {
        return listenToAllPopulatedOrdinals;
    }

    /**
     * Experimental: When there are no updates for a type shard in a delta, skip updating that type shard.
     */
    @Impure
    public void setSkipTypeShardUpdateWithNoAdditions(boolean skipTypeShardUpdateWithNoAdditions) {
        this.skipTypeShardUpdateWithNoAdditions = skipTypeShardUpdateWithNoAdditions;
    }

    @Pure
    public boolean isSkipTypeShardUpdateWithNoAdditions() {
        return skipTypeShardUpdateWithNoAdditions;
    }

    @Impure
    @Override
    public List<HollowSchema> getSchemas() {
        List<HollowSchema> schemas = new ArrayList<HollowSchema>();

        for(Map.Entry<String, HollowTypeReadState> entry : typeStates.entrySet()) {
            schemas.add(entry.getValue().getSchema());
        }

        return schemas;
    }
    
    @Pure
    @Impure
    @Override
    public HollowSchema getSchema(String type) {
        HollowTypeReadState typeState = getTypeState(type);
        return typeState == null ? null : typeState.getSchema();
    }

    @Impure
    @Override
    public HollowSchema getNonNullSchema(String type) {
        HollowSchema schema = getSchema(type);
        if (schema == null) {
            throw new SchemaNotFoundException(type, getAllTypes());
        }
        return schema;
    }

    @SideEffectFree
    protected void afterInitialization() { }

    @Impure
    public void setMissingDataHandler(MissingDataHandler handler) {
        this.missingDataHandler = handler;
    }

    @Pure
    @Override
    public MissingDataHandler getMissingDataHandler() {
        return missingDataHandler;
    }

    @Impure
    public void setHeaderTags(Map<String, String> headerTags) {
        this.headerTags = headerTags;
        populatedDefinedHashCodesTypesIfHeaderTagIsPresent();
    }

    @Pure
    @Override
    public Map<String, String> getHeaderTags() {
        return headerTags;
    }

    @Pure
    @Override
    public String getHeaderTag(String name) {
        return headerTags.get(name);
    }

    @Impure
    public void invalidate() {
        listeners.clear();

        for(Map.Entry<String, HollowTypeReadState> entry : typeStates.entrySet())
            entry.getValue().invalidate();

        memoryRecycler = null;
    }

    @Impure
    @Override
    public void resetSampling() {
        for(Map.Entry<String, HollowTypeReadState> entry : typeStates.entrySet())
            entry.getValue().getSampler().reset();
    }

    @Impure
    @Override
    public boolean hasSampleResults() {
        for(Map.Entry<String, HollowTypeReadState> entry : typeStates.entrySet())
            if(entry.getValue().getSampler().hasSampleResults())
                return true;
        return false;
    }

    @SideEffectFree
    @Impure
    public boolean updatedLastCycle() {
        for(Map.Entry<String, HollowTypeReadState> entry : typeStates.entrySet()) {
            if(entry.getValue().getListener(PopulatedOrdinalListener.class).updatedLastCycle())
                return true;
        }
        return false;
    }

    @Pure
    public Set<String> getTypesWithDefinedHashCodes() {
        return typesWithDefinedHashCodes;
    }

    @Pure
    public long getCurrentRandomizedTag() {
        return currentRandomizedTag;
    }

    @Pure
    public long getOriginRandomizedTag() {
        return originRandomizedTag;
    }

    @Impure
    public void setCurrentRandomizedTag(long currentRandomizedTag) {
        this.currentRandomizedTag = currentRandomizedTag;
    }

    @Impure
    public void setOriginRandomizedTag(long originRandomizedTag) {
        this.originRandomizedTag = originRandomizedTag;
    }

    @Impure
    private void populatedDefinedHashCodesTypesIfHeaderTagIsPresent() {
        String definedHashCodesTag = headerTags.get(HollowObjectHashCodeFinder.DEFINED_HASH_CODES_HEADER_NAME);
        if(definedHashCodesTag == null || "".equals(definedHashCodesTag)) {
            this.typesWithDefinedHashCodes = Collections.<String>emptySet();
        } else {
            Set<String>definedHashCodeTypes = new HashSet<String>();
            for(String type : definedHashCodesTag.split(","))
                definedHashCodeTypes.add(type);
            this.typesWithDefinedHashCodes = definedHashCodeTypes;
        }
    }

}
