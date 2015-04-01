/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.configuration;

import com.ufpr.br.opla.utils.Utils;
import java.io.File;

/**
 *
 * @author elf
 */
public class UserHome {

    private static final String user_home = System.getProperty("user.home");
    private static final String file_separator = "/";
    private static final String home = user_home + file_separator + "oplatool" + file_separator;

    public static void copyConfigFileToUserHome() {
        //Copia arquivo de configuracao para pasta default (oplatool).
        String a = Thread.currentThread().getContextClassLoader().
                getResource("config/application.yaml").getFile();
        
        Utils.copy(a, home + "application.yaml");
    }
    
    public static String getOplaUserHome(){
        return System.getProperty("user.home") + System.getProperty("file.separator") + "oplatool" + System.getProperty("file.separator") ;
    }
    
    public static String getConfigurationFilePath(){
        return home + file_separator + "application.yaml";
    }
    
    public static String getGuiSettingsFilePath(){
      return home + file_separator + "guisettings.yml";
    }
    
    public static void createDefaultOplaPathIfDontExists(){
        File f = new File(getOplaUserHome());
        if(!f.exists())
            f.mkdirs();
    }

    public static void createProfilesPath() {
      Utils.createPath(home + "profiles/");
    }

   public static void createTemplatePath() {
     Utils.createPath(home + "templates/");
   }

   public static void createOutputPath() {
    Utils.createPath(home + "output/");
   }

   public static void createTempPath() {
    Utils.createPath(home + "temp/");
   }
   
   public static String getFileSeparator(){
       return file_separator;
   }

   public static String getPathToDb() {
    return getOplaUserHome() + "db" + file_separator + "oplatool.db";
   }
   
   public static String getPathToConfigFile(){
       return getOplaUserHome() + "application.yaml";
   }
       
}
