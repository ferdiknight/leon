/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.rest.module;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author ferdinand
 */
@XmlRootElement(name="test")
public class TestBean
{
    private int id;
    private String[] terms;
    private String name;
    private float score;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public float getScore()
    {
        return score;
    }

    public void setScore(float score)
    {
        this.score = score;
    }

    public String[] getTerms()
    {
        return terms;
    }

    public void setTerms(String[] terms)
    {
        this.terms = terms;
    }
}
