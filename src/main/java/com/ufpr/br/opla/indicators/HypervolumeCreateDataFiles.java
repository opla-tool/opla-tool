/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.indicators;

import com.ufpr.br.opla.configuration.UserHome;
import com.ufpr.br.opla.utils.Utils;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import results.Execution;
import results.FunResults;

/**
 *
 * @author elf
 * 
 * NAO Usada Mais. Deletar
 */
public class HypervolumeCreateDataFiles {

  /**
   * Dado um conjunto de ids (experiments). Deve gerar os arquivos
   * correspondentes para o hypervolume
   * 
   * TODO Validação: Os experimentos passados como argumento devem ter o mesmo número de rodadas e objetivos. ??
   */
  public Map<String, List<Double>> generateHyperVolumeFiles(String... ids) throws IOException {

    //Usado temporariamente. Após cálculos estes arquivos serão apagados.
    String pathToSaveFiles = UserHome.getOplaUserHome();
   
    Map<String, List<Double>> fileToContent = new HashMap<>();

    for (String id : ids) {
      String nameFile = (pathToSaveFiles + Utils.generateFileName(id)).replaceAll("\\s+","");
      
      try (PrintWriter pw = new PrintWriter(new FileWriter(nameFile))) {
        List<Double> values = new ArrayList<>();
        for (Execution execution : db.Database.getAllExecutionsByExperimentId(id)) {
         
          for (FunResults fun : execution.getFuns()) {
            String o = fun.getObjectives().trim().replace("|", " ");
            String[] ov = o.split(" ");

            for (int i = 0; i < ov.length; i++)
              values.add(Double.parseDouble(ov[i]));
            
            pw.write(o);
            pw.write("\n");
          }
          
          pw.write("\n");
          pw.write("\n");
        }
        fileToContent.put(nameFile, values);
      }
    }
    
    return fileToContent;

  }
}
