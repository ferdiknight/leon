/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.demo.typeahead.serializer.nat;

import cleo.search.ElementSerializationException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;

/**
 *
 * @author ferdinand
 */
public class AvroSpecificSerializer<E>
{
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();
    private GenericDatumWriter<E> WRITER;
    private GenericDatumReader<E> READER;
    private BinaryEncoder encoder;
    private BinaryDecoder decoder;
    
    public AvroSpecificSerializer(Schema schema)
    {
        WRITER = new SpecificDatumWriter<E>(schema);
        READER = new SpecificDatumReader<E>(schema);
    }

    public byte[] serialize(E element) throws ElementSerializationException
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            encoder = ENCODER_FACTORY.binaryEncoder(out, encoder);
            WRITER.write(element, encoder);
            encoder.flush();
            return out.toByteArray();
        }
        catch (Exception ex)
        {
            throw new ElementSerializationException(ex);
        }
    }

    public E deserialize(byte[] bytes,E e) throws ElementSerializationException
    {
        try
        {         
            decoder = DECODER_FACTORY.binaryDecoder(bytes, decoder);
            return READER.read(e,decoder);
        }
        catch (Exception ex)
        {
            throw new ElementSerializationException(ex);
        }
    }
    
    public static void main(String[] args)
    {
        TestElement e = new TestElement();
        AvroSpecificSerializer<TestElement> serializer = new AvroSpecificSerializer<TestElement>(e.getSchema());
        
        
        TestElement src = new TestElement();
        e.setId(1);
        e.setName("test");
        e.setScore(0.23f);
        List<CharSequence> terms = new ArrayList<CharSequence>();
        terms.add("res");
        terms.add("ap");
        e.setTerms(terms);
        e.setTimestamp(0l);
        
        long start = System.currentTimeMillis();
        
        for(int i=0;i<10000;i++)
        {
            serializer.serialize(e);
        }
        
        long end =  System.currentTimeMillis();
        
        System.out.println(end - start);
        
        byte[] b = serializer.serialize(e);
        
        start = System.currentTimeMillis();
        
        for(int i=0;i<10000;i++)
        {
            serializer.deserialize(b, new TestElement());
        }
        
        end = System.currentTimeMillis();
        
        System.out.println(end - start);
        
       System.out.println(b.length);
        
    }
}
