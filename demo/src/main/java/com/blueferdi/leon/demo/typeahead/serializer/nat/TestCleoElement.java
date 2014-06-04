/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.serializer.nat;

import cleo.search.Element;

/**
 *
 * @author ferdinand
 */
public class TestCleoElement implements Element
{
    private static final long serialVersionUID = 1L;

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

    public int compareTo(Element o)
    {
        return score < o.getScore() ? -1 : (score == o.getScore() ? (this.getElementId() - o.getElementId()) : 1);
    }
    
}
