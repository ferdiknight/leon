/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.connection;

import com.blueferdi.leon.demo.typeahead.util.AvroObject;
import org.apache.avro.Schema;

/**
 *
 * @author ferdinand
 */
public class TestConnection implements AvroConnection
{

    private static final long serialVersionUID = 1L;
    private Schema schema = new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"TestConnection\",\"namespace\":\"com.sodao.leon.demo.typeahead.connection\",\"fields\":[{\"name\":\"source\",\"type\":\"int\"},{\"name\":\"target\",\"type\":\"int\"},{\"name\":\"strength\",\"type\":\"int\"},{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"isActive\",\"type\":\"boolean\"}]}");
    private int source;
    private int target;
    private int strength;
    private long timestamp;
    private boolean isActive;

    public TestConnection(int source,int target,boolean isActive)
    {
        this.source = source;
        this.target = target;
        this.isActive = isActive;
    }
    
    public <E extends AvroObject> E clone()
    {
        TestConnection clone = new TestConnection(this.source,this.target,this.isActive);
        clone.setStrength(this.strength);
        clone.setTimestamp(this.timestamp);
        
        return (E)clone;
    }

    public int source()
    {
        return this.source;
    }

    public int target()
    {
        return this.target;
    }

    public int getStrength()
    {
        return this.strength;
    }

    public void setStrength(int strength)
    {
        this.strength = strength;
    }

    public long getTimestamp()
    {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public boolean isActive()
    {
        return this.isActive;
    }

    public void setActive(boolean b)
    {
        this.isActive = b;
    }

    public void put(int i, Object v)
    {
        switch (i)
        {
            case 0:
                source = (java.lang.Integer) v;
                break;
            case 1:
                target = (java.lang.Integer) v;
                break;
            case 2:
                strength = (java.lang.Integer) v;
                break;
            case 3:
                timestamp = (java.lang.Long) v;
                break;
            case 4:
                isActive = (java.lang.Boolean) v;
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    public Object get(int i)
    {
        switch (i)
        {
            case 0:
                return source;
            case 1:
                return target;
            case 2:
                return strength;
            case 3:
                return timestamp;
            case 4:
                return isActive;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    public Schema getSchema()
    {
        return this.schema;
    }
}
