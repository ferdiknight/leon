/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.demo.typeahead.serializer.nat;

import java.util.Arrays;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TCompactProtocol;

/**
 *
 * @author ferdinand
 */
public class SerializerTest
{    
    public static void main(String[] args) throws TException
    {
        ThriftTestElement r = new ThriftTestElement();
        r.setId(1);
        r.setName("test");
        r.setTimestamp(0l);
        r.setScore(0.23);
        r.setTerms(Arrays.asList("res","ap"));
        long start = System.currentTimeMillis();
        for(int i=0;i < 10000;i++)
        {
            new TSerializer(new TCompactProtocol.Factory()).serialize(r);
        }
        
        long end = System.currentTimeMillis();
        
        System.out.println(end - start);
        
        byte[] b = new TSerializer(new TCompactProtocol.Factory()).serialize(r);
        
        start = System.currentTimeMillis();
        
        for(int i=0;i < 10000;i++)
        {
            new TDeserializer(new TCompactProtocol.Factory()).deserialize(new ThriftTestElement(), b);
        }
        
        end = System.currentTimeMillis();
        
        System.out.println(end - start);
        
        System.out.println(b.length);
        
    }
}
