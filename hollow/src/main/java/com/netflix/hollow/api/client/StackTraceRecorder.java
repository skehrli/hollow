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

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StackTraceRecorder {

    private final int maxStackTraceElementsToRecord;

    private final ConcurrentHashMap<String, StackTraceNode> rootNodes;

    @Impure
    public StackTraceRecorder(int maxStackTraceElementsToRecord) {
        this.maxStackTraceElementsToRecord = maxStackTraceElementsToRecord;
        this.rootNodes = new ConcurrentHashMap<String, StackTraceRecorder.StackTraceNode>();
    }

    @Impure
    public void recordStackTrace() {
        recordStackTrace(1);
    }

    @Impure
    public void recordStackTrace(int omitFirstNFrames) {
        ++omitFirstNFrames;

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if(stackTrace.length <= omitFirstNFrames)
            return;

        int maxFrameIndexToRecord = Math.min(stackTrace.length, maxStackTraceElementsToRecord + omitFirstNFrames);

        StackTraceNode node = getNode(stackTrace[omitFirstNFrames], rootNodes);
        node.increment();

        for(int i=omitFirstNFrames+1;i<maxFrameIndexToRecord;i++) {
            node = node.getChild(stackTrace[i]);
            node.increment();
        }
    }

    @Pure
    public Map<String, StackTraceNode> getRootNodes() {
        return rootNodes;
    }

    public class StackTraceNode {
        private final String traceLine;
        private final AtomicInteger count;
        private final ConcurrentHashMap<String, StackTraceNode> children;

        @Impure
        public StackTraceNode(String traceLine) {
            this.traceLine = traceLine;
            this.count = new AtomicInteger(0);
            this.children = new ConcurrentHashMap<String, StackTraceNode>(2);
        }

        @Pure
        public String getTraceLine() {
            return traceLine;
        }

        @Impure
        public int getCount() {
            return count.get();
        }

        @Pure
        public Map<String, StackTraceNode> getChildren() {
            return children;
        }

        @Impure
        public void increment() {
            count.incrementAndGet();
        }

        @Impure
        public StackTraceNode getChild(StackTraceElement element) {
            return getNode(element, children);
        }

    }

    @Impure
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for(Map.Entry<String, StackTraceNode> entry : rootNodes.entrySet()) {
            append(builder, entry.getValue(), 0);
        }

        return builder.toString();
    }

    @Impure
    private void append(StringBuilder builder, StackTraceNode node, int level) {
        for(int i=0;i<level;i++)
            builder.append("  ");
        builder.append(node.getTraceLine()).append(" (").append(node.getCount()).append(")\n");

        for(Map.Entry<String, StackTraceNode> entry : node.getChildren().entrySet()) {
            append(builder, entry.getValue(), level + 1);
        }
    }

    @Impure
    private StackTraceNode getNode(StackTraceElement element, ConcurrentHashMap<String, StackTraceNode> nodes) {
        String line = element.toString();
        StackTraceNode node = nodes.get(line);
        if(node != null)
            return node;
        node = new StackTraceNode(line);
        StackTraceNode existingNode = nodes.putIfAbsent(line, node);
        return existingNode == null ? node : existingNode;
    }
}
