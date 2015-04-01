/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.charts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadObjectives {

  public static List<List<Double>> read(int[] columns, String filePath) throws IOException {
    String sCurrentLine;
    List<List<Double>> content = new ArrayList< >();
    
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      while ((sCurrentLine = br.readLine()) != null) {
        List<Double> objectiveValue = new ArrayList<>();
        for (int column : columns) {
          String[] line = sCurrentLine.split(",");
          objectiveValue.add(Double.parseDouble(line[column].trim()));
        }
        content.add(objectiveValue);
      }
    }
    return content;
  }
}
