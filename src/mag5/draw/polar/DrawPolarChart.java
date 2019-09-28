package mag5.draw.polar;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

import mag5.book.PdfConfig;
import mag5.chartlabels.ChartLabel;
import mag5.constellation.ConstellationLines;
import mag5.deepskyobject.DeepSkyObject;
import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.DrawChart;
import mag5.draw.Projection;
import mag5.star.Star;
import mag5.translate.Label;

/**
 Draw the polar star chart.
*/
public class DrawPolarChart extends DrawChart  {
  
  public DrawPolarChart(
      Integer mapNum, Bounds bounds, List<Star> stars, ConstellationLines constellationLines, 
      List<DeepSkyObject> dsos, Label labels, List<ChartLabel> chartLabels, Graphics2D g) {
      super(mapNum, bounds, stars, constellationLines, dsos, labels, chartLabels, projection(bounds), g);
  }

  /** See {@link DrawPolarRightAscensionLines}. */
  @Override protected void drawRightAscensionGridLines() {
    DrawPolarRightAscensionLines raLines = new DrawPolarRightAscensionLines(
      chartUtil.getWidth(), chartUtil.getHeight(), projection, g
    );
    raLines.draw();
  }

  /** See {@link DrawPolarDeclinationLines}. */
  @Override protected void drawDeclinationGridLines() {
    DrawPolarDeclinationLines decLines = new DrawPolarDeclinationLines(
      chartUtil.getWidth(), chartUtil.getHeight(), projection, g
    );
    decLines.draw();
  }

  /** See {@link DrawPolarDateScale}. */
  @Override protected void drawDateScale() {
    DrawPolarDateScale dateScale = new DrawPolarDateScale(
      mapNum, chartUtil, labels, projection, g
    );
    dateScale.draw();
  }

  @Override protected void drawDueSouthAt8() {
    drawDueSouthAt8oclock(80, 50);
  }

  /** Draw a title, and a remark about which charts are for observers in the northern/southern hemisphere. */
  @Override protected void drawExtras() {
    drawTitle();
    northernVsSouthern();
  }

  // PRIVATE
  
  /** Swap the width and the height, since landscape.  */
  private static Projection projection(Bounds bounds) {
    return new ProjectPolar(bounds, PdfConfig.HEIGHT, PdfConfig.WIDTH);
  }
  
  private void drawTitle() {
    if (projection.getBounds().isTopChart()) {
      boolean isNorth = projection.getBounds().isNorth();
      String key = isNorth ? "north" : "south";
      key = key + "-celestial-pole";
      String text = labels.text(key, ChartUtil.lang());
      double y = chartUtil.percentHeight(5);
      double x = chartUtil.percentWidth(45);
      Point2D.Double ctr = chartUtil.centerTextOn(x, y, text, g);
      chartUtil.drawTextFontSize(text, ctr.x, ctr.y, 2.0f, g);
    }
  }
  
  private void northernVsSouthern() {
    if (projection.getBounds().isTopChart()) {
      boolean isNorth = projection.getBounds().isNorth();
      String key = isNorth ? "northern" : "southern";
      key = key + "-charts";
      String text = labels.text(key, ChartUtil.lang());
      double y = chartUtil.percentHeight(10);
      double x = chartUtil.percentWidth(40);
      Point2D.Double ctr = chartUtil.centerTextOn(x, y, text, g);
      chartUtil.drawTextFontSize(text, ctr.x, ctr.y, 2.0f, g);
    }
  }
}