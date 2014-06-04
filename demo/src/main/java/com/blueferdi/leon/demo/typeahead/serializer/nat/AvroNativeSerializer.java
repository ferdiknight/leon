/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.serializer.nat;

import cleo.search.ElementSerializationException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;

/**
 *
 * @author ferdinand
 */
public class AvroNativeSerializer<E>
{

    private static final DecoderFactory DECODER_FACTORY = DecoderFactory.get();
    private static final EncoderFactory ENCODER_FACTORY = EncoderFactory.get();
    private GenericDatumWriter<E> WRITER;
    private GenericDatumReader<E> READER;
    private BinaryEncoder encoder;
    private BinaryDecoder decoder;

    public AvroNativeSerializer(Schema schema)
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

    public E deserialize(byte[] bytes, E e) throws ElementSerializationException
    {
        try
        {
            decoder = DECODER_FACTORY.binaryDecoder(bytes, decoder);
            return READER.read(e, decoder);
        }
        catch (Exception ex)
        {
            throw new ElementSerializationException(ex);
        }
    }

    public static void main(String[] args)
    {
        TestElement e = new TestElement();
        AvroNativeSerializer<TestElement> serializer = new AvroNativeSerializer<TestElement>(e.getSchema());


        TestElement src = new TestElement();
        e.setId(1);
        e.setName("test");
        e.setScore(0.23f);
        List<CharSequence> terms = new ArrayList<CharSequence>();
        terms.add("res");
        terms.add("ap");
        terms.add("中文");
        e.setTerms(terms);
        e.setTimestamp(System.currentTimeMillis());

        long serial_avr = 0,deserial_avr = 0;
        
        for (int j = 0; j < 100; j++)
        {

            long start = System.currentTimeMillis();

            for (int i = 0; i < 1000000; i++)
            {
                serializer.serialize(e);
            }

            long end = System.currentTimeMillis();
            long serial = end - start;
            serial_avr = (serial+serial_avr)/2;
            System.out.println(serial);

            byte[] b = serializer.serialize(e);

            start = System.currentTimeMillis();

            for (int i = 0; i < 1000000; i++)
            {
                serializer.deserialize(b, new TestElement());
            }

            end = System.currentTimeMillis();
            long deserial = end - start;
            deserial_avr = (deserial+deserial_avr)/2;
            System.out.println(deserial);

            System.out.println(b.length);
            System.out.println();
        }

        System.out.println("serial_avr :" + serial_avr + "\n" + "deserial_avr :" + deserial_avr);
        
    }
}
