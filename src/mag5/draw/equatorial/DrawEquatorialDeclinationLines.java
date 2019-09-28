package mag5.draw.equatorial;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.Hemisphere;
import mag5.draw.Projection;
import mag5.util.Maths;

/** The declination lines for an equatorial chart. */
class DrawEquatorialDeclinationLines {

  DrawEquatorialDeclinationLines(double width, double height, Projection projection, Graphics2D g) {
    this.projection = projection;
    this.g = g;
    this.chartUtil = new ChartUtil(width, height);
  }
  
  /** Draw the grid lines for declination. */
  public void draw() {
    drawDecGridLines();
  }
  
  // PRIVATE
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  
  private static double TEN_DEGREES = 10.0;
  private static double FIVE_DEGREES = 5.0;
  private static double ONE_DEGREE = Maths.degToRads(1.0);

  /**
   Draw them on the sides and in the middle, in 1-degree increments.
   Every 10 degrees has a large horizontal line across the whole chart.
   The gridlines consist of wee horizontal lines and numbers. 
   The numbers include the sign of the declination + or - in all cases.
   I'm using projections that have the declination evenly spaced on all charts.
  
   The numbers are not drawn in the middle of the chart:
   <ul> 
    <li>not really necessary 
    <li>sometimes they collide with stars/text/constellation lines
   </ul>  
  */
  private void drawDecGridLines() {
    //go up the left/right side of the chart, in steps of 1 degree
    Bounds bounds = projection.getBounds();
    double raMax = Maths.hoursToRads(bounds.maxRaHours);
    double maxDec = Maths.degToRads(bounds.maxDecDeg);
    int Δ = Maths.round(chartUtil.percentWidth(ChartUtil.EQUATORIAL_CHART_TICK_SIZE)); 
    double dec = Maths.degToRads(bounds.minDecDeg); //start with min
    double decLimit = Math.abs(Math.round(Maths.degToRads(bounds.decFurthestFromEq()))); // 60
    while (dec <= maxDec + ChartUtil.ε)  {
      Point2D.Double point = projection.project(dec, raMax);
      if (point.x < chartUtil.borderWidth()) {
        //COERCE: it's off the edge, too far to the left; push it over to the left-border
        point.x = chartUtil.borderWidth();
      }
      if (point.x > chartUtil.getWidth() - chartUtil.borderWidth()) {
        //COERCE: it's off the edge, too far to the right; push it over to the right-border
        point.x = chartUtil.getWidth() - chartUtil.borderWidth();
      }
      double xleft = point.x - Δ;
      double xright = chartUtil.getWidth() - xleft;
      if (chartUtil.isNearMultipleOf(TEN_DEGREES, dec) && Math.abs(dec) > ChartUtil.ε) {
        if (Math.abs(dec) < decLimit) {
          //complete line across the chart, BUT, the color is not uniform
          //so, we need to draw it in 3 parts, with different colors
          horizontalLine(xleft, xleft + Δ, point.y, g); //left
          horizontalLine(xright, xright - Δ, point.y, g); //right
          chartUtil.drawGrey(g, a -> horizontalLine(xleft + Δ, xright - Δ, point.y, a)); //middle
        }
        declinationNumber(dec, xleft, point.y, g, chartUtil.getWidthInt());
        declinationNumber(dec, xright, point.y, g, chartUtil.getWidthInt());
      } 
      else {
        // 3 ticks (left, center, right), and sometimes a number 
        // this is done for dec = 0 too
        horizontalLine(xleft, xleft + Δ, point.y, g); //left
        horizontalLine(xright, xright - Δ, point.y, g); //right
        
        double center = chartUtil.getWidth()/2.0;
        chartUtil.drawGrey(g, a -> horizontalLine(center - Δ/2, center + Δ/2, point.y, a)); //middle
        if (chartUtil.isNearMultipleOf(FIVE_DEGREES, dec)) {
          declinationNumber(dec, xleft, point.y, g, chartUtil.getWidthInt());
          declinationNumber(dec, xright, point.y, g, chartUtil.getWidthInt());
        }
      }
      dec = dec + ONE_DEGREE;
    }
  }
  
  private void horizontalLine(double from, double to, double y, Graphics2D g) {
    GeneralPath path = new GeneralPath();
    path.moveTo(from, y);
    path.lineTo(to, y); 
    g.draw(path);
  }
  
  /** 
   Center text vertically on the given y-level. 
   Align either to the right or left of the given point.
   The sign appears only for negative values. 
  */
  private void declinationNumber(Double dec, double x, double y, Graphics2D g, int chartWidth) {
    //if (Math.abs(dec) > ChartUtil.ε) {
      String text = Long.valueOf(Math.round(Maths.radsToDegs(dec))).toString()+ "°" ;
      double textWidth = chartUtil.textWidth(text + "a", g); //the extra letter 'a' is padding
      double multiplier =  0.0;
      //silly positional tweaks
      if (Hemisphere.NORTH == ChartUtil.HEMISPHERE) {
        multiplier =  x < chartWidth/2.0 ? -2.0 : +0.3;
      }
      else {
        multiplier =  x < chartWidth/2.0 ? -2.5 : +1.0;
      }
      Point2D.Double pText = chartUtil.centerTextVerticallyOn(x, y, text, g);
      g.drawString(text, Maths.round(pText.x + (multiplier*textWidth/2)), Maths.round(pText.y));
    }
  //}
}