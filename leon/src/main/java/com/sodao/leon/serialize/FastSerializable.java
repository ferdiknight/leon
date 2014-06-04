/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sodao.leon.serialize;


import com.sodao.leon.buffer.ChannelBuffer;

/**
 *
 * @author ferdinand
 */
public interface FastSerializable
{
    public void write(ChannelBuffer buffer);
    
    public void read(ChannelBuffer buffer);
}
