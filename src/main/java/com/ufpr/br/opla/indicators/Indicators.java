/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.indicators;

import database.Database;
import exceptions.MissingConfigurationException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections4.map.HashedMap;

/**
 *
 * @author elf
 */
public class Indicators {

  /**
   * Only for non dominated solutions
   *
   */
  public static SortedMap<String, Double> getEdsForExperiment(String experimentID) {

    try {
      try (Statement statement = database.Database.getConnection().createStatement()) {
        SortedMap<String, Double> results = new TreeMap();

        StringBuilder query = new StringBuilder();
        query.append("SELECT ed, solution_name FROM distance_euclidean WHERE experiment_id = ");
        query.append(experimentID);

        ResultSet result = statement.executeQuery(query.toString());

        while (result.next()) {
          results.put(result.getString("solution_name"), result.getDouble("ed"));
        }
        statement.close();

        return results;
      }



    } catch (MissingConfigurationException | ClassNotFoundException | SQLException ex) {
      Logger.getLogger(Indicators.class.getName()).log(Level.SEVERE, null, ex);
    }

    return null;
  }

  public static String getEdForSelectedSolution(String fileName, String experimentID) {
    try {
      String ed;
      try (Statement statement = database.Database.getConnection().createStatement()) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ed FROM distance_euclidean WHERE experiment_id = ");
        query.append(experimentID);
        query.append(" AND solution_name ='");
        query.append(fileName.substring(0, fileName.length() - 4));
        query.append("'");
        ResultSet result = statement.executeQuery(query.toString());
        ed = result.getString("ed");
      }
    return ed;

    } catch (MissingConfigurationException | ClassNotFoundException | SQLException ex) {
      Logger.getLogger(Indicators.class.getName()).log(Level.SEVERE, null, ex);
    }

    return "0.0";

  }
  
  public static Entry<String, Double> getSolutionWithBestTradeOff(String experimentId) {

    SortedMap<String, Double> eds = getEdsForExperiment(experimentId);
    Double ed = Double.MAX_VALUE;
    Entry<String, Double> solution = null;
    
    for(Map.Entry<String, Double> entry : eds.entrySet()){
     if(entry.getValue() < ed){
       ed = entry.getValue();
       solution = entry;
     }
    }  
    
    return solution;
  }
  
  /**
   * Returns only eds for non-dominated solutions.
   * 
   * @param ids
   * @return array of double in "asc" order
   */
  public static double[] getAllEdsForExperiments(String... ids) {
    ArrayList<Double> eds = new ArrayList<>();
    
    for (String id : ids) {
      SortedMap<String, Double> values = getEdsForExperiment(id);

      for (Entry<String, Double> entry : values.entrySet())
        eds.add(entry.getValue());
    }
    
    double[] edsAsc = new double[eds.size()];
    
    for (int i = 0; i < edsAsc.length; i++)
      edsAsc[i] = eds.get(i);

    Arrays.sort(edsAsc);

    return edsAsc;
  }
   
  /**
   * Retorna a quantidade de soluções encontrads por valor de ED.
   * 
   * @param selectedExperiments - Experimentos que se deseja "consultar".
   * @param experimentId - Experimento que deseja-se buscar por EDS.
   * 
   * @return 
   */
  public static Map<String, Map<Double, Integer>> quantityEdBySolutions(String[] selectedExperiments, String experimentId){
    
    double[] allEds = Indicators.getAllEdsForExperiments(selectedExperiments);
    Map<Double, Integer> map = new  TreeMap<>();
    
    Map<String, Map<Double, Integer>> algoritmNameToEds = new HashedMap<>();
    
    Statement statement = null;
    String algorithmName = db.Database.getAlgoritmUsedToExperimentId(experimentId);
    
    try {
      statement = database.Database.getConnection().createStatement();

      for (int i = 0; i < allEds.length; i++) {

        StringBuilder query = new StringBuilder();
        query.append("SELECT count(*) FROM distance_euclidean WHERE ed = ");
        query.append(allEds[i]);
        query.append(" AND experiment_id = ");
        query.append(experimentId);

        ResultSet r = statement.executeQuery(query.toString());
        Integer n = Integer.parseInt(r.getString("count(*)"));

        if (n != null && n != 0) {
          int currentValue = map.get(allEds[i]) == null ? 0 : map.get(allEds[i]);
          map.put(allEds[i], +currentValue + n);
        } else {
          map.put(allEds[i], 0);
        }
        
      }
 
    } catch (SQLException | MissingConfigurationException | ClassNotFoundException ex) {
      Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
    } finally {
      try {
        statement.close();
      } catch (SQLException ex) {
        Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    algoritmNameToEds.put(algorithmName, map);
    return algoritmNameToEds;
   
  }
    
  
}