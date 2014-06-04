/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cleo.search.typeahead;

import cleo.search.Element;
import cleo.search.collector.Collector;
import cleo.search.collector.MultiCollector;
import cleo.search.collector.MultiSourceCollector;
import cleo.search.collector.SortedCollector;
import cleo.search.store.ArrayStoreElement;
import cleo.search.store.ArrayStoreWeights;
import cleo.search.store.IntArrayPartition;
import cleo.search.util.Range;
import cleo.search.util.Strings;
import java.util.*;
import java.util.concurrent.*;
import org.apache.log4j.Logger;

/**
 *
 * @param <E>
 * @author ferdinand
 */
public class MultiNetworkTypeahead<E extends Element> implements NetworkTypeahead<E>
{

    protected final List<PartitionableWeightedNetworkTypeahead<E>> typeaheads;
    private final Map<String, PartitionableWeightedNetworkTypeahead<E>> typeaheadMap;
    private final ExecutorService executor;
    private final static Logger logger = Logger.getLogger(MultiNetworkTypeahead.class);
    protected final String name;
    protected final Range range;
    protected final ArrayStoreElement<E> global_elementStore;
    protected final IntArrayPartition global_filterStore;
    protected final ArrayStoreWeights global_connectionsStore;

    public MultiNetworkTypeahead(String name, List<PartitionableWeightedNetworkTypeahead<E>> typeaheads,
            ArrayStoreElement<E> elementStore, IntArrayPartition filterStore, ArrayStoreWeights connectionStore)
    {
        this(name, typeaheads, elementStore, filterStore, connectionStore, Executors.newFixedThreadPool(100, new TypeaheadTaskThreadFactory()));
    }

    public MultiNetworkTypeahead(String name, List<PartitionableWeightedNetworkTypeahead<E>> typeaheads,
            ArrayStoreElement<E> elementStore, IntArrayPartition filterStore, ArrayStoreWeights connectionStore,
            ExecutorService executor)
    {
        int start = 0, count = 0;
        this.typeaheadMap = new HashMap<String, PartitionableWeightedNetworkTypeahead<E>>();
        for (Iterator<PartitionableWeightedNetworkTypeahead<E>> it = typeaheads.iterator(); it.hasNext();)
        {
            PartitionableWeightedNetworkTypeahead<E> typeahead = it.next();
            start = typeahead.getRange().getStart() < start ? typeahead.getRange().getStart() : start;
            count += typeahead.getRange().getCount();
            typeahead.setGlobal_elementStore(elementStore);
            typeahead.setGlobal_filterStore(filterStore);
            typeahead.setGlobal_connectionsStore(connectionStore);
            typeaheadMap.put(typeahead.getName(), typeahead);

        }
        this.global_connectionsStore = connectionStore;
        this.global_elementStore = elementStore;
        this.global_filterStore = filterStore;
        this.name = name;
        this.typeaheads = typeaheads;
        this.range = new Range(start, count);
        this.executor = executor;
    }

    public Range getRange()
    {
        return this.range;
    }

    public NetworkTypeaheadContext createContext(int uid)
    {
        NetworkTypeaheadContext context = new NetworkTypeaheadContextPlain(uid);

        if (global_connectionsStore.hasIndex(uid))
        {
            int[][] connStrengths = global_connectionsStore.getWeightData(uid);
            context.setConnectionStrengths(connStrengths);
        }

        return context;
    }

    public Collector<E> searchNetwork(int uid, String[] terms, Collector<E> collector, NetworkTypeaheadContext context)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getName()
    {
        return name;
    }

    public List<E> search(int uid, String[] terms)
    {
        return search(uid, terms, Integer.MAX_VALUE, Long.MAX_VALUE);
    }

    public List<E> search(int uid, String[] terms, long timeoutMillis)
    {
        return search(uid, terms, Integer.MAX_VALUE, timeoutMillis);
    }

    public List<E> search(int uid, String[] terms, int maxNumResults, long timeoutMillis)
    {
        Collector<E> collector = new SortedCollector<E>(maxNumResults);
        collector = search(uid, terms, collector, timeoutMillis);
        return collector.elements();
    }

    public Collector<E> search(int uid, String[] terms, Collector<E> collector)
    {
        return search(uid, terms, collector, Long.MAX_VALUE);
    }

    public Collector<E> search(int uid, String[] terms, Collector<E> collector, long timeoutMillis)
    {
        List<TypeaheadTask<E>> taskList = new ArrayList<TypeaheadTask<E>>(typeaheads.size());
        MultiSourceCollector<E> multiCollector = null;

        // Prepare tasks
        if (collector instanceof MultiCollector)
        {
            MultiCollector<E> mc = ((MultiCollector<E>) collector);
            for (String source : mc.sources())
            {
                Collector<E> c = mc.getCollector(source);
                Typeahead<E> ta = typeaheadMap.get(source);
                if (c != null && ta != null)
                {
                    taskList.add(new TypeaheadTask<E>(ta, uid, terms, c, timeoutMillis));
                }
            }
        }
        else
        {
            multiCollector = new MultiSourceCollector<E>();
            for (Typeahead<E> ta : typeaheads)
            {
                Collector<E> c = collector.newInstance();
                multiCollector.putCollector(ta.getName(), c);
                taskList.add(new TypeaheadTask<E>(ta, uid, terms, c, timeoutMillis));
            }
        }

        int numTasks = taskList.size();
        if (numTasks > 0)
        {
            // Execute tasks
            List<Future<Collector<E>>> futureList = new ArrayList<Future<Collector<E>>>(taskList.size());
            for (TypeaheadTask<E> t : taskList)
            {
                futureList.add(executor.submit(t));
            }

            for (Future<Collector<E>> f : futureList)
            {
                try
                {
                    f.get(timeoutMillis, TimeUnit.MILLISECONDS);
                }
                catch (TimeoutException e)
                {
                    warnTimeout(uid, terms, timeoutMillis);
                }
                catch (Exception e)
                {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        else
        {
            multiCollector = null;
        }

        return multiCollector == null ? collector : multiCollector;
    }

    protected void warnTimeout(int user, String[] terms, long timeout)
    {
        StringBuilder sb = new StringBuilder();

        sb.append(getName()).append(" Timeout").append(" user=").append(user).append(" time=").append(timeout).append(" terms=").append(Strings.toSet(terms));

        logger.warn(sb.toString());
    }

    public final List<PartitionableWeightedNetworkTypeahead<E>> subTypeaheads()
    {
        return typeaheads;
    }

    private final static class TypeaheadTaskThreadFactory implements ThreadFactory
    {

        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    }
}
