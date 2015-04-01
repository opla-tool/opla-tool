/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.configuration;

/**
 *
 * @author elf
 */
public class GuiFile {
  
  private static ManagerGuiSettingsConfig instance = null;

  protected GuiFile() {
  }
  
   public static ManagerGuiSettingsConfig getInstance() {
    if (instance == null)
      instance = new ManagerGuiSettingsConfig();

    return instance;
  }
  
}
