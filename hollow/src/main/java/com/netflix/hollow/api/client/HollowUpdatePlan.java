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
package com.netflix.hollow.api.client;

import org.checkerframework.framework.qual.RequiresQualifier;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.checker.collectionownership.qual.PolyOwningCollection;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.framework.qual.EnsuresQualifier;
import static java.util.stream.Collectors.toList;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.core.HollowConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A plan, containing one or more {@link HollowConsumer.Blob}s, which will be used to update the current data state to a desired data state.
 */
public class HollowUpdatePlan implements Iterable<HollowConsumer.Blob> {

    public static HollowUpdatePlan DO_NOTHING = new HollowUpdatePlan(Collections.emptyList());

    private final List<HollowConsumer.Blob> transitions;

    @SideEffectFree
    private HollowUpdatePlan(List<HollowConsumer.Blob> transitions) {
        this.transitions = transitions;
    }

    @SideEffectFree
    @EnsuresQualifier(expression="this.transitions", qualifier=org.checkerframework.checker.collectionownership.qual.OwningCollectionWithoutObligation.class)
    public HollowUpdatePlan() {
        this.transitions = new ArrayList();
    }

    @Pure
    @Impure
    public boolean isSnapshotPlan() {
        return !transitions.isEmpty() && transitions.get(0).isSnapshot();
    }

    @Impure
    public HollowConsumer.Blob getSnapshotTransition() {
        if(!isSnapshotPlan())
            return null;
        return transitions.get(0);
    }

    @Impure
    public List<HollowConsumer.Blob> getDeltaTransitions() {
        if(!isSnapshotPlan())
            return transitions;
        return transitions.subList(1, transitions.size());
    }

    @Pure
    public HollowConsumer.Blob getTransition(int index) {
        return transitions.get(index);
    }

    @Pure
    public List<HollowConsumer.Blob> getTransitions() {
        return transitions;
    }

    @Impure
    public List<HollowConsumer.Blob.BlobType> getTransitionSequence() {
        return transitions.stream()
                .map(t -> t.getBlobType())
                .collect(toList());
    }

    @Impure
    public long destinationVersion(long currentVersion) {
        long dest = destinationVersion();
        return dest == HollowConstants.VERSION_NONE ? currentVersion : dest;
    }

    @Pure
    @Impure
    public long destinationVersion() {
        return transitions.isEmpty() ? HollowConstants.VERSION_NONE
            : transitions.get(transitions.size() - 1).getToVersion();
    }

    @Pure
    public int numTransitions() {
        return transitions.size();
    }

    @Impure
    public void add(HollowConsumer.Blob transition) {
        transitions.add(transition);
    }

    @Impure
    public void appendPlan(HollowUpdatePlan plan) {
        transitions.addAll(plan.transitions);
    }

    @SideEffectFree
    @Override
    public Iterator<HollowConsumer.Blob> iterator(@PolyOwningCollection HollowUpdatePlan this) {
        return transitions.iterator();
    }

    @Impure
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (transitions!= null) {
            for (int i=0; i<transitions.size(); i++) {
                HollowConsumer.Blob blob = transitions.get(i);
                result.append(blob.getBlobType()).append(" to ").append(blob.getToVersion());
                if (i < transitions.size()-1) {
                    result.append(", ");
                }
            }
        }
        return result.toString();
    }
}
