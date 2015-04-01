/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.algorithms;

/**
 *
 * @author elf
 */
public class Solution {
    
    private String id;
    private String name;
    
    public Solution(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

   
    public String getName() {
        return name;
    }
    
    @Override
    public String toString(){
        return this.name;
    }
    
}
