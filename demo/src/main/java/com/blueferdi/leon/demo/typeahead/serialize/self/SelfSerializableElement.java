/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.serialize.self;

import cleo.search.Element;
import com.blueferdi.leon.demo.typeahead.buffer.ChannelBuffer;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ferdinand
 */
@XmlRootElement(name="element")
public class SelfSerializableElement implements FastSerializableElement
{
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private long timestamp;
    private String[] terms;
    private float score;
    private Map<String,String> attrs;
    
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }
    
    public Map<String, String> getAttrs()
    {
        return attrs;
    }

    public void setAttrs(Map<String, String> attrs)
    {
        this.attrs = attrs;
    }
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public int getElementId()
    {
        return this.id;
    }

    public void setElementId(int id)
    {
        this.id = id;
    }

    public long getTimestamp()
    {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public String[] getTerms()
    {
        return this.terms;
    }

    public void setTerms(String... terms)
    {
        this.terms = terms;
    }

    public float getScore()
    {
        return score;
    }

    public void setScore(float score)
    {
        this.score = score;
    }

    public int compareTo(Element o)
    {
        return score < o.getScore() ? -1 : (score == o.getScore() ? (this.getElementId() - o.getElementId()) : 1);
    }

    public void write(ChannelBuffer buffer)
    {
        Convert.encodeInt(buffer,id);
        
        Convert.encodeString(buffer,name);
        
        Convert.encodeFloat(buffer,score);
        
        Convert.encodeStringArray(buffer,terms);
        
        Convert.encodeLong(buffer,timestamp);
        
        Convert.encodeStringMap(buffer,attrs);
    }

    public void read(ChannelBuffer buffer)
    {
        this.id = Convert.readInt(buffer);
        
        this.name = Convert.readString(buffer);
        
        this.score = Convert.readFloat(buffer);
        
        this.terms = Convert.readStringArray(buffer);
        
        this.timestamp = Convert.readLong(buffer);
        
        this.attrs = Convert.readMap(buffer);
        
    }
    
}
