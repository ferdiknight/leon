/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.rest.util;

import cleo.search.connection.Connection;
import cleo.search.connection.ConnectionIndexer;
import cleo.search.connection.SimpleConnection;
import com.mongodb.*;
import com.blueferdi.leon.TypeaheadSerializableElement;
import com.blueferdi.leon.rest.PartitionableWeightedNetworkTypeaheadInstance;
import com.blueferdi.leon.termer.SimpleTermer;
import com.blueferdi.leon.termer.Termer;
import com.blueferdi.leon.typehead.index.construct.IndexConstructor.Client;
import com.blueferdi.leon.typehead.index.construct.TypeaheadElement;
import com.blueferdi.leon.typehead.index.construct.TypeaheadElementSet;
import java.io.File;
import java.util.List;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 *
 * @author ferdinand
 */
public class DataInsert
{
    public static void main(String[] args) throws Exception
    {
        File file = new File("src/main/resources/config/network_typeahead");
        
        PartitionableWeightedNetworkTypeaheadInstance<TypeaheadSerializableElement> instance = new PartitionableWeightedNetworkTypeaheadInstance<TypeaheadSerializableElement>("User",file);
        
        Termer termer = new SimpleTermer();
        String address = "10.0.250.75";
        int port = 3188;
        int clientTimeout = 30000000;
        TTransport transport = new TFramedTransport(new TSocket(address, port,
                clientTimeout));
        TProtocol protocol = new TBinaryProtocol(transport);
        transport.open();
        Client c = new Client(protocol);
        
        boolean flag = true;
        String position = "";
        TypeaheadSerializableElement element = new TypeaheadSerializableElement();
        TypeaheadElementSet set;
        String[] terms_contents;
        int usercount = 0;
        while(flag)
        {
            set = c.execute(position, 0);
            for (TypeaheadElement thriftelement : set.sets)
            {
                
                element.setId((int)thriftelement.id);
                element.setName(thriftelement.name);
                terms_contents = new String[thriftelement.term_contents.size()];
                thriftelement.term_contents.toArray(terms_contents);
                element.setTerms(termer.analyze(terms_contents));
                element.setTimestamp(System.currentTimeMillis());
                element.setScore((float)thriftelement.score);
                element.setAttrs(thriftelement.attrs);
                instance.getIndexer().index(element);
            }
            
            flag = set.hasNext;
            position = set.position;
            usercount += set.sets.size();
            System.out.println(position + " " + set.sets.size());
        }
        
        System.out.println(usercount);
        
        transport.close();
        
        Mongo mongo = new Mongo("10.0.11.174");
        DB db = mongo.getDB("user_relation_sharding_0");
        int total = 0;
        DBCollection coll;
        DBCursor cur;
        int j = 0;
        for(int i=0;i<256;i++)
        {

            coll = db.getCollection("friends_sharding_" + i);

            cur = coll.find();
            j = networkStore(cur,instance.getConnectionIndexer());
            System.out.println( j + " " + i);
            cur.close();
            total += j;
        }
//        demo.networkStore();
        //System.out.println(System.currentTimeMillis() - start_time);
        
        System.out.println(total);
        
    }
    
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
                System.out.println(id);
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
                System.out.println(id);
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
}
