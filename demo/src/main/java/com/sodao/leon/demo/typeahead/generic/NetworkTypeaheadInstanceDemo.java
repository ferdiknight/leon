/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.demo.typeahead.generic;

import cleo.search.ElementSerializer;
import cleo.search.SimpleElement;
import cleo.search.collector.Collector;
import cleo.search.collector.SortedCollector;
import cleo.search.connection.Connection;
import cleo.search.connection.ConnectionFilter;
import cleo.search.connection.SimpleConnection;
import cleo.search.connection.TransitivePartitionConnectionFilter;
import cleo.search.selector.ScoredElementSelectorFactory;
import cleo.search.selector.SelectorFactory;
import cleo.search.store.ArrayStoreElement;
import cleo.search.store.ArrayStoreWeights;
import cleo.search.store.StoreFactory;
import cleo.search.tool.WeightedNetworkTypeaheadInitializer;
import cleo.search.typeahead.NetworkTypeaheadConfig;
import cleo.search.typeahead.NetworkTypeaheadContext;
import cleo.search.typeahead.WeightedNetworkTypeahead;
import cleo.search.util.Range;
import com.mongodb.*;
import com.sodao.leon.demo.typeahead.element.AvroElement;
import com.sodao.leon.demo.typeahead.element.TestElement;
import com.sodao.leon.demo.typeahead.serialize.self.SelfElementSerializer;
import com.sodao.leon.demo.typeahead.serialize.self.SelfSerializableElement;
import java.io.File;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import krati.core.segment.MappedSegmentFactory;
import krati.core.segment.MemorySegmentFactory;
import krati.core.segment.SegmentFactory;

/**
 *
 * @author ferdinand
 */
public class NetworkTypeaheadInstanceDemo
{
    private WeightedNetworkTypeahead<SelfSerializableElement> typeahead;
    private Random rand = new Random();
    
    private int start = 0;
    private int count = 60000000;

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }
    
    public String getHomeDir()
    {
        return "testnetworktypeahead";
    }

    public ConnectionFilter createConnectionFilter()
    {
        return new TransitivePartitionConnectionFilter(new Range(start, count));
    }

    protected ArrayStoreElement<AvroElement> createElementStore(ElementSerializer<AvroElement> serializer) throws Exception
    {
        File elementStoreDir = new File(getHomeDir(), "element-store");
        int elementStoreSegMB = 32;

        ArrayStoreElement<AvroElement> elementStore =
                StoreFactory.createElementStorePartition(
                elementStoreDir,
                start,
                count,
                new MemorySegmentFactory(),
                elementStoreSegMB,
                serializer);
        return elementStore;
    }
    
    public ArrayStoreWeights createWeightedConnectionsStore() throws Exception {
    File connectionsStoreDir = new File(getHomeDir(), "weighted-connections-store");
    int connectionsStoreSegMB = 32;
    SegmentFactory connectionsStoreSegFactory = new MemorySegmentFactory();
    
    ArrayStoreWeights connectionsStore =
      StoreFactory.createArrayStoreWeights(
          connectionsStoreDir,
          count,
          connectionsStoreSegFactory,
          connectionsStoreSegMB);
    
    return connectionsStore;
  }

    public SelectorFactory<SelfSerializableElement> createSelectorFactory()
    {
        return new ScoredElementSelectorFactory<SelfSerializableElement>();
    }

    public WeightedNetworkTypeahead<SelfSerializableElement> createTypeahead() throws Exception
    {
        NetworkTypeaheadConfig<SelfSerializableElement> config = new NetworkTypeaheadConfig<SelfSerializableElement>();

        config.setName("Network");

        config.setPartitionStart(start);
        config.setPartitionCount(count);


        
        ElementSerializer<SelfSerializableElement> serializer = new SelfElementSerializer<SelfSerializableElement>();
        config.setElementSerializer(serializer);
        config.setElementStoreDir(new File(getHomeDir(), "element-store"));
        config.setElementStoreIndexStart(start);
        config.setElementStoreCapacity(count);
        config.setElementStoreSegmentMB(32);
        config.setElementStoreCached(false);

        config.setConnectionsStoreDir(new File(getHomeDir(), "weighted-connections-store"));
        config.setConnectionsStoreIndexStart(start);
        config.setConnectionsStoreCapacity(count);
        config.setConnectionsStoreSegmentMB(32);
        config.setConnectionsStoreSegmentFactory(new MappedSegmentFactory());

        config.setConnectionFilter(createConnectionFilter());
        config.setSelectorFactory(createSelectorFactory());
        config.setFilterPrefixLength(2);

        WeightedNetworkTypeaheadInitializer<SelfSerializableElement> initializer =
                new WeightedNetworkTypeaheadInitializer<SelfSerializableElement>(config);

        return (WeightedNetworkTypeahead<SelfSerializableElement>) initializer.getTypeahead();
    }
    
    public NetworkTypeaheadInstanceDemo()
    {
        try
        {
            typeahead = this.createTypeahead();
        }
        catch (Exception ex)
        {
            Logger.getLogger(NetworkTypeaheadInstanceDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void basisStore() throws Exception
    {
        int uid = rand.nextInt(100000);
        int elemId = rand.nextInt(100000);

        Connection conn = new SimpleConnection(uid, elemId, true);
        conn.setTimestamp(System.currentTimeMillis());

        SelfSerializableElement elem = new SelfSerializableElement();
        elem.setId(elemId);
        elem.setTimestamp(System.currentTimeMillis());
        elem.setTerms("Bloom", "filter");
        
        typeahead.index(elem);
        typeahead.index(conn);
        typeahead.flush(); 
    }
    
    public void networkStore() throws Exception
    {
        int total = 0;
        Connection conn;
        SelfSerializableElement e;
//        for(int i=start;i<start + count;i++)
//        {
//            int number = rand.nextInt(2000);
//            int target = start + rand.nextInt(count - 2000);
//            for(int j=0;j<number;j++)
//            {
        
        
//                e = new SelfSerializableElement();
//                e.setId(236754);
//                e.setName("testuser1");
//                e.setScore(0);
//                e.setTerms("testuser1");
//                e.setTimestamp(System.currentTimeMillis());
//                typeahead.index(e);
//                
//                e = new SelfSerializableElement();
//                e.setId(225637);
//                e.setName("testuser2");
//                e.setScore(0);
//                e.setTerms("testuser2");
//                e.setTimestamp(System.currentTimeMillis());
//                typeahead.index(e);
//                
//                e = new SelfSerializableElement();
//                e.setId(222123);
//                e.setName("testuser3");
//                e.setScore(0);
//                e.setTerms("testuser3");
//                e.setTimestamp(System.currentTimeMillis());
//                typeahead.index(e);
//                
//                e = new SelfSerializableElement();
//                e.setId(242576);
//                e.setName("testuser4");
//                e.setScore(0);
//                e.setTerms("testuser4");
//                e.setTimestamp(System.currentTimeMillis());
//                typeahead.index(e);
//        
//        
//                conn = new SimpleConnection(236754, 225637, true);
//                conn.setTimestamp(System.currentTimeMillis());
//                typeahead.index(conn);
//                
//                
//                conn = new SimpleConnection(236754,222123,true);
//                conn.setTimestamp(System.currentTimeMillis());
//                typeahead.index(conn);
//                
//                conn = new SimpleConnection(222123,242576,true);
//                conn.setTimestamp(System.currentTimeMillis());
//                typeahead.index(conn);
//                
//                conn = new SimpleConnection(236754,242576,true);
//                conn.setTimestamp(System.currentTimeMillis());
//                typeahead.index(conn);
//        
//                e = new SelfSerializableElement();
//                e.setId(236755);
//                e.setName("testuser5");
//                e.setScore(0);
//                e.setTerms("testuser5");
//                e.setTimestamp(System.currentTimeMillis());
//                typeahead.index(e);
//                
//                conn = new SimpleConnection(242576,236755,true);
//                conn.setTimestamp(System.currentTimeMillis());
//                typeahead.index(conn);
//                
//                typeahead.flush();
                
                int[][] weight = typeahead.getConnectionsStore().getWeightData(222123);
        
                NetworkTypeaheadContext context = typeahead.createContext(222123);
                Collector<SelfSerializableElement> collector = new SortedCollector<SelfSerializableElement>(10, 100);
                
                List<SelfSerializableElement> list = typeahead.search(222123, new String[]{"t"});
                Collector<SelfSerializableElement> network_list = typeahead.searchNetwork(236754, new String[]{"t"}, collector, context);
                
                //target ++;
            //}
            
            //total += number;            
//        }
//        typeahead.flush();
        System.out.println(total);
    }
    
    public int networkStore(DBCursor cur) throws Exception
    {
        int total = 0;
        Connection conn;
        List<Long> ids;
        long uid = 0l;
        Object id;
        DBObject object;
        while(cur.hasNext())
        {
            object = cur.next();
            id = object.get("_id");        
            if(id instanceof Long)
            {
                uid = Long.class.cast(id).longValue();
            }
            else if(id instanceof String)
            {
                try
                {
                    uid = Long.parseLong(String.class.cast(id).substring(0, ((String)id).indexOf("_")));
                }
                catch (Exception ex)
                {
                    System.out.println(id + ":" + ex.getMessage());
                    continue;
                }
            }
            else
            {
                continue;
            }
            
            ids = (List<Long>)object.get("targets");
            
            if(ids == null)
                continue;
            
            for(int i=0;i<ids.size();i++)
            {
                if(i % 3000 == 0)
                    typeahead.flush();
                long target = ids.get(i);
                conn = new SimpleConnection((int)uid,(int)target,true);
                conn.setTimestamp(System.currentTimeMillis());
                typeahead.index(conn);
            }
            
            total += ids.size();
        }
        return total;
    }
    
    public void elementStore() throws Exception
    {
        for(int i=start;i<start + count;i++)
        {
            SelfSerializableElement e = new SelfSerializableElement();
            e.setId(i);
            e.setTimestamp(System.currentTimeMillis());
            e.setName(Integer.toString(i));
            e.setTerms(Integer.toString(i));
            if(!typeahead.index(e))
                break;
        }
        typeahead.flush();
    }

    public static void main(String[] args) throws Exception
    {
        NetworkTypeaheadInstanceDemo demo = new NetworkTypeaheadInstanceDemo();
        demo.networkStore();
//        long start = System.currentTimeMillis();
//        
//        Mongo mongo = new Mongo("10.0.11.174");
//        DB db = mongo.getDB("user_relation_sharding_0");
//        int total = 0;
//        DBCollection coll;
//        DBCursor cur;
//        int j = 0;
//        for(int i=0;i<256;i++)
//        {
//
//            coll = db.getCollection("friends_sharding_" + i);
//
//            cur = coll.find();
//            j = demo.networkStore(cur);
//            System.out.println( j + " " + i);
//            cur.close();
//            total += j;
//        }
////        demo.networkStore();
//        System.out.println(System.currentTimeMillis() - start);
//        
//        System.out.println(total);

    }
}
