/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.serializer;

import cleo.search.ElementSerializationException;
import cleo.search.ElementSerializer;
import com.blueferdi.leon.demo.typeahead.element.AvroElement;
import com.blueferdi.leon.demo.typeahead.element.TestElement;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;

/**
 *
 * @param <E>
 * @author ferdinand
 */
public class AvroGenericSerializer<E extends AvroElement> implements ElementSerializer<E>
{
    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();
    private GenericDatumWriter<E> WRITER;
    private GenericDatumReader<E> READER;
    private BinaryEncoder encoder;
    private BinaryDecoder decoder;
    
    private E e;
    
    public void setE(E e)
    {
        this.e = e;
    }
    
    public AvroGenericSerializer(Schema schema)
    {
        WRITER = new GenericDatumWriter<E>(schema);
        READER = new GenericDatumReader<E>(schema);
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

    public E deserialize(byte[] bytes) throws ElementSerializationException
    {
        try
        {
            E temp = (E) e.getClass().newInstance();
            decoder = DECODER_FACTORY.binaryDecoder(bytes, decoder);
            E res = READER.read(e,decoder);
            e = temp;temp = null;
            return res;
            
        }
        catch (Exception ex)
        {
            throw new ElementSerializationException(ex);
        }
    }
    
    public static void main(String[] args) throws IOException
    {
        TestElement target = new TestElement();
        AvroGenericSerializer<TestElement> serializer = new AvroGenericSerializer<TestElement>(target.getSchema());
        
        serializer.setE(target);
        
        PrintStream out = System.out;
        
        TestElement e = new TestElement();
        e.setId(1);
        e.setName("test");
        e.setScore(0.23f);
        e.setTerms("res","ap");
        e.setTimestamp(0l);
        
        long start = System.currentTimeMillis();
        for(int i=0;i<10000;i++)
        {
            serializer.serialize(e);
        }
        
        long end = System.currentTimeMillis();
        
        out.println("serializing ..." + ": cost " + (end - start) + " ms");
        
        byte[] data = serializer.serialize(e);
        
        start =  System.currentTimeMillis();
        
        for(int i=0;i<10000;i++)
        {
            serializer.deserialize(data);
        }
        
        end = System.currentTimeMillis();
        
        out.println("deserializing ..." + ": cost " + (end - start) + " ms");
        
        System.out.println(data.length);
    }
}
