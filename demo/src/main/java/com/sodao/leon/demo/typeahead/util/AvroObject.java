/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.demo.typeahead.util;

import org.apache.avro.generic.IndexedRecord;

/**
 *
 * @author ferdinand
 */
public interface AvroObject extends IndexedRecord
{
    /**
     * 
     * @param <E>
     * @return
     */
    public <E extends AvroObject> E clone();
}
