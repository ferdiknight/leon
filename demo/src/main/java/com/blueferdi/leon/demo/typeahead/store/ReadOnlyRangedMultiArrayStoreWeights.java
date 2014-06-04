/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.store;

import cleo.search.store.ArrayStoreWeights;
import cleo.search.util.Range;
import cleo.search.util.Weight;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ferdinand
 */
public class ReadOnlyRangedMultiArrayStoreWeights implements ArrayStoreWeights
{
    protected final Map<Range,ArrayStoreWeights> arrayStores;
    protected final Set<Range> keySet;
    protected final Type type;
    
    public ReadOnlyRangedMultiArrayStoreWeights(Map<Range,ArrayStoreWeights> arrayStores)
    {
        this.arrayStores = arrayStores==null?new HashMap<Range,ArrayStoreWeights>():arrayStores;
        this.keySet = arrayStores.keySet();
        this.type = arrayStores.values().iterator().next().getType();
    }
    
    private Range checkRange(int index)
    {
        for(Range range : keySet)
        {
            if(range.has(index))
                return range;
        }
        throw new RuntimeException("out of range: " + index);
    }
    
    public int getWeight(int index, int elemId)
    {
        return arrayStores.get(checkRange(index)).getWeight(index, elemId);
    }

    public void setWeight(int index, int elemId, int elemWeight, long scn) throws Exception
    {
        throw new RuntimeException("This store is read only!");
    }

    public int[][] getWeightData(int index)
    {
        return arrayStores.get(this.checkRange(index)).getWeightData(index);
    }

    public void setWeightData(int index, int[][] weightData, long scn) throws Exception
    {
        throw new RuntimeException("This store is read only!");
    }

    public void setWeightData(int index, List<Weight> weightData, long scn) throws Exception
    {
        throw new RuntimeException("This store is read only!");
    }

    public void remove(int index, int elemId, long scn) throws Exception
    {
        throw new RuntimeException("This store is read only!");
    }

    public void delete(int index, long scn) throws Exception
    {
        throw new RuntimeException("This store is read only!");
    }

    public int getLength(int index)
    {
        return arrayStores.get(this.checkRange(index)).getLength(index);
    }

    public byte[] getBytes(int index)
    {
        return arrayStores.get(this.checkRange(index)).getBytes(index);
    }

    public int getBytes(int index, byte[] dst)
    {
        return arrayStores.get(this.checkRange(index)).getBytes(index, dst);
    }

    public int getBytes(int index, byte[] dst, int offset)
    {
        return arrayStores.get(this.checkRange(index)).getBytes(index, dst, offset);
    }

    public int readBytes(int index, byte[] dst)
    {
        return arrayStores.get(this.checkRange(index)).readBytes(index, dst);
    }

    public int readBytes(int index, int offset, byte[] dst)
    {
        return arrayStores.get(this.checkRange(index)).readBytes(index, offset, dst);
    }

    public void sync() throws IOException
    {
        throw new RuntimeException("This store is read only!");
    }

    public void persist() throws IOException
    {
        throw new RuntimeException("This store is read only!");
    }

    public long getLWMark()
    {
        throw new RuntimeException("This store is read only!");
    }

    public long getHWMark()
    {
        throw new RuntimeException("This store is read only!");
    }

    public void saveHWMark(long endOfPeriod) throws Exception
    {
        throw new RuntimeException("This store is read only!");
    }

    public void clear()
    {
        throw new RuntimeException("This store is read only!");
    }

    public int length()
    {
        int length = 0;
        for(ArrayStoreWeights store : arrayStores.values())
        {
            length += store.length();
        }
        return length;
    }

    public boolean hasIndex(int index)
    {
        return arrayStores.get(this.checkRange(index)).hasIndex(index);
    }

    public Type getType()
    {
        return type;
    }
    
}
