package mag5.draw.polar;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.time.Month;
import java.util.List;
import java.util.function.Consumer;

import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.Projection;
import mag5.star.gmst.GmstFiveDay;
import mag5.star.gmst.GmstMonth;
import mag5.star.gmst.GreenwichMeanSiderealTime;
import mag5.translate.Label;
import mag5.util.Maths;

/** Date scale at the edge of the chart. */
class DrawPolarDateScale {

  DrawPolarDateScale(Integer mapNum, ChartUtil chartUtil, Label labels, Projection projection, Graphics2D g) {
    this.mapNum = mapNum;
    this.chartUtil = chartUtil;
    this.labels = labels;
    this.projection = projection;
    this.g = g;
  }
  
  /**
   Scales on the circumference, to translate the date into the local mean sidereal time at 20h00 in the evening.
   Settings control the radius of where these appear (as delta-declinations, so that its easy to find xy via projection).
   The text is rotated such that it has axial symmetry with respect to the pole. 
   For the top and bottom chart, the full year will appear on the circumference.
   For each individual chart (top/bottom), half the year will appear; this will cut off the months that appear at the limits
   of the chart; in that case, special handling is needed to center the month-text.
  */
  public void draw() {
    Bounds bounds = projection.getBounds();
    
    double dDecDaysStart = ChartUtil.POLAR_DAY_SCALE_START; //rads, positive DELTA from the circumference
    double dDecDaysEnd = ChartUtil.POLAR_DAY_SCALE_END; 
    double dDecMonthsEnd = ChartUtil.POLAR_MONTH_SCALE_END;
    
    Double minRa = Maths.hoursToRads(bounds.minRaHours);
    Double maxRa = Maths.hoursToRads(bounds.maxRaHours);
    
    bigCirclesCenteredOnThePole(dDecDaysStart, dDecDaysEnd, dDecMonthsEnd);
    showEvery5days(dDecDaysStart, dDecDaysEnd, minRa, maxRa);
    showMonths(dDecDaysEnd, dDecMonthsEnd, minRa, maxRa);
    borderLineAtEndToClose(dDecDaysStart, dDecMonthsEnd, minRa, maxRa);
  }

  // PRIVATE
  private Integer mapNum;
  private ChartUtil chartUtil;
  private Label labels;
  private Projection projection;
  private Graphics2D g;

  /** Three circles centered on the pole; distinguished only by their radii/declinations. */
  private void bigCirclesCenteredOnThePole(double dDecDaysStart, double dDecDaysEnd, double dDecMonthsEnd) {
    circleFor(dDecDaysStart);
    circleFor(dDecDaysEnd);
    circleFor(dDecMonthsEnd);
  }

  /** Big circle on the circumference. Centered on the celestial pole (the center of projection). */
  private void circleFor(Double dDec /* >0, positive delta, rads */) {
    double radius = circleRadius(dDec);
    ProjectPolar projectPolar = (ProjectPolar) projection;
    Shape halfMoonShape = projectPolar.halfMoon(radius, Arc2D.Double.OPEN);
    g.draw(halfMoonShape);
  }
  
  private double circleRadius(Double dDec) {
    //int sign = projection.getBounds().isNorth() ? -1 : +1;
    int sign = -1;
    double decFurthestFromPole = projection.getBounds().decFurthestFromPole(); //degs, with sign
    double baseDec = Maths.degToRads(Math.abs(decFurthestFromPole)); //remove the sign!
    double radsFromPole = Math.PI/2.0 - (baseDec + sign*dDec);
    double result = projection.distancePerRad() * (radsFromPole);
    return result;
  }

  /** Every 5 days, show a tick mark and the day-of-the-month number. */
  private void showEvery5days(double dDecDaysStart, double dDecDaysEnd, Double minRa, Double maxRa) {
    for(GmstFiveDay gmst : new GreenwichMeanSiderealTime().everyFiveDays()) {
      if (Maths.inRangeRa(minRa, maxRa, gmst.getRa())){
        radialLineFor(gmst.getRa(), dDecDaysStart, dDecDaysEnd, gmst.getDay());
      }
    }
  }
  
  /** 
   Radial line from the center, between 2 points with the same right ascension; vary the declination.
   The dec args here are POSITIVE deltas from the circumference.
   Also draw the number representing the day of the month. 
  */
  private void radialLineFor(double ra, double dDecStart, double dDecEnd, Integer day /*possible null*/) {
    Double baseDec = Maths.degToRads(projection.getBounds().decFurthestFromPole()); //with sign
    int sign = projection.getBounds().isNorth() ? -1 : +1;
    Double startDec = baseDec + sign * dDecStart; 
    Double endDec = baseDec + sign * dDecEnd;
    
    double smallerRadius = circleRadius(dDecStart);
    double largerRadius = circleRadius(dDecEnd);
    DrawRay drawRay = new DrawRay(projection, g);
    drawRay.ray(ra, smallerRadius, largerRadius);

    //the number text
    if (day != null && day <= 30) { //don't do it for the 31st
      dayNumber(ra, day, startDec, endDec);
    }
  }

  /** 
   The number is placed a bit off to the side of the given right ascension. 
   The text needs to be rotated, to be axially symmetric with respect to the pole.
  */
  private void dayNumber(double ra, Integer day, Double startDec, Double endDec) {
    String text = day.toString();
    Double decMidway = (startDec + endDec)/2.0;
    Double raTweak = -1 * Maths.hoursToRads(4.0/60.0);
    Point2D.Double target = projection.project(decMidway, ra + raTweak);
    if (!isOutOfBounds(target)) {
      double rotationAngle = chartUtil.rotationAngle(projection, target);
      Consumer<Graphics2D> drawer = x -> {
        Point2D.Double centered = chartUtil.centerTextOn(0, 0, text, g);
        x.drawString(text, Maths.round(centered.x), Maths.round(centered.y));
      };
      chartUtil.drawRotated(g, rotationAngle, target, drawer);
    }
  }
  
  private boolean isOutOfBounds(Point2D.Double target) {
    boolean result = false;
    double ctrProjY = projection.centerOfProj().getY();
    if (projection.getBounds().isTopChart()) {
      result = target.getY() > ctrProjY;
    }
    else {
      result = target.getY() < ctrProjY;
    }
    return result;
  }

  private void showMonths(double dDecDaysEnd, double dDecMonthsEnd, Double minRa, Double maxRa) {
    //months: show the limits
    List<GmstMonth> monthLimits = new GreenwichMeanSiderealTime().forCharts().get(mapNum);
    Integer DONT_SHOW_DAY_NUM = null;
    for (GmstMonth monthLimit : monthLimits) {
      if (Maths.inRangeRa(minRa, maxRa, monthLimit.getRa())){
        radialLineFor(monthLimit.getRa(), dDecDaysEnd, dDecMonthsEnd, DONT_SHOW_DAY_NUM);
      }
      String shortMonthName = labels.shortMonthName(ChartUtil.lang(), Month.of(monthLimit.getMonth()));
      if (Maths.inRangeRa(minRa, maxRa, monthLimit.getRaMidMonth())) {
        drawShortMonthName(shortMonthName, dDecDaysEnd, dDecMonthsEnd, monthLimit.getRaMidMonth());
      }
      else {
        //calculate the midpoint? needed? maybe not!
      }
    }
  }

  /** Draw the month name in the middle of the month. */
  private void drawShortMonthName(String shortMonthName, Double dDecStart, Double dDecEnd, Double raMidMonth) {
    Double baseDec = Maths.degToRads(projection.getBounds().decFurthestFromPole()); //with sign
    int sign = projection.getBounds().isNorth() ? -1 : +1;
    Double dec = baseDec + sign * (dDecStart + dDecEnd)/2.0; //midway 
    Point2D.Double baseCircum = projection.project(dec, raMidMonth);
    //chartUtil.debuggingDot(baseCircum, g);
    
    double textRotationAngle = chartUtil.rotationAngle(projection, baseCircum);
    Consumer<Graphics2D> drawer = g-> {
      Point2D.Double newTextPoint = chartUtil.centerTextOn(0, 0, shortMonthName, g); 
      g.drawString(shortMonthName, Maths.round(newTextPoint.x), Maths.round(newTextPoint.y));
      //chartUtil.debuggingDot(newTextPoint, g);
    }; 
    chartUtil.drawRotated(
      g, textRotationAngle, baseCircum, drawer 
    );
  }
  
  /** Small lines to close the semi-circles at the ends. */
  private void borderLineAtEndToClose(Double dDecDaysStart, Double dDecMonthsEnd, Double minRa, Double maxRa) {
    radialLineFor(minRa, dDecDaysStart, dDecMonthsEnd, null);
    radialLineFor(maxRa, dDecDaysStart, dDecMonthsEnd, null);
  }
}