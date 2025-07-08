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

public abstract class HollowSamplingDirector {

    private Thread updateThread;
    
    @Impure
    public abstract boolean shouldRecord();
    
    @Impure
    public void setUpdateThread(Thread t) {
        this.updateThread = t;
    }
    
    @Impure
    protected boolean isUpdateThread() {
        return updateThread != null && updateThread == Thread.currentThread();
    }

}
