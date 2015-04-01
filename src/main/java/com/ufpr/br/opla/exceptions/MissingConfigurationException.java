/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ufpr.br.opla.exceptions;

/**
 *
 * @author elf
 */
public class MissingConfigurationException extends Exception{
    
    public MissingConfigurationException(String message){
        super(message);
    }
    
}
