package mag5.draw.equatorial;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.Hemisphere;
import mag5.draw.Projection;
import mag5.util.Maths;

/**
 The equatorial charts use a sinsuoidal projection.
 
 The Edmund Mag 5 Star Atlas uses the <a href='https://en.wikipedia.org/wiki/Sinusoidal_projection'>sinusoidal projection</a> 
 (it's noted in the text of the atlas).
 
 <P>The equatorial chart comes in two styles, for showing the positive or negative declinations.
 
 <P>There is significant distortion near the edge. 
 Is this more or less unavoidable, when the scale is so large?
*/
class ProjectEquatorial implements Projection {

  ProjectEquatorial(Bounds bounds, double chartWidth, double chartHeight) {
    this.chartUtil = new ChartUtil(chartWidth, chartHeight);
    
    double spineMargin = chartUtil.getSpineMargin();
    this.topChart = bounds.isTopChart();
    double centerOfProjY = topChart ? chartHeight - spineMargin : +spineMargin; //bottom/top of the chart
    this.centerOfProj = new Point2D.Double(chartWidth/2, centerOfProjY);
    
    double numDegreesHigh = bounds.maxDecDeg - bounds.minDecDeg;
    this.distancePerRad = pixelsFromZeroDecToMaxDec(chartHeight) / Maths.degToRads(numDegreesHigh);
    this.raCenter = Maths.hoursToRads(bounds.raCenterHours());
    this.raHalfWidth = Maths.hoursToRads(bounds.raHalfWidthHours());
    this.chartWidth = chartWidth;
    this.chartHeight = chartHeight;
    this.bounds = bounds;
  }
  
  @Override public Point2D.Double project(Double dec, Double ra) {
    Point2D.Double result = new Point2D.Double();
    int sign = Hemisphere.NORTH == ChartUtil.HEMISPHERE ? -1 : +1;
    result.y = centerOfProj.y + sign * dec * distancePerRad;
    double deltaRa = ra - raCenter;
    if (bounds.straddlesVernalEquinox()  && (ra < Math.PI)) {
      //the given ra is east of 0h; add 24h
      deltaRa = deltaRa + 2*Math.PI;
    }
    // x increases to the right
    // northern hem: ra increases to the left
    // southern hem: ra increases to the right
    result.x = centerOfProj.x + sign * deltaRa * distancePerRad * Math.cos(dec); 
    return result;
  }

  /**
   This method implements the clipping area as the intersection of 2 shapes. 
   One is a simple rectangle. 
   The other is a 3-sided shape: one horizontal line, and two curves representing constant right ascension, 
   going from the celestial equator to the pole.
   Intersections use Area objects: https://docs.oracle.com/javase/tutorial/2d/advanced/complexshapes.html
  */
  @Override public Shape innerBoundary() {
    Area a1 = new Area(innerRectangle(chartWidth, chartHeight));
    Area a2 = new Area(polarSector(raHalfWidth));
    a1.intersect(a2);
    return a1;
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

  // PRIVATE
  
  private double distancePerRad;
  /** The chart is for either above or below the celestial equator. */
  private boolean topChart;
  private Point2D.Double centerOfProj;
  private double chartWidth;
  private double chartHeight;
  private Bounds bounds;
  private ChartUtil chartUtil;
  
  private double raCenter; //decCenter is always 0 is this imple!
  
  /** From the center, this amount to the right and left. */
  private double raHalfWidth;
  
  /** 
   The stars are projected onto a certain area on the drawing context.
   So, the scale is calculated from the size of that area, not from the raw size of the drawing context. 
  */
  private double pixelsFromZeroDecToMaxDec(double chartHeight) {
    double result = 0.0;
    if (topChart) {
      result = chartUtil.getHeightMinusSpineMargin() - chartUtil.borderWidthPlusDateScaleWidth();
    }
    else {
      result = chartUtil.getHeightMinusSpineMargin() - chartUtil.borderWidthPlusAlphabetWidth();
    }
    return result;
  }
  
  private Rectangle2D innerRectangle(double chartWidth, double chartHeight) {
    Rectangle2D.Double result = new Rectangle2D.Double();
    if (topChart) {
      result = new Rectangle2D.Double(
        chartUtil.borderWidth(), //x 
        chartUtil.borderWidth() + chartUtil.borderWidthDateScale(), //y 
        chartWidth - chartUtil.borderWidth() * 2,  //w
        chartUtil.getHeightMinusSpineMargin() - chartUtil.borderWidth() - chartUtil.borderWidthDateScale()//h
      );
    }
    else {
      result = new Rectangle2D.Double(
        chartUtil.borderWidth(), //x 
        chartUtil.getSpineMargin(), //y
        chartWidth - chartUtil.borderWidth() * 2, //w 
        chartUtil.getHeightMinusSpineMargin() - chartUtil.borderWidth() - chartUtil.borderWidthAlphabet() //h
      );
    }
    return result;
  }
  
  private Shape polarSector(Double raHalfWidth) {
    GeneralPath result = new GeneralPath(); 
    Point2D.Double a = project(0.0, raCenter + raHalfWidth); //equator, on the left/right, outside of the drawing area
    Point2D.Double b = project(0.0, raCenter - raHalfWidth); //equator, on the right/left, "
    result.moveTo(a.x, a.y); //starting point
    result.lineTo(b.x, b.y); //line across the bottom, left to right
    equatorToPoleConstantRightAscension(raCenter - raHalfWidth, result); //right/left side 
    poleToEquatorConstantRightAscension(raCenter + raHalfWidth, result); //left/right side, back to the start
    //should I manually close the path here, to be sure? doesn't seem necessary
    return result;
  }

  /** Draw fancy curves parametrically, as usual. */
  private void equatorToPoleConstantRightAscension(double ra, GeneralPath result) {
    double poleSign = poleSign();
    double deltaTheta = deltaTheta();
    long lastIndex = Math.round(Math.PI/2.0 / deltaTheta); //the last iteration corresponds to the pole, 90 deg
    long index = 0;
    Point2D.Double point = null;
    
    while (index <= lastIndex) {
      ++index; //increase the abs mag of the dec from 0 to 90
      point = project(poleSign * index * deltaTheta, ra); 
      result.lineTo(point.x,  point.y);
    }
  }
  
  private void poleToEquatorConstantRightAscension(double ra, GeneralPath result) {
    //there's some code repetition here with the above method
    double poleSign = poleSign();
    double deltaTheta = deltaTheta();
    long lastIndex = Math.round(Math.PI/2.0 / deltaTheta); //the last iteration corresponds to the pole, 90 deg
    long index = lastIndex;
    Point2D.Double point = null;
    
    while (index >= 0) {
      --index; //decrease abs mag of the dec from 90 to 0
      point = project(poleSign * index * deltaTheta, ra); 
      result.lineTo(point.x,  point.y);
    }
  }
  
  private int poleSign() {
    int result = topChart ? +1 : -1;
    if (Hemisphere.SOUTH == ChartUtil.HEMISPHERE) {
      result = (-1) * result;
    }
    return result;
  }
  
  /** WARNING: the delta-theta needs to divide evenly into 90 degrees! */
  private double deltaTheta() {
    return Maths.degToRads(ChartUtil.DELTA_THETA_DEGS);
  }
}