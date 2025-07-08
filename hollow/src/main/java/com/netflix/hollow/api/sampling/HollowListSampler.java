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
package com.netflix.hollow.api.sampling;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import com.netflix.hollow.core.read.filter.HollowFilterConfig;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class HollowListSampler implements HollowSampler {

    public static final HollowListSampler NULL_SAMPLER = new HollowListSampler("", DisabledSamplingDirector.INSTANCE);

    private final String typeName;

    private HollowSamplingDirector director;

    private long sizeSamples;
    private long getSamples;
    private long iteratorSamples;

    @SideEffectFree
    public HollowListSampler(String typeName, HollowSamplingDirector director) {
        this.typeName = typeName;
        this.director = director;
    }

    @Impure
    public void setSamplingDirector(HollowSamplingDirector director) {
        if(!"".equals(typeName))
            this.director = director;
    }
    
    @Impure
    public void setFieldSpecificSamplingDirector(HollowFilterConfig fieldSpec, HollowSamplingDirector director) {
        if(!"".equals(typeName) && fieldSpec.doesIncludeType(typeName))
            this.director = director;
    }
    
    @Impure
    @Override
    public void setUpdateThread(Thread t) {
        director.setUpdateThread(t);
    }

    @Impure
    public void recordSize() {
        if(director.shouldRecord())
            sizeSamples++;
    }

    @Impure
    public void recordGet() {
        if(director.shouldRecord())
            getSamples++;
    }

    @Impure
    public void recordIterator() {
        if(director.shouldRecord())
            iteratorSamples++;
    }

    @Pure
    public boolean hasSampleResults() {
        return sizeSamples > 0 || getSamples > 0 || iteratorSamples > 0;
    }

    @Impure
    @Override
    public Collection<SampleResult> getSampleResults() {
        List<SampleResult> results = new ArrayList<SampleResult>();
        results.add(new SampleResult(typeName + ".size()", sizeSamples));
        results.add(new SampleResult(typeName + ".get()", getSamples));
        results.add(new SampleResult(typeName + ".iterator()", iteratorSamples));
        return results;
    }

    @Impure
    @Override
    public void reset() {
        sizeSamples = 0;
        getSamples = 0;
        iteratorSamples = 0;
    }

}
