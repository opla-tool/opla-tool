/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.charts;

import com.ufpr.br.opla.indicators.Indicators;
import java.awt.Color;
import java.text.NumberFormat;
import java.util.Map;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author elf
 */
public class EdBar {
  private final String[] idsAllSelectedExperiments;
  private final DefaultCategoryDataset data;
  private final String title;
  
  public EdBar(String idsAllSelectedExperiments[], String chartTitle){
    this.idsAllSelectedExperiments = idsAllSelectedExperiments;
    DefaultCategoryDataset data = createDataset();
    this.data = data;
    this.title = chartTitle;
  }

  private void configureHorizontalLines(JFreeChart chart) {
    final CategoryPlot plot = chart.getCategoryPlot();
    
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.BLACK);
  }

  private DefaultCategoryDataset createDataset() {
    DefaultCategoryDataset objDataset = new DefaultCategoryDataset();
    final XYSeriesCollection dataset = new XYSeriesCollection();

    for (int i = 0; i < idsAllSelectedExperiments.length; i++) {
      Map<String, Map<Double, Integer>> map = Indicators.quantityEdBySolutions(idsAllSelectedExperiments, idsAllSelectedExperiments[i]);

      Map.Entry<String, Map<Double, Integer>> content = map.entrySet().iterator().next();
      final XYSeries serie = new XYSeries(content.getKey());

      Map<Double, Integer> a = content.getValue();

      for (Map.Entry<Double, Integer> entry : a.entrySet()) {
        Double double1 = entry.getKey();
        Integer integer = entry.getValue();
        objDataset.addValue(integer, content.getKey(), double1);
      }

      dataset.addSeries(serie);
    }

    return objDataset;

  }

  private JFreeChart createChart(DefaultCategoryDataset chartData, String title) {
    String xLabel = "Euclidean Distance";
    String yLabel = "Number of Solutions";
    String titleChart = ((title == null)|| (title.isEmpty())) ? "Euclidean Distance" : title;
    JFreeChart chart = ChartFactory.createBarChart(titleChart, xLabel, yLabel,
            chartData,
            PlotOrientation.HORIZONTAL,
            true,
            true,
            false);
    
    configureRangeYAxis(chart);
    configureToolTip(chart);
    configureBarMarginSize(chart);
    chart.setBackgroundPaint(Color.white);
    configureHorizontalLines(chart);


    return chart;
  }

  private void configureBarMarginSize(JFreeChart chart) {
    BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
    renderer.setItemMargin(-0.5);
  }

  private void configureRangeYAxis(JFreeChart chart) {
    NumberAxis yAxis = (NumberAxis) chart.getCategoryPlot().getRangeAxis();
    yAxis.setTickUnit(new NumberTickUnit(1));
    ValueAxis rangeAxis = chart.getCategoryPlot().getRangeAxis();
    yAxis.setRange(0, rangeAxis.getUpperBound() + 0.5);
  }

  private void configureToolTip(JFreeChart chart) {
    BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
    
    renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
            "(Euclidean Value: {1}, Number Of Solutions: {2})", NumberFormat.getInstance()));
  }

  private void buildChart(DefaultCategoryDataset data, String chartTitle) {
    JFreeChart chart = createChart(data, chartTitle);

    final ChartPanel chartPanel = new ChartPanel(chart);

    JFrame frame = new JFrame(chartTitle);
    frame.add(chartPanel);

    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }
  
  public void displayOnFrame(){
     buildChart(data, title);
  }
}
