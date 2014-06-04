/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.serializer;

import cleo.search.Element;
import cleo.search.ElementSerializationException;
import cleo.search.ElementSerializer;

/**
 *
 * @param <E> 
 * @author ferdinand
 */
public class ThriftSerializer<E extends Element> implements ElementSerializer<E>
{

    public byte[] serialize(E element) throws ElementSerializationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public E deserialize(byte[] bytes) throws ElementSerializationException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
