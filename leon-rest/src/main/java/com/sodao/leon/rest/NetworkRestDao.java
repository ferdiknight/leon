/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.rest;

import cleo.search.Indexer;
import cleo.search.connection.Connection;
import cleo.search.connection.ConnectionIndexer;
import cleo.search.connection.SimpleConnection;
import cleo.search.store.ArrayStoreElement;
import cleo.search.store.ArrayStoreWeights;
import cleo.search.store.IntArrayPartition;
import cleo.search.typeahead.NetworkTypeahead;
import com.sodao.leon.TypeaheadSerializableElement;
import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author ferdinand
 */
public enum NetworkRestDao implements RestDAO<TypeaheadSerializableElement>
{

    INSTANCE;
    private Log log = LogFactory.getLog(NetworkRestDao.class);
    private WeightedNetworkTypeaheadInstance<TypeaheadSerializableElement> loader;

    private NetworkRestDao()
    {
        try
        {
            String name = System.getProperty("cleo.instance.name");//"User";//
            String type = System.getProperty("cleo.instance.type");//"com.sodao.leon.rest.PartitionableWeightedNetworkTypeaheadInstance";//
            String conf = System.getProperty("cleo.instance.conf");//"config/network_typeahead";//
            File confPath = new File(conf);

            @SuppressWarnings("unchecked")
            Class<WeightedNetworkTypeaheadInstance<TypeaheadSerializableElement>> instanceClass = (Class<WeightedNetworkTypeaheadInstance<TypeaheadSerializableElement>>) Class.forName(type);
            loader = instanceClass.getConstructor(String.class, File.class).newInstance(name, confPath);
        }
        catch (Exception ex)
        {
            log.error("REST DAO init error", ex);
        }
    }

    public final Indexer<TypeaheadSerializableElement> getIndexer()
    {
        return loader.getIndexer();
    }

    public final NetworkTypeahead<TypeaheadSerializableElement> getSearcher()
    {
        return loader.getSearcher();
    }

    public final ConnectionIndexer getConnectionIndexer()
    {
        return loader.getConnectionIndexer();
    }

    public final ArrayStoreElement<TypeaheadSerializableElement> getElementStore()
    {
        return loader.getElementStore();
    }

    public final ArrayStoreWeights getConnectionStore()
    {
        return loader.getConnectionStore();
    }

    public final IntArrayPartition getFilterStore()
    {
        return loader.getFilterStore();
    }

    @Override
    public TypeaheadSerializableElement getElement(int elementId)
    {
        TypeaheadSerializableElement element = this.getElementStore().getElement(elementId);

        if (element != null && element.isSearchable())
        {
            return element;
        }
        else
        {
            return null;
        }
    }

    @Override
    public TypeaheadSerializableElement deleteElement(int elementId) throws Exception
    {
        TypeaheadSerializableElement element = this.getElementStore().getElement(elementId);

        if (element != null)
        {
            TypeaheadSerializableElement newElement = element.clone();
            newElement.setTerms(new String[0]);
            getIndexer().index(newElement);
        }

        if (element != null && element.isSearchable())
        {
            return element;
        }
        else
        {
            return null;
        }
    }

    @Override
    public TypeaheadSerializableElement updateElement(TypeaheadSerializableElement element) throws Exception
    {
        TypeaheadSerializableElement oldElement = getElementStore().getElement(element.getElementId());
        getIndexer().index(element);

        if (oldElement != null && oldElement.isSearchable())
        {
            return oldElement;
        }
        else
        {
            return null;
        }
    }

    @Override
    public boolean insertElement(TypeaheadSerializableElement element) throws Exception
    {
        int elementId = element.getElementId();

        if (getElementStore().hasIndex(elementId))
        {
            return getIndexer().index(element);
        }

        return false;
    }

    @Override
    public int getConnectionWeight(int source, int target)
    {
        return getConnectionStore().getWeight(source, target);
    }

    @Override
    public int deleteConnection(int source, int target)
    {
        try
        {
            int oldWeight = getConnectionStore().getWeight(source, target);
            SimpleConnection conn = new SimpleConnection(source, target, false);
            conn.setTimestamp(System.currentTimeMillis());
            getConnectionIndexer().index(conn);
            return oldWeight;
        }
        catch (Exception ex)
        {
            log.error("delete connection error", ex);
        }

        return 0;
    }

    @Override
    public int updateConnection(Connection conn)
    {
        try
        {
            int oldWeight = getConnectionStore().getWeight(conn.source(), conn.target());
            getConnectionIndexer().index(conn);
            return oldWeight;
        }
        catch (Exception ex)
        {
            log.error("update connection error", ex);
        }
        return 0;
    }

    @Override
    public boolean insertConnection(Connection conn)
    {
        try
        {
            if (getConnectionStore().hasIndex(conn.source()))
            {
                return getConnectionIndexer().index(conn);
            }
        }
        catch (Exception ex)
        {
            log.error("insert connection error", ex);
        }

        return false;
    }
}
