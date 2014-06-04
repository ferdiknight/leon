/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blueferdi.leon.demo.typeahead.serialize.self;


import com.blueferdi.leon.demo.typeahead.buffer.ChannelBuffer;

/**
 *
 * @author ferdinand
 */
public interface FastSerializable
{
    public void write(ChannelBuffer buffer);
    
    public void read(ChannelBuffer buffer);
}
