/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.serialize;

import cleo.search.ElementSerializationException;
import cleo.search.ElementSerializer;
import com.sodao.leon.TypeaheadSerializableElement;
import com.sodao.leon.buffer.ChannelBuffer;
import com.sodao.leon.buffer.ChannelBuffers;
import com.sodao.leon.buffer.HeapChannelBufferFactory;
import java.nio.ByteOrder;
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
    private static final int INIT_SIZE = 1024;
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

            E e = (E) new TypeaheadSerializableElement();//(E)Class.forName(clazz).newInstance();//
            e.read(buffer);
            return e;
        }
        catch (Exception ex)
        {
            log.error("create instance error :" + clazz + ":" + ex.getMessage(), ex);
            throw new ElementSerializationException(ex);
        }
    }
}
