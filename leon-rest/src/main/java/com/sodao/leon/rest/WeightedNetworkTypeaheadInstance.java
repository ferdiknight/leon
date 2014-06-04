/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.rest;

import cleo.search.Element;
import cleo.search.Indexer;
import cleo.search.Searcher;
import cleo.search.connection.ConnectionIndexer;
import cleo.search.store.ArrayStoreElement;
import cleo.search.store.ArrayStoreWeights;
import cleo.search.store.IntArrayPartition;
import cleo.search.typeahead.NetworkTypeahead;

/**
 *
 * @param <E> 
 * @author ferdinand
 */
public interface WeightedNetworkTypeaheadInstance<E extends Element>
{
    public Indexer<E> getIndexer();
    
    public ArrayStoreElement<E> getElementStore();
    
    public NetworkTypeahead<E> getSearcher();
    
    public ConnectionIndexer getConnectionIndexer();
    
    public ArrayStoreWeights getConnectionStore();
    
    public IntArrayPartition getFilterStore();
    
}
