/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.demo.typeahead.index.construct;

import cleo.search.ElementSerializer;
import cleo.search.connection.*;
import cleo.search.selector.ScoredElementSelectorFactory;
import cleo.search.selector.ScoredPrefixSelectorFactory;
import cleo.search.typeahead.*;
import cleo.search.util.Range;
import com.mongodb.*;
import com.sodao.leon.demo.typeahead.serialize.self.SelfElementSerializer;
import com.sodao.leon.demo.typeahead.serialize.self.SelfSerializableElement;
import com.sodao.leon.demo.typeahead.termer.SimpleTermer;
import com.sodao.leon.demo.typeahead.termer.Termer;
import com.sodao.leon.typehead.index.construct.IndexConstructor.Client;
import com.sodao.leon.typehead.index.construct.TypeaheadElement;
import com.sodao.leon.typehead.index.construct.TypeaheadElementSet;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import krati.core.segment.MappedSegmentFactory;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 *
 * @author ferdinand
 */
public class TypeaheadDataInit
{
    
    public static int networkStore(DBCursor cur,ConnectionIndexer typeahead) throws Exception
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
                long target = ids.get(i);
                conn = new SimpleConnection((int)uid,(int)target,true);
                conn.setTimestamp(System.currentTimeMillis());
                typeahead.index(conn);
            }
            
            total += ids.size();
        }
        return total;
    }

    public static void main(String[] args) throws Exception
    {
        long start_time = System.currentTimeMillis();
        String homeDir = "testuser";
        ElementSerializer<SelfSerializableElement> serializer = new SelfElementSerializer<SelfSerializableElement>();
        @SuppressWarnings("unchecked")
        NetworkTypeaheadConfig<SelfSerializableElement>[] configs = new NetworkTypeaheadConfig[10];
        for (int i = 0; i < 10; i++)
        {
            NetworkTypeaheadConfig<SelfSerializableElement> config = new NetworkTypeaheadConfig<SelfSerializableElement>();

            int start = i * 1000000 + (i == 0 ? 0 : 1);
            int count = 1000000;

            config.setName("user" + i);
            config.setPartitionStart(start);
            config.setPartitionCount(count);
            config.setElementSerializer(serializer);
            config.setElementStoreDir(new File(homeDir + i, "element-store"));
            config.setElementStoreIndexStart(start);
            config.setElementStoreCapacity(count);
            config.setElementStoreSegmentMB(32);
            config.setElementStoreCached(false);

            config.setConnectionsStoreDir(new File(homeDir + i, "weighted-connections-store"));
            config.setConnectionsStoreIndexStart(start);
            config.setConnectionsStoreCapacity(count);
            config.setConnectionsStoreSegmentMB(32);
            config.setConnectionsStoreSegmentFactory(new MappedSegmentFactory());
            config.setConnectionFilter(new SourcePartitionConnectionFilter(new Range(start, count)));

//            config.setConnectionFilter(new TransitivePartitionConnectionFilter(new Range(start, count)));
            config.setSelectorFactory(new ScoredPrefixSelectorFactory<SelfSerializableElement>());
            config.setFilterPrefixLength(2);
            configs[i] = config;
        }

        PartitionableWeightedNetworkTypeaheadInstance<SelfSerializableElement> instance = new PartitionableWeightedNetworkTypeaheadInstance<SelfSerializableElement>("UserTypeahead", configs);
        
        List<SelfSerializableElement> list = instance.getSearcher().search(4304663, new String[]{"z"});
//        List<PartitionableWeightedNetworkTypeahead<SelfSerializableElement>> typeaheads = ((MultiNetworkTypeahead<SelfSerializableElement>)instance.getSearcher()).subTypeaheads();
//        List<SelfSerializableElement> result = new ArrayList<SelfSerializableElement>();
//        for(PartitionableWeightedNetworkTypeahead<SelfSerializableElement> typeahead : typeaheads)
//        {
//             List<SelfSerializableElement> parti_result = typeahead.search(4304663, new String[]{"zhe"});
//             result.addAll(parti_result);
//        }

        
        
//        Termer termer = new SimpleTermer();
//        String address = "10.0.250.75";
//        int port = 3188;
//        int clientTimeout = 30000000;
//        TTransport transport = new TFramedTransport(new TSocket(address, port,
//                clientTimeout));
//        TProtocol protocol = new TBinaryProtocol(transport);
//        transport.open();
//        Client c = new Client(protocol);
//        
//        boolean flag = true;
//        String position = "";
//        while(flag)
//        {
//            TypeaheadElementSet set = c.execute(position, 0);
//            for (TypeaheadElement thriftelement : set.sets)
//            {
//                SelfSerializableElement element = new SelfSerializableElement();
//                element.setId((int)thriftelement.id);
//                element.setName(thriftelement.name);
//                String[] terms_contents = new String[thriftelement.term_contents.size()];
//                thriftelement.term_contents.toArray(terms_contents);
//                element.setTerms(termer.analyze(terms_contents));
//                element.setTimestamp(System.currentTimeMillis());
//                element.setScore((float)thriftelement.score);
//                element.setAttrs(thriftelement.attrs);
//                instance.getIndexer().index(element);
//            }
//            
//            flag = set.hasNext;
//            position = set.position;
//            System.out.println(position);
//        }
//        
//        transport.close();
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
//            j = networkStore(cur,instance.getConnectionIndexer());
//            //System.out.println( j + " " + i);
//            cur.close();
//            total += j;
//        }
////        demo.networkStore();
//        System.out.println(System.currentTimeMillis() - start_time);
//        
//        System.out.println(total);


     }
}
