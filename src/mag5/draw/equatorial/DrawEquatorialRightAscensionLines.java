package mag5.draw.equatorial;

import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import mag5.book.Clipping;
import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.Hemisphere;
import mag5.draw.Projection;
import mag5.util.Maths;

/** The right ascension grid lines for an equatorial chart. */
class DrawEquatorialRightAscensionLines {

  DrawEquatorialRightAscensionLines(double width, double height, Projection projection, Graphics2D g) {
    this.projection = projection;
    this.g = g;
    this.chartUtil = new ChartUtil(width, height);
  }

  /**
   Full lines for each hour. 
   Every N minutes gets ticks marks; the ticks are placed every 10 degrees of declination.
   The hour number is indicated for each hour line.
  */ 
  public void draw() {
    drawRAGridLines();
  }
  
  void hourLineArc(double ra, double minDec, double maxDec,  Clipping useClipping, Graphics2D g) {
    //build a big polyline, then draw it at the end
    double dec = minDec;
    GeneralPath path = new GeneralPath();
    Point2D.Double start = projection.project(dec, ra);
    path.moveTo(start.getX(), start.getY());
    double Δ = Maths.degToRads(ChartUtil.DELTA_THETA_DEGS);
    //while (dec <= maxDec + ε) {
    while (dec < (maxDec - Maths.degToRads(0.10))) { //STUPID HACK: was drawing too high in dec
      dec = dec + Δ;
      Point2D.Double point = projection.project(dec, ra);
      path.lineTo(point.getX(), point.getY());
    }
    if (useClipping == Clipping.ON) {
      chartUtil.clippingOn(projection, g);
    }
    g.draw(path);
    if (useClipping == Clipping.ON) {
      chartUtil.clippingOff(g);
    }
  }
  
  // PRIVATE 
  
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  
  private static double TWENTY_MINUTES = Maths.hoursToRads(2.0/6.0);
  private static double ONE_HOUR = 15.0; //degrees!
  private static final double TEN_DEGREES = 10.0;

  private void drawRAGridLines() {
    //start on min-ra (right/left), increase to max-ra (left/right)
    Bounds bounds = projection.getBounds();
    double ra = Maths.hoursToRads(bounds.minRaHours);  
    double maxRa = Maths.hoursToRads(bounds.maxRaHours);
    if (bounds.straddlesVernalEquinox()) {
      maxRa = maxRa + 2 * Math.PI;
    }
    while (ra <= maxRa + ChartUtil.ε)  {
      if (chartUtil.isNearMultipleOf(ONE_HOUR, ra)) {
        //every 1h gets a full curve for the hour line
        hourLineFor(ra);
      }
      else {
        tickMarksForMinutes(ra);
      }
      ra = ra + TWENTY_MINUTES; 
    }
  }
  
  /** 
   Start at the min-dec, increase to the max-dec.
   Show the hour number, but only on the scale, outside the main drawing area. 
  */
  private void hourLineFor(double ra) {
    double decMin = Maths.degToRads(projection.getBounds().minDecDeg); //start at min
    double decMax = Maths.degToRads(projection.getBounds().maxDecDeg); //end at max
    chartUtil.drawGrey(g, a -> hourLineArc(ra, decMin, decMax, Clipping.ON, a));
    Double decForHourNumber = projection.getBounds().isNorth() ? decMax : decMin;
    hourNumber(ra, decForHourNumber);
  }
  
  /** Center text on the given position. */
  private void hourNumber(Double ra, Double decForHour) {
    Long hours = Math.round(Maths.radsToHours(ra)) % 24;
    String text = hours + "h";
    //get the x-level from the ra and the extreme dec
    Point2D.Double point = projection.project(decForHour, ra);
    //fudge the y-level from settings
    double bwds = chartUtil.borderWidthDateScale();
    double bw = chartUtil.borderWidth();
    double bwa = chartUtil.borderWidthAlphabet();
    int top = Maths.round(bw + bwds - 0.20*bw);
    //int top = Maths.round(bw + bwds - 0.25*bw);
    int bottom = Maths.round(chartUtil.getHeight() - (bw + bwa) + 0.25*bw);
    int y = 0;
    if (Hemisphere.NORTH == ChartUtil.HEMISPHERE) {
      y = projection.getBounds().isNorth() ? top : bottom;
    }
    else {
      y = projection.getBounds().isNorth() ? bottom : top;
    }
    Point2D.Double pText = chartUtil.centerTextOn(point.x, y, text, g);
    g.drawString(text, Maths.round(pText.x), Maths.round(pText.y)); 
  }
  
  /**
   Every N minutes gets a set of N ticks, generated near multiples of 10deg declination.
   Alignment of ticks is asymmetric: at the endpoints, things are different than in the middle.
  */
  private void tickMarksForMinutes(double ra) {
    double dec = Maths.degToRads(projection.getBounds().minDecDeg); //start at min
    double decMax = Maths.degToRads(projection.getBounds().maxDecDeg); //end at max
    double Δ = Maths.degToRads(TEN_DEGREES);
    while (dec <= decMax + ChartUtil.ε) {
      tickMarkForMinute(dec, ra);      
      dec = dec + Δ; 
    }
  }
  
  /** From the given dec-ra, the tick can go up or down.*/
  private void tickMarkForMinute(double dec, double ra) {
    //small polyline near the given coordinates
    //it will be slightly curved, following the projection (not vertical)
    hourLineArcTick(ra, dec, Clipping.ON);
  }
  
  /** Ticks are centered on the given dec, and go both above and below it. */
  private void hourLineArcTick(double ra, double declination,  Clipping useClipping) {
    double tickSize = Maths.degToRads(ChartUtil.EQUATORIAL_CHART_TICK_SIZE_DEC);
    double maxDec = declination + tickSize;
    double dec = declination - tickSize;
    GeneralPath path = new GeneralPath();
    Point2D.Double start = projection.project(dec, ra);
    path.moveTo(start.getX(), start.getY());
    double Δ = Maths.degToRads(ChartUtil.DELTA_THETA_DEGS);
    //while (dec <= maxDec + ε) {
    while (dec < maxDec) {
      dec = dec + Δ;
      Point2D.Double point = projection.project(dec, ra);
      path.lineTo(point.getX(), point.getY());
    }
    if (Clipping.ON == useClipping) {
      chartUtil.clippingOn(projection, g);
    }
    chartUtil.drawGrey(g, a -> a.draw(path));
    if (Clipping.ON == useClipping) {
      chartUtil.clippingOff(g);
    }
  }
}