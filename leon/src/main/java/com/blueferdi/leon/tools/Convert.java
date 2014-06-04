/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.tools;

import com.blueferdi.leon.buffer.ChannelBuffer;
import com.blueferdi.leon.buffer.ChannelBuffers;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author ferdinand
 */
public class Convert
{

    private static Log log = LogFactory.getLog(Convert.class);
    
    private static final String CHARSET = "UTF-8";

    public static boolean readBoolean(ChannelBuffer buffer)
    {
        return buffer.readByte() == 0 ? true : false;
    }

    public static String readString(ChannelBuffer buffer)
    {
        try
        {
            int length = readInt(buffer);
            
            if(length == 0)
                return "";
            
            byte[] b = new byte[length];
            buffer.readBytes(b);
            String result = new String(b, CHARSET);
            return result;
        }
        catch (UnsupportedEncodingException ex)
        {
            log.error(ex.getMessage(), ex);
        }
        return "";
    }

    public static String[] readStringArray(ChannelBuffer buffer)
    {
        int length = readInt(buffer);
        if(length == 0)
            return new String[0];
        
        String[] terms = new String[length];
        
        for(int i=0;i<length;i++)
        {
            terms[i] = readString(buffer);
        }
        
        return terms;
        
    }

    public static long readLong(ChannelBuffer buffer)
    {
        int b = buffer.readByte() & 0xff;
        int n = b & 0x7f;
        long l;
        if (b > 0x7f)
        {
            b = buffer.readByte() & 0xff;
            n ^= (b & 0x7f) << 7;
            if (b > 0x7f)
            {
                b = buffer.readByte() & 0xff;
                n ^= (b & 0x7f) << 14;
                if (b > 0x7f)
                {
                    b = buffer.readByte() & 0xff;
                    n ^= (b & 0x7f) << 21;
                    if (b > 0x7f)
                    {
                        // only the low 28 bits can be set, so this won't carry
                        // the sign bit to the long
                        l = innerLongDecode(buffer,(long) n);
                    }
                    else
                    {
                        l = n;
                    }
                }
                else
                {
                    l = n;
                }
            }
            else
            {
                l = n;
            }
        }
        else
        {
            l = n;
        }
        return (l >>> 1) ^ -(l & 1); // back to two's-complement
    }

    // splitting readLong up makes it faster because of the JVM does more
    // optimizations on small methods
    private static long innerLongDecode(ChannelBuffer buffer,long l)
    {
        int b = buffer.readByte() & 0xff;
        l ^= (b & 0x7fL) << 28;
        if (b > 0x7f)
        {
            b = buffer.readByte() & 0xff;
            l ^= (b & 0x7fL) << 35;
            if (b > 0x7f)
            {
                b = buffer.readByte() & 0xff;
                l ^= (b & 0x7fL) << 42;
                if (b > 0x7f)
                {
                    b = buffer.readByte() & 0xff;
                    l ^= (b & 0x7fL) << 49;
                    if (b > 0x7f)
                    {
                        b = buffer.readByte() & 0xff;
                        l ^= (b & 0x7fL) << 56;
                        if (b > 0x7f)
                        {
                            b = buffer.readByte() & 0xff;
                            l ^= (b & 0x7fL) << 63;
                            if (b > 0x7f)
                            {
                                log.error("incorrect long encoding!");
                            }
                        }
                    }
                }
            }
        }
        return l;
    }

    public static float readFloat(ChannelBuffer buffer)
    {
        int n = (buffer.readByte() & 0xff) | ((buffer.readByte() & 0xff) << 8)
                | ((buffer.readByte() & 0xff) << 16) | ((buffer.readByte() & 0xff) << 24);
        return Float.intBitsToFloat(n);
    }

    public static double readDouble(ChannelBuffer buffer)
    {
        int n1 = (buffer.readByte() & 0xff) | ((buffer.readByte() & 0xff) << 8)
                | ((buffer.readByte() & 0xff) << 16) | ((buffer.readByte() & 0xff) << 24);
        int n2 = (buffer.readByte() & 0xff) | ((buffer.readByte() & 0xff) << 8)
                | ((buffer.readByte() & 0xff) << 16) | ((buffer.readByte() & 0xff) << 24);

        return Double.longBitsToDouble((((long) n1) & 0xffffffffL)
                | (((long) n2) << 32));
    }

    public static int readInt(ChannelBuffer buffer)
    {
        int b = buffer.readByte() & 0xff;
        int n = b & 0x7f;
        if (b > 0x7f)
        {
            b = buffer.readByte() & 0xff;
            n ^= (b & 0x7f) << 7;
            if (b > 0x7f)
            {
                b = buffer.readByte() & 0xff;
                n ^= (b & 0x7f) << 14;
                if (b > 0x7f)
                {
                    b = buffer.readByte() & 0xff;
                    n ^= (b & 0x7f) << 21;
                    if (b > 0x7f)
                    {
                        b = buffer.readByte() & 0xff;
                        n ^= (b & 0x7f) << 28;
                        if (b > 0x7f)
                        {
                            log.error("incorrect int encoding!");
                        }
                    }
                }
            }
        }
        return (n >>> 1) ^ -(n & 1); // back to two's-complement
    }

    public static Map<String,String> readMap(ChannelBuffer buffer)
    {
        int size = readInt(buffer);
        Map<String,String> map = new HashMap<String,String>();
        if(size == 0)
        {
            return map;
        }

        for(int i=0;i<size;i++)
        {
            map.put(readString(buffer), readString(buffer));
        }
        
        return map;
    }
    
    public static void encodeBoolean(ChannelBuffer buffer,boolean b)
    {
        buffer.writeByte(b ? 1 : 0);
    }

    public static void encodeString(ChannelBuffer buffer,String s)
    {
        try
        {
            if(s == null || s.equals(""))
            {
                buffer.writeByte(0);
            }
            else
            {
                byte[] result = s.getBytes(CHARSET);
                encodeInt(buffer,result.length);
                buffer.writeBytes(result);
            }
        }
        catch (Exception ex)
        {
            log.error(ex.getMessage(), ex);
        }
    }

    public static void encodeStringArray(ChannelBuffer buffer, String... s)
    {
        if(s.length == 0)
        {
            buffer.writeByte(0);
        }
        else
        {
            encodeInt(buffer,s.length);
            for(int i=0,cnt=s.length;i<cnt;i++)
            {
                encodeString(buffer,s[i]);
            }
        }
    }

    /**
     * Encode an integer to the byte array at the given position. Will throw
     * IndexOutOfBounds if it overflows. Users should ensure that there are at
     * least 5 bytes left in the buffer before calling this method.
     *
     * @param n
     * @param buffer
     */
    public static void encodeInt(ChannelBuffer buffer,int n)
    {
        // move sign to low-order bit, and flip others if negative
        n = (n << 1) ^ (n >> 31);
        if ((n & ~0x7F) != 0)
        {
            buffer.writeByte((byte) ((n | 0x80) & 0xFF));
            n >>>= 7;
            if (n > 0x7F)
            {
                buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                n >>>= 7;
                if (n > 0x7F)
                {
                    buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                    n >>>= 7;
                    if (n > 0x7F)
                    {
                        buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                        n >>>= 7;
                    }
                }
            }
        }
        buffer.writeByte((byte) n);
    }

    /**
     * Encode a long to the byte array at the given position. Will throw
     * IndexOutOfBounds if it overflows. Users should ensure that there are at
     * least 10 bytes left in the buffer before calling this method.
     *
     * @param n
     * @param buffer
     */
    public static void encodeLong(ChannelBuffer buffer,long n)
    {
        // move sign to low-order bit, and flip others if negative
        n = (n << 1) ^ (n >> 63);
        if ((n & ~0x7FL) != 0)
        {
            buffer.writeByte((byte) ((n | 0x80) & 0xFF));
            n >>>= 7;
            if (n > 0x7F)
            {
                buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                n >>>= 7;
                if (n > 0x7F)
                {
                    buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                    n >>>= 7;
                    if (n > 0x7F)
                    {
                        buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                        n >>>= 7;
                        if (n > 0x7F)
                        {
                            buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                            n >>>= 7;
                            if (n > 0x7F)
                            {
                                buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                                n >>>= 7;
                                if (n > 0x7F)
                                {
                                    buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                                    n >>>= 7;
                                    if (n > 0x7F)
                                    {
                                        buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                                        n >>>= 7;
                                        if (n > 0x7F)
                                        {
                                            buffer.writeByte((byte) ((n | 0x80) & 0xFF));
                                            n >>>= 7;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        buffer.writeByte((byte) n);
    }

    /**
     * Encode a float to the byte array at the given position. Will throw
     * IndexOutOfBounds if it overflows. Users should ensure that there are at
     * least 4 bytes left in the buffer before calling this method.
     *
     * @param f
     * @param buffer
     */
    public static void encodeFloat(ChannelBuffer buffer,float f)
    {
        int bits = Float.floatToRawIntBits(f);
        // hotspot compiler works well with this variant 
        buffer.writeByte((byte) ((bits) & 0xFF));
        buffer.writeByte((byte) ((bits >>> 8) & 0xFF));
        buffer.writeByte((byte) ((bits >>> 16) & 0xFF));
        buffer.writeByte((byte) ((bits >>> 24) & 0xFF));
    }

    /**
     * Encode a double to the byte array at the given position. Will throw
     * IndexOutOfBounds if it overflows. Users should ensure that there are at
     * least 8 bytes left in the buffer before calling this method.
     *
     * @param d
     * @param buffer
     */
    public static void encodeDouble(ChannelBuffer buffer,double d)
    {
        long bits = Double.doubleToRawLongBits(d);
        int first = (int) (bits & 0xFFFFFFFF);
        int second = (int) ((bits >>> 32) & 0xFFFFFFFF);
        // the compiler seems to execute this order the best, likely due to
        // register allocation -- the lifetime of constants is minimized.
        buffer.writeByte((byte) ((first) & 0xFF));
        buffer.writeByte((byte) ((first >>> 8) & 0xFF));
        buffer.writeByte((byte) ((first >>> 16) & 0xFF));
        buffer.writeByte((byte) ((first >>> 24) & 0xFF));
        buffer.writeByte((byte) ((second) & 0xFF));
        buffer.writeByte((byte) ((second >>> 8) & 0xFF));
        buffer.writeByte((byte) ((second >>> 16) & 0xFF));
        buffer.writeByte((byte) ((second >>> 24) & 0xFF));
    }

    public static void encodeStringMap(ChannelBuffer buffer,Map<String,String> map)
    {
        if(map == null || map.isEmpty())
        {
            buffer.writeByte(0);
        }
        else
        {
            encodeInt(buffer,map.size());
            Iterator<Entry<String,String>> iterator = map.entrySet().iterator();
            while(iterator.hasNext())
            {
                Entry<String,String> entry = iterator.next();
                encodeString(buffer,entry.getKey());
                encodeString(buffer,entry.getValue());
            }
        }   
    }
    
    public static double log(double value, double base)
    {
        return Math.log(value) / Math.log(base);
    }
}
