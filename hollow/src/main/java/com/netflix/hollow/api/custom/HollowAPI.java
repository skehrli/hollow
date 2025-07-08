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
package com.netflix.hollow.api.custom;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Pure;
import com.netflix.hollow.api.codegen.HollowAPIGenerator;
import com.netflix.hollow.api.sampling.HollowSamplingDirector;
import com.netflix.hollow.api.sampling.SampleResult;
import com.netflix.hollow.core.read.dataaccess.HollowDataAccess;
import com.netflix.hollow.core.read.filter.HollowFilterConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A HollowAPI wraps a HollowDataAccess.  This is the parent class of any Generated Hollow API.
 * 
 * Generated Hollow APIs are created via the {@link HollowAPIGenerator}.
 */
public class HollowAPI {

    private final HollowDataAccess dataAccess;
    private final List<HollowTypeAPI> typeAPIs;

    protected HollowSamplingDirector samplingDirector;

    @SideEffectFree
    public HollowAPI(HollowDataAccess dataAccess) {
        this.dataAccess = dataAccess;
        this.typeAPIs = new ArrayList<HollowTypeAPI>();
    }

    @Pure
    public HollowDataAccess getDataAccess() {
        return dataAccess;
    }

    @Pure
    public HollowSamplingDirector getSamplingDirector() {
        return samplingDirector;
    }

    @Impure
    public void setSamplingDirector(HollowSamplingDirector samplingDirector) {
        this.samplingDirector = samplingDirector;
        for(HollowTypeAPI typeAPI : typeAPIs) {
            typeAPI.setSamplingDirector(samplingDirector);
        }
    }
    
    @Impure
    public void setFieldSpecificSamplingDirector(HollowFilterConfig fieldSpec, HollowSamplingDirector director) {
        for(HollowTypeAPI typeAPI : typeAPIs) {
            typeAPI.setFieldSpecificSamplingDirector(fieldSpec, director);
        }
    }
    
    @Impure
    public void ignoreUpdateThreadForSampling(Thread t) {
        for(HollowTypeAPI typeAPI : typeAPIs) {
            typeAPI.ignoreUpdateThreadForSampling(t);
        }
    }

    @Impure
    public List<SampleResult> getAccessSampleResults() {
        List<SampleResult> sampleResults = new ArrayList<SampleResult>();
        for(HollowTypeAPI typeAPI : typeAPIs) {
            sampleResults.addAll(typeAPI.getAccessSampleResults());
        }

        Collections.sort(sampleResults);

        return sampleResults;
    }

    @Impure
    public List<SampleResult> getBoxedSampleResults() {
        List<SampleResult> sampleResults = new ArrayList<SampleResult>();
        for(HollowTypeAPI typeAPI : typeAPIs) {
            if(typeAPI instanceof HollowObjectTypeAPI) {
                sampleResults.addAll(((HollowObjectTypeAPI)typeAPI).getBoxedFieldAccessSampler().getSampleResults());
            }
        }

        Collections.sort(sampleResults);

        return sampleResults;
    }

    @SideEffectFree
    public void detachCaches() { }

    @Impure
    protected void addTypeAPI(HollowTypeAPI typeAPI) {
        this.typeAPIs.add(typeAPI);
    }

}
