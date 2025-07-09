package com.netflix.hollow.core.write.objectmapper.flatrecords.traversal;

import org.checkerframework.checker.collectionownership.qual.NotOwningCollection;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import com.netflix.hollow.core.schema.HollowListSchema;
import com.netflix.hollow.core.schema.HollowObjectSchema;
import com.netflix.hollow.core.write.objectmapper.flatrecords.FlatRecordOrdinalReader;

import java.util.AbstractList;
import java.util.Map;

public class FlatRecordTraversalListNode extends AbstractList<FlatRecordTraversalNode> implements FlatRecordTraversalNode {
  private final FlatRecordOrdinalReader reader;
  private final HollowListSchema schema;
  private final int ordinal;
  private final int[] elementOrdinals;

  private Map<String, HollowObjectSchema> commonSchemaMap;

  @Impure
  public FlatRecordTraversalListNode(FlatRecordOrdinalReader reader, HollowListSchema schema, int ordinal) {
    this.reader = reader;
    this.ordinal = ordinal;
    this.schema = schema;

    int size = reader.readSize(ordinal);
    elementOrdinals = new int[size];
    reader.readListElementsInto(ordinal, elementOrdinals);
  }

  @Pure
  @Override
  public HollowListSchema getSchema() {
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

  @Pure
  public FlatRecordTraversalObjectNode getObject(int index) {
    return (FlatRecordTraversalObjectNode) get(index);
  }

  @Pure
  public FlatRecordTraversalListNode getList(int index) {
    return (FlatRecordTraversalListNode) get(index);
  }

  @Pure
  public FlatRecordTraversalSetNode getSet(int index) {
    return (FlatRecordTraversalSetNode) get(index);
  }

  @Pure
  public FlatRecordTraversalMapNode getMap(int index) {
    return (FlatRecordTraversalMapNode) get(index);
  }

  @Impure
  @Override
  public FlatRecordTraversalNode get(@NotOwningCollection FlatRecordTraversalListNode this, int index) {
    if (index >= elementOrdinals.length) {
      throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + elementOrdinals.length);
    }
    int elementOrdinal = elementOrdinals[index];
    if (elementOrdinal == -1) {
      return null;
    }
    return createNode(reader, elementOrdinal);
  }

  @Pure
  @Override
  public int size(@NotOwningCollection FlatRecordTraversalListNode this) {
    return elementOrdinals.length;
  }

  @Impure
  @Override
  public int hashCode(@NotOwningCollection FlatRecordTraversalListNode this) {
    int hashCode = 1;
    for (FlatRecordTraversalNode e : this) {
      FlatRecordTraversalObjectNode objectNode = (FlatRecordTraversalObjectNode) e;
      if (objectNode != null && commonSchemaMap.containsKey(objectNode.getSchema().getName())) {
        objectNode.setCommonSchema(commonSchemaMap);
        hashCode = 31 * hashCode + objectNode.hashCode();
      }
      else if (objectNode == null) {
        hashCode = 31 * hashCode;
      }
    }
    return hashCode;
  }
}
