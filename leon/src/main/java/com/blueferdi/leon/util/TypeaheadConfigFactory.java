/*
 * Copyright (c) 2013 Sodao,Inc
 */
package com.blueferdi.leon.util;

import cleo.search.Element;
import cleo.search.ElementJavaSerializer;
import cleo.search.ElementSerializer;
import cleo.search.connection.ConnectionFilter;
import cleo.search.connection.SourcePartitionConnectionFilter;
import cleo.search.selector.SelectorFactory;
import cleo.search.typeahead.GenericTypeaheadConfig;
import cleo.search.typeahead.NetworkTypeaheadConfig;
import cleo.search.util.PropertiesResolver;
import cleo.search.util.Range;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import krati.core.segment.SegmentFactory;

/**
 * TypeaheadConfigFactory creates standard configurations for {@link cleo.search.typeahead.Typeahead Typeahead}.
 *
 * @author ferdinand
 * @since 22.01,2013
 *
 * <p> 22.01,2013 add {@link krati.core.segment.SegmentFactory},{@link cleo.search.selector.SelectorFactory},elementStoreSegment
 * Cache and scoreScanner for elementStore/connectionStore into config file<br>
 *
 */
@SuppressWarnings("unchecked")
public class TypeaheadConfigFactory
{

    /**
     * Creates an instance of {@link NetworkTypeaheadConfig} based on the
     * specified properties. <p> Here is a sample for network typeahead
     * configuration: </p>
     *
     * <pre>
     *  cleo.search.network.typeahead.config.name=i001
     *  cleo.search.network.typeahead.config.partition.start=0
     *  cleo.search.network.typeahead.config.partition.count=5000000
     *  cleo.search.network.typeahead.config.homeDir=network-typeahead/member/i001
     *  cleo.search.network.typeahead.config.searchTimeoutMillis=15
     *
     *  cleo.search.network.typeahead.config.elementSerializer.class=cleo.search.TypeaheadElementSerializer
     *  cleo.search.network.typeahead.config.elementStoreSegmentFactory.class=krati.core.segment.MemorySegmentFacotry
     *  cleo.search.network.typeahead.config.connectionFilter.class=cleo.search.connection.TransitivePartitionConnectionFilter
     *  cleo.search.network.typeahead.config.selectorFactory.class=cleo.search.selector.ScoredPrefixSelectorFactory
     *  cleo.search.network.typeahead.config.connectionStoreSegmentFactory.class=krati.core.segment.MemorySegmentFactory
     *
     *  cleo.search.network.typeahead.config.elementStoreDir=${cleo.search.network.typeahead.config.homeDir}/element-store
     *  cleo.search.network.typeahead.config.elementStoreIndexStart=${cleo.search.network.typeahead.config.partition.start}
     *  cleo.search.network.typeahead.config.elementStoreCapacity=${cleo.search.network.typeahead.config.partition.count}
     *  cleo.search.network.typeahead.config.elementStoreSegmentMB=64
     *  cleo.search.network.typeahead.config.elementStoreSegmentCached=true;
     *
     *  cleo.search.network.typeahead.config.connectionsStoreDir=${cleo.search.network.typeahead.config.homeDir}/weighted-connections-store
     *  cleo.search.network.typeahead.config.connectionsStoreIndexStart=0
     *  cleo.search.network.typeahead.config.connectionsStoreCapacity=150000000
     *  cleo.search.network.typeahead.config.connectionsStoreSegmentMB=64
     *
     *  cleo.search.network.typeahead.config.filterPrefixLength=2
     * </pre>
     *
     * @param <E>
     * @param properties - Typeahead configuration properties
     * @return
     * <code>NetworkTypeaheadConfig</code> if the properties specify a correct
     * configuration.
     * @throws Exception if the properties specify an incorrect configuration.
     */
    public static <E extends Element> NetworkTypeaheadConfig<E> createNetworkTypeaheadConfig(Properties properties) throws Exception
    {
        // Resolve config properties
        PropertiesResolver.resolve(properties);

        NetworkTypeaheadConfig<E> config = new NetworkTypeaheadConfig<E>();
        config.setName(properties.getProperty("cleo.search.network.typeahead.config.name"));

        if (properties.containsKey("cleo.search.network.typeahead.config.searchTimeoutMillis"))
        {
            config.setSearchTimeoutMillis(Long.parseLong(properties.getProperty("cleo.search.network.typeahead.config.searchTimeoutMillis")));
        }

        // connectionFilter for network partition
        config.setPartitionStart(Integer.parseInt(properties.getProperty("cleo.search.network.typeahead.config.partition.start")));
        config.setPartitionCount(Integer.parseInt(properties.getProperty("cleo.search.network.typeahead.config.partition.count")));
        Range partitionRange = new Range(config.getPartitionStart(), config.getPartitionCount());

        //connectionFilter
        if (properties.containsKey("cleo.search.network.typeahead.config.connectionFilter.class"))
        {
            ConnectionFilter connectionFilter = (ConnectionFilter) Class.forName(properties.getProperty("cleo.search.network.typeahead.config.connectionFilter.class")).getConstructor(Range.class).newInstance(partitionRange);
            config.setConnectionFilter(connectionFilter);
        }
        else
        {
            config.setConnectionFilter(new SourcePartitionConnectionFilter(partitionRange));
        }

        // elementSerializer  
        if (properties.containsKey("cleo.search.network.typeahead.config.elementSerializer.class"))
        {
            ElementSerializer<E> elementSerializer = (ElementSerializer<E>) Class.forName(properties.getProperty("cleo.search.network.typeahead.config.elementSerializer.class")).newInstance();
            config.setElementSerializer(elementSerializer);
        }
        else
        {
            config.setElementSerializer(new ElementJavaSerializer<E>());
        }

        //elementStoreSegmentFactory
        if (properties.containsKey("cleo.search.network.typeahead.config.elementStoreSegmentFactory.class"))
        {
            SegmentFactory elementSegmentFactory = (SegmentFactory) Class.forName(properties.getProperty("cleo.search.network.typeahead.config.elementStoreSegmentFactory.class")).newInstance();
            config.setElementStoreSegmentFactory(elementSegmentFactory);
        }

        //selectorFactory
        if (properties.containsKey("cleo.search.network.typeahead.config.selectorFactory.class"))
        {
            SelectorFactory<E> selectorFactory = (SelectorFactory) Class.forName(properties.getProperty("cleo.search.network.typeahead.config.selectorFactory.class")).newInstance();
            config.setSelectorFactory(selectorFactory);
        }

        //connectionStoreSegmentFactory
        if (properties.containsKey("cleo.search.network.typeahead.config.connectionStoreSegmentFactory.class"))
        {
            SegmentFactory connectionSegmentFactory = (SegmentFactory) Class.forName(properties.getProperty("cleo.search.network.typeahead.config.connectionStoreSegmentFactory.class")).newInstance();
            config.setConnectionsStoreSegmentFactory(connectionSegmentFactory);
        }

        // elementStore
        config.setElementStoreDir(new File(properties.getProperty("cleo.search.network.typeahead.config.elementStoreDir")));
        config.setElementStoreSegmentMB(Integer.parseInt(properties.getProperty("cleo.search.network.typeahead.config.elementStoreSegmentMB")));
        config.setElementStoreIndexStart(Integer.parseInt(properties.getProperty("cleo.search.network.typeahead.config.elementStoreIndexStart")));
        config.setElementStoreCapacity(Integer.parseInt(properties.getProperty("cleo.search.network.typeahead.config.elementStoreCapacity")));
        config.setElementStoreCached(Boolean.parseBoolean(properties.getProperty("cleo.search.network.typeahead.config.elementStoreSegmentCached")));

        // connectionsStore
        config.setConnectionsStoreDir(new File(properties.getProperty("cleo.search.network.typeahead.config.connectionsStoreDir")));
        config.setConnectionsStoreCapacity(Integer.parseInt(properties.getProperty("cleo.search.network.typeahead.config.connectionsStoreCapacity")));
        config.setConnectionsStoreIndexStart(Integer.parseInt(properties.getProperty("cleo.search.network.typeahead.config.connectionsStoreIndexStart")));
        config.setConnectionsStoreSegmentMB(Integer.parseInt(properties.getProperty("cleo.search.network.typeahead.config.connectionsStoreSegmentMB")));

        // BloomFilter prefix length
        config.setFilterPrefixLength(Integer.parseInt(properties.getProperty("cleo.search.network.typeahead.config.filterPrefixLength")));

        return config;
    }

    /**
     * Creates an instance of {@link GenericTypeaheadConfig} based on the
     * specified properties.
     *
     * <p> Here is a sample for generic typeahead configuration: </p>
     *
     * <pre>
     * cleo.search.generic.typeahead.config.name=i001
     * cleo.search.generic.typeahead.config.searchTimeoutMillis=15
     * cleo.search.generic.typeahead.config.partition.start=0
     * cleo.search.generic.typeahead.config.partition.count=1000000
     * cleo.search.generic.typeahead.config.homeDir=bootstrap/company/i001
     *
     * cleo.search.generic.typeahead.config.elementSerializer.class=cleo.search.TypeaheadElementSerializer
     * cleo.search.generic.typeahead.config.elementStoreSegmentFactory.class=krati.core.segment.MemorySegmentFacotry
     * cleo.search.generic.typeahead.config.selectorFactory.class=cleo.search.selector.ScoredPrefixSelectorFactory
     * cleo.search.generic.typeahead.config.connectionStoreSegmentFactory.class=krati.core.segment.MemorySegmentFactory
     * cleo.search.generic.typeahead.config.connectionStoreIndexSegmentFactory.class=krati.core.segment.MemorySegmentFacotry
     *
     * cleo.search.generic.typeahead.config.elementStoreDir=${cleo.search.generic.typeahead.config.homeDir}/element-store
     * cleo.search.generic.typeahead.config.elementStoreIndexStart=${cleo.search.generic.typeahead.config.partition.start}
     * cleo.search.generic.typeahead.config.elementStoreCapacity=${cleo.search.generic.typeahead.config.partition.count}
     * cleo.search.generic.typeahead.config.elementStoreSegmentMB=32
     * cleo.search.generic.typeahead.config.elementStoreCached=true
     * cleo.search.generic.typeahead.config.elementScoreFile=${cleo.search.generic.typeahead.config.homeDir}/scoreFile
     *
     * cleo.search.generic.typeahead.config.connectionsStoreDir=${cleo.search.generic.typeahead.config.homeDir}/connections-store
     * cleo.search.generic.typeahead.config.connectionsStoreCapacity=1000000
     * cleo.search.generic.typeahead.config.connectionsStoreSegmentMB=64
     * cleo.search.generic.typeahead.config.connectionsStoreIndexSegmentMB=8
     * cleo.search.generic.typeahead.config.connectionsStoreCached=true
     *
     * cleo.search.generic.typeahead.config.filterPrefixLength=2
     * cleo.search.generic.typeahead.config.maxKeyLength=5
     * </pre>
     *
     * @param <E> 
     * @param properties - Typeahead configuration properties
     * @return
     * <code>GenericTypeaheadConfig</code> if the properties specify a correct
     * configuration.
     * @throws Exception if the properties specify an incorrect configuration.
     */
    public static <E extends Element> GenericTypeaheadConfig<E> createGenericTypeaheadConfig(Properties properties) throws Exception
    {
        // Resolve config properties
        PropertiesResolver.resolve(properties);

        GenericTypeaheadConfig<E> config = new GenericTypeaheadConfig<E>();
        config.setName(properties.getProperty("cleo.search.generic.typeahead.config.name"));
        if (properties.containsKey("cleo.search.generic.typeahead.config.searchTimeoutMillis"))
        {
            config.setSearchTimeoutMillis(Long.parseLong(properties.getProperty("cleo.search.network.typeahead.config.searchTimeoutMillis")));
        }

        // elementSerializer  
        if (properties.containsKey("cleo.search.generic.typeahead.config.elementSerializer.class"))
        {
            ElementSerializer<E> elementSerializer = (ElementSerializer<E>) Class.forName(properties.getProperty("cleo.search.generic.typeahead.config.elementSerializer.class")).newInstance();
            config.setElementSerializer(elementSerializer);
        }
        else
        {
            config.setElementSerializer(new ElementJavaSerializer<E>());
        }

        //elementStoreSegmentFactory
        if (properties.containsKey("cleo.search.generic.typeahead.config.elementStoreSegmentFactory.class"))
        {
            SegmentFactory elementSegmentFactory = (SegmentFactory) Class.forName(properties.getProperty("cleo.search.generic.typeahead.config.elementStoreSegmentFactory.class")).newInstance();
            config.setElementStoreSegmentFactory(elementSegmentFactory);
        }

        //selectorFactory
        if (properties.containsKey("cleo.search.generic.typeahead.config.selectorFactory.class"))
        {
            SelectorFactory<E> selectorFactory = (SelectorFactory) Class.forName(properties.getProperty("cleo.search.generic.typeahead.config.selectorFactory.class")).newInstance();
            config.setSelectorFactory(selectorFactory);
        }

        //connectionStoreSegmentFactory
        if (properties.containsKey("cleo.search.generic.typeahead.config.connectionStoreSegmentFactory.class"))
        {
            SegmentFactory connectionSegmentFactory = (SegmentFactory) Class.forName(properties.getProperty("cleo.search.generic.typeahead.config.connectionStoreSegmentFactory.class")).newInstance();
            config.setConnectionsStoreSegmentFactory(connectionSegmentFactory);
        }
        
        //connectionStoreIndexSegmentFactory
        if(properties.containsKey("cleo.search.generic.typeahead.config.connectionStoreIndexSegmentFactory.class"))
        {
            SegmentFactory connectionSegmentFactory = (SegmentFactory) Class.forName(properties.getProperty("cleo.search.generic.typeahead.config.connectionStoreIndexSegmentFactory.class")).newInstance();
            config.setConnectionsStoreIndexSegmentFactory(connectionSegmentFactory);
        }

        // elementStore
        config.setElementStoreDir(new File(properties.getProperty("cleo.search.generic.typeahead.config.elementStoreDir")));
        config.setElementStoreIndexStart(Integer.parseInt(properties.getProperty("cleo.search.generic.typeahead.config.elementStoreIndexStart")));
        config.setElementStoreCapacity(Integer.parseInt(properties.getProperty("cleo.search.generic.typeahead.config.elementStoreCapacity")));
        config.setElementStoreSegmentMB(Integer.parseInt(properties.getProperty("cleo.search.generic.typeahead.config.elementStoreSegmentMB")));
        config.setElementStoreCached(Boolean.parseBoolean(properties.getProperty("cleo.search.generic.typeahead.config.elementStoreCached")));
        if(properties.containsKey("cleo.search.generic.typeahead.config.elementScoreFile"))
            config.setElementScoreFile(new File(properties.getProperty("cleo.search.generic.typeahead.config.elementScoreFile")));

        // connectionsStore
        config.setConnectionsStoreDir(new File(properties.getProperty("cleo.search.generic.typeahead.config.connectionsStoreDir")));
        config.setConnectionsStoreCapacity(Integer.parseInt(properties.getProperty("cleo.search.generic.typeahead.config.connectionsStoreCapacity")));
        config.setConnectionsStoreSegmentMB(Integer.parseInt(properties.getProperty("cleo.search.generic.typeahead.config.connectionsStoreSegmentMB")));
        config.setConnectionsStoreIndexSegmentMB(Integer.parseInt(properties.getProperty("cleo.search.generic.typeahead.config.connectionsStoreIndexSegmentMB")));
        config.setConnectionsStoreCached(Boolean.parseBoolean(properties.getProperty("cleo.search.generic.typeahead.config.connectionStoreCached")));
        
        config.setMaxKeyLength(Integer.parseInt(properties.getProperty("cleo.search.generic.typeahead.config.maxKeyLength")));
        config.setFilterPrefixLength(Integer.parseInt(properties.getProperty("cleo.search.generic.typeahead.config.filterPrefixLength")));

        return config;
    }

    /**
     * Creates an instance of {@link GenericTypeaheadConfig} based on the
     * specified configuration properties file.
     *
     * @param <E>
     * @param configFile - the configuration properties file
     * @return the configuration for instantiating {@link cleo.search.typeahead.GenericTypeahead GenericTypeahead}.
     * @throws Exception if the configuration cannot be created.
     */
    public static <E extends Element> GenericTypeaheadConfig<E> createGenericTypeaheadConfig(File configFile) throws Exception
    {
        // Load config properties
        Properties properties = new Properties();
        FileInputStream inStream = new FileInputStream(configFile);
        InputStreamReader reader = new InputStreamReader(inStream, "UTF-8");
        properties.load(reader);

        try
        {
            return createGenericTypeaheadConfig(properties);
        }
        finally
        {
            // List config properties
            properties.list(System.out);
        }
    }

    /**
     * Creates an instance of {@link NetworkTypeaheadConfig} based on the
     * specified configuration properties file.
     *
     * @param <E>
     * @param configFile - the configuration properties file
     * @return the configuration for instantiating {@link cleo.search.typeahead.NetworkTypeahead NetworkTypeahead}.
     * @throws Exception if the configuration cannot be created.
     */
    public static <E extends Element> NetworkTypeaheadConfig<E> createNetworkTypeaheadConfig(File configFile) throws Exception
    {
        // Load config properties
        Properties properties = new Properties();
        FileInputStream inStream = new FileInputStream(configFile);
        InputStreamReader reader = new InputStreamReader(inStream, "UTF-8");
        properties.load(reader);

        try
        {
            return createNetworkTypeaheadConfig(properties);
        }
        finally
        {
            // List config properties
            properties.list(System.out);
        }
    }
}
