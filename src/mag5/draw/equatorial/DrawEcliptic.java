package mag5.draw.equatorial;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import mag5.draw.ChartUtil;
import mag5.draw.Projection;
import mag5.util.Maths;

/** Render the great circle representing the ecliptic. */
class DrawEcliptic {

  DrawEcliptic(ChartUtil chartUtil, Projection projection, Graphics2D g){
    this.chartUtil = chartUtil;
    this.projection = projection;
    this.g = g;
  }
  
  /**
   Draw a dashed line for the ecliptic. Equatorial charts only. 
   Show a wee tick-mark indicating the start of a month. 
  */
  public void draw() {
    polylineForEcliptic();
    tickMarkForStartOfEachMonth();
  }

  // PRIVATE 
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  
  /** 2019.0 value from the Observer's Handbook */
  private static double OBLIQUITY = Maths.degToRads(23.4368); 
  
  /** Polyline for a large chunk of the ecliptic, with clipping on. */
  private void polylineForEcliptic() {
    Range rangeLambda = rangeLambda();
    GeneralPath path = new GeneralPath();
    double lambda = rangeLambda.start;
    Point2D.Double point = onEcliptic(lambda);
    path.moveTo(point.getX(), point.getY());
    //move in small steps around the whole ecliptic, increasing ecliptic longitude
    double Δ = Maths.degToRads(ChartUtil.DELTA_THETA_DEGS); 
    while (lambda <= rangeLambda.end) {
      lambda = lambda + Δ;
      point = onEcliptic(lambda);
      path.lineTo(point.getX(), point.getY());
    }
    
    chartUtil.clippingOn(projection, g);
    //dashed line https://docs.oracle.com/javase/tutorial/2d/geometry/strokeandfill.html
    float dash1[] = {3.0f};
    BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, dash1, 0.0f);
    Stroke regularStroke = g.getStroke();
    g.setStroke(dashed);
    g.draw(path);
    g.setStroke(regularStroke);
    chartUtil.clippingOff(g);
  }
  
  /** 
   Point on the ecliptic, for the given ecliptic longitude. 
   Convert to right ascension and declination, then project to x-y coords. 
  */ 
  private Point2D.Double onEcliptic(double lambda) {
    double ra = Math.atan2(Math.cos(OBLIQUITY) * Math.sin(lambda) , Math.cos(lambda)); 
    ra = Maths.in2pi(ra);
    double dec = Math.asin(Math.sin(OBLIQUITY) * Math.sin(lambda)); //-90..+90
    Point2D.Double result = projection.project(dec, ra);
    return result;
  }
  
  /**
   Along the great circle representing the ecliptic, put a wee hash mark denoting the approximate 
   start of each month, perpendicular to the great circle.
   PROBLEM: the tick in Cancer is on top of a star, and so is invisible! 
   So, I cheat and tweak it a little.
  */
  private void tickMarkForStartOfEachMonth() {
    List<Double> solarLongitudes = solarLongitudeStartOfMonth();
    double SMALL_BETA = Maths.degToRads(0.50);
    for (Double solarLong : solarLongitudes) {
      Double solarLongRads = Maths.degToRads(solarLong);
      PosEquatorial start = tickPos(solarLongRads, SMALL_BETA);
      PosEquatorial end = tickPos(solarLongRads, -SMALL_BETA);
      Point2D.Double startPoint = projection.project(start.delta, start.alpha);
      Point2D.Double endPoint = projection.project(end.delta, end.alpha);
      //draw a line between the points; use paths to avoid int's
      GeneralPath path = new GeneralPath();
      path.moveTo(startPoint.getX(), startPoint.getY());
      path.lineTo(endPoint.getX(), endPoint.getY());
      chartUtil.clippingOn(projection, g);
      g.draw(path);
      chartUtil.clippingOff(g);
    }
  }
  
  /** Input in rads, output in rads. */
  private PosEquatorial tickPos(Double lambda, Double beta) {
    PosEcliptic pos = new PosEcliptic();
    pos.beta = beta;
    pos.lambda = lambda;
    return transform(pos);
  }

  /**
   The ecliptic solar longitude at the start of each month, in sequence, in degrees.
   These numbers vary from year to year. Don't take them too seriously.
   Use them as representative numbers.
   Source: https://www.imo.net/resources/solar-longitude-tables/solar-longitudes-2018/
  */
  private List<Double> solarLongitudeStartOfMonth(){
    List<Double> result = new ArrayList<Double>();
    result.add(280.265); // Jan 1
    result.add(311.820);
    result.add(340.099);
    result.add(10.967); //April 1
    result.add(40.333);
    result.add(70.224);
    result.add(98.884);
    result.add(/*128.467*/ 128.25); //Aug 1 is on top of a star in Cancer! Fudge it!
    result.add(158.268);
    result.add(187.513);
    result.add(218.253);
    result.add(248.471);
    return result;
  }
  
  private class PosEcliptic {
    Double beta;
    Double lambda;
  }
  
  private class PosEquatorial {
    Double alpha;
    Double delta;
  }
  
  /** Input is in rads, output is in rads. */
  private PosEquatorial transform(PosEcliptic posEcliptic) {
    PosEquatorial result = new PosEquatorial();
    
    double numerator = Math.sin(posEcliptic.lambda) * Math.cos(OBLIQUITY) - Math.tan(posEcliptic.beta) * Math.sin(OBLIQUITY);
    double denominator = Math.cos(posEcliptic.lambda);
    double alpha = Math.atan2(numerator, denominator); //-pi..+pi
    alpha = Maths.in2pi(alpha); //0..2pi
    
    double sinDelta = Math.sin(posEcliptic.beta) * Math.cos(OBLIQUITY) + Math.cos(posEcliptic.beta) * Math.sin(OBLIQUITY) * Math.sin(posEcliptic.lambda);
    double delta = Math.asin(sinDelta);
    
    result.delta = delta;
    result.alpha = alpha;
    return result;
  }
  
  /**
   It's important to restrict the ecliptic path to some nearby range. 
   If you do the whole circle, the projection freaks out.
   As a representative point, use the ctr's alpha, on the celestial equator.
   Get its lambda, plus/minus N degrees. 
  */
  private Range rangeLambda() {
    double ctrRa = Maths.hoursToRads(projection.getBounds().raCenterHours());
    double numer = Math.sin(ctrRa)*Math.cos(OBLIQUITY) + 0; //on the equator, delta = 0
    double denom = Math.cos(ctrRa);
    double lambda = Math.atan2(numer, denom); //-pi..pi
    lambda = Maths.in2pi(lambda);
    double HALF_WIDTH = Maths.degToRads(55); //45 is too small; can see gaps of several degrees on charts 2 and 5 
    double start = lambda - HALF_WIDTH; //can be negative!
    double end = lambda + HALF_WIDTH; //can exceed 2pi
    Range result = new Range(start, end);
    return result;
  }
  
  private class Range {
    Range(double start, double end){
      this.start = start;
      this.end = end;
    }
    private double start;
    private double end;
  }
}
