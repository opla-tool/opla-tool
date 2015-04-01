package com.ufpr.br.opla.configuration;

import com.ufpr.br.opla.utils.Utils;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ho.yaml.Yaml;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template
 * in the editor.
 */
/**
 *
 * @author elf
 */
public class ManagerApplicationConfig {

  private DirTarget configurationFile;

  public ManagerApplicationConfig() {
    try {
      this.configurationFile = Yaml.loadType(new File(UserHome.getConfigurationFilePath()), DirTarget.class);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(ManagerApplicationConfig.class.getName()).log(Level.SEVERE, null, ex);
      System.exit(1);
    }
  }

  public DirTarget getConfig() {
    return this.configurationFile;
  }

  public void updatePathToProfileSmarty(String newpath) {
    this.configurationFile.setPathToProfile(newpath);
    updateConfigurationFile();
  }

  public void updatePathToProfilePatterns(String newpath) {
    this.configurationFile.setPathToProfilePatterns(newpath);
    updateConfigurationFile();
  }

  public void updatePathToProfileRelationships(String newpath) {
    this.configurationFile.setPathToProfileRelationships(newpath);
    updateConfigurationFile();
  }

  public void updatePathToProfileConcerns(String newpath) {
    this.configurationFile.setPathToProfileConcern(newpath);
    updateConfigurationFile();
  }

  public void updatePathToTemplateFiles(String newpath) {
    this.configurationFile.setPathToTemplateModelsDirectory(newpath);
    updateConfigurationFile();
  }

  public void updatePathToExportModels(String newpath) {
    this.configurationFile.setDirectoryToExportModels(newpath);
    updateConfigurationFile();
  }

  public void updatePathToSaveModels(String path) {
    this.configurationFile.setDirectoryToSaveModels(path);
    updateConfigurationFile();
  }

  /**
   * Retorna os profile que estão em uso ou seja, não "" nem null.
   *
   */
  public String getProfilesUsed() {
    StringBuilder profiles = new StringBuilder();

    if (Utils.notNullAndNotEmpty(this.configurationFile.getPathToProfile())) {
      profiles.append(this.configurationFile.getPathToProfile());
      profiles.append(",");
    }

    if (Utils.notNullAndNotEmpty(this.configurationFile.getPathToProfileConcern())) {
      profiles.append(this.configurationFile.getPathToProfileConcern());
      profiles.append(",");
    }

    if (Utils.notNullAndNotEmpty(this.configurationFile.getPathToProfilePatterns())) {
      profiles.append(this.configurationFile.getPathToProfilePatterns());
      profiles.append(",");
    }

    if (Utils.notNullAndNotEmpty(this.configurationFile.getPathToProfileRelationships())) {
      profiles.append(this.configurationFile.getPathToProfileRelationships());
    }
    return profiles.toString();
  }

  private void updateConfigurationFile() {
    try {
      Yaml.dump(configurationFile, new File(UserHome.getConfigurationFilePath()), true);
    } catch (FileNotFoundException ex) {
      Logger.getLogger(ManagerApplicationConfig.class.getName()).log(Level.SEVERE, "Ops, Error when try update configuration file: {0}", ex);
    }
  }
}
