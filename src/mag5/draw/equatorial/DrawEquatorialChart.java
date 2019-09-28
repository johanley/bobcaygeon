package mag5.draw.equatorial;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

import mag5.book.PdfConfig;
import mag5.chartlabels.ChartLabel;
import mag5.constellation.ConstellationLines;
import mag5.deepskyobject.DeepSkyObject;
import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.DrawChart;
import mag5.draw.Hemisphere;
import mag5.draw.Projection;
import mag5.star.Star;
import mag5.translate.Label;
import mag5.util.Maths;

/** Draw an equatorial star chart. */
public class DrawEquatorialChart extends DrawChart {
  
  public DrawEquatorialChart(
      Integer mapNum, Bounds bounds, List<Star> stars, ConstellationLines constellationLines, 
      List<DeepSkyObject> dsos, Label labels, List<ChartLabel> chartLabels, Graphics2D g) {
      super(mapNum, bounds, stars, constellationLines, dsos, labels, chartLabels, projection(bounds), g);
  }

  /** See {@link DrawEquatorialRightAscensionLines}. */
  @Override protected void drawRightAscensionGridLines() {
    DrawEquatorialRightAscensionLines raLines = new DrawEquatorialRightAscensionLines(
      chartUtil.getWidth(), chartUtil.getHeight(), projection, g
    );
    raLines.draw();
  }

  /** See {@link DrawEquatorialDeclinationLines}. */
  @Override protected void drawDeclinationGridLines() {
    DrawEquatorialDeclinationLines decLines = new DrawEquatorialDeclinationLines(
      chartUtil.getWidth(), chartUtil.getHeight(), projection, g
    );
    decLines.draw();
  }

  /** See {@link DrawEquatorialDateScale}. */
  @Override protected void drawDateScale() {
    DrawEquatorialDateScale dateScale = new DrawEquatorialDateScale(
      mapNum, labels, chartUtil, projection, g
    );
    dateScale.draw();
  }

  @Override protected void drawDueSouthAt8() {
    drawDueSouthAt8oclock(77, 5);
  }

  /** Draw the ecliptic, and East-West labels. */
  @Override protected void drawExtras() {
    DrawEcliptic ecliptic = new DrawEcliptic(chartUtil, projection, g);
    ecliptic.draw();
    drawEastWestLabels();
  }
  
  // PRIVATE
  
  private static final String EAST = "East";
  private static final String WEST = "West";
  private static boolean LEFT = true;
  private static boolean RIGHT = false;
  
  /** Swap the width and the height, since landscape.  */
  private static Projection projection(Bounds bounds) {
    return new ProjectEquatorial(bounds, PdfConfig.HEIGHT, PdfConfig.WIDTH);
  }
  
  private void drawEastWestLabels() {
    int eastWidth = Maths.round(chartUtil.percentWidth(5));
    int westWidth = Maths.round(chartUtil.percentWidth(92));
    int heightPct = 0;
    int TOP = 27;
    int BOTTOM = 74;
    if (Hemisphere.NORTH == ChartUtil.HEMISPHERE) {
      heightPct = projection.getBounds().isNorth() ? TOP: BOTTOM;
    }
    else {
      heightPct = projection.getBounds().isNorth() ? BOTTOM: TOP;
    }
    int heightPercent = Maths.round(chartUtil.percentHeight(heightPct));

    //we need a bigger font
    Font originalFont = g.getFont();
    //the text alignment depends on the font size, so that needs to be set early
    Font biggerFont = chartUtil.resizedFont(2.0f, g);
    g.setFont(biggerFont);
    g.drawString(getText(LEFT), eastWidth, heightPercent);
    g.drawString(getText(RIGHT), westWidth, heightPercent);
    g.setFont(originalFont);
  }
  
  /** 
   The labels are switched for the southern hemisphere view.
   In the north (south), east (west) is on the left side of the page. 
  */
  private String getText(boolean isLeftSide) {
    String key = "";
    if (Hemisphere.NORTH == ChartUtil.HEMISPHERE) {
      key = isLeftSide ? EAST : WEST;
    }
    else {
      key = isLeftSide ? WEST : EAST;
    }
    return  labels.text(key, ChartUtil.lang());
  }
}