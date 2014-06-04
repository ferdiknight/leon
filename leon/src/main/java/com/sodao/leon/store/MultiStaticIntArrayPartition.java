/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.store;

import cleo.search.store.IntArrayPartition;
import java.util.ArrayList;
import java.util.List;
import krati.array.Array;

/**
 *
 * @author ferdinand
 */
public class MultiStaticIntArrayPartition implements IntArrayPartition
{
    protected final List<IntArrayPartition> arrays;
    
    public MultiStaticIntArrayPartition(List<IntArrayPartition> arrays)
    {
        this.arrays = arrays==null?new ArrayList<IntArrayPartition>():arrays;
    }

    public void clear()
    {
        for(IntArrayPartition array : arrays)
        {
            array.clear();
        }
    }

    public int length()
    {
        int length = 0;
        for(IntArrayPartition array : arrays)
        {
            length += array.length();
        }
        return length;
    }

    public boolean hasIndex(int index)
    {
        for(IntArrayPartition array : arrays)
        {
            if(array.hasIndex(index))
                return true;
        }
        
        return false;
    }

    public Type getType()
    {
        return Array.Type.STATIC;
    }

    public int capacity()
    {
        int capacity = 0;
        for(IntArrayPartition array : arrays)
        {
            capacity += array.capacity();
        }
        return capacity;
    }

    public int getIndexStart()
    {
        int indexStart = 0;
        for(IntArrayPartition array : arrays)
        {
            if(array.getIndexStart() < indexStart)
                indexStart = array.getIndexStart();
        }
        return indexStart;
    }

    public int getIndexEnd()
    {
        int indexEnd = 0;
        for(IntArrayPartition array : arrays)
        {
            if(array.getIndexEnd() > indexEnd)
                indexEnd = array.getIndexEnd();
        }
        return indexEnd;
    }

    public int get(int index)
    {
        int result = 0;
        for(IntArrayPartition array : arrays)
        {
            if(array.hasIndex(index))
                result = array.get(index);
        }
        return result;
    }

    public void set(int index, int value)
    {
        for(IntArrayPartition array : arrays)
        {
            if(array.hasIndex(index))
            {
                array.set(index, value);
                break;
            }
        }
    }

    public int[] getInternalArray()
    {
        int[] internalArray = new int[this.capacity()];
        for(IntArrayPartition array : arrays)
        {
            System.arraycopy(array.getInternalArray(), 0, internalArray, array.getIndexStart(),array.length());
        }
        return internalArray;
    }
    
}
