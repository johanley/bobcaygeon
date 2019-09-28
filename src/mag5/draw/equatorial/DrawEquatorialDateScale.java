package mag5.draw.equatorial;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.time.Month;
import java.util.List;

import mag5.book.Clipping;
import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.Hemisphere;
import mag5.draw.Projection;
import mag5.star.gmst.GmstFiveDay;
import mag5.star.gmst.GmstMonth;
import mag5.star.gmst.GreenwichMeanSiderealTime;
import mag5.translate.Label;
import mag5.util.Maths;

/** The date-scale that appears at the top of the top-chart. */
class DrawEquatorialDateScale {

  DrawEquatorialDateScale(Integer mapNum, Label labels, ChartUtil chartUtil, Projection projection, Graphics2D g){
    this.mapNum = mapNum;
    this.labels = labels;
    this.chartUtil = chartUtil;
    this.projection = projection;
    this.g = g;
    this.raLines = new DrawEquatorialRightAscensionLines(chartUtil.getWidth(), chartUtil.getHeight(), projection, g);
  }

  /**
   Top chart only. 
   Scales at the top, to translate the date into the local mean sidereal time at 20h00 in the evening.
   Settings control the y-level of where these appear (as an offset declination from the top of the map). 
  */
  public void draw() {
    if (projection.getBounds().isTopChart()) {
      drawDateScales();
    }
  }
  
  // PRIVATE
  private Integer mapNum;
  private Label labels;
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  private DrawEquatorialRightAscensionLines raLines;

  private void drawDateScales() {
    Bounds bounds = projection.getBounds();
    Double minRa = Maths.hoursToRads(bounds.minRaHours);
    Double maxRa = Maths.hoursToRads(bounds.maxRaHours);

    Double furthestFromEq = bounds.decFurthestFromEq();
    int sign = Hemisphere.NORTH == ChartUtil.HEMISPHERE ? +1 : -1; 
    Double decDays = Maths.degToRads(furthestFromEq + sign * ChartUtil.DELTA_DEC_SCALE_DAY_TO_RA);
    Double decMonths = Maths.degToRads(furthestFromEq + sign * ChartUtil.DELTA_DEC_SCALE_MONTH_TO_RA);
    Double decMonthsTop = Maths.degToRads(furthestFromEq + sign * ChartUtil.DELTA_DEC_TO_MONTH_SCALE_TOP);
    
    lineAcross(decDays);
    GreenwichMeanSiderealTime gmst = new GreenwichMeanSiderealTime();
    dayScale(minRa, maxRa, decMonths, decDays, gmst);
    monthScale(bounds, minRa, maxRa, decMonths, decMonthsTop, gmst);
    //borders on the sides, a little bit past the usual ra-limits
    hourLineArc(monthScaleLeftEdgeRa(), decDays, decMonthsTop);
    hourLineArc(monthScaleRightEdgeRa(), decDays, decMonthsTop);
  }

  /** Tick mark, with the day-number no the RIGHT (LEFT) of the tick mark, in the northern (southern) hem. */
  private void dayScale(Double minRa, Double maxRa, Double decMonths, Double decDays, GreenwichMeanSiderealTime gmst) {
    List<GmstFiveDay> everyFiveDays = gmst.everyFiveDays();
    for(GmstFiveDay gmstFiveDay : everyFiveDays) {
      if (Maths.inRangeRa(minRa, maxRa, gmstFiveDay.getRa())){
        hourLineArc(gmstFiveDay.getRa(), decDays, decMonths);
        if (gmstFiveDay.getDay() != 31) { //don't show the 31st!
          Point2D.Double point = projection.project(decDays, gmstFiveDay.getRa());
          double width = chartUtil.getWidth();
          //there are HACKY tweaks here to get the placement right
          double dx = 0.0;
          if (Hemisphere.NORTH == ChartUtil.HEMISPHERE) {
            //to the right of the mark
            dx = 5.0*((width*1.0 - point.x)/width); //1.0 added to avoid integer division!
          }
          else {
            //to the left of the mark
            double textWidth = chartUtil.textWidth(gmstFiveDay.getDay().toString(), g);
            dx = - (textWidth + 5.0*point.x/width); 
          }
          shearFor60DegreesDec(gmstFiveDay.getDay().toString(), point.x + dx , point.y - 2);
        }
      }
    }
  }
  
  /** 
   Tick at the end of the month. Month name in the middle.
   Sometimes only a part of the month is shown; in that case, need to compute where exactly to put the 
   month-name, since it can't go in the mid-point between 2 month-ends. 
  */
  private void monthScale(Bounds bounds, Double minRa, Double maxRa, Double decMonths, Double decMonthsTop, GreenwichMeanSiderealTime gmst) {
    lineAcross(decMonths);
    List<GmstMonth> monthLimits = gmst.forCharts().get(mapNum);
    int previous = -1; //lets me get the previous item in the iteration
    for (GmstMonth monthLimit : monthLimits) {
      if (Maths.inRangeRa(minRa, maxRa, monthLimit.getRa())){
        hourLineArc(monthLimit.getRa(), decMonths, decMonthsTop); //end of month
      }
      String shortMonthName = labels.shortMonthName(ChartUtil.lang(), Month.of(monthLimit.getMonth()));
      if (Maths.inRangeRa(minRa, maxRa, monthLimit.getRaMidMonth())) {
        drawShortMonthName(shortMonthName, decMonths, monthLimit.getRaMidMonth());
      }
      else {
        Double chartCenterRa = Maths.hoursToRads(bounds.raCenterHours());
        if (monthLimit.getRaMidMonth() < chartCenterRa) {
          //if right hand, take midpoint of min and month-end
          Double midpointRa = Maths.midpoint(minRa, monthLimit.getRa());
          drawShortMonthName(shortMonthName, decMonths, midpointRa);
        }
        else {
          //if left hand, take midpoint of max and the end of the PREVIOUS month
          if (previous > -1) {  
            Double midpointRa = Maths.midpoint(maxRa, monthLimits.get(previous).getRa());
            drawShortMonthName(shortMonthName, decMonths, midpointRa);
          }
        }
      }
      ++previous;
    }
    lineAcross(decMonthsTop);
  }

  private void drawShortMonthName(String shortMonthName, Double decMonths, Double ra) {
    //we need a bigger font
    Font originalFont = g.getFont();
    //the text alignment depends on the font size, so that needs to be set early
    Font biggerFont = chartUtil.resizedFont(1.25f, g);
    g.setFont(biggerFont);
    Point2D.Double point = projection.project(decMonths, ra);
    Point2D.Double centered = chartUtil.centerTextOn(point.x, point.y, shortMonthName, g);
    g.drawString(shortMonthName, Maths.round(centered.x), Maths.round(centered.y - 7));
    g.setFont(originalFont);
  }
  
  private void lineAcross(Double dec) {
    //the endpoints go a bit beyond the regular limits in ra
    Point2D.Double l = projection.project(dec, monthScaleLeftEdgeRa());
    Point2D.Double r = projection.project(dec, monthScaleRightEdgeRa());
    GeneralPath path = new GeneralPath();
    path.moveTo(l.getX(), l.getY());
    path.lineTo(r.getX(), r.getY());
    g.draw(path);
  }
  
  private Double monthScaleLeftEdgeRa() {
    Double result = Maths.hoursToRads(projection.getBounds().maxRaHours) + deltaAlphaMonthScale();
    return result;
  }
  
  private Double monthScaleRightEdgeRa() {
    Double result = Maths.hoursToRads(projection.getBounds().minRaHours) - deltaAlphaMonthScale();
    return result;
  }

  /** How for to go beyond the regular ra-limits when drawing the date-scale. */
  private double deltaAlphaMonthScale() {
    return Maths.degToRads(ChartUtil.DELTA_ALPHA_MONTH_SCALE);
  }
  
  private void shearFor60DegreesDec(String text, double x, double y) {
    AffineTransform originalAff = g.getTransform();
    double BASE_SHEAR = 0.90;
    double halfWidth = chartUtil.getWidth()/2;
    double shear = BASE_SHEAR * (x - halfWidth)/halfWidth;
    
    AffineTransform translateAndShear = new AffineTransform();
    translateAndShear.translate(x, y);
    translateAndShear.shear(shear,0.0);  
    g.transform(translateAndShear);
    g.drawString(text, 0, 0); //at the origin of the 'new' coords'
    g.setTransform(originalAff); //back to the way it was
  }
  
  /** In the southern hem, we need to swap min and max here.  */
  private void hourLineArc(double ra, double decMin, double decMax) {
    if (Hemisphere.NORTH == ChartUtil.HEMISPHERE) {
      raLines.hourLineArc(ra, decMin, decMax, Clipping.OFF, g);
    }
    else {
      raLines.hourLineArc(ra, decMax, decMin, Clipping.OFF, g);
    }
  }
}