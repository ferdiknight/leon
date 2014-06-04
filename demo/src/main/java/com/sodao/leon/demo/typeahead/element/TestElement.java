/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.demo.typeahead.element;

import cleo.search.Element;
import com.sodao.leon.demo.typeahead.util.AvroObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.avro.Schema;

/**
 *
 * @author ferdinand
 */
public class TestElement implements AvroElement
{

    private static final long serialVersionUID = 1L;
    private static Schema schema = new Schema.Parser().parse("{\"type\":\"record\",\"name\":\"TestElement\",\"namespace\":\"com.sodao.leon.demo.typeahead.serializer.nat\",\"fields\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"id\",\"type\":\"int\"},{\"name\":\"timestamp\",\"type\":\"long\"},{\"name\":\"score\",\"type\":\"float\"},{\"name\":\"terms\",\"type\":{\"type\":\"array\",\"items\":\"string\"}}]}");
    private int id;
    private long timestamp;
    private String[] terms;
    private float score;
    private String name;
    
    public int getId()
    {
        return this.id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public int getElementId()
    {
        return id;
    }

    @Override
    public void setElementId(int id)
    {
        this.id = id;
    }

    @Override
    public long getTimestamp()
    {
        return timestamp;
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

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public int compareTo(Element o)
    {
        return score < o.getScore() ? -1 : (score == o.getScore() ? (this.getElementId() - o.getElementId()) : 1);
    }

    @Override
    public <E extends AvroObject> E clone()
    {
        TestElement e = new TestElement();
        e.setElementId(id);
        e.setScore(score);
        e.setTerms(terms);
        e.setTimestamp(timestamp);
        e.setName(name);
        E res = (E)e;
        return res;
    }

    @Override
    public int hashCode()
    {
        int hashCode = id;
        hashCode += timestamp / 23;

        if (terms != null)
        {
            for (String t : terms)
            {
                hashCode += t.hashCode();
            }
        }

        return hashCode;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (o.getClass() == getClass())
        {
            TestElement e = (TestElement) o;
            return id == e.getElementId()
                    && timestamp == e.getTimestamp()
                    && Arrays.equals(terms, e.getTerms())
                    && score == e.getScore();
        }
        else
        {
            return false;
        }
    }

    public void put(int i, Object o)
    {
        switch (i)
        {
            case 0:
                name = CharSequence.class.cast(o).toString();
                break;
            case 1:
                id = (Integer) o;
                break;
            case 2:
                timestamp = (Long) o;
                break;
            case 3:
                score = (Float) o;
                break;
            case 4:
                List terms_list = List.class.cast(o);
                terms = cs2s(terms_list);
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    private String[] cs2s(List<CharSequence> src)
    {
        List<String> target = new ArrayList<String>();
        for(int i=0;i<src.size();i++)
        {
            target.add(src.get(i).toString());
        }
        String[] s = new String[target.size()];
        target.toArray(s);
        return s;
    }
    
    public Object get(int i)
    {
        switch (i)
        {
            case 0:
                return name;
            case 1:
                return id;
            case 2:
                return timestamp;
            case 3:
                return score;
            case 4:
                return terms == null?null:Arrays.asList(terms);
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    public Schema getSchema()
    {
        return this.schema;
    }

    private enum Param
    {

        ID(0),
        NAME(1),
        TIMESTAMP(2),
        TERMS(3),
        SCORE(4);
        private int intValue;

        Param(int i)
        {
            this.intValue = i;
        }

        public int getIntValue()
        {
            return this.intValue;
        }

        public static Param fromInt(int intValue)
        {
            switch (intValue)
            {
                case 0:
                    return ID;
                case 1:
                    return NAME;
                case 2:
                    return TIMESTAMP;
                case 3:
                    return TERMS;
                case 4:
                    return SCORE;
                default:
                    throw new RuntimeException("undefined param!");
            }
        }
    }
}
