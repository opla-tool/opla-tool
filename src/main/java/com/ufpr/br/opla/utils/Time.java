/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author elf
 */
public class Time {

  public static String timeNow() {
    return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ").format(new Date());
  }
  
  public static String convertMsToMin(long time){
    return new SimpleDateFormat("mm:ss").format(new Date(time));
  }
  
}
