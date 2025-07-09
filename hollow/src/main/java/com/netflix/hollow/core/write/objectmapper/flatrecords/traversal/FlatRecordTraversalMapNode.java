package com.netflix.hollow.core.write.objectmapper.flatrecords.traversal;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import com.netflix.hollow.core.schema.HollowMapSchema;
import com.netflix.hollow.core.schema.HollowObjectSchema;
import com.netflix.hollow.core.write.objectmapper.flatrecords.FlatRecordOrdinalReader;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FlatRecordTraversalMapNode extends AbstractMap<FlatRecordTraversalNode, FlatRecordTraversalNode> implements FlatRecordTraversalNode {
    private final FlatRecordOrdinalReader reader;
    private final HollowMapSchema schema;
    private final int ordinal;
    private final int[] keyOrdinals;
    private final int[] valueOrdinals;

    private Map<String, HollowObjectSchema> commonSchemaMap;

    @Impure
    public FlatRecordTraversalMapNode(FlatRecordOrdinalReader reader, HollowMapSchema schema, int ordinal) {
        this.reader = reader;
        this.schema = schema;
        this.ordinal = ordinal;

        int size = reader.readSize(ordinal);
        keyOrdinals = new int[size];
        valueOrdinals = new int[size];
        reader.readMapElementsInto(ordinal, keyOrdinals, valueOrdinals);
    }

    @Pure
    @Override
    public HollowMapSchema getSchema() {
        return schema;
    }

    @Pure
    @Override
    public int getOrdinal() {
        return ordinal;
    }

    @Impure
    @Override
    public void setCommonSchema(Map<String, HollowObjectSchema> commonSchema) {
        this.commonSchemaMap = commonSchema;
    }

    @Impure
    @Override
    public Set<Entry<FlatRecordTraversalNode, FlatRecordTraversalNode>> entrySet() {
        return new AbstractSet<Entry<FlatRecordTraversalNode, FlatRecordTraversalNode>>() {
            @Impure
            @Override
            public Iterator<Entry<FlatRecordTraversalNode, FlatRecordTraversalNode>> iterator() {
                return new EntrySetIteratorImpl<>();
            }

            @Pure
            @Override
            public int size() {
                return keyOrdinals.length;
            }
        };
    }

    @Impure
    public <K extends FlatRecordTraversalNode, V extends FlatRecordTraversalNode> Iterator<Entry<K, V>> entrySetIterator() {
        return new EntrySetIteratorImpl<>();
    }

    private class EntrySetIteratorImpl<K extends FlatRecordTraversalNode, V extends FlatRecordTraversalNode> implements Iterator<Entry<K, V>> {
        private int index = 0;

        @Pure
        @Override
        public boolean hasNext() {
            return index < keyOrdinals.length;
        }

        @Impure
        @Override
        public Entry<K, V> next() {
            if (index >= keyOrdinals.length) {
                throw new IllegalStateException("No more elements");
            }

            int keyOrdinal = keyOrdinals[index];
            int valueOrdinal = valueOrdinals[index];
            index++;

            return new Entry<K, V>() {
                @Impure
                @Override
                public K getKey() {
                    if (keyOrdinal == -1) {
                        return null;
                    }
                    return (K) createNode(reader, keyOrdinal);
                }

                @Impure
                @Override
                public V getValue() {
                    if (valueOrdinal == -1) {
                        return null;
                    }
                    return (V) createNode(reader, valueOrdinal);
                }

                @Pure
                @Override
                public V setValue(V value) {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    @Impure
    @Override
    public int hashCode() {
        int h = 0;
        for (Entry<FlatRecordTraversalNode, FlatRecordTraversalNode> e : entrySet()) {
            FlatRecordTraversalNode key = e.getKey();
            FlatRecordTraversalNode value = e.getValue();
            if (commonSchemaMap.containsKey(key.getSchema().getName())) {
                key.setCommonSchema(commonSchemaMap);
                h += key.hashCode();
            }
            if (commonSchemaMap.containsKey(value.getSchema().getName())) {
                value.setCommonSchema(commonSchemaMap);
                h += value.hashCode();
            }
        }
        return h;
    }
}
