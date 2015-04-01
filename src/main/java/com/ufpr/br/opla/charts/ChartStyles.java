/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ufpr.br.opla.charts;

import java.awt.BasicStroke;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;

/**
 *
 * @author elf
 */
public class ChartStyles {
  
    /** Line style: line */
    public static final String STYLE_LINE = "line";
    /** Line style: dashed */
    public static final String STYLE_DASH = "dash";
    /** Line style: dotted */
    public static final String STYLE_DOT = "dot";

    /**
    * Convert style string to stroke object.
    * 
    * @param style One of STYLE_xxx.
    * @return Stroke for <i>style</i> or null if style not supported.
    */
   private BasicStroke toStroke(String style) {
        BasicStroke result = null;
        
        if (style != null) {
            float lineWidth = 3.0f;
            float dash[] = {3.0f};
            float dot[] = {lineWidth};
    
            if (style.equalsIgnoreCase(STYLE_LINE)) {
                result = new BasicStroke(lineWidth);
            } else if (style.equalsIgnoreCase(STYLE_DASH)) {
                result = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f);
            } else if (style.equalsIgnoreCase(STYLE_DOT)) {
                result = new BasicStroke(lineWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f, dot, 0.0f);
            }
        }//else: input unavailable
        
        return result;
    }//toStroke()

    /**
     * Set color of series.
     * 
     * @param chart JFreeChart.
     * @param seriesIndex Index of series to set color of (0 = first series)
     * @param style One of STYLE_xxx.
     */
    public void setSeriesStyle(JFreeChart chart, int seriesIndex, String style) {
        if (chart != null && style != null) {
            BasicStroke stroke = toStroke(style);
            
            Plot plot = chart.getPlot();
            if (plot instanceof CategoryPlot) {
                CategoryPlot categoryPlot = chart.getCategoryPlot();
                CategoryItemRenderer cir = categoryPlot.getRenderer();
                try {
                    cir.setSeriesStroke(seriesIndex, stroke); //series line style
                } catch (Exception e) {
                    System.err.println("Error setting style '"+style+"' for series '"+seriesIndex+"' of chart '"+chart+"': "+e);
                }
            } else if (plot instanceof XYPlot) {
                XYPlot xyPlot = chart.getXYPlot();
                XYItemRenderer xyir = xyPlot.getRenderer();
                try {
                    xyir.setSeriesStroke(seriesIndex, stroke); //series line style
                } catch (Exception e) {
                    System.err.println("Error setting style '"+style+"' for series '"+seriesIndex+"' of chart '"+chart+"': "+e);
                }
            } else {
                System.out.println("setSeriesColor() unsupported plot: "+plot);
            }
        }
    }
  
}
