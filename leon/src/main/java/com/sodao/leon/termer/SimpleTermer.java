/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.termer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ferdinand
 */
public class SimpleTermer implements Termer
{
    private final char CHINESE_FLAG = 19968;
    
    public String[] analyze(String content)
    {
        Set<String> termsset = term(content);      
        String[] terms = new String[termsset.size()];
        termsset.toArray(terms);
        return terms;
        
    }
    
    private Set<String> term(String content)
    {
        char[] chararray = content.toCharArray();
        Set<String> termslist = new HashSet<String>();
        StringBuilder builder = new StringBuilder();
        char c;
        boolean flag = true;
        for(int i=chararray.length - 1;i >= 0;i--)
        {
            c = chararray[i];
            
            if(isChinese(c))
            {
                if(flag)
                {
                    builder.insert(0,c);
                    termslist.add(builder.toString().trim());
                }
                else
                {
                    if(builder.length() != 0)
                    {
                        termslist.add(builder.toString().trim());
                        builder.delete(0, builder.length());
                    }
                    builder.insert(0,c);
                    termslist.add(builder.toString().trim());
                    flag = true;
                }
            }
            else
            {
                if(flag)
                {
                    if(builder.length() != 0)
                        builder.delete(0, builder.length());
                    builder.insert(0,c);
                    flag = false;
                }
                else
                {
                    builder.insert(0,c);
                }
            }
        }
        
        if(!flag)
            termslist.add(builder.toString().trim());
        
        return termslist;
    }
    
    private boolean isChinese(char c)
    {
        
        return c > CHINESE_FLAG;
    }

    public String[] analyze(String[] contents)
    {
        Set<String> termsset = new HashSet<String>();
        for(int i=0,cnt=contents.length;i<cnt;i++)
        {
            termsset.addAll(term(contents[i]));
        }
        String[] terms = new String[termsset.size()];
        termsset.toArray(terms);
        return terms;
    }
}
