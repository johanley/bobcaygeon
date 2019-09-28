package mag5.draw;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import mag5.book.PdfConfig;
import mag5.chartlabels.ChartLabel;
import mag5.constellation.ConstellationLines;
import mag5.deepskyobject.DeepSkyObject;
import mag5.draw.equatorial.DrawDsoNameEquatorial;
import mag5.draw.equatorial.DrawStarNamesEquatorial;
import mag5.draw.polar.DrawDsoNamePolar;
import mag5.draw.polar.DrawStarNamesPolar;
import mag5.star.Star;
import mag5.translate.Label;
import mag5.util.Maths;

/**
 Abstract Base Class for drawing a chart. 
 This class has knowledge of the drawing context, but nothing else. 
 There are two concrete subclasses, one for polar charts, and one for equatorial charts.
*/
public abstract class DrawChart {
  
  /** The constructor, as usual, takes the various data needed to construct the star chart. */
  public DrawChart(
    Integer mapNum, Bounds bounds, List<Star> stars, ConstellationLines constellationLines, 
    List<DeepSkyObject> dsos, Label labels, List<ChartLabel> chartLabels, Projection projection, Graphics2D g) {
    //IMPORTANT: WE SWAP width and height here: the charts are rotated on the page
    this.width = PdfConfig.HEIGHT;
    this.height = PdfConfig.WIDTH;
    this.mapNum = mapNum;
    this.bounds = bounds;
    this.stars = stars;
    this.lines = constellationLines.all();
    this.deepSkyObjects = dsos;
    this.labels = labels;
    this.chartLabels = chartLabels;
    
    this.widthInt = Maths.round(this.width);
    this.heightInt = Maths.round(this.height);
    this.g = g;
    this.projection = projection;
    this.chartUtil = new ChartUtil(width, height);
  }
  
  /** 
   Draw the chart.
   Template method. Calls the various abstract methods defined by this class.  
  */
  public void draw() {
    log("Creating chart " + chartName() + " h:"+ height + " w:"+width);
    log("Num stars in the chart (filtered): " + stars.size());
    log("Num lines in the chart (unfiltered): " + lines.size());
    initGraphicsContext();

    drawOutline();
    drawRightAscensionGridLines();
    drawDeclinationGridLines();
    drawMagnitudeScale();
    drawMapNumber();
    drawSymbolKey();
    drawGreekAlphabet();
    drawDueSouthAt8();
    drawDateScale();
    drawExtras();
    
    findStarPositions(stars, projection);
    findDsoPositions(deepSkyObjects, projection);
    
    chartUtil.clippingOn(projection, g);
    drawConstellationLines();
    drawDeepSkyObjects();
    
    drawStarDots();
    drawStarNames();
    drawDsoName();
    drawChartLabels();
    
    chartUtil.clippingOff(g);
  }
  
  protected abstract void drawRightAscensionGridLines();
  
  protected abstract void drawDeclinationGridLines();
  
  /** The scale at the edge of the chart, used to determine sideral time for a given date and time.*/
  protected abstract void drawDateScale();
  
  /** Simple explanatory text for the date scale. */
  protected abstract void drawDueSouthAt8();
  
  /** Items that aren't captured anywhere else. */
  protected abstract void drawExtras();
  
  /** Various utility methods for drawing, and data. */
  protected ChartUtil chartUtil;
  
  /** Translatable text. */
  protected Label labels;
  
  /** The chart number. */
  protected Integer mapNum;
  
  /** What projection is used to draw the chart. */
  protected Projection projection;
  
  /** 
   The graphics context.
   IMPORTANT: pdf files and libraries have a built-in graphics context. 
   You can draw directly into the pdf. 
  */
  protected Graphics2D g;

  protected void drawDueSouthAt8oclock(double percentWidth, double percentHeight) {
    if (projection.getBounds().isTopChart()) {
      //we need a bigger font
      Font originalFont = g.getFont();
      //the text alignment depends on the font size, so that needs to be set early
      Font biggerFont = chartUtil.resizedFont(1.6f, g);
      g.setFont(biggerFont);
      String text = projection.getBounds().isNorth() ? "Due south at 8pm" : "Due north at 8pm"; 
      g.drawString(labels.text(text, ChartUtil.lang()), Maths.round(chartUtil.percentWidth(percentWidth)), Maths.round(chartUtil.percentHeight(percentHeight)));
      g.setFont(originalFont);
    }
  }
  
  //PRIVATE
  
  /** The bounds of the projection/chart. */
  private Bounds bounds;
  
  /** Filtered using settings. */
  private List<Star> stars;
  
  /** All constellation lines, for the whole sky. */
  private Map<String, List<List<Integer>>> lines;
  
  /** The DSOs that appear on the chart. */
  private List<DeepSkyObject> deepSkyObjects;
  
  /** 
   Remember where each star is drawn.
   This exists in order to remember how to draw the lines joining the stars.
   The key is the (slightly modified) YBS index.  
  */
  private Map<Integer, Point2D.Double> starPoints = new LinkedHashMap<>();
  
  /** 
   Remember where each deep sky object is drawn.
   This exists in order to remember where to draw the designations.
   The key is the deep sky objects (DSO) designation.  
  */
  private Map<String, Point2D.Double> dsoPoints = new LinkedHashMap<>();
  
  /** Names of constellations, asterisms, and the brightest stars. */
  private List<ChartLabel> chartLabels;
  
  private double width;
  private double height;
  private int widthInt;
  private int heightInt;

  private static void log(Object... msgs) {
    Stream.of(msgs).forEach(System.out::println);    
  }
  
  /** For logging only. */
  private String chartName() {
    String sky = bounds.isTopChart() ? "a" : "b";
    String result = "chart_" + mapNum + "_" + sky;
    return result;
  }
  
  private void initGraphicsContext() {
    g.setFont(ChartUtil.baseFont());
    //improves the circles and maybe the text, but not the lines
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    whiteBackgroundThenDrawWithBlack();
    //g.setStroke(new BasicStroke());
    rotateTheDrawingContext();
  }
  
  /** 
   The charts are drawn 'on their side'. The user rotates the page in order to use.
   Right-to-left is transformed to bottom-to-top. 
  */  
  private void rotateTheDrawingContext() {
    AffineTransform displaceAndRotate = new AffineTransform();
    displaceAndRotate.translate(0, height*aspectRatio()); //WARNING: with a png, I don't need to multiply by the aspect ratio
    displaceAndRotate.rotate(-Math.PI/2);
    g.transform(displaceAndRotate);
  }
  
  private double aspectRatio() {
    return width/height;
  }
  
  private void whiteBackgroundThenDrawWithBlack() {
    g.setColor(Color.white);
    g.fillRect(0, 0, widthInt, heightInt);
    g.setColor(Color.black);
  }
  
  private void drawOutline() {
    Shape boundary = projection.innerBoundary();
    g.draw(boundary);
  }
  
  private void drawConstellationLines() {
    DrawConstellations constellations = new DrawConstellations(lines, starPoints, g);
    constellations.draw();
  }
  
  private void drawDeepSkyObjects() {
    DrawDeepSkyObject dsos = new DrawDeepSkyObject(deepSkyObjects, projection, g);
    dsos.draw();
  }
  
  private void drawStarDots() {
    DrawStars starDots = new DrawStars(stars, starPoints, g);
    starDots.draw();
  }
  
  private void drawMagnitudeScale() {
    DrawMagnitudeScale magScale = new DrawMagnitudeScale(labels, width, height, projection, g);
    magScale.draw();
  }
  
  private void drawMapNumber() {
    DrawMapNumber drawMapNum = new DrawMapNumber(mapNum, width, height, projection, g);
    drawMapNum.draw();
  }
  
  private void drawSymbolKey() {
    DrawSymbolKey  drawSymbolKey = new DrawSymbolKey(labels, chartUtil, projection, g);
    drawSymbolKey.draw();
  }
  
  private void drawGreekAlphabet() {
    DrawGreekAlphabet greek = new DrawGreekAlphabet(chartUtil, projection, g);
    greek.draw();
  }
  
  private void findStarPositions(List<Star> stars, Projection projection) {
    for (Star star : stars) {
      Point2D.Double where = projection.project(star.DEC, star.RA);
      starPoints.put(star.INDEX, where); 
    }
  }
  
  private void findDsoPositions(List<DeepSkyObject> dsos, Projection projection) {
    for (DeepSkyObject dso : dsos) {
      Point2D.Double where = projection.project(dso.getDec(), dso.getRa());
      dsoPoints.put(dso.getDesig(), where); 
    }
  }
  
  /** Should this be in an abstract method? */
  private void drawStarNames() {
    if (projection.getBounds().isEquatorial()) {
      DrawStarNamesEquatorial starNames = new DrawStarNamesEquatorial(stars, starPoints, chartUtil, projection, g);
      starNames.draw();
    }
    else {
      DrawStarNamesPolar starNames = new DrawStarNamesPolar(stars, starPoints, chartUtil, projection, g);
      starNames.draw();
    }
  }
  
  /** Should this be in an abstract method? */
  private void drawDsoName() {
    if (projection.getBounds().isEquatorial()) {
      DrawDsoNameEquatorial dsoName = new DrawDsoNameEquatorial(deepSkyObjects, dsoPoints, chartUtil, projection, g);
      dsoName.draw();
    }
    else {
      DrawDsoNamePolar dsoName = new  DrawDsoNamePolar(deepSkyObjects, dsoPoints, chartUtil, projection, g);
      dsoName.draw();
    }
  }
  
  private void drawChartLabels() {
    DrawChartLabels chLabels = new DrawChartLabels(chartLabels, chartUtil, projection, g);
    chLabels.draw();
  }
}