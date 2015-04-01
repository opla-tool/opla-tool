/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.indicators;

import com.ufpr.br.opla.configuration.VolatileConfs;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author elf
 */
public class HypervolumeGenerateObjsData {

  public static void generate(List<String> files) {
  }

  public static List<HypervolumeData> generate(Map<String, List<Double>> content) throws IOException {
    List<HypervolumeData> hypervolumeDatas = new ArrayList<>();

    for (Map.Entry<String, List<Double>> entry : content.entrySet()) {
      String pathToFile = entry.getKey();
      
      String[] splited = pathToFile.split("_");
      String pla = getPlaName(splited[1]);
      String algorithm = getAlgorithmName(splited[2]);
      String experimentId = getExperimentId(splited[0]);
      
      
       //Acha o ponto de referencia
      String referencePoint = findReferencePoint(entry.getValue(), experimentId);
  
       List<Double> values = ExecuteHypervolumeScript.exec(referencePoint, pathToFile);

      hypervolumeDatas.add(new HypervolumeData(experimentId, values, pla, algorithm));
    }
    
   // deleteGeneratedFiles(content);
    
    return hypervolumeDatas;
  }

  /**
   * Retorna o ponto de referência que é usado ao chamar o script em C.
   * 
   * @param values
   * @param experimentId
   * @return 
   */
  public static String findReferencePoint(List<Double> values, String experimentId) {
    int numberOfObjectives = db.Database.getNumberOfFunctionForExperimentId(experimentId);
    String ref = "";
    
    if(!VolatileConfs.hypervolumeNormalized()){
      Double max = Double.MIN_VALUE;
      
            
      for (Double double1 : values){
        if (double1 > max){
          max = double1;
        }
      }
      double point = max + 1;
      for(int i=0; i < numberOfObjectives; i++)
        ref += String.valueOf(point) + " ";

      
    }else{
       for(int i=0; i < numberOfObjectives; i++){
          ref += "1.01" + " ";
       }
    }
    
    return ref.trim();
  }


  /**
   * Deleta arquivos .txt gerados após processamento
   * 
   * @param content 
   */
  private static void deleteGeneratedFiles(Map<String, List<Double>> content) {
    for (Map.Entry<String, List<Double>> entry : content.entrySet()) {
      String pathToFile = entry.getKey();
      File f = new File(pathToFile);
      f.delete();
    }
  }

  private static String getExperimentId(String str) {
    return str.substring(str.lastIndexOf("/") + 1, str.length());
  }
  
  private static String getPlaName(String str) {
    return str.substring(str.lastIndexOf("/") + 1, str.length());
  }

  private static String getAlgorithmName(String str) {
    return str.substring(0, str.lastIndexOf("."));
  }
}
