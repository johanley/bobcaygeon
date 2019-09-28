package mag5.draw;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import mag5.star.Star;
import mag5.translate.Label;
import mag5.util.Maths;

/**
 Demonstrate the different symbols used to render stars of different magnitudes.
*/
class DrawMagnitudeScale {

  DrawMagnitudeScale(Label labels, double width, double height, Projection projection, Graphics2D g) {
    this.projection = projection;
    this.g = g;
    this.labels = labels;
    this.chartUtil = new ChartUtil(width, height);
  }

  /** Rendered for the top chart only. */
  void draw() {
    if (projection.getBounds().isTopChart()) {
      int LEFT_INDENT = 5; //percentages
      int DOWN_INDENT = 5;
      
      //we need a bigger font
      Font originalFont = g.getFont();
      //the text alignment depends on the font size, so that needs to be set early
      Font biggerFont = chartUtil.resizedFont(2.0f, g);
      g.setFont(biggerFont);
      g.drawString(labels.text("Magnitude", ChartUtil.lang()), Maths.round(chartUtil.percentWidth(LEFT_INDENT-1)), Maths.round(chartUtil.percentHeight(DOWN_INDENT)));
      g.setFont(originalFont);
      
      for (int mag = 0; mag < ChartUtil.LIMITING_MAG; ++mag) {
        Integer theMag = mag;
        Double magnitude = Double.valueOf(theMag.toString());
        drawMagnitudeScaleFor(magnitude, LEFT_INDENT, DOWN_INDENT);
      }
    }
  }
  
  //PRIVATE 
  private Projection projection;
  private Graphics2D g;
  private Label labels;
  private ChartUtil chartUtil;

  private void drawMagnitudeScaleFor(Double mag, int LEFT_INDENT, int DOWN_INDENT) {
    Star star = new Star();
    star.MAG = mag;
    int radius = DrawStars.starSize(star);
    Point2D.Double where = new Point2D.Double();
    double STEP = 2.5;
    double gap = STEP + STEP * mag;
    where.x = chartUtil.percentWidth(LEFT_INDENT);
    where.y = chartUtil.percentHeight(DOWN_INDENT+gap);
    //careful: uses a bounding rectangle! the xy denotes the top left.
    g.fillOval(Maths.round(where.x - radius), Maths.round(where.y - radius), radius*2/*width*/, radius*2 /*height*/);
    int tweakY = Maths.round(chartUtil.textHeight(star.MAG.toString(), g) / 2.0);
    g.drawString(star.MAG.toString(), Maths.round(chartUtil.percentWidth(LEFT_INDENT+1)), Maths.round(chartUtil.percentHeight(DOWN_INDENT+gap) + tweakY));
  }
}
