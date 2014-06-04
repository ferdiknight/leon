/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.concurrent;

import cleo.search.Element;
import cleo.search.collector.Collector;
import cleo.search.typeahead.NetworkTypeahead;
import cleo.search.typeahead.NetworkTypeaheadContext;
import java.util.concurrent.Callable;

/**
 *
 * @param <E>
 * @author ferdinand
 */
public class NetworkTypeaheadTask<E extends Element> implements Callable<Collector<E>>
{

    private final NetworkTypeahead<E> ta;
    private final int uid;
    private final String[] terms;
    private final Collector<E> collector;
    private final NetworkTypeaheadContext context;
    
    public NetworkTypeaheadTask(NetworkTypeahead<E> ta,int uid,String[] terms,Collector<E> collector,NetworkTypeaheadContext context)
    {
        this.ta = ta;
        this.uid = uid;
        this.terms = terms;
        this.collector = collector;
        this.context = context;
    }

    public Collector<E> call() throws Exception
    {
        return ta.searchNetwork(uid, terms, collector, context);
    }

    public Collector<E> getCollector()
    {
        return collector;
    }

    public NetworkTypeaheadContext getContext()
    {
        return context;
    }

    public NetworkTypeahead<E> getTa()
    {
        return ta;
    }

    public String[] getTerms()
    {
        return terms;
    }

    public int getUid()
    {
        return uid;
    }   
}
