/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import java.io.Serializable;

/**
 * Non-resizable {@link ListUByte} implementation backed by a {@code byte[]}.
 */
public final class ArrayUByte extends ListUByte implements Serializable {

    private static final long serialVersionUID = 1L;

    private final byte[] array;
    private final int startIndex;
    private final int size;
    private final boolean checkBoundaries;
    private final boolean readOnly;
    

    /**
     * Constructs a list containing the values provided by the specified collection
     * in the order returned by its iterator.
     * 
     * @param coll the collection whose values will be placed in this list
     */    
    public ArrayUByte(CollectionNumber coll) {
        this(coll.toArray(new byte[coll.size()]), 0, coll.size(), false);
    }

    /**
     * A new {@code ArrayUByte} that wraps around the given array.
     *
     * @param array an array
     * @param startIndex first element
     * @param size number of elements
     * @param readOnly if false the wrapper allows writes to the array
     * @throws IndexOutOfBoundsException if startIndex and size are out of range
     *         (@code{startIndex < 0 || startIndex + size > array.length})
     */
    ArrayUByte(byte[] array, int startIndex, int size, boolean readOnly) {
        if (startIndex < 0 || startIndex + size > array.length)
            throw new IndexOutOfBoundsException("Start index: "+startIndex+", Size: "+size+", Array length: "+array.length);
        this.array = array;
        this.readOnly = readOnly;
        this.startIndex = startIndex;
        this.size = size;
        this.checkBoundaries = startIndex != 0 || size != array.length;
    }

    @Override
    public final IteratorUByte iterator() {
        return new IteratorUByte() {

            private int index = startIndex;

            @Override
            public boolean hasNext() {
                return index < startIndex + size;
            }

            @Override
            public byte nextByte() {
                return array[index++];
            }
        };
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    public final byte getByte(int index) {
        if (checkBoundaries) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
        }
        return array[index];
    }

    @Override
    public void setByte(int index, byte value) {
        if (!readOnly) {
            if (checkBoundaries) {
                if (index < 0 || index >= this.size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
            }
            array[startIndex + index] = value;
        } else {
            throw new UnsupportedOperationException("Read only list.");
        }
    }

    @Override
    public ArrayUByte subList(int fromIndex, int toIndex) {
        return new ArrayUByte(array, fromIndex + startIndex, toIndex - fromIndex, readOnly);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if (list instanceof ArrayUByte) {
            if (readOnly) {
                throw new UnsupportedOperationException("Read only list.");
            }
            ArrayUByte other = (ArrayUByte) list;
            System.arraycopy(other.array, other.startIndex, array, startIndex + index, other.size);
        } else {
            super.setAll(index, list);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ArrayUByte) {
            ArrayUByte other = (ArrayUByte) obj;
            
            if ((array == other.array) && startIndex == other.startIndex && size == other.size)
                return true;
        }

        return super.equals(obj);
    }

    @Override
    public <T> T toArray(T array) {
        if (array instanceof byte[]) {
            byte[] byteArray;
            if (((byte[]) array).length < size()) {
                byteArray = new byte[size()];
            } else {
                byteArray = (byte[]) array;
            }
            System.arraycopy(this.array, startIndex, byteArray, 0, size);
            return (T) byteArray;
        }        
        return super.toArray(array);
    }

    byte[] wrappedArray() {
        return array;
    }
    
    int startIndex() {
        return startIndex;
    }
    
    boolean isReadOnly() {
        return readOnly;
    }
    
    /**
     * Returns an unmodifiable {@link ArrayUByte} wrapper for the given unsigned {@code byte} array.
     * 
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayUByte of(byte... values) {
        return CollectionNumbers.unmodifiableListUByte(values);
    }
}
