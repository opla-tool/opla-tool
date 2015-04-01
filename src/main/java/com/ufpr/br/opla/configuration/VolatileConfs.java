package com.ufpr.br.opla.configuration;

import br.ufpr.inf.opla.patterns.strategies.scopeselection.impl.ElementsWithSameDesignPatternSelection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author elf
 *
 * Classe que guarda algumas configurações/paths que não são persistidos.
 */
public class VolatileConfs {

  private static String[] architecturesInputPath;
  private static String algorithmName; // Cambo algorithm
  private static List<String> objectiveFunctionsSelected = new ArrayList<>(); //checkboxs metrics
  private static int numberOfRuns; //numberOfRuns text-field
  private static int maxEvaluations; //maxEvaluations text-field
  private static int fieldPopulationSize; //entendeu ja neh?
  private static ElementsWithSameDesignPatternSelection scopePatterns;
  private static boolean normalized; // indica se o hypervolume deve ser calculado usando normalizacao

  public static String[] getArchitectureInputPath() {
    return architecturesInputPath;
  }

  public static void setArchitectureInputPath(String[] path) {
    architecturesInputPath = path;
  }

  public static String getAlgorithmName() {
    return algorithmName;
  }

  public static void setAlgorithmName(String algorithmName) {
    VolatileConfs.algorithmName = algorithmName;
  }

  public static String[] getArchitecturesInputPath() {
    return architecturesInputPath;
  }

  public static void setArchitecturesInputPath(String[] architecturesInputPath) {
    VolatileConfs.architecturesInputPath = architecturesInputPath;
  }

  public static List<String> getObjectiveFunctionSelected() {
    return objectiveFunctionsSelected;
  }

  public static int getNumberOfRuns() {
    return numberOfRuns;
  }

  public static void setNumberOfRuns(int i) {
    numberOfRuns = i;
  }

  public static void setMaxEvaluations(int i) {
    maxEvaluations = i;
  }

  public static void setPopulationSize(int i) {
    fieldPopulationSize = i;
  }

  public static void setScopePatterns(ElementsWithSameDesignPatternSelection scopePatterns) {
    VolatileConfs.scopePatterns = scopePatterns;
  }

  public static ElementsWithSameDesignPatternSelection getScopePatterns() {
    return scopePatterns;
  }

  /**
   * null - Random (selecinado por default na GUI)
   *
   */
  public static void configureDefaultPatternScope() {
   VolatileConfs.setScopePatterns(null);
  }

  public static boolean hypervolumeNormalized() {
    return normalized;
  }

  public static void enableHybervolumeNormalization() {
    normalized = true;
  }
  
  public static void disableHybervolumeNormalization() {
    normalized = false;
  }
}
