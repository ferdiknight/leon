/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.index.construct;

import cleo.search.Element;
import cleo.search.Indexer;
import cleo.search.MultiIndexer;
import cleo.search.connection.ConnectionIndexer;
import cleo.search.connection.MultiConnectionIndexer;
import cleo.search.store.ArrayStoreElement;
import cleo.search.store.ArrayStoreWeights;
import cleo.search.store.IntArrayPartition;
import cleo.search.store.MultiArrayStoreElement;
import cleo.search.typeahead.*;
import cleo.search.util.Range;
import com.blueferdi.leon.demo.typeahead.store.MultiStaticIntArrayPartition;
import com.blueferdi.leon.demo.typeahead.store.ReadOnlyRangedMultiArrayStoreWeights;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @param <E>
 * @author ferdinand
 */
public class PartitionableWeightedNetworkTypeaheadInstance<E extends Element>
{
    private Log logger = LogFactory.getLog(PartitionableWeightedNetworkTypeaheadInstance.class);

    final ConnectionIndexer connectionIndexer;
    final Indexer<E> indexer;
    final NetworkTypeahead<E> searcher;
    final ArrayStoreElement<E> elementStore;
    final IntArrayPartition filterStore;
    final ArrayStoreWeights connectionsStore;

    public PartitionableWeightedNetworkTypeaheadInstance(String name, NetworkTypeaheadConfig<E>[] configs)
    {
        List<ConnectionIndexer> connectionIndexerList = new ArrayList<ConnectionIndexer>();
        List<Indexer<E>> indexerList = new ArrayList<Indexer<E>>();
        List<PartitionableWeightedNetworkTypeahead<E>> searcherList = new ArrayList<PartitionableWeightedNetworkTypeahead<E>>();

        List<ArrayStoreElement<E>> storeList = new ArrayList<ArrayStoreElement<E>>();
        List<IntArrayPartition> filterList = new ArrayList<IntArrayPartition>();
        Map<Range, ArrayStoreWeights> connectionsMap = new HashMap<Range, ArrayStoreWeights>();

        for (int i = 0, cnt = configs.length; i < cnt; i++)
        {
            PartitionableWeightedNetworkTypeahead<E> pnt = createTypeahead(configs[i]);
            connectionIndexerList.add(pnt);
            indexerList.add(pnt);
            searcherList.add(pnt);
            storeList.add(pnt.getElementStore());
            filterList.add(pnt.getFilterStore());
            connectionsMap.put(pnt.getRange(), pnt.getConnectionsStore());
        }

        // Create indexer, searcher and elementStore
        connectionIndexer = new MultiConnectionIndexer(name, connectionIndexerList);
        indexer = new MultiIndexer<E>(name, indexerList);
        elementStore = new MultiArrayStoreElement<E>(storeList);
        filterStore = new MultiStaticIntArrayPartition(filterList);
        connectionsStore = new ReadOnlyRangedMultiArrayStoreWeights(connectionsMap);

        searcher = new MultiNetworkTypeahead<E>(name, searcherList, elementStore, filterStore, connectionsStore);

        addShutdownHook();
    }

    private void addShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {

            @Override
            public void run()
            {
                try
                {
                    indexer.flush();
                    connectionIndexer.flush();
                }
                catch (IOException e)
                {
                    logger.error("flush datas error when stopping : " + e.getMessage(), e);
                }
            }
        });
    }

    private PartitionableWeightedNetworkTypeahead<E> createTypeahead(NetworkTypeaheadConfig<E> config)
    {
        try
        {
            PartitionableWeightedNetworkTypeaheadInitializer<E> initializer =
                    new PartitionableWeightedNetworkTypeaheadInitializer<E>(config);

            return (PartitionableWeightedNetworkTypeahead<E>) initializer.getTypeahead();
        }
        catch (Exception ex)
        {
            logger.error("Error when creating typeahead : " + ex.getMessage(), ex);
        }
        return null;
    }

    public ArrayStoreWeights getConnectionsStore()
    {
        return connectionsStore;
    }

    public IntArrayPartition getFilterStore()
    {
        return filterStore;
    }
    
    public ConnectionIndexer getConnectionIndexer()
    {
        return connectionIndexer;
    }

    public ArrayStoreElement<E> getElementStore()
    {
        return elementStore;
    }

    public Indexer<E> getIndexer()
    {
        return indexer;
    }

    public NetworkTypeahead<E> getSearcher()
    {
        return searcher;
    }
}
