/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.rest;

import cleo.search.connection.Connection;

/**
 *
 * @param <T> 
 * @author ferdinand
 */
public interface RestDAO<T>
{
    public T getElement(int elementId);
    
    public T deleteElement(int elementId) throws Exception;
    
    public T updateElement(T element) throws Exception;
    
    public boolean insertElement(T element) throws Exception;
    
    public int getConnectionWeight(int source,int target);
    
    public int deleteConnection(int source, int target);
    
    public int updateConnection(Connection conn);
    
    public boolean insertConnection(Connection conn);
}
