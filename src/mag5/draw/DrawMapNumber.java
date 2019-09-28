package mag5.draw;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import mag5.util.Maths;

/** Render the chart number, in a corner of the chart. */
class DrawMapNumber {

  DrawMapNumber(Integer mapNum, double width, double height, Projection projection, Graphics2D g) {
    this.mapNum = mapNum;
    this.projection = projection;
    this.g = g;
    this.chartUtil = new ChartUtil(width, height);
    
  }
  
  /** Top chart only. Top right. */
  void draw() {
    if (projection.getBounds().isTopChart()) {
      Point2D.Double centerPoint = new Point2D.Double();
      centerPoint.x = chartUtil.percentWidth(95);
      centerPoint.y = chartUtil.percentHeight(7);
      //we need a bigger font
      Font originalFont = g.getFont();
      //the text alignment depends on the font size, so that needs to be set early
      Font biggerFont = chartUtil.resizedFont(4.0f, g);
      g.setFont(biggerFont);
      Point2D.Double drawPoint = chartUtil.centerTextOn(centerPoint.x, centerPoint.y, mapNum.toString(), g);
      g.drawString(mapNum.toString(), (int)drawPoint.x, (int)drawPoint.y);
      g.setFont(originalFont);
      
      //square border centered on the map number
      //draw from top left
      Point2D.Double squareTopLeft = new Point2D.Double();
      double side = chartUtil.percentWidth(4);
      squareTopLeft.x = centerPoint.x - side/2;
      squareTopLeft.y = centerPoint.y - side/2;
      g.drawRect(Maths.round(squareTopLeft.x), Maths.round(squareTopLeft.y), Maths.round(side), Maths.round(side));
    }
  }
  
  //PRIVATE 
  private Integer mapNum;
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
}
