/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.index.construct;

import cleo.search.Element;
import cleo.search.Indexer;
import cleo.search.connection.ConnectionFilter;
import cleo.search.connection.ConnectionIndexer;
import cleo.search.connection.TransitivePartitionConnectionFilter;
import cleo.search.filter.BloomFilter;
import cleo.search.filter.FnvBloomFilter;
import cleo.search.selector.PrefixSelectorFactory;
import cleo.search.selector.SelectorFactory;
import cleo.search.store.ArrayStoreElement;
import cleo.search.store.ArrayStoreWeights;
import cleo.search.store.MemoryArrayStoreElement;
import cleo.search.store.StoreFactory;
import cleo.search.tool.ConnectionIndexerInitializer;
import cleo.search.tool.IndexerInitializer;
import cleo.search.tool.TypeaheadInitializer;
import cleo.search.typeahead.NetworkTypeaheadConfig;
import cleo.search.typeahead.PartitionableWeightedNetworkTypeahead;
import cleo.search.typeahead.Typeahead;
import cleo.search.util.Range;

/**
 *
 * @param <E>
 * @author ferdinand
 */
public class PartitionableWeightedNetworkTypeaheadInitializer<E extends Element>
        implements TypeaheadInitializer<E>, IndexerInitializer<E>, ConnectionIndexerInitializer
{

    private final PartitionableWeightedNetworkTypeahead<E> typeahead;

    public PartitionableWeightedNetworkTypeaheadInitializer(NetworkTypeaheadConfig<E> config) throws Exception
    {
        this.typeahead = createTypeahead(config);
    }

    private PartitionableWeightedNetworkTypeahead<E> createTypeahead(NetworkTypeaheadConfig<E> config) throws Exception
    {
        // create weightedConnectionsStore
        ArrayStoreWeights weightedConnectionsStore = StoreFactory.createArrayStoreWeights(
                config.getConnectionsStoreDir(),
                config.getConnectionsStoreCapacity(),
                config.getConnectionsStoreSegmentFactory(),
                config.getConnectionsStoreSegmentMB());

        // create elementStore
        ArrayStoreElement<E> elementStore = StoreFactory.createElementStorePartition(
                config.getElementStoreDir(),
                config.getElementStoreIndexStart(),
                config.getElementStoreCapacity(),
                config.getElementStoreSegmentFactory(),
                config.getElementStoreSegmentMB(),
                config.getElementSerializer());

        // load elementStore in memory
        if (config.isElementStoreCached())
        {
            elementStore = new MemoryArrayStoreElement<E>(elementStore);
        }

        // create bloomFilter
        BloomFilter<Integer> bloomFilter = new FnvBloomFilter(config.getFilterPrefixLength());

        // create selectorFactory
        SelectorFactory<E> selectorFactory = config.getSelectorFactory();
        if (selectorFactory == null)
        {
            selectorFactory = new PrefixSelectorFactory<E>();
        }

        // create connectionFilter
        ConnectionFilter connectionFilter = config.getConnectionFilter();
        if (connectionFilter == null)
        {
            connectionFilter = new TransitivePartitionConnectionFilter(new Range(config.getPartitionStart(), config.getPartitionCount()));
        }

        // create PartitionableWeightedNetworkTypeahead
        return new PartitionableWeightedNetworkTypeahead<E>(config.getName(), elementStore, weightedConnectionsStore, selectorFactory, bloomFilter, connectionFilter);
    }

    public Typeahead<E> getTypeahead()
    {
        return this.typeahead;
    }

    public Indexer<E> getIndexer()
    {
        return this.typeahead;
    }

    public ConnectionIndexer getConnectionIndexer()
    {
        return this.typeahead;
    }
}
