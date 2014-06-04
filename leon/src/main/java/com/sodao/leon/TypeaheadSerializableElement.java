/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon;

import cleo.search.Element;
import com.sodao.leon.buffer.ChannelBuffer;
import com.sodao.leon.serialize.FastSerializableElement;
import com.sodao.leon.tools.Convert;
import java.util.Arrays;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ferdinand
 */
@XmlRootElement(name="element")
public class TypeaheadSerializableElement implements FastSerializableElement
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
    
    @Override
    public int getElementId()
    {
        return this.id;
    }

    @Override
    public void setElementId(int id)
    {
        this.id = id;
    }

    @Override
    public long getTimestamp()
    {
        return this.timestamp;
    }

    @Override
    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    @Override
    public String[] getTerms()
    {
        return this.terms;
    }

    @Override
    public void setTerms(String... terms)
    {
        this.terms = terms;
    }

    @Override
    public float getScore()
    {
        return score;
    }

    @Override
    public void setScore(float score)
    {
        this.score = score;
    }

    @Override
    public int compareTo(Element o)
    {
        return score < o.getScore() ? -1 : (score == o.getScore() ? (this.getElementId() - o.getElementId()) : 1);
    }

    @Override
    public boolean equals(Object o) {
      if(o == null) return false;
      if(o.getClass() == getClass()) {
          TypeaheadSerializableElement e = (TypeaheadSerializableElement)o;
          return id == e.getElementId()&&
              timestamp == e.getTimestamp() &&
              Arrays.equals(terms, e.getTerms()) &&
              score == e.getScore();
      } else {
          return false;
      }
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 41 * hash + this.id;
        hash = 41 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
        hash = 41 * hash + Arrays.deepHashCode(this.terms);
        hash = 41 * hash + Float.floatToIntBits(this.score);
        return hash;
    }
    
    @Override
    public TypeaheadSerializableElement clone()
    {
        TypeaheadSerializableElement e = new TypeaheadSerializableElement();
        e.setId(id);
        e.setName(name);
        e.setScore(score);
        e.setTerms(terms);
        e.setTimestamp(timestamp);
        e.setAttrs(attrs);
        return e;
    }
    
    public boolean isSearchable()
    {
        return terms != null && terms.length > 0;
    }
    
    @Override
    public void write(ChannelBuffer buffer)
    {
        Convert.encodeInt(buffer,id);
        
        Convert.encodeString(buffer,name);
        
        Convert.encodeFloat(buffer,score);
        
        Convert.encodeStringArray(buffer,terms);
        
        Convert.encodeLong(buffer,timestamp);
        
        Convert.encodeStringMap(buffer,attrs);
    }

    @Override
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
