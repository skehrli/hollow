/*
 *  Copyright 2021 Netflix, Inc.
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
package com.netflix.hollow.api.testdata;

import org.checkerframework.dataflow.qual.SideEffectFree;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import com.netflix.hollow.api.consumer.HollowConsumer.Blob;
import com.netflix.hollow.api.consumer.HollowConsumer.BlobRetriever;
import com.netflix.hollow.api.consumer.HollowConsumer.HeaderBlob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of a BlobRetriever which allows adding blobs and holds them all in
 * memory.
 */
class HollowTestBlobRetriever implements BlobRetriever {
    private final Map<Long, Blob> snapshots = new HashMap<>();
    private final Map<Long, Blob> deltas = new HashMap<>();
    private final Map<Long, Blob> reverseDeltas = new HashMap<>();
    private final Map<Long, HeaderBlob> headers = new HashMap<>();

    @Pure
    @Override
    public Blob retrieveSnapshotBlob(long desiredVersion) {
        return snapshots.get(desiredVersion);
    }

    @Pure
    @Override
    public Blob retrieveDeltaBlob(long currentVersion) {
        return deltas.get(currentVersion);
    }

    @Pure
    @Override
    public Blob retrieveReverseDeltaBlob(long currentVersion) {
        return reverseDeltas.get(currentVersion);
    }

    @Pure
    @Override
    public HeaderBlob retrieveHeaderBlob(long desiredVersion) {
        return headers.get(desiredVersion);
    }

    @Impure
    public void addSnapshot(long desiredVersion, Blob transition) {
        snapshots.put(desiredVersion, transition);
    }

    @Impure
    public void addDelta(long currentVersion, Blob transition) {
        deltas.put(currentVersion, transition);
    }

    @Impure
    public void addHeader(long desiredVersion, HeaderBlob headerBlob) {
        headers.put(desiredVersion, headerBlob);
    }

    @Impure
    public void addReverseDelta(long currentVersion, Blob transition) {
        reverseDeltas.put(currentVersion, transition);
    }

    public static class TestHeaderBlob extends HeaderBlob {
        private final byte[] data;

        @SideEffectFree
        @Impure
        protected TestHeaderBlob(long version, byte[] data) {
            super(version);
            this.data = data;
        }

        @Impure
        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }
    }

    public static class TestBlob extends Blob {
    	
    	private final byte[] data;
    	
    	@Impure
    	public TestBlob(long snapshotVersion, byte[] data) {
    		super(snapshotVersion);
    		this.data = data;
    	}
    	
    	@Impure
    	public TestBlob(long fromVersion, long toVersion, byte[] data) {
    		super(fromVersion, toVersion);
    		this.data = data;
    	}

		@Impure
		@Override
		public InputStream getInputStream() throws IOException {
			return new ByteArrayInputStream(data);
		}
    	
    }

}

