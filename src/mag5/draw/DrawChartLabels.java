package mag5.draw;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.function.Consumer;

import mag5.chartlabels.ChartLabel;
import mag5.chartlabels.ChartLabelType;
import mag5.util.Maths;

/** Render the names of constellations, asterisms, and the brightest stars. */
class DrawChartLabels {
  
  DrawChartLabels(List<ChartLabel> chartLabels, ChartUtil chartUtil, Projection projection, Graphics2D g){
    this.chartLabels = chartLabels;
    this.chartUtil = chartUtil;
    this.projection = projection;
    this.g = g;
  }
  
  /** Draw the labels attached to this chart. */
  void draw() {
    for(ChartLabel chartLabel : chartLabels) {
      if (projection.getBounds().isPolar()) {
        drawPolar(chartLabel);
      }
      else {
        drawEquatorial(chartLabel);
      }
    }
  }
  
  // PRIVATE
  private List<ChartLabel> chartLabels;
  private ChartUtil chartUtil;
  private Projection projection;
  private Graphics2D g;
  
  /** Center on the given coords. */
  private void drawEquatorial(ChartLabel chartLabel) {
    String text = textFor(chartLabel);
    Point2D.Double where = projection.project(chartLabel.DEC, chartLabel.RA);
    Point2D.Double whereCtr = chartUtil.centerTextOn(where.x, where.y, text, g);
    drawAccordingToType(text, chartLabel, whereCtr);
  }
  
  private void drawAccordingToType(String text, ChartLabel chartLabel, Point2D.Double whereCtr) {
    if (ChartLabelType.ASTERISM == chartLabel.TYPE) {
      chartUtil.drawTextItalic(text, whereCtr.x, whereCtr.y, g);
    }
    else if (ChartLabelType.CONSTELLATION == chartLabel.TYPE) {
      chartUtil.drawTextBold(text, whereCtr.x, whereCtr.y, g);
    }
    else {
      g.drawString(text, Maths.round(whereCtr.x), Maths.round(whereCtr.y));
    }
  }

  /** The idea is to distinguish between different types. */
  private String textFor(ChartLabel chartLabel) {
    String result = chartLabel.TEXT;
    if (ChartLabelType.CONSTELLATION == chartLabel.TYPE) {
      result = result.toUpperCase(); 
    }
    return result;
  }

  /** Rotate to be symmetric with respect to the celestial pole. Center on the given coords. */
  private void drawPolar(ChartLabel chartLabel) {
    String text = textFor(chartLabel);
    Point2D.Double target = projection.project(chartLabel.DEC, chartLabel.RA);
    double rotationAngle = chartUtil.rotationAngle(projection, target);
    Consumer<Graphics2D> drawer = x -> {
      Point2D.Double centered = chartUtil.centerTextOn(0, 0, text, g);
      drawAccordingToType(text, chartLabel, centered);
    };
    chartUtil.drawRotated(g, rotationAngle, target, drawer);
  }
}