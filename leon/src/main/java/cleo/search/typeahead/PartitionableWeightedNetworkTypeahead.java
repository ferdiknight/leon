/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cleo.search.typeahead;

import cleo.search.Element;
import cleo.search.collector.Collector;
import cleo.search.connection.ConnectionFilter;
import cleo.search.filter.BloomFilter;
import cleo.search.network.Proximity;
import cleo.search.selector.Selector;
import cleo.search.selector.SelectorContext;
import cleo.search.selector.SelectorFactory;
import cleo.search.store.ArrayStoreElement;
import cleo.search.store.ArrayStoreWeights;
import cleo.search.store.IntArrayPartition;
import cleo.search.util.*;
import java.util.HashSet;

/**
 *
 * @param <E>
 * @author ferdinand
 */
public class PartitionableWeightedNetworkTypeahead<E extends Element> extends WeightedNetworkTypeahead<E>
{

    protected ArrayStoreElement<E> global_elementStore;
    protected IntArrayPartition global_filterStore;
    protected ArrayStoreWeights global_connectionsStore;

    public PartitionableWeightedNetworkTypeahead(String name,
            ArrayStoreElement<E> elementStore,
            ArrayStoreWeights connectionsStore,
            SelectorFactory<E> selectorFactory,
            BloomFilter<Integer> bloomFilter,
            ConnectionFilter connFilter)
    {
        this(name, elementStore, connectionsStore, selectorFactory, bloomFilter, connFilter, new ConnectionStrengthAdjuster());
    }

    public PartitionableWeightedNetworkTypeahead(String name,
            ArrayStoreElement<E> elementStore,
            ArrayStoreWeights connectionsStore,
            SelectorFactory<E> selectorFactory,
            BloomFilter<Integer> bloomFilter,
            ConnectionFilter connFilter,
            WeightAdjuster weightAdjuster)
    {
        super(name, elementStore, connectionsStore, selectorFactory, bloomFilter, connFilter, weightAdjuster);
    }

    public IntArrayPartition getFilterStore()
    {
        return filterStore;
    }

    public ArrayStoreWeights getGlobal_connectionsStore()
    {
        return global_connectionsStore;
    }

    public void setGlobal_connectionsStore(ArrayStoreWeights global_connectionsStore)
    {
        this.global_connectionsStore = global_connectionsStore;
    }

    public ArrayStoreElement<E> getGlobal_elementStore()
    {
        return global_elementStore;
    }

    public void setGlobal_elementStore(ArrayStoreElement<E> global_elementStore)
    {
        this.global_elementStore = global_elementStore;
    }

    public IntArrayPartition getGlobal_filterStore()
    {
        return global_filterStore;
    }

    public void setGlobal_filterStore(IntArrayPartition global_filterStore)
    {
        this.global_filterStore = global_filterStore;
    }

    @Override
    protected void searchNetworkInternal(int uid, String[] terms, Collector<E> collector, Selector<E> selector, HitStats hitStats, NetworkTypeaheadContext context)
    {
        final long timeoutMillis = context.getTimeoutMillis();
        final long startTime = System.currentTimeMillis();
        long totalTime = 0;

        int filter = bloomFilter.computeQueryFilter(terms);
        if (connectionsStore.hasIndex(uid))
        {
            int[][] connStrengths = context.getConnectionStrengths();
            if (connStrengths != null)
            {
                long timeout = timeoutMillis;
                HashSet<Integer> uniqIds = new HashSet<Integer>(199);

                // Filter out the network center 
                uniqIds.add(context.getSource());

                // Process 1st degree connections
                applyFilter(filter, connStrengths, collector, selector, uniqIds, hitStats, timeout);
                if (collector.canStop())
                {
                    return;
                }

                // Check timeout
                totalTime = System.currentTimeMillis() - startTime;
                timeout = timeoutMillis - totalTime;
                if (timeout <= 0)
                {
                    return;
                }

                // Process 2nd degree connections
                int[] connIds = connStrengths[ArrayStoreWeights.ELEMID_SUBARRAY_INDEX];
                int[] weights = connStrengths[ArrayStoreWeights.WEIGHT_SUBARRAY_INDEX];

                /**
                 * *******************************************************************
                 * Reuse a byte array for read second-degree connection strength
                 * data.
                 * ********************************************************************
                 */
                // Get a byte array from resource pool
                byte[] bytes = getBytesFromPool();

                try
                {
                    for (int i = 0, cnt = connIds.length; i < cnt; i++)
                    {
                        int connectionId = connIds[i];

                        WeightIteratorFromBytes connStrengthIter = getConnectionStrengthIterator2(connectionId, bytes);
                        if (connStrengthIter == null)
                        {
                            continue;
                        }
                        bytes = connStrengthIter.array();

                        applyFilter2(filter, weights[i], connStrengthIter, collector, selector, uniqIds, hitStats, timeout);
                        if (collector.canStop())
                        {
                            break;
                        }

                        // Check timeout
                        totalTime = System.currentTimeMillis() - startTime;
                        timeout = timeoutMillis - totalTime;
                        if (timeout <= 0)
                        {
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    getLogger().warn(e.getMessage(), e);
                }
                finally
                {
                    // Return the byte array to resource pool
                    if (bytes != null && bytes.length == byteArraySize)
                    {
                        bytesPool.put(bytes);
                    }
                    else
                    {
                        if (bytes != null)
                        {
                            getLogger().info("bytes on the fly: " + bytes.length);
                        }
                    }
                }
            }
        }
    }

    WeightIteratorFromBytes getConnectionStrengthIterator2(int uid, byte[] bytes)
    {
        if (global_connectionsStore.hasIndex(uid))
        {
            // Read connection strength data into raw byte array
            int lenRead = partialReadEnabled
                    ? global_connectionsStore.readBytes(uid, bytes) : global_connectionsStore.getBytes(uid, bytes);

            // Check whether connection strength data was read successfully
            if (lenRead < 0)
            {
                if (global_connectionsStore.getLength(uid) > bytes.length)
                {
                    // Read a new byte array from the connection store
                    byte[] bytesNew = global_connectionsStore.getBytes(uid);
                    if (bytesNew != null)
                    {
                        lenRead = bytesNew.length;

                        // Return the byte array to resource pool
                        if (lenRead > 0 && bytes.length == byteArraySize)
                        {
                            bytesPool.put(bytes);
                        }
                        bytes = bytesNew;
                    }
                }
            }

            if (lenRead > 0)
            {
                return new WeightIteratorFromBytes(bytes, 0, lenRead);
            }
        }

        return null;
    }

    @Override
    protected long applyFilter(int filter, WeightIterator connStrengthIter, Collector<E> collector, Selector<E> selector, HitStats hitStats, long timeoutMillis)
    {
        long totalTime = 0;
        long startTime = System.currentTimeMillis();

        int numBrowseHits = 0;
        int numFilterHits = 0;
        int numResultHits = 0;

        Weight w = new Weight(0, 0);
        SelectorContext ctx = new SelectorContext();

        while (connStrengthIter.hasNext())
        {
            numBrowseHits++;
            connStrengthIter.next(w);
            int elemId = w.elementId;

            if (global_elementStore.hasIndex(elemId) && (global_filterStore.get(elemId) & filter) == filter)
            {
                numFilterHits++;

                E elem = global_elementStore.getElement(elemId);
                if (elem != null)
                {
                    if (selector.select(elem, ctx))
                    {
                        numResultHits++;

                        double hitScore = ctx.getScore() * (w.elementWeight + 1);
                        collector.add(elem, hitScore, getName(), Proximity.DEGREE_1);
                        if (collector.canStop())
                        {
                            break;
                        }
                    }

                    ctx.clear();
                }
            }

            if (numBrowseHits % 100 == 0)
            {
                totalTime = System.currentTimeMillis() - startTime;
                if (totalTime > timeoutMillis)
                {
                    break;
                }
            }
        }

        hitStats.numBrowseHits += numBrowseHits;
        hitStats.numFilterHits += numFilterHits;
        hitStats.numResultHits += numResultHits;

        return System.currentTimeMillis() - startTime;
    }

    @Override
    protected long applyFilter(int filter, WeightIterator connStrengthIter, Collector<E> collector, Selector<E> selector, HashSet<Integer> uniqIdSet, HitStats hitStats, long timeoutMillis)
    {
        long totalTime = 0;
        long startTime = System.currentTimeMillis();

        int numBrowseHits = 0;
        int numFilterHits = 0;
        int numResultHits = 0;

        Weight w = new Weight(0, 0);
        SelectorContext ctx = new SelectorContext();

        while (connStrengthIter.hasNext())
        {
            numBrowseHits++;
            connStrengthIter.next(w);
            int elemId = w.elementId;

            if (global_elementStore.hasIndex(elemId) && (global_filterStore.get(elemId) & filter) == filter)
            {
                numFilterHits++;

                if (!uniqIdSet.contains(elemId))
                {
                    uniqIdSet.add(elemId);

                    E elem = global_elementStore.getElement(elemId);
                    if (elem != null)
                    {
                        if (selector.select(elem, ctx))
                        {
                            numResultHits++;

                            double hitScore = ctx.getScore() * (w.elementWeight + 1);
                            collector.add(elem, hitScore, getName(), Proximity.DEGREE_1);
                            if (collector.canStop())
                            {
                                break;
                            }
                        }

                        ctx.clear();
                    }
                }
            }

            if (numBrowseHits % 100 == 0)
            {
                totalTime = System.currentTimeMillis() - startTime;
                if (totalTime > timeoutMillis)
                {
                    break;
                }
            }
        }

        hitStats.numBrowseHits += numBrowseHits;
        hitStats.numFilterHits += numFilterHits;
        hitStats.numResultHits += numResultHits;

        return System.currentTimeMillis() - startTime;
    }

    @Override
    protected long applyFilter(int filter, int[][] connStrengths, Collector<E> collector, Selector<E> selector, HashSet<Integer> uniqIdSet, HitStats hitStats, long timeoutMillis)
    {
        long totalTime = 0;
        long startTime = System.currentTimeMillis();

        int i = 0;
        int numFilterHits = 0;
        int numResultHits = 0;

        int[] elemIds = connStrengths[ArrayStoreWeights.ELEMID_SUBARRAY_INDEX];
        int[] weights = connStrengths[ArrayStoreWeights.WEIGHT_SUBARRAY_INDEX];

        SelectorContext ctx = new SelectorContext();

        for (int cnt = elemIds.length; i < cnt; i++)
        {
            int elemId = elemIds[i];

            if (global_elementStore.hasIndex(elemId) && (global_filterStore.get(elemId) & filter) == filter)
            {
                numFilterHits++;

                if (!uniqIdSet.contains(elemId))
                {
                    uniqIdSet.add(elemId);

                    E elem = global_elementStore.getElement(elemId);
                    if (elem != null)
                    {
                        if (selector.select(elem, ctx))
                        {
                            numResultHits++;

                            double hitScore = ctx.getScore() * (weights[i] + 1);
                            collector.add(elem, hitScore, getName(), Proximity.DEGREE_1);
                            if (collector.canStop())
                            {
                                i++;
                                break;
                            }
                        }

                        ctx.clear();
                    }
                }
            }

            if (i % 100 == 0)
            {
                totalTime = System.currentTimeMillis() - startTime;
                if (totalTime > timeoutMillis)
                {
                    break;
                }
            }
        }

        hitStats.numBrowseHits += i;
        hitStats.numFilterHits += numFilterHits;
        hitStats.numResultHits += numResultHits;

        return System.currentTimeMillis() - startTime;
    }

    @Override
    long applyFilter2(int filter, int connStrengthInherited, WeightIterator connStrengthIterator, Collector<E> collector, Selector<E> selector, HashSet<Integer> uniqIdSet, HitStats hitStats, long timeoutMillis)
    {
        long totalTime = 0;
        long startTime = System.currentTimeMillis();

        int numBrowseHits = 0;
        int numFilterHits = 0;
        int numResultHits = 0;

        Weight w = new Weight(0, 0);
        SelectorContext ctx = new SelectorContext();

        while (connStrengthIterator.hasNext())
        {
            numBrowseHits++;
            connStrengthIterator.next(w);
            int elemId = w.elementId;

            if (global_elementStore.hasIndex(elemId) && (global_filterStore.get(elemId) & filter) == filter)
            {
                numFilterHits++;

                if (!uniqIdSet.contains(elemId))
                {
                    uniqIdSet.add(elemId);

                    E elem = global_elementStore.getElement(elemId);
                    if (elem != null)
                    {
                        if (selector.select(elem, ctx))
                        {
                            numResultHits++;

                            double hitScore = ctx.getScore() * (weightAdjuster.adjust(connStrengthInherited, w.elementWeight) + 1);
                            collector.add(elem, hitScore, getName(), Proximity.DEGREE_2);
                            if (collector.canStop())
                            {
                                break;
                            }
                        }

                        ctx.clear();
                    }
                }
            }

            if (numBrowseHits % 100 == 0)
            {
                totalTime = System.currentTimeMillis() - startTime;
                if (totalTime > timeoutMillis)
                {
                    break;
                }
            }
        }

        hitStats.numBrowseHits += numBrowseHits;
        hitStats.numFilterHits += numFilterHits;
        hitStats.numResultHits += numResultHits;

        return System.currentTimeMillis() - startTime;
    }
}
