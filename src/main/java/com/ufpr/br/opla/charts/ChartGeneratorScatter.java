/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.charts;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.TickUnitSource;

public class ChartGeneratorScatter {

  private String name;
  private final XYSeriesCollection dataset = new XYSeriesCollection();
  private String xAxisLabel;
  private String yAxisLabel;

  /**
   *
   *
   * @param chartName - Chart Name
   * @param xLabel - Axis X Label Name
   * @param yLabel - Axis Y Label Name
   */
  public ChartGeneratorScatter(String chartName, String xLabel, String yLabel) {
    this.name = chartName;

    this.xAxisLabel = getNameFunction(xLabel);
    this.yAxisLabel = getNameFunction(yLabel);
  }

  /**
   * Set a HashMap of series
   *
   * @param series
   */
  public void setDataSet(HashMap<String, List<XYDataItem>>... series) {
    for (HashMap<String, List<XYDataItem>> mapValue : series) {
      for (Entry<String, List<XYDataItem>> entry : mapValue.entrySet()) {
        String serieName = entry.getKey(); // Exemplo NSGA-II

        final XYSeries serie = new XYSeries(serieName);
        List<XYDataItem> value = entry.getValue();
        for (XYDataItem xyValues : value) {
          serie.add(xyValues.getX(), xyValues.getY());
        }

        this.dataset.addSeries(serie);
      }
    }
  }

  public ChartPanel plot() {
    if (this.dataset.getSeries().isEmpty()) {
      throw new ExceptionInInitializerError("dataset not initialized. Call .setDataSet before cal this method");
    }

    final JFreeChart chart = createChart();
    final ChartPanel chartPanel = new ChartPanel(chart);
    return chartPanel;
  }

  private JFreeChart createChart() {
    final JFreeChart chart = ChartFactory.createScatterPlot("", this.xAxisLabel, this.yAxisLabel, this.dataset,
            PlotOrientation.VERTICAL, true, true, false);

    custonConfs(chart);

    return chart;
  }

  private void custonConfs(final JFreeChart chart) {
    chart.setBackgroundPaint(Color.white);

    final XYPlot plot = chart.getXYPlot();
    chart.getTitle().setPaint(Color.RED);

    Font font3 = new Font("Dialog", Font.PLAIN, 20);
    Font title = new Font("Dialog", Font.PLAIN, 23);

    chart.getTitle().setFont(title);

    plot.getDomainAxis().setLabelFont(font3);
    plot.getRangeAxis().setLabelFont(font3);



    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);

    // set a few custom plot features
    plot.setBackgroundPaint(new Color(0xffffe0));
    plot.setDomainGridlinesVisible(true);
    plot.setDomainGridlinePaint(Color.lightGray);
    plot.setRangeGridlinePaint(Color.lightGray);

    final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

    final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createStandardTickUnits());
    rangeAxis.setTickLabelFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 18));
    plot.getDomainAxis().setTickLabelFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 18));

    // LegendTitle lt = chart.getLegend();
    // 
// set the plot's axes to display integers
        TickUnitSource ticks = NumberAxis.createIntegerTickUnits();
        
        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setStandardTickUnits(ticks);
        
//        NumberAxis range = (NumberAxis) plot.getRangeAxis();
//        range.setStandardTickUnits(ticks);
        
        
        
        
    NumberFormat format = NumberFormat.getNumberInstance();
    format.setMaximumFractionDigits(3);
    StandardXYToolTipGenerator ttG = new StandardXYToolTipGenerator("({1},{2})", format, format);
    renderer.setBaseToolTipGenerator(ttG);
 


    LegendTitle lt = new LegendTitle(plot);
    lt.setItemFont(new Font("Dialog", Font.PLAIN, 9));
    lt.setBackgroundPaint(new Color(200, 200, 255, 100));
    lt.setPosition(RectangleEdge.BOTTOM);
    lt.setItemFont(new Font("Verdana", Font.PLAIN, 15));  
      

    XYTitleAnnotation xyta = new XYTitleAnnotation(0.98, 0.98, lt,RectangleAnchor.TOP_RIGHT);
    plot.addAnnotation(xyta);

    chart.removeLegend();
    ChartStyles styles = new ChartStyles();



    plot.setRenderer(renderer);
    styles.setSeriesStyle(chart, 0, "dash");
    styles.setSeriesStyle(chart, 1, "dash");
  }

  private String getNameFunction(String xLabel) {
    if ("conventional".equalsIgnoreCase(xLabel)) {
      return "CM";
    }
    if ("featureDriven".equalsIgnoreCase(xLabel)) {
      return "FM";
    }if ("plaExtensibility".equalsIgnoreCase(xLabel)) {
      return "EXT";
    } else {
      return "";
    }
  }

  public XYSeriesCollection getDataSet() {
    return this.dataset;
  }
}
