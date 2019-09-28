package mag5.draw.polar;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.Projection;
import mag5.util.Maths;

/** Draw all declination lines and tick marks for the polar chart. */
class DrawPolarDeclinationLines {
  
  DrawPolarDeclinationLines(double width, double height, Projection projection, Graphics2D g) {
    this.projection = projection;
    this.g = g;
    this.chartUtil = new ChartUtil(width, height);
  }

  /**
   Every 10 degrees of dec has a circle centered on the pole.
  
   For the ONE vertical hour line in the middle of the chart, tick every degree, 
   and write the actual number (with sign for neg), every 5 deg.
   On the bottom-chart, the number is printed upside down.
   
   <P>For all other hour lines, tick every 5 degrees.
  
   <P>The ticks are rotated, such that they are symmetric around the pole.
  */
  public void draw() {
    chartUtil.clippingOn(projection, g);
    //go up the middle of the chart, in steps of 1 degree
    Bounds bounds = projection.getBounds();
    double minDec = Maths.degToRads(bounds.minDecDeg);
    double maxDec = Maths.degToRads(bounds.maxDecDeg);
    double minRa = Maths.hoursToRads(bounds.minRaHours);
    double maxRa = Maths.hoursToRads(bounds.maxRaHours);
    if (bounds.straddlesVernalEquinox()) {
      maxRa = maxRa + 2*Math.PI;
    }
    double raCtr = Maths.hoursToRads(bounds.raCenterHours());
    double FIVE_DEGREES = 5.0;
    double ONE_DEGREE = Maths.degToRads(1.0);
    double Δ = chartUtil.percentWidth(ChartUtil.EQUATORIAL_CHART_TICK_SIZE);
    
    Color orig = g.getColor();
    g.setColor(chartUtil.greyScale());
    Point2D.Double ctr = projection.centerOfProj();
    Point2D.Double nominal = null; //on the vertical hour, in the middle, since that's where the action is
    double dec = minDec;
    boolean isTopChart = bounds.isTopChart();
    while (dec <= maxDec + ChartUtil.ε)  { 
      boolean isNearPole = chartUtil.isNear(90, dec);
      double circumferenceDegs = projection.getBounds().isNorth() ? Maths.radsToDegs(minDec) : Maths.radsToDegs(maxDec); 
      boolean isNearCircumference =  chartUtil.isNear(circumferenceDegs, dec); //to avoid overwriting the existing outline
      if (!isNearPole && !isNearCircumference) {
        nominal = projection.project(dec, raCtr);
        if (chartUtil.isNearMultipleOf(TEN_DEGREES, dec)) {
          multipleOfTen(bounds, ctr, nominal, dec, isTopChart);
        }
        else if (chartUtil.isNearMultipleOf(FIVE_DEGREES, dec)) {
          multipleOfFive(bounds, minRa, maxRa, nominal, dec, isTopChart);
        }
        //always 1 horizontal tick on the nominal point, on raCtr
        g.drawLine(Maths.round(nominal.x - Δ), Maths.round(nominal.y), Maths.round(nominal.x + Δ), Maths.round(nominal.y));
      }
      dec = dec + ONE_DEGREE;
    }
    g.setColor(orig);
    chartUtil.clippingOff(g);
  }

  // PRIVATE
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  private static double TEN_DEGREES = 10.0;
  private static double ONE_HOUR = Maths.hoursToRads(1.0);
  
  /** Circle centered on the pole. */
  private void multipleOfTen(Bounds bounds, Point2D.Double ctr, Point2D.Double nominal, double dec, boolean isTopChart) {
    double radius = Math.abs(nominal.y - ctr.y);
    int w = Maths.round(2*radius);
    int h = w; //circle
    g.drawOval(Maths.round(ctr.x - radius), Maths.round(ctr.y - radius), w , h);
    declinationNumber(dec, nominal.x, nominal.y, isTopChart, bounds);
  }
  
  /**
   Ticks on all of the hour lines.
   These ticks are not horizontal or vertical; they have 'axial symmetry' around the pole.
  */
  private void multipleOfFive(Bounds bounds, double minRa, double maxRa, Point2D.Double nominal, double dec, boolean isTopChart) {
    Integer tick = Maths.round(chartUtil.percentWidth(ChartUtil.EQUATORIAL_CHART_TICK_SIZE));
    double ra = minRa;
    while (ra <= maxRa + ChartUtil.ε) {
      //don't draw twice on the vertical hour line
      if (!chartUtil.isNearHour(bounds.raCenterHours(), ra)) {
        RotationAngle rotAngle = new RotationAngle(projection, dec, ra);
        chartUtil.drawRotatedAndGrey(
          g, 
          rotAngle.rotationAngle(), 
          rotAngle.target(), 
          x -> x.drawLine(0, -tick, 0, +tick) 
        );
      }
      ra = ra + ONE_HOUR;
    }
    declinationNumber(dec, nominal.x, nominal.y, isTopChart, bounds);
  }
  
  /** 
   Show the declination as a number. The sign is included only for neg values. 
   Output only at raCtr, the vertical hour-line.
   Top-chart: to the right of the hour-line
   Bottom-chart: on the left of the hour line, and upside down!
   Problem: overwriting; this didn't exist for the equatorial chart, where the text is always outside the drawing area.
   One way to treat this is to have a white background for a rectangle surrounding the text.
  */
  private void declinationNumber(Double dec, double x, double y, boolean isTopChart, Bounds bounds) {
    //stay away from the pole and the circumference
    if (Math.abs(dec) < Math.PI/2.0 && Math.abs(dec) > Math.abs(Maths.degToRads(bounds.decFurthestFromPole()))) { 
      String text = Long.valueOf(Math.round(Maths.radsToDegs(dec))).toString()+ "°" ;
      double textWidth = chartUtil.textWidth(text, g); 
      double gap = 0; 
      int sign = isTopChart ? +1 : -1;
      double dx = sign * (gap + textWidth/2.0);
      Point2D.Double target = new Point2D.Double(x + dx, y);
      if (isTopChart) {
        Point2D.Double pText = chartUtil.centerTextVerticallyOn(target.x, target.y, text, g);
        //rect that engulfs the text: draw it as white, to overwrite anything that's already there
        chartUtil.overwritingWhiteRect(text, pText, g);
      }
      else {
        //? Should this use existing code in ChartUtil?
        AffineTransform backToOriginalAffTr = g.getTransform();
        AffineTransform displaceAndRotate = new AffineTransform();
        displaceAndRotate.translate(target.x, target.y);
        displaceAndRotate.rotate(Math.PI);
        g.transform(displaceAndRotate);
        
        Point2D.Double pText = chartUtil.centerTextVerticallyOn(0, 0, text, g);
        chartUtil.overwritingWhiteRect(text, pText, g);
        
        g.setTransform(backToOriginalAffTr);
      }
    }
  }
}