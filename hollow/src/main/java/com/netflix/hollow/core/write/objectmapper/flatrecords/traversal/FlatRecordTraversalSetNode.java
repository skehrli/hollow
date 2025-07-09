package com.netflix.hollow.core.write.objectmapper.flatrecords.traversal;

import org.checkerframework.framework.qual.EnsuresQualifier;
import org.checkerframework.checker.collectionownership.qual.NotOwningCollection;
import org.checkerframework.checker.collectionownership.qual.PolyOwningCollection;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import com.netflix.hollow.core.schema.HollowObjectSchema;
import com.netflix.hollow.core.schema.HollowSetSchema;
import com.netflix.hollow.core.write.objectmapper.flatrecords.FlatRecordOrdinalReader;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;

public class FlatRecordTraversalSetNode extends AbstractSet<FlatRecordTraversalNode> implements FlatRecordTraversalNode {
  private final FlatRecordOrdinalReader reader;
  private final HollowSetSchema schema;
  private final int ordinal;
  private final int[] elementOrdinals;

  private Map<String, HollowObjectSchema> commonSchemaMap;

  @Impure
  public FlatRecordTraversalSetNode(FlatRecordOrdinalReader reader, HollowSetSchema schema, int ordinal) {
    this.reader = reader;
    this.ordinal = ordinal;
    this.schema = schema;

    int size = reader.readSize(ordinal);
    elementOrdinals = new int[size];
    reader.readSetElementsInto(ordinal, elementOrdinals);
  }

  @Pure
  @Override
  public HollowSetSchema getSchema() {
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
  public Iterator<FlatRecordTraversalObjectNode> objects() {
    return new IteratorImpl<>();
  }

  @Impure
  public Iterator<FlatRecordTraversalListNode> lists() {
    return new IteratorImpl<>();
  }

  @Impure
  public Iterator<FlatRecordTraversalSetNode> sets() {
    return new IteratorImpl<>();
  }

  @Impure
  public Iterator<FlatRecordTraversalMapNode> maps() {
    return new IteratorImpl<>();
  }

  @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.PolyOwningCollection.class)
  @Impure
  @Override
  public Iterator<FlatRecordTraversalNode> iterator(@PolyOwningCollection @NotOwningCollection FlatRecordTraversalSetNode this) {
    return new IteratorImpl<>();
  }

  @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.NotOwningCollection.class)
  @Pure
  @Override
  public int size(@NotOwningCollection FlatRecordTraversalSetNode this) {
    return elementOrdinals.length;
  }

  @EnsuresQualifier(expression="this", qualifier=org.checkerframework.checker.collectionownership.qual.NotOwningCollection.class)
  @Impure
  @Override
  public int hashCode(@NotOwningCollection FlatRecordTraversalSetNode this) {
    int h = 0;
    for (FlatRecordTraversalNode obj : this) {
      if (obj != null && commonSchemaMap.containsKey(obj.getSchema().getName())) {
        obj.setCommonSchema(commonSchemaMap);
        h += obj.hashCode();
      }
    }
    return h;
  }

  private class IteratorImpl<T extends FlatRecordTraversalNode> implements Iterator<T> {
    private int index = 0;

    @Pure
    @Override
    public boolean hasNext(@NotOwningCollection FlatRecordTraversalSetNode.IteratorImpl<T> this) {
      return index < elementOrdinals.length;
    }

    @Impure
    @Override
    public T next(@NotOwningCollection FlatRecordTraversalSetNode.IteratorImpl<T> this) {
      int elementOrdinal = elementOrdinals[index++];
      if (elementOrdinal == -1) {
        return null;
      }
      return (T) createNode(reader, elementOrdinal);
    }
  }
}
