/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.generic;

import cleo.search.ElementSerializer;
import cleo.search.collector.Collector;
import cleo.search.collector.SortedCollector;
import cleo.search.connection.ConnectionFilter;
import cleo.search.connection.TransitivePartitionConnectionFilter;
import cleo.search.selector.ScoredElementSelectorFactory;
import cleo.search.selector.SelectorFactory;
import cleo.search.store.ArrayStoreElement;
import cleo.search.store.StoreFactory;
import cleo.search.tool.GenericTypeaheadInitializer;
import cleo.search.typeahead.GenericTypeahead;
import cleo.search.typeahead.GenericTypeaheadConfig;
import cleo.search.util.Range;
import com.blueferdi.leon.demo.typeahead.serialize.self.SelfElementSerializer;
import com.blueferdi.leon.demo.typeahead.serialize.self.SelfSerializableElement;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import krati.core.segment.MemorySegmentFactory;

/**
 *
 * @author ferdinand
 */
public class GenericTypeaheadInstanceDemo
{

    private GenericTypeahead<SelfSerializableElement> typeahead;
    private Random rand = new Random();
    private int start = 0;
    private int count = 10000000;

    public String getHomeDir()
    {
        return "testgenerictypeahead";
    }

    public ConnectionFilter createConnectionFilter()
    {
        return new TransitivePartitionConnectionFilter(new Range(start, count));
    }

    public SelectorFactory<SelfSerializableElement> createSelectorFactory()
    {
        return new ScoredElementSelectorFactory<SelfSerializableElement>();
    }

    protected ArrayStoreElement<SelfSerializableElement> createElementStore(ElementSerializer<SelfSerializableElement> serializer) throws Exception
    {
        File elementStoreDir = new File(getHomeDir(), "element-store");
        int elementStoreSegMB = 32;

        ArrayStoreElement<SelfSerializableElement> elementStore =
                StoreFactory.createElementStorePartition(
                elementStoreDir,
                start,
                count,
                new MemorySegmentFactory(),
                elementStoreSegMB,
                serializer);
        return elementStore;

    }

    public GenericTypeahead<SelfSerializableElement> createTypeahead() throws Exception
    {
        GenericTypeaheadConfig<SelfSerializableElement> config = new GenericTypeaheadConfig<SelfSerializableElement>();
        config.setName("generic demo");


        ElementSerializer<SelfSerializableElement> serializer = new SelfElementSerializer<SelfSerializableElement>();
        config.setElementSerializer(serializer);
        config.setElementStoreDir(new File(getHomeDir(), "element-store"));
        config.setElementStoreIndexStart(start);
        config.setElementStoreCapacity(count);
        config.setElementStoreSegmentMB(32);
        //config.setElementScoreFile(new File(getHomeDir(),"element-score"));
        
        config.setConnectionsStoreDir(new File(getHomeDir(), "connections-store"));
        config.setConnectionsStoreCapacity(count);
        config.setConnectionsStoreIndexSegmentMB(8);
        config.setConnectionsStoreSegmentMB(32);

        config.setSelectorFactory(createSelectorFactory());
        config.setFilterPrefixLength(2);

        GenericTypeaheadInitializer<SelfSerializableElement> initializer =
                new GenericTypeaheadInitializer<SelfSerializableElement>(config);

        return (GenericTypeahead<SelfSerializableElement>) initializer.getTypeahead();



        /*
         * config.setName("Generic");
         * config.setElementSerializer(createElementSerializer());
         * config.setElementStoreDir(new File(getHomeDir(), "element-store"));
         * config.setElementStoreIndexStart(getElementStoreIndexStart());
         * config.setElementStoreCapacity(getElementStoreCapacity());
         * config.setElementStoreSegmentMB(32);
         *
         * int connectionsStoreCapacity = 500000; int
         * connectionsStoreIndexSegmentMB = 8; SegmentFactory
         * connectionsStoreIndexSegmentFactory = new MemorySegmentFactory(); int
         * connectionsStoreSegmentMB = 32; SegmentFactory
         * connectionsStoreSegmentFactory = new MemorySegmentFactory();
         *
         * config.setConnectionsStoreCapacity(connectionsStoreCapacity);
         * config.setConnectionsStoreDir(new File(getHomeDir(),
         * "connections-store"));
         * config.setConnectionsStoreIndexSegmentFactory(connectionsStoreIndexSegmentFactory);
         * config.setConnectionsStoreIndexSegmentMB(connectionsStoreIndexSegmentMB);
         * config.setConnectionsStoreSegmentFactory(connectionsStoreSegmentFactory);
         * config.setConnectionsStoreSegmentMB(connectionsStoreSegmentMB);
         *
         * config.setSelectorFactory(createSelectorFactory());
         * config.setFilterPrefixLength(getFilterPrefixLength());
         * config.setMaxKeyLength(getMaxKeyLength());
         */


    }

    public GenericTypeaheadInstanceDemo()
    {
        try
        {
            typeahead = this.createTypeahead();
            addShutdownHook();
        }
        catch (Exception ex)
        {
            Logger.getLogger(GenericTypeaheadInstanceDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    typeahead.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void removeElement(SelfSerializableElement element)
    {
        
    }
    
    public void indexElement(SelfSerializableElement element) throws Exception
    {
        typeahead.index(element);
        typeahead.flush();
    }
    
    public void indexElements(List<SelfSerializableElement> elements) throws Exception
    {
        for(int i=0;i<elements.size();i++)
        {
            typeahead.index(elements.get(i));
        }
    }
    
    public void flush() throws IOException
    {
        typeahead.flush();
    }
    
    public Collector<SelfSerializableElement> testSearch(int id,Collector<SelfSerializableElement> collector,String... terms) throws Exception
    {
        return typeahead.search(id,terms,collector);

    }
    
    public List<SelfSerializableElement> testSimpleSearch(int id,String... terms)
    {
        return typeahead.search(id,terms);
    }

    public static void main(String[] args) throws IOException 
    {
        GenericTypeaheadInstanceDemo demo = new GenericTypeaheadInstanceDemo();
        try
        {
//            SelfSerializableElement element = new SelfSerializableElement();
//
//            element.setId(5);
//            element.setName("中文ace");
//            element.setTerms("zhong","wen","中文","文","acs");
//            element.setTimestamp(System.currentTimeMillis());
//            element.setScore(0.45f);
//            demo.indexElement(element);
//            
//            element.setId(2);
//            element.setName("alice");
//            element.setTerms("alice");
//            element.setTimestamp(System.currentTimeMillis());
//            element.setScore(0.23f);
//            demo.indexElement(element);
//            
//            element.setId(3);
//            element.setName("Bill Gate");
//            element.setTerms("Bill","Gate");
//            element.setTimestamp(System.currentTimeMillis());
//            element.setScore(0.23f);
//            demo.indexElement(element);
            
            
            
            Collector<SelfSerializableElement> collector = new SortedCollector<SelfSerializableElement>(10, 100);
            collector = demo.testSearch(2, collector, "ali");
//            System.out.println(list.get(0).getScore() == 0.23f);
            
            for(SelfSerializableElement hit : collector.elements()){
                System.out.println(hit.getId());
            }
            System.out.println();
        }
        catch (Exception ex)
        {
            ex.getMessage();
        }
        
    }
}
