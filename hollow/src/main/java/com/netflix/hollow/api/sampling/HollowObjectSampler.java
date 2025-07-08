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
import com.netflix.hollow.core.read.filter.HollowFilterConfig;
import com.netflix.hollow.core.read.filter.HollowFilterConfig.ObjectFilterConfig;
import com.netflix.hollow.core.schema.HollowObjectSchema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class HollowObjectSampler implements HollowSampler {

    public static final HollowObjectSampler NULL_SAMPLER = new HollowObjectSampler(
            new HollowObjectSchema("test", 0), DisabledSamplingDirector.INSTANCE);

    private final String typeName;
    private final String fieldNames[];
    private final long sampleCounts[];
    private final HollowSamplingDirector samplingDirectors[];
    private boolean isSamplingDisabled;

    @Impure
    public HollowObjectSampler(HollowObjectSchema schema, HollowSamplingDirector director) {
        this.typeName = schema.getName();
        this.sampleCounts = new long[schema.numFields()];
        this.isSamplingDisabled = director == DisabledSamplingDirector.INSTANCE;
        HollowSamplingDirector[] samplingDirectors = new HollowSamplingDirector[schema.numFields()];
        Arrays.fill(samplingDirectors, director);

        String fieldNames[] = new String[schema.numFields()];
        for(int i=0;i<fieldNames.length;i++) {
            fieldNames[i] = schema.getFieldName(i);
        }
        this.fieldNames = fieldNames;
        this.samplingDirectors = samplingDirectors;
    }

    @Impure
    public void setSamplingDirector(HollowSamplingDirector director) {
        if(!"".equals(typeName)) {
            this.isSamplingDisabled = director == DisabledSamplingDirector.INSTANCE;
            Arrays.fill(samplingDirectors, director);
        }
    }

    @Impure
    @Override
    public void setFieldSpecificSamplingDirector(HollowFilterConfig fieldSpec, HollowSamplingDirector director) {
        ObjectFilterConfig typeConfig = fieldSpec.getObjectTypeConfig(typeName);

        for(int i=0;i<fieldNames.length;i++) {
            if(typeConfig.includesField(fieldNames[i])) {
                this.isSamplingDisabled = false;
                samplingDirectors[i] = director;
            }
        }
    }

    @Impure
    public void setUpdateThread(Thread t) {
        for(int i=0;i<samplingDirectors.length;i++)
            samplingDirectors[i].setUpdateThread(t);
    }

    @Impure
    public void recordFieldAccess(int fieldPosition) {
        if (this.isSamplingDisabled) return;
        if(samplingDirectors[fieldPosition].shouldRecord())
            sampleCounts[fieldPosition]++;
    }

    @Pure
    public boolean hasSampleResults() {
        for(int i=0;i<sampleCounts.length;i++)
            if(sampleCounts[i] > 0)
                return true;
        return false;
    }

    @Impure
    @Override
    public Collection<SampleResult> getSampleResults() {
        List<SampleResult> sampleResults = new ArrayList<SampleResult>(sampleCounts.length);

        for(int i=0;i<sampleCounts.length;i++) {
            sampleResults.add(new SampleResult(typeName + "." + fieldNames[i], sampleCounts[i]));
        }

        return sampleResults;
    }

    @Impure
    @Override
    public void reset() {
        Arrays.fill(sampleCounts, 0L);
    }

}
