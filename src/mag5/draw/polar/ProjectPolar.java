package mag5.draw.polar;

import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.Projection;
import mag5.util.Maths;

/** 
 <a href='https://en.wikipedia.org/wiki/Azimuthal_equidistant_projection'>Equidistant projection</a> for the polar charts.
 
 Note that right ascension increases clockwise (anticlockwise) for the north pole (south pole).
 
 This implementation puts 0h to the right for the south pole, but 2h to the right for the north pole.
 This is only to avoid cutting off certain things across the middle.
*/
class ProjectPolar implements Projection {

   ProjectPolar(Bounds bounds, double chartWidth, double chartHeight) {
     this.chartUtil = new ChartUtil(chartWidth, chartHeight);
     this.chartWidth = chartWidth;
     this.chartHeight = chartHeight;
     double spineMargin = chartUtil.getSpineMargin();
     double centerOfProjY = bounds.isTopChart() ? chartHeight - spineMargin : +spineMargin; //bottom/top of the chart
     this.centerOfProj = new Point2D.Double(chartWidth/2, centerOfProjY);
     double numDegreesHigh = bounds.maxDecDeg - bounds.minDecDeg;
     this.distancePerRad = distanceFromMinDecToMaxDec() / Maths.degToRads(numDegreesHigh);
     this.bounds = bounds;
   }
   
   @Override public Point2D.Double project(Double dec, Double ra) {
     Point2D.Double result = new Point2D.Double();
     int sign = bounds.isNorth() ? +1 : -1; //north pole or south pole! not top-chart or bottom-chart.
     double HALF_PI = Math.PI/2.0;
     /** 
      For the north pole, we take 2h as being the rightmost ra; this is only to avoid cutting important things in half, 
      between the top page and the bottom page. 
     */
     double TWO_HOURS = Math.PI/6.0;
     double rho = (HALF_PI - sign * dec) * distancePerRad; //pixels away from the 'pole position'
     double theta = bounds.isNorth() ? ra - TWO_HOURS : ra; //angle away from the RIGHTMOST part of the circle
     
     double dx = rho * Math.cos(theta); // pixels from the center of proj
     double dy = sign * rho * Math.sin(theta); // pixels "
     
     result.x = centerOfProj.x + dx;
     result.y = centerOfProj.y + dy;    
     
     return result;
   }

   /** Half-moon shape. */
   @Override public Shape innerBoundary() {
     return halfMoon(chartWidth/2.0 - totalBorderWidth(), Arc2D.Double.PIE);
   }
   
   @Override public Bounds getBounds() {
     return bounds;
   }
   
   @Override public Double distancePerRad() {
    return distancePerRad;
   }
   
   @Override public Point2D.Double centerOfProj() {
     return centerOfProj;
  }

  /**
   A circle cut in two (arc plus line).
   Top chart: straight line across the bottom.
   Bottom chart: straight line across the top.
   */
  Shape halfMoon(double radius, int type) {
     double x = centerOfProj.getX() - radius;
     double y = bounds.isTopChart() ? chartHeight - chartUtil.getSpineMargin() - radius : -radius + chartUtil.getSpineMargin();
     double w = 2*radius;
     double h = w; //always a circular arc
     double start = bounds.isTopChart() ? 0 : 180;
     Arc2D.Double result = new Arc2D.Double(x, y, w, h, start, 180, /*Arc2D.Double.PIE*/type);
     return result;
   }
  
   // PRIVATE
   
   /** From the center, this number of points/pixels from the center (pole) to the circumference. */
   private double distancePerRad;
   private Point2D.Double centerOfProj;
   private double chartWidth;
   private double chartHeight;
   private Bounds bounds;
   private ChartUtil chartUtil;
   
   private double totalBorderWidth() {
     return chartUtil.borderWidthPlusDateScaleWidth();
   }
   
   /** 
    The stars are projected onto a certain area on the image.
    So, the scale is calculated from the size of that area.
   */
   private double distanceFromMinDecToMaxDec() {
     double result = chartWidth/2.0 - totalBorderWidth();
     return result;
   }
}