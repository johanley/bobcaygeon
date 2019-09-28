package mag5.draw;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import mag5.translate.Label;
import mag5.util.Maths;

/** Render a small table of symbols used to designate deep sky objects. */
class DrawSymbolKey {
  
  public DrawSymbolKey(Label labels, ChartUtil chartUtil, Projection projection, Graphics2D g) {
    this.labels = labels;
    this.projection = projection;
    this.g = g;
    this.chartUtil = chartUtil;
  }
  
  /** Applied to the bottom chart only. */
  void draw() {
    if (!projection.getBounds().isTopChart()) {
      
      //we need a bigger font
      Font originalFont = g.getFont();
      //the text alignment depends on the font size, so that needs to be set early
      Font biggerFont = chartUtil.resizedFont(1.75f, g);
      g.setFont(biggerFont);
      g.drawString(labels.text("Symbols", ChartUtil.lang()), Maths.round(chartUtil.percentWidth(LEFT_INDENT-1)), Maths.round(chartUtil.percentHeight(DOWN_INDENT)));
      
      int size = DrawDeepSkyObject.sizeOfDso(projection);

      drawSymbolPlusText(1, size, "Open Cluster", DrawDeepSkyObject::drawOpenCluster);
      drawSymbolPlusText(2, size, "Globular Cluster", DrawDeepSkyObject::drawGlobularCluster);
      drawSymbolPlusText(3, size, "Galaxy", DrawDeepSkyObject::drawGalaxy);
      drawSymbolPlusText(4, size, "Nebula", DrawDeepSkyObject::drawNebula);
      
      g.setFont(originalFont);
    }
  }
  
  // PRIVATE 
  
  private Label labels;
  private ChartUtil chartUtil;
  private Projection projection;
  private Graphics2D g;
  
  private static final int LEFT_INDENT = 3; //percentages
  private static final int DOWN_INDENT = 80;
  private static final int GAP = 3;
  private static final int GAP_HORIZ = 1;

  @FunctionalInterface
  interface DrawSymbol {
    void drawSomething(Point2D.Double target, int size, Graphics2D g);
  }
  
  private void drawSymbolPlusText(int index, int size, String labelKey, DrawSymbol drawer) {
    Point2D.Double target = new Point2D.Double(chartUtil.percentWidth(LEFT_INDENT), chartUtil.percentHeight(DOWN_INDENT + index*GAP));
    drawer.drawSomething(target, size, g);
    Point2D.Double targetText = new  Point2D.Double(chartUtil.percentWidth(LEFT_INDENT+GAP_HORIZ), chartUtil.percentHeight(DOWN_INDENT+index*GAP));
    String text = labels.text(labelKey, ChartUtil.lang());
    Point2D.Double pText = chartUtil.centerTextVerticallyOn(targetText.x, targetText.y, text, g);
    g.drawString(text, Maths.round(pText.x), Maths.round(pText.y));
  }
}
