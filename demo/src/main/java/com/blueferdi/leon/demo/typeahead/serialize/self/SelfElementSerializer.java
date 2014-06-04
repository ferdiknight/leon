/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.serialize.self;

import cleo.search.ElementSerializationException;
import cleo.search.ElementSerializer;
import com.blueferdi.leon.demo.typeahead.buffer.ChannelBuffer;
import com.blueferdi.leon.demo.typeahead.buffer.ChannelBuffers;
import com.blueferdi.leon.demo.typeahead.buffer.HeapChannelBufferFactory;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @param <E>
 * @author ferdinand
 */
public class SelfElementSerializer<E extends FastSerializableElement> implements ElementSerializer<E>
{

    private Log log = LogFactory.getLog(SelfElementSerializer.class);
    private static final int INIT_SIZE = 256;
    private String clazz;

    public String getClazz()
    {
        return clazz;
    }

    public void setClazz(String clazz)
    {
        this.clazz = clazz;
    }

    public byte[] serialize(E element) throws ElementSerializationException
    {
        ChannelBuffer buffer = ChannelBuffers.dynamicBuffer(ByteOrder.BIG_ENDIAN, INIT_SIZE, HeapChannelBufferFactory.getInstance());
//        ByteBuffer buffer =  ByteBuffer.allocate(1024);
        element.write(buffer);

        byte[] result = new byte[buffer.writerIndex()];
        buffer.readBytes(result);

        return result;
    }

    @SuppressWarnings("unchecked")
    public E deserialize(byte[] bytes) throws ElementSerializationException
    {
        try
        {
            ChannelBuffer buffer = HeapChannelBufferFactory.getInstance(ByteOrder.BIG_ENDIAN).getBuffer(bytes.length<INIT_SIZE?INIT_SIZE:bytes.length);
            buffer.writeBytes(bytes);

            E e = (E) new SelfSerializableElement();//(E)Class.forName(clazz).newInstance();//
            e.read(buffer);
            return e;
        }
        catch (Exception ex)
        {
            log.error("create instance error :" + clazz + ":" + ex.getMessage(), ex);
            throw new ElementSerializationException(ex);
        }
    }

    public static void main(String[] args)
    {
        SelfElementSerializer<SelfSerializableElement> serializer = new SelfElementSerializer<SelfSerializableElement>();
        serializer.setClazz("com.sodao.leon.demo.typeahead.serialize.self.SelfSerializableElement");

        SelfSerializableElement element = new SelfSerializableElement();

        element.setId(1);
        element.setName("中文");
        element.setTerms("zhong", "wen", "中文", "文");
        element.setTimestamp(System.currentTimeMillis());
        element.setScore(0.23f);
        Map<String,String> map = new HashMap<String,String>();
        
        map.put("description", "test");
        map.put("school","zj");
        element.setAttrs(map);

//        byte[] result = serializer.serialize(element);
//        SelfSerializableElement element_des = serializer.deserialize(result);

         long serial_avr = 0;
        long deserial_avr = 0;
        
        for (int j = 0; j < 100; j++)
        {
            long start = System.currentTimeMillis();

            for (int i = 0; i < 1000000; i++)
            {
                serializer.serialize(element);
            }
            long stop = System.currentTimeMillis();
            
            long serial = stop - start;
            
            serial_avr = (serial_avr + serial)/2;
            
            System.out.println(serial);

            byte[] serializion = serializer.serialize(element);

            start = System.currentTimeMillis();

            for (int i = 0; i < 1000000; i++)
            {
                serializer.deserialize(serializion);
            }

            stop = System.currentTimeMillis();
            
            long deserial = stop - start;
            deserial_avr = (deserial_avr + deserial)/2; 
            
            System.out.println(deserial);
            
            System.out.println(serializion.length);
            
            System.out.println();
        }
        
        System.out.println("avr_serial :" + serial_avr);
        System.out.println("avr_deserial :" + deserial_avr);

    }
}
