/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.generic;

import cleo.search.Element;
import cleo.search.ElementJavaSerializer;
import cleo.search.ElementSerializer;
import cleo.search.SimpleElement;
import cleo.search.connection.ConnectionFilter;
import cleo.search.connection.TransitivePartitionConnectionFilter;
import cleo.search.selector.ScoredElementSelectorFactory;
import cleo.search.selector.SelectorFactory;
import cleo.search.store.ArrayStoreElement;
import cleo.search.store.StoreFactory;
import cleo.search.tool.GenericTypeaheadInitializer;
import cleo.search.tool.WeightedNetworkTypeaheadInitializer;
import cleo.search.typeahead.GenericTypeahead;
import cleo.search.typeahead.GenericTypeaheadConfig;
import cleo.search.typeahead.WeightedNetworkTypeahead;
import cleo.search.util.Range;
import com.blueferdi.leon.demo.typeahead.element.AvroElement;
import com.blueferdi.leon.demo.typeahead.serialize.self.SelfElementSerializer;
import com.blueferdi.leon.demo.typeahead.serialize.self.SelfSerializableElement;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import krati.core.segment.MemorySegmentFactory;
import krati.core.segment.SegmentFactory;

/**
 *
 * @author ferdinand
 */
public class GenericTypeaheadInstanceDemo1
{

    private GenericTypeahead<SimpleElement> typeahead;
    private Random rand = new Random();
    private int start = 0;
    private int count = 10000000;

    public String getHomeDir()
    {
        return "testgenericsimpletypeahead";
    }

    public ConnectionFilter createConnectionFilter()
    {
        return new TransitivePartitionConnectionFilter(new Range(start, count));
    }

    public SelectorFactory<SimpleElement> createSelectorFactory()
    {
        return new ScoredElementSelectorFactory<SimpleElement>();
    }

    protected ArrayStoreElement<SimpleElement> createElementStore(ElementSerializer<SimpleElement> serializer) throws Exception
    {
        File elementStoreDir = new File(getHomeDir(), "element-store");
        int elementStoreSegMB = 32;

        ArrayStoreElement<SimpleElement> elementStore =
                StoreFactory.createElementStorePartition(
                elementStoreDir,
                start,
                count,
                new MemorySegmentFactory(),
                elementStoreSegMB,
                serializer);
        return elementStore;

    }

    public GenericTypeahead<SimpleElement> createTypeahead() throws Exception
    {
        GenericTypeaheadConfig<SimpleElement> config = new GenericTypeaheadConfig<SimpleElement>();
        config.setName("generic demo");


        ElementSerializer<SimpleElement> serializer = new ElementJavaSerializer<SimpleElement>();
        config.setElementSerializer(serializer);
        config.setElementStoreDir(new File(getHomeDir(), "element-store"));
        config.setElementStoreIndexStart(start);
        config.setElementStoreCapacity(count);
        config.setElementStoreSegmentMB(32);

        config.setConnectionsStoreDir(new File(getHomeDir(), "connections-store"));
        config.setConnectionsStoreCapacity(count);
        config.setConnectionsStoreIndexSegmentMB(8);
        config.setConnectionsStoreSegmentMB(32);

        config.setSelectorFactory(createSelectorFactory());
        config.setFilterPrefixLength(2);

        GenericTypeaheadInitializer<SimpleElement> initializer =
                new GenericTypeaheadInitializer<SimpleElement>(config);

        return (GenericTypeahead<SimpleElement>) initializer.getTypeahead();



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

    public GenericTypeaheadInstanceDemo1()
    {
        try
        {
            typeahead = this.createTypeahead();
        }
        catch (Exception ex)
        {
            Logger.getLogger(GenericTypeaheadInstanceDemo1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void removeElement(SelfSerializableElement element)
    {
        
    }
    
    public void indexElement(SimpleElement element) throws Exception
    {
        typeahead.index(element);
//        typeahead.flush();
    }
    
    public void indexElements(List<SimpleElement> elements) throws Exception
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
    
    public List<SimpleElement> testSearch(int id,String... terms) throws Exception
    {
        return typeahead.search(id,terms);

    }

    public static void main(String[] args) throws IOException 
    {
        GenericTypeaheadInstanceDemo1 demo = new GenericTypeaheadInstanceDemo1();
        try
        {
            

//            SimpleElement element = new SimpleElement(1);
//            element.setTerms("zhong","wen","中文","文");
//            element.setTimestamp(System.currentTimeMillis());
//            element.setScore(0.23f);
//            demo.indexElement(element);
//            
//            element.setElementId(2);
//            element.setTerms("alice");
//            element.setTimestamp(System.currentTimeMillis());
//            element.setScore(0.23f);
//            demo.indexElement(element);
//            
//            element.setElementId(3);
//            element.setTerms("Bill","Gate");
//            element.setTimestamp(System.currentTimeMillis());
//            element.setScore(0.23f);
//            demo.indexElement(element);
//            
            
            
        
            List<SimpleElement> list = demo.testSearch(0,"中文");
            System.out.println(list.get(0).getScore() == 0.23f);
            System.out.println();
        }
        catch (Exception ex)
        {
            ex.getMessage();
        }
        
        demo.flush();
        
    }
}
