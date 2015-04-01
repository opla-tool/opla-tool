/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.utils;

import com.ufpr.br.opla.configuration.VolatileConfs;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;

/**
 *
 * @author elf
 */
public class Validators {

  public static boolean validateEntries(String archsInput) {
    if (archsInput.isEmpty()) {
      JOptionPane.showMessageDialog(null,
              "You need enter at least one architecture");
      return true;
    }
    String archs[] = archsInput.trim().split(",");
    List<String> invalidsEntries = new ArrayList<>();
    invalidsEntries.clear();

    for (int i = 0; i < archs.length; i++) {
      String arch;
      try {
        arch = archs[i].substring(archs[i].indexOf('.'), archs[i].length());
        if (!arch.equalsIgnoreCase(".uml")) {
          invalidsEntries.add(archs[i]);
        }
        File f = new File(archs[i]);
        if (!f.exists()) {
          throw new FileNotFoundException();
        }
      } catch (Exception e) {
        invalidsEntries.add(archs[i]);
      }

    }

    if (invalidsEntries.isEmpty()) {
      VolatileConfs.setArchitectureInputPath(archs);
    } else {
      JOptionPane.showMessageDialog(null, "The following architectures are not valid or not are found\n " + invalidsEntries.toString() + "\n\n Check it please");
    }

    if (invalidsEntries.isEmpty()) {
      return false;
    } else {
      return true;
    }
  }

  public static boolean validateCheckedsFunctions(List<JCheckBox> checkeds) {     
    HashMap<String, String> map = getMapExperimentFunctionsSelecteds(checkeds);
        
    //Checa se map contem funcoes das duas execucoes selecinadas
    //Ou seja, se o usuário selecionou funcoes das duas execuções escolhidas.
    if(((map.entrySet().size() == 1)) || (map.isEmpty()))
      return true;
    
    String first = map.entrySet().iterator().next().getValue();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      String value = entry.getValue();
      if(!value.equals(first))
        return true;
    }
   
    return false;
  }



  /**
   * Verifica se o map passado como argumento contém todos "values" iguais.
   * 
   * @param map
   * @return true if valid, false if invalid
   */
  public static boolean selectedsExperimentsHasTheSameObjectiveFunctions(HashMap<String, String[]> map) {
     String[] first = map.entrySet().iterator().next().getValue();
     boolean valid = true;
     for (Map.Entry<String, String[]> entry : map.entrySet()) {
       if(!Arrays.equals(entry.getValue(), first))
         return false;
     }
    
    return valid;
  }

  public static boolean hasMoreThatTwoFunctionsSelectedForSelectedExperiments(List<JCheckBox> allChecks) {
    HashMap<String, String> map = getMapExperimentFunctionsSelecteds(allChecks);
    for (Entry<String, String> entry : map.entrySet()) {
      if(entry.getValue().split(",").length > 2)
        return true;
    }
    
   return false;
  }
  
    private static HashMap<String, String> getMapExperimentFunctionsSelecteds(List<JCheckBox> checkeds) {
    HashMap<String, String> map = new HashMap<>();  
    
    for (JCheckBox checkBox : checkeds) {
      if (checkBox.isSelected()) {
        String experimentId = checkBox.getName().split(",")[0];
        if (map.containsKey(experimentId)) {
          String actualContent = map.get(experimentId);
          map.put(experimentId, actualContent + "," + checkBox.getName().split(",")[1]);
        } else {
          map.put(experimentId, checkBox.getName().split(",")[1]);
        }
      }
    }
    
    return map;
  }
}
