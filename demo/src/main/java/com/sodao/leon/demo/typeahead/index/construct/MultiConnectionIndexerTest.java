/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.demo.typeahead.index.construct;

import cleo.search.ElementSerializer;
import cleo.search.connection.*;
import cleo.search.selector.ScoredElementSelectorFactory;
import cleo.search.store.ArrayStoreWeights;
import cleo.search.tool.WeightedNetworkTypeaheadInitializer;
import cleo.search.typeahead.NetworkTypeahead;
import cleo.search.typeahead.NetworkTypeaheadConfig;
import cleo.search.typeahead.WeightedNetworkTypeahead;
import cleo.search.util.Range;
import com.sodao.leon.demo.typeahead.serialize.self.SelfElementSerializer;
import com.sodao.leon.demo.typeahead.serialize.self.SelfSerializableElement;
import com.sodao.leon.demo.typeahead.store.ReadOnlyRangedMultiArrayStoreWeights;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import krati.core.segment.MappedSegmentFactory;

/**
 *
 * @author ferdinand
 */
public class MultiConnectionIndexerTest
{
    public static void main(String[] args) throws Exception
    {
        String homeDir = "testMultiIndex";
        ElementSerializer<SelfSerializableElement> serializer = new SelfElementSerializer<SelfSerializableElement>();
        @SuppressWarnings("unchecked")
        NetworkTypeaheadConfig<SelfSerializableElement>[] configs = new NetworkTypeaheadConfig[10];
        for (int i = 0; i < 10; i++)
        {
            NetworkTypeaheadConfig<SelfSerializableElement> config = new NetworkTypeaheadConfig<SelfSerializableElement>();

            int start = i * 10 + 1;
            int count = 10;

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
            config.setSelectorFactory(new ScoredElementSelectorFactory<SelfSerializableElement>());
            config.setFilterPrefixLength(2);
            configs[i] = config;
        }
        
        List<ConnectionIndexer> c_indexers = new ArrayList<ConnectionIndexer>();
        List<WeightedNetworkTypeahead<SelfSerializableElement>> typeaheads = new ArrayList<WeightedNetworkTypeahead<SelfSerializableElement>>();
        Map<Range,ArrayStoreWeights> map = new HashMap<Range,ArrayStoreWeights>();
        
        for(NetworkTypeaheadConfig<SelfSerializableElement> config : configs)
        {
            WeightedNetworkTypeaheadInitializer<SelfSerializableElement> initializer =
                    new WeightedNetworkTypeaheadInitializer<SelfSerializableElement>(config);
            WeightedNetworkTypeahead<SelfSerializableElement> typeahead = (WeightedNetworkTypeahead<SelfSerializableElement>)initializer.getTypeahead();
            c_indexers.add(initializer.getConnectionIndexer());
            typeaheads.add(typeahead);
            map.put(typeahead.getRange(), typeahead.getConnectionsStore());
        }
        
        final ReadOnlyRangedMultiArrayStoreWeights connectionStore = new ReadOnlyRangedMultiArrayStoreWeights(map);
        final ConnectionIndexer indexer = new MultiConnectionIndexer(c_indexers);
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    indexer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        
        PrintStream out = System.out;
        int index = 35;
//        for(int i=1;i<100;i++)
//        {
            if(connectionStore.hasIndex(index))
            {
                int[] connectionIds = connectionStore.getWeightData(index)[0];
                for(int i : connectionIds)
                {
                    out.print(i + ",");
                }
                out.println();
                out.println();
            }
//        }
//        SimpleConnection conn;
//        
//        for(int i=1;i<101;i++)
//        {
//            for(int j=1;j<100;j++)
//            {
//                if(i == j)
//                    continue;
//                conn = new SimpleConnection(i,j,true);
//                indexer.index(conn);
//            }
//        }
//        
//        indexer.flush();
        
        
    }
}
