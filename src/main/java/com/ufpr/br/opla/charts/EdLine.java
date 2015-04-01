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
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author elf
 */
public class EdLine {

  private final String[] idsAllSelectedExperiments;
  private final XYDataset data;
  private final String title;

  public EdLine(String idsAllSelectedExperiments[], String chartTitle) {
    this.idsAllSelectedExperiments = idsAllSelectedExperiments;
    this.data = createDataset();
    this.title = chartTitle;
  }

  private XYDataset createDataset() {
    final XYSeriesCollection dataset = new XYSeriesCollection();

    for (int i = 0; i < idsAllSelectedExperiments.length; i++) {
      Map<String, Map<Double, Integer>> map = Indicators.quantityEdBySolutions(idsAllSelectedExperiments, idsAllSelectedExperiments[i]);

      Map.Entry<String, Map<Double, Integer>> content = map.entrySet().iterator().next();
      final XYSeries serie = new XYSeries(content.getKey());

      Map<Double, Integer> a = content.getValue();

      for (Map.Entry<Double, Integer> entry : a.entrySet()) {
        Double double1 = entry.getKey();
        Integer integer = entry.getValue();
        serie.add(double1, integer);
      }
      dataset.addSeries(serie);
    }

    return dataset;

  }

  private JFreeChart createChart(final XYDataset dataset, String title) {
    String xLabel = "Euclidean Distance";
    String yLabel = "Number of Solutions";
    String titleChart = ((title == null)|| (title.isEmpty())) ? "Euclidean Distance" : title;
    final JFreeChart chart = ChartFactory.createXYLineChart(
            titleChart,
            xLabel,
            yLabel,
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false);

    chart.setBackgroundPaint(Color.white);

    final XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);

    final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    renderer.setSeriesLinesVisible(0, true);
    renderer.setSeriesShapesVisible(1, true);
    renderer.setSeriesShapesVisible(2, true);
    plot.setRenderer(renderer);


// 	// change the auto tick unit selection to integer units only...
	final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
  
  	// change the auto tick unit selection to integer units only...
	final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
	xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    
	NumberFormat format = NumberFormat.getNumberInstance();
  format.setMaximumFractionDigits(2);
	StandardXYToolTipGenerator ttG = new StandardXYToolTipGenerator( "(Euclidean Value: {1}, Number Of Solutions: {2})", format, format);
	renderer.setBaseToolTipGenerator(ttG);
   
    

    return chart;

  }

  private void buildChart(XYDataset data, String chartTitle) {
    JFreeChart chart = createChart(data, chartTitle);

    final ChartPanel chartPanel = new ChartPanel(chart);

    JFrame frame = new JFrame(chartTitle);
    frame.add(chartPanel);

    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  public void displayOnFrame() {
    buildChart(data, title);
  }
}
