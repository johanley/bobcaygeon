package mag5.draw.polar;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

import mag5.book.Clipping;
import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.Projection;
import mag5.util.Maths;

/** Draw all right ascension lines and tick marks for the polar chart. */
class DrawPolarRightAscensionLines {
  
  DrawPolarRightAscensionLines(double width, double height, Projection projection, Graphics2D g) {
    this.projection = projection;
    this.g = g;
    this.chartUtil = new ChartUtil(width, height);
  }
  
  /**
   Hours: straight lines radiating from the pole.
   The pole is crowded; only go all the way to the pole for the vertical right ascension; otherwise just go near to the pole.
   Ticks every N min: circumference, lines of 10 degrees of declination. 
  */
  public void draw() {
    double ONE_HOUR = 15.0; //degrees!
    double TWENTY_MINUTES = Maths.hoursToRads(20.0/60.0);
    Bounds bounds = projection.getBounds();
    double ra = Maths.hoursToRads(bounds.minRaHours + 1) - 2*TWENTY_MINUTES; //avoid the extremes, since they have already been drawn  
    double maxRa = Maths.hoursToRads(bounds.maxRaHours - 1) + 2*TWENTY_MINUTES ;
    if (bounds.straddlesVernalEquinox()) {
      maxRa = maxRa + 2*Math.PI;
    }
    while (ra <= maxRa /*+ ChartUtil.ε*/)  {
      if (chartUtil.isNearMultipleOf(ONE_HOUR, ra)) {
        hourLineFor(ra);
      }
      else {
        tickMarksForMinutes(ra);
      }
      ra = ra + TWENTY_MINUTES; 
    }
    //at the edges: just do the numbers, not the lines
    hourNumber(Maths.hoursToRads(bounds.minRaHours));
    hourNumber(Maths.hoursToRads(bounds.maxRaHours));
  }

  // PRIVATE
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  
  private static final double TEN_DEGREES = 10.0;
  private static final double NEAR_POLE = Maths.degToRads(85);

  private boolean isCentralRa(double ra, Projection projection) {
    double raCtr = Maths.hoursToRads(projection.getBounds().raCenterHours());
    boolean result = Math.abs(raCtr - ra) < ChartUtil.ε;
    return result;
  }

  /**
   Straight line between the pole and the circumference. 
   Show the hour number, but only on the scale, outside the main drawing area. 
  */
  private void hourLineFor(double ra) {
    double decMin = Maths.degToRads(projection.getBounds().minDecDeg); 
    double decMax = Maths.degToRads(projection.getBounds().maxDecDeg);
    hourLine(ra, decMin, decMax);
    hourNumber(ra);
  }
  
  /** Line between the pole (usually 85 deg, to avoid crowding at the pole) and the circumference. */
  private void hourLine(double ra, double minDec, double maxDec) {
    int sign = projection.getBounds().isNorth() ? +1 : -1;
    boolean isVerticalRa = isCentralRa(ra, projection);
    double decPole = isVerticalRa ? Math.PI/2 : NEAR_POLE;
    double decCircum = projection.getBounds().isNorth() ? minDec : maxDec;
    Point2D.Double from = projection.project(sign*decPole, ra);
    Point2D.Double to = projection.project(decCircum, ra);
    chartUtil.drawGrey(g, g ->  
      g.drawLine(Maths.round(from.x), Maths.round(from.y), Maths.round(to.x), Maths.round(to.y))
    );
  }

  /** The text needs to be rotated, according to where it appears on the circumference of the circle. */
  private void hourNumber(Double ra) {
    double decMin = Maths.degToRads(projection.getBounds().minDecDeg); 
    double decMax = Maths.degToRads(projection.getBounds().maxDecDeg);
    Double decForHour = projection.getBounds().isNorth() ? decMin : decMax;
    
    Long hours = Math.round(Maths.radsToHours(ra)) % 24;
    String text = hours + "h";
    
    Point2D.Double baseCircum = projection.project(decForHour, ra); //point on the circumference
    
    double textRotationAngle = chartUtil.rotationAngle(projection, baseCircum);
    double theta = textRotationAngle - Math.PI/2.0;
    //calc where the text should go with respect to the baseCircum point
    double tweakSize = chartUtil.percentWidth(0.15*ChartUtil.BORDER_WIDTH);
    double dx = tweakSize * Math.cos(theta);
    double dy = tweakSize * Math.sin(theta);
    Point2D.Double textPoint = new Point2D.Double(baseCircum.x + dx, baseCircum.y + dy);
    //chartUtil.debuggingDot(textPoint, g);
    
    Consumer<Graphics2D> drawer = g-> {
      Point2D.Double newTextPoint = chartUtil.centerTextOn(0, 0, text, g); 
      //chartUtil.debuggingDot(newTextPoint, g);
      g.drawString(text, Maths.round(newTextPoint.x), Maths.round(newTextPoint.y));
    }; 
    chartUtil.drawRotated(
      g, textRotationAngle, textPoint, drawer 
    );
  }
  
  /**
   Every N minutes gets a set of N ticks, generated near multiples of 10deg declination.
   Alignment of ticks is asymmetric: at the endpoints, things are different than in the middle.
  */
  private void tickMarksForMinutes(double ra) {
    double minDec = Maths.degToRads(projection.getBounds().minDecDeg); //start at min
    double maxDec = Maths.degToRads(projection.getBounds().maxDecDeg); //end at max
    
    int sign = projection.getBounds().isNorth() ? +1 : -1;
    double decPole = sign * NEAR_POLE;
    double decCircum = projection.getBounds().isNorth() ? minDec : maxDec;
    
    double dec = decCircum;
    double Δ = Maths.degToRads(TEN_DEGREES);
    if (projection.getBounds().isNorth()) {
      while (dec <= decPole + ChartUtil.ε) {
        tickMarkForMinute(dec, ra);      
        dec = dec + Δ; 
      }
    }
    else {
      while (dec >= decPole + ChartUtil.ε) {
        tickMarkForMinute(dec, ra);      
        dec = dec - Δ; 
      }
    }
  }
  
  private void tickMarkForMinute(double dec, double ra) {
    hourLineArcTick(ra, dec, Clipping.ON);
  }
  
  /** Ticks are centered on the given dec, and go both above and below it. */
  private void hourLineArcTick(double ra, double declination,  Clipping useClipping) {
    if (Clipping.ON == useClipping) {
      clippingOn();
    }
    Integer tick = Maths.round(chartUtil.percentWidth(ChartUtil.EQUATORIAL_CHART_TICK_SIZE));
    RotationAngle rotAngle = new RotationAngle(projection, declination, ra);
    chartUtil.drawRotatedAndGrey(
      g, 
      rotAngle.rotationAngle() + Math.PI/2.0, 
      rotAngle.target(), 
      x -> x.drawLine(0, -tick, 0, +tick) 
    );
    if (Clipping.ON == useClipping) {
      clippingOff();
    }
  }
  
  /** Clip all drawing to the interior of the chart, where the stars and lines are. */
  private void clippingOn() {
    Shape clip = projection.innerBoundary();
    g.setClip(clip);
  }
  
  private void clippingOff() {
    g.setClip(null);
  }
}
