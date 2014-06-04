/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.rest.resource;

import cleo.search.collector.Collector;
import cleo.search.collector.SortedCollector;
import com.blueferdi.leon.TypeaheadSerializableElement;
import com.blueferdi.leon.rest.NetworkRestDao;
import com.blueferdi.leon.rest.module.TestBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import org.springframework.stereotype.Component;

/**
 *
 * @author ferdinand
 */
@Component
@Path("/typeahead")
public class TypeaheadResource
{
    @Context
    UriInfo uriInfo;
    
    @Context
    Request request;
    
//    @GET
//    @Path("/{start}..{end}")
//    @Produces({MediaType.APPLICATION_JSON})
//    public List<SelfSerializableElement> getElements(@PathParam("start")int start, @PathParam("end")int end) 
//    {
//        List<SelfSerializableElement> dtos = new ArrayList<SelfSerializableElement>();
//        for(int i=start;i<=end;i++)
//        {
//            TypeaheadSerializableElement dto = NetworkRestDao.INSTANCE.getElement(i);
//            if(dto != null)
//                dtos.add(dto);
//        }
//        
//        return dtos;
//    }
    
    @GET
    @Path("/search")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public List<TypeaheadSerializableElement> search(@QueryParam("uid")int uid,
                                 @QueryParam("query")String query) 
    {
        if(query == null) {
            return new ArrayList<TypeaheadSerializableElement>();
        }
        
        String[] terms = query.toLowerCase().split(" ");
        Collector<TypeaheadSerializableElement> collector = new SortedCollector<TypeaheadSerializableElement>(10, 100);
        
        collector = NetworkRestDao.INSTANCE.getSearcher().search(uid, terms, collector);//searchNetwork(uid, terms, collector, NetworkRestDao.INSTANCE.getSearcher().createContext(uid));
        
        return collector.elements();
        
    }
    
    @GET
    @Path("/searchNetwork")
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public List<TypeaheadSerializableElement> searchNetwork(@QueryParam("uid")int uid,@QueryParam("query")String query)
    {
        if(query == null) {
            return new ArrayList<TypeaheadSerializableElement>();
        }
        
        String[] terms = query.toLowerCase().split(" ");
        Collector<TypeaheadSerializableElement> collector = new SortedCollector<TypeaheadSerializableElement>(10, 100);
        
        collector = NetworkRestDao.INSTANCE.getSearcher().searchNetwork(uid, terms, collector, NetworkRestDao.INSTANCE.getSearcher().createContext(uid));
        
        return collector.elements();
    }
    
    @GET
    @Path("/test")
    @Produces({MediaType.APPLICATION_XML,MediaType.APPLICATION_JSON})
    public List<TestBean> test(@QueryParam("test")String test)
    {
        TestBean tb = new TestBean();
        tb.setId(1);
        tb.setName(test);
        tb.setTerms(new String[0]);
        
        return Arrays.asList(tb);
    }
    
}
