/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.utils;

/**
 *
 * @author elf
 */
public class OsUtils {

  private static String OS = null;

  public static String getOsName() {
    if (OS == null) {
      OS = System.getProperty("os.name");
    }
    return OS;
  }

  public static boolean isWindows() {
    return getOsName().startsWith("Windows");
  }
}
