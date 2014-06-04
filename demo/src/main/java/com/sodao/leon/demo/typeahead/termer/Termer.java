/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.demo.typeahead.termer;

/**
 *
 * @author ferdinand
 */
public interface Termer
{
    /**
     * analyze the content
     * @param content
     * @return a terms for search
     */
    public String[] analyze(String content);
    
    public String[] analyze(String[] contents);
}
