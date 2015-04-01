/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ho.yaml.Yaml;

/**
 *
 * @author elf
 */
public class ManagerGuiSettingsConfig {

  private GuiSettings guisettings;

  public ManagerGuiSettingsConfig() {
    try {
      this.guisettings = Yaml.loadType(new File(UserHome.getGuiSettingsFilePath()), GuiSettings.class);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(GuiSettings.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public int getFontSize() {
    return this.guisettings.getFontSize();
  }
  
  public String getEdChartType(){
    return this.guisettings.getEdChartType();
  }
  
  public String getSaveChartsAsPng(){
    return this.guisettings.getSaveChartsAsPng();
  }
  
  /**
   * Não usado ainda.
   * @param fontSize 
   */
  public void setFontSize(int fontSize){
    this.guisettings.setFontSize(fontSize);
    updateConfigurationFile();
  }

  private void updateConfigurationFile() {
    try {
      Yaml.dump(guisettings, new File(UserHome.getGuiSettingsFilePath()), true);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(ManagerApplicationConfig.class.getName()).log(Level.SEVERE, "Ops, Error when try update configuration gui file: {0}", ex);
    }
  }
}
