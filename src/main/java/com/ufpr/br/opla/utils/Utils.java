/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.ufpr.br.opla.configuration.UserHome;
import com.ufpr.br.opla.gui2.main;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.WordUtils;

/**
 *
 * @author elf
 */
public class Utils {
  
  private static final String PATH_CONFIGURATION_FILE = "config/application.yaml";

  public static String extractSolutionIdFromSolutionFileName(String fileName) {
    return fileName.substring(fileName.indexOf("-") + 1, fileName.length());
  }

  public static String capitalize(String word) {
    return WordUtils.capitalize(word);
  }

  public static void copy(String source, String target) {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader().
              getResourceAsStream(source);
      try (FileOutputStream out = new FileOutputStream(target)) {
        byte[] buffer = new byte[1024];
        int len = in.read(buffer);
        while (len != -1) {
          out.write(buffer, 0, len);
          len = in.read(buffer);
        }
      }
      Logger.getLogger(main.class.getName()).log(Level.INFO, "File copy from {0} to {1}", new Object[]{source, target});
    } catch (Exception e) {
      System.err.println(e);
      Logger.getLogger(main.class.getName()).log(Level.SEVERE, e.toString());
      System.exit(1);
    }
  }

  public static void createPath(String path) {
    File pathDir = new File(path);
    if (!pathDir.exists()) {
      Logger.getLogger(main.class.getName()).log(Level.INFO, "Diretorio não existe, criando....");
      pathDir.mkdirs();
      Logger.getLogger(main.class.getName()).log(Level.INFO, "Directory {0} created successfully", path);
    }
  }

  /*
   * Get the extension of a file.
   */
  public static String getExtension(File f) {
    String ext = null;
    String s = f.getName();
    int i = s.lastIndexOf('.');

    if (i > 0 && i < s.length() - 1) {
      ext = s.substring(i + 1).toLowerCase();
    }

    return ext;
  }

  public static boolean selectedSolutionIsNonDominated(String fileName) {
    if (fileName.startsWith("VAR_All")) {
      return true;
    }

    return false;
  }

  public static List<Entry<String, Double>> shortMap(SortedMap<String, Double> resultsEds) {
    List<Map.Entry<String, Double>> edsValues = Lists.newArrayList(resultsEds.entrySet());

    Ordering<Map.Entry<String, Double>> byMapValues = new Ordering<Map.Entry<String, Double>>() {

      @Override
      public int compare(Map.Entry<String, Double> left, Map.Entry<String, Double> right) {
        return left.getValue().compareTo(right.getValue());
      }
    };

    Collections.sort(edsValues, byMapValues);

    return edsValues;
  }

  public static boolean isDigit(String text) {
    try {
      Integer.parseInt(text);
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  public static boolean notNullAndNotEmpty(String str) {
    return str != null && !str.isEmpty();
  }

  /**
   *
   * @param selectedExperiment
   * @param directoryToExportModels
   */
  public static String getProfilesUsedForSelectedExperiment(String selectedExperiment, String directoryToExportModels) {
    try {
      String exts[] = {"uml"};
      StringBuilder names = new StringBuilder();

      StringBuilder path = new StringBuilder();
      path.append(directoryToExportModels);
      path.append(selectedExperiment);
      path.append("/resources/");
      System.out.println(path.toString());
      List<File> files = (List<File>) FileUtils.listFiles(
              new File(path.toString()),
              exts, false);

      for (File file : files) {
        names.append(file.getName().toLowerCase());
        names.append(", ");
      }

      return names.deleteCharAt(names.lastIndexOf(",")).toString().trim();
    } catch (Exception e) {
      //I dont care.
    }
    return "-";
  }

  public static void createPathsOplaTool() {
    try {
      UserHome.createDefaultOplaPathIfDontExists();

      String target = UserHome.getOplaUserHome() + "application.yaml";

      //Somente copia arquivo de configuracao se
      //ainda nao existir na pasta da oplatool do usuario
      if (!(new File(target).exists())) {
        Utils.copy(PATH_CONFIGURATION_FILE, target);
      }

      UserHome.createProfilesPath();
      UserHome.createTemplatePath();
      UserHome.createOutputPath();
      UserHome.createTempPath(); //Manipulation dir. apenas para uso intenro
      
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(main.class.getName()).log(Level.SEVERE, ex.getMessage());
      System.exit(1);
    }
  }
  
  public static String generateFileName(String id) {
    String algorithmName = db.Database.getAlgoritmUsedToExperimentId(id);
    String plaName = db.Database.getPlaUsedToExperimentId(id);

    StringBuilder fileName = new StringBuilder();
    fileName.append(id);
    fileName.append("_");
    fileName.append(plaName);
    fileName.append("_");
    fileName.append(algorithmName);
    fileName.append(".txt");

    return fileName.toString();
  }

 
}
