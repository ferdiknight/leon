/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.demo.typeahead.serializer.nat;

import cleo.search.ElementJavaSerializer;

/**
 *
 * @author ferdinand
 */
public class JavaNativeSerializer
{
    public static void main(String[] args)
    {
        ElementJavaSerializer<TestCleoElement> serializer = new ElementJavaSerializer<TestCleoElement>();
        
        TestCleoElement e = new TestCleoElement();
        e.setId(1);
        e.setName("test");
        e.setScore(0.23f);
        e.setTerms("res","ap");
        e.setTimestamp(0l);
        
        long start =  System.currentTimeMillis();
        
        for(int i=0;i<10000;i++)
        {
            serializer.serialize(e);
        }
        
        long end = System.currentTimeMillis();
        
        System.out.println(end - start);
        
        byte[] b = serializer.serialize(e);
        
        start = System.currentTimeMillis();
        
        for(int i=0;i<10000;i++)
        {
            serializer.deserialize(b);
        }
        
        end = System.currentTimeMillis();
        
        System.out.println(end - start);
        
        System.out.println(b.length);
    }
}
