package db;

import java.util.Map.Entry;
import java.util.*;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import logs.log_log.Logger;
import metrics.Conventional;
import metrics.Elegance;
import metrics.FeatureDriven;
import metrics.PLAExtensibility;

public class BestSolutionBySelectedFitness {

  public static List<metrics.Metrics> calculateBestFeatureDriven(String experimentId) {
    return db.Database.getAllFeatureDrivenMetricsForExperimentId(experimentId);
  }

  public static List<metrics.Metrics> calculateBestElegance(String experimentId) {
    return db.Database.getAllEleganceMetricsForExperimentId(experimentId);
    
  }

  public static List<metrics.Metrics> calculateBestConventional(String experimentId) {
    return db.Database.getAllConventionalMetricsForExperimentId(experimentId);
    
  }

  public static List<metrics.Metrics> calculateBestPlaExt(String experimentId) {
    return db.Database.getAllPLAExtMetricsForExperimentId(experimentId);
  }

  public static void buildTable(JTable tableMinorFitnessValues, List<metrics.Metrics> map) {
    
    Object[][] data = new Object[map.size()][map.size()];
      
    for (int i = 0; i < map.size(); i++) {
      data[i] = new String[] {db.Database.getNameSolutionById(map.get(i).getIdSolution()), String.valueOf(getValueFitness(map.get(i)))};
    }
    
    String columnNames[] = { "Solution Name", "Value" };
    
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override
      public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    
     tableMinorFitnessValues.setModel(model);
     TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
     tableMinorFitnessValues.setRowSorter(sorter);

  }

  public static void buildTableObjectives(JTable tableObjectives, HashMap<String, String> result) {
    DefaultTableModel model = new DefaultTableModel();

    model.addColumn("Objective");
    model.addColumn("Value");
    tableObjectives.setModel(model);

    Iterator<Entry<String, String>> it = result.entrySet().iterator();
    while (it.hasNext()) {
      Object[] row = new Object[2];
      Map.Entry<String, String> pairs = (Map.Entry<String, String>) it.next();
      row[1] = pairs.getValue();
      row[0] = pairs.getKey();
      it.remove(); // evitar ConcurrentModificationException
      model.addRow(row);
    }

    tableObjectives.updateUI();
  }

  private static SortedMap<String, metrics.Metrics> buildMapElegance(List<Elegance> data) {
    SortedMap<String, metrics.Metrics> map = new TreeMap<>();
    for (int i = 0; i < data.size(); i++) {
      Elegance elegance = data.get(i);
      map.put(elegance.getIdSolution(), elegance);
    }
    return map;
  }

  private static SortedMap<String, metrics.Metrics> buildMapConventional(List<Conventional> data) {
    SortedMap<String, metrics.Metrics> map = new TreeMap();
    for (int i = 0; i < data.size(); i++) {
      Conventional conventional = data.get(i);
      map.put(conventional.getIdSolution(), conventional);
    }
    return map;
  }

  private static SortedMap<String, metrics.Metrics> buildMapFeatureDriven(List<FeatureDriven> data) {
    SortedMap<String, metrics.Metrics> map = new TreeMap();
    for (int i = 0; i < data.size(); i++) {
      FeatureDriven fd = data.get(i);
      map.put(fd.getIdSolution(), fd);
    }
    return map;
  }

  private static SortedMap<String, metrics.Metrics> buildMapPLAExt(List<PLAExtensibility> data) {
    SortedMap<String, metrics.Metrics> map = new TreeMap();
    for (int i = 0; i < data.size(); i++) {
      PLAExtensibility plaExt = data.get(i);
      map.put(plaExt.getIdSolution(), plaExt);
    }
    return map;
  }

  private static Double getValueFitness(metrics.Metrics f) {
    if(f instanceof Conventional){
      return ((Conventional) f).evaluateMACFitness();
    }else if(f instanceof FeatureDriven){
      return ((FeatureDriven) f).evaluateMSIFitness();
    }else if(f instanceof PLAExtensibility){
      return ((PLAExtensibility) f).getPlaExtensibility();
    }else if(f instanceof Elegance){
      return ((Elegance) f).evaluateEleganceFitness();
    }else{
      Logger.getLogger().putLog("I dont know " + f.getClass().getName());
      throw new IllegalArgumentException("I dont know " + f.getClass().getName());
    }
  }


}
