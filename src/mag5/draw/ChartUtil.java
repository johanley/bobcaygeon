package mag5.draw;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

import mag5.translate.Lang;
import mag5.util.Maths;

/** Settings and utility methods for chart building. */
public class ChartUtil {

  /**
   The current chart being generated is for this Hemisphere.
   
   <P>WARNING: THIS CHANGES AT RUNTIME. Mutable global data!
   This has no effect upon the polar charts; only on the equatorial charts.
   Permutes the 'top' and 'bottom' charts, and so on.
  */
  public static Hemisphere HEMISPHERE = Hemisphere.NORTH;
  
  /** Percent of total width. */
  public static final Double BORDER_WIDTH = 4.0;
  /** Percent of total width. */
  public static final Double BORDER_WIDTH_DATE_SCALE = 3.0;
  /** Percent of total width. */
  public static final Double BORDER_WIDTH_ALPHABET = 3.0;
  
  /** 
   Percent of total height.
   Need to leave some room near the spine; otherwise, there are problems when printing. 
   Top chart: room at the bottom of the page.
   Bottom chart: room at the top of the page.
  */
  public static final Double SPINE_MARGIN = 2.0;
  
  /** Degrees. Used to generate long polylines. Needs to divide evenly into 90. */
  public static final Double DELTA_THETA_DEGS = 0.1;
  
  /** Degrees. Tick marks showing minutes of right ascension. */
  public static final Double EQUATORIAL_CHART_TICK_SIZE_DEC = 0.20;
  
  /** Percent of the total chart width. */
  public static final Double EQUATORIAL_CHART_TICK_SIZE = 0.5;
  
  /** The limiting magnitude used for all charts. Value: {@value}. */
  public static final Double LIMITING_MAG = 5.01;
  
  /**
   Overlap at the edge of a chart.
   The lines that join stars often need to go "off the chart", when they are near the edge.
   That can only be done if stars that are off-chart actually participate in the chart-building.
   This settings lets the caller create a set of stars that include those beyond the edge of the chart. 
  */  
  public static final Integer EDGE_OVERLAP_DEGS = 60; 

  /**
   The font used for the PDF.   
   WARNING: Greek letters don't render in all fonts.
   Please see {@link mag5.book.MyFontMapper} as well.
  */
  public static final String BASE_FONT_NAME = "Times New Roman";
  public static final int BASE_FONT_SIZE = 8;
  
  /** Used to determine if an angle is sufficiently near some value (degrees). */
  public static final double εDegs = 0.01; 
  /** Used to determine if an angle is sufficiently near some value (radians). */
  public static final double ε = Maths.degToRads(εDegs);
  
  /**
   Width of a stroke. 
   Setting stroke-width to 0 forces the minimum width.
   I've seen printers in which a 0-setting prints really poorly; perhaps best to avoid. 
  */
  public static final float STROKE_WIDTH_DEFAULT = 0.25f;
  public static final float STROKE_WIDTH_CONSTELLATION_LINE = 0.25f;
  
  /** How big the symbols are for deep sky objects. Degrees. */
  public static final double DEEP_SKY_OBJECT_SIZE = 0.3;
  
  /** Expressed as a percentage of the image width. */
  public static final double GREEK_ALPHABET_SPACING = 3.5;

  /** Offset from the min/max declination. Radians */
  public static final Double POLAR_DAY_SCALE_START = Maths.degToRads(1.3);
  /** Offset from the min/max declination. Radians */
  public static final Double POLAR_DAY_SCALE_END = Maths.degToRads(2.6);
  /** Offset from the min/max declination. Radians */
  public static final Double POLAR_MONTH_SCALE_END = Maths.degToRads(3.9);

  /** For equatorial charts, the maximum declination away from the equator (degrees). */
  public static final Double EQUATORIAL_CHART_MAX_DEC_DEGS = 60.0;
  
  /** How far the date scale needs to go, beyond regular limits of the chart (degreess, equatorial only).  */
  public static final Double DELTA_ALPHA_MONTH_SCALE = 3.0;
  
  /** Distance in declination between max dec and the bottom of the day-scale (degrees) */
  public static final Double DELTA_DEC_SCALE_DAY_TO_RA = 1.5;
  /** Distance in declination between max dec and the bottom of the month-scale (degrees). */
  public static final Double DELTA_DEC_SCALE_MONTH_TO_RA  = 2.5;
  /** Distance in declination between max dec and the top of the month-scale (degrees). */
  public static final Double DELTA_DEC_TO_MONTH_SCALE_TOP = 4.0;

  /** Params are in pixels. */
  public ChartUtil(double width, double height) {
    this.width = width;
    this.height = height;
  }
  
  /** WARNING: some operations need to have the width passed to the other constructor. */
  public ChartUtil() {};
  
  /** Chart width in pixels. */
  public double getWidth() {
    return width;
  }
  
  /** Chart width in pixels. */
  public int getWidthInt() {
    return Maths.round(width);
  }
  
  /** Chart height in pixels. */
  public double getHeight() {
    return height;
  }
  
  /** Chart height in pixels. */
  public int getHeightInt() {
    return Maths.round(height);
  }
  
  /** Leave some blank space near the spine. Can't use the entire height of the page. */
  public double getHeightMinusSpineMargin() {
    return getHeight() - percentWidth(SPINE_MARGIN);
  }
  
  public double getSpineMargin() {
    return percentWidth(SPINE_MARGIN);
  }
  
  /** WARNING: hard-coded to English, for the moment. */
  public static Lang lang() {
    return Lang.en;
  }

  /** Returns pixels. */
  public double percentWidth(double percent) {
    return width * percent / 100.0;
  }
  
  /** Returns pixels. */
  public double percentHeight(double percent) {
    return height * percent / 100.0;
  }
  
  public double borderWidth() {
    return percentWidth(BORDER_WIDTH);
  }
  
  public double borderWidthDateScale() {
    return percentWidth(BORDER_WIDTH_DATE_SCALE);
  }
  
  public double borderWidthAlphabet() {
    return percentWidth(BORDER_WIDTH_ALPHABET);
  }
  
  public double borderWidthPlusDateScaleWidth() {
    return borderWidth() + borderWidthDateScale();
  }
  
  public double borderWidthPlusAlphabetWidth() {
    return borderWidth() + borderWidthAlphabet();
  }
  
  /** Perform a drawing operation in grey. Template method.*/
  public void drawGrey(Graphics2D g, Consumer<Graphics2D> drawer) {
    drawColor(greyScale(), g, drawer);
  }
  
  /**
   Return the angle by which an item is to be rotated, to center on the celestial pole. 
   Polar charts only.
   @param baseCirum a base point on the circumference somewhere. 
   @return the radians by which text should be rotated in order to be 'axially symmetric' with respect to the pole. 
  */
  public double rotationAngle(Projection projection, Point2D.Double baseCircum) {
    Point2D.Double ctr = projection.centerOfProj();
    double dxTheta = baseCircum.x - ctr.x;
    double dyTheta = baseCircum.y - ctr.y;
    //where we are on the circumference
    double theta = Math.atan2(dyTheta, dxTheta); //measured from 3 o'clock to the hour angle; increase clockwise towards 6 o'clock
    //the text needs to be rotated, so we use an affine transform: move to the point, then rotate by this angle
    double result = Math.PI/2.0 + theta; //the text is rotated, such that it runs perpendicular to the radius at that point
    return result;
  }
  
  /**
   Perform a drawing operation in a grid that's rotated with respect to the current grid. 
   AffineTransfrom: move to the given point, rotate by the given angle, then draw in the new coordinate system. 
   Template method.
  */
  public void drawRotated(Graphics2D g, double rotationAngle, Point2D.Double point, Consumer<Graphics2D> drawer) {
    AffineTransform origTr = g.getTransform();
    AffineTransform affTr = new AffineTransform();
    affTr.translate(point.x, point.y);
    affTr.rotate(rotationAngle);
    g.transform(affTr);
    drawer.accept(g);
    //go back to the old world
    g.setTransform(origTr);
  }
  
  /** Perform a drawing operation in a grid that's rotated with respect to the current grid, and using grey. */ 
  public void drawRotatedAndGrey(Graphics2D g, double rotationAngle, Point2D.Double point, Consumer<Graphics2D> drawer) {
    Color origColor = g.getColor();
    g.setColor(greyScale());
    
    AffineTransform origTr = g.getTransform();
    AffineTransform affTr = new AffineTransform();
    affTr.translate(point.x, point.y);
    affTr.rotate(rotationAngle);
    g.transform(affTr);
    drawer.accept(g);
    
    //go back to the old world
    g.setTransform(origTr);
    g.setColor(origColor);
  }

  /** Render text that's bigger than normal by the given size factor. */
  public void drawTextFontSize(String text, double x, double y, float fontSizeFactor, Graphics2D g) {
    Font originalFont = g.getFont();
    //the text alignment depends on the font size, so that needs to be set early
    Font biggerFont = resizedFont(fontSizeFactor, g);
    g.setFont(biggerFont);
    g.drawString(text, Maths.round(x), Maths.round(y));
    g.setFont(originalFont);
  }
  
  /** Render text in italic. */
  public void drawTextItalic(String text, double x, double y, Graphics2D g) {
    Font originalFont = g.getFont();
    //the text alignment depends on the font size, so that needs to be set early
    Font italicFont = italicBaseFont();
    g.setFont(italicFont);
    g.drawString(text, Maths.round(x), Maths.round(y));
    g.setFont(originalFont);
  }
  
  /** Render text in bold. */
  public void drawTextBold(String text, double x, double y, Graphics2D g) {
    Font originalFont = g.getFont();
    //the text alignment depends on the font size, so that needs to be set early
    Font boldFont = boldBaseFont();
    g.setFont(boldFont);
    g.drawString(text, Maths.round(x), Maths.round(y));
    g.setFont(originalFont);
  }
  
  /** Render text centered on the given spot. */
  public Point2D.Double centerTextOn(double x, double y, String text, Graphics2D g) {
    return new Point2D.Double(x - textWidth(text, g)/2, y + textHeight(text, g)/2);
  }
  
  /** Render text centered vertically on the given spot. */
  public Point2D.Double centerTextVerticallyOn(double x, double y, String text, Graphics2D g) {
    return new Point2D.Double(x, y + textHeight(text, g)/2);
  }
  
  /** Render text centered horizontally on the given spot. */
  public Point2D.Double centerTextHorizontallyOn(int x, int y, String text, Graphics2D g) {
    return new Point2D.Double(x - textWidth(text, g)/2, y);
  }

  /** Return the height of the given text, when rendered in the given context. */
  public int textHeight(String str, Graphics2D g) {
    LineMetrics lm = g.getFont().getLineMetrics(str, g.getFontRenderContext());
    float ascent = lm.getAscent();
    float descent = lm.getDescent();
    float height = ascent + descent; //don't include the leading!
    float FUDGE_FACTOR_FOR_AESTHETIC_PURPOSES = 0.60F; //withouth this the centering is off - too low
    return Maths.round(height*FUDGE_FACTOR_FOR_AESTHETIC_PURPOSES); 
  }

  /** Return the width of the given text, when rendered in the given context. */
  public int textWidth(String str, Graphics2D g) {
    return g.getFontMetrics().stringWidth(str);
  }

  /** Return the current font, but resized by the given factor. */
  public Font resizedFont(float factor, Graphics2D g) {
    Font currentFont = g.getFont();
    Font result = currentFont.deriveFont(currentFont.getSize() * factor);
    return result;
  }

  public static Font baseFont() {
    return new Font(BASE_FONT_NAME, Font.PLAIN, BASE_FONT_SIZE);
    //return FontFactory.getFont(BASE_FONT_NAME, BaseFont.IDENTITY_H, BASE_FONT_SIZE, com.itextpdf.text.Font.NORMAL);
  }
      
  public static Font modifiedBaseFont(int size) {
    return new Font(BASE_FONT_NAME, Font.PLAIN, size); 
  }
  
  public static Font italicBaseFont() {
    return new Font(BASE_FONT_NAME, Font.ITALIC, BASE_FONT_SIZE); 
  }
  
  public static Font boldBaseFont() {
    return new Font(BASE_FONT_NAME, Font.BOLD, BASE_FONT_SIZE); 
  }
  
  /**
   The exact grey color to use when drawing grey. 
   Use the template method {@link #drawGrey(Graphics2D, Consumer)} when the code is short, or when you have a named method. 
  */
  public Color greyScale() {
    //int val = 100; //too dim
    int val = 0; //not grey! 
    Color result = new Color(val, val, val);
    return result;
  }
  
  /**
   Return true only if valRads is near a multiple of targetDegs. 
   CAREFUL: one val is degs, one is rads. Rounds to the nearest degree. 
  */
  public boolean isNearMultipleOf(double targetDegs, double valRads) {
    //WEIRD: we need to unwind rads to degrees here!
    //the reason is rounding: if the number is like 19.9998, then the remainder is 0.9998, not 0.0002
    double val = Math.round(Math.abs(Maths.radsToDegs(valRads)));
    boolean result = (val % targetDegs == 0);
    return result;
  }
  
  /** 
   Return true only if valRads is near targetDegs.
   CAREFUL: one val is degs, one is rads. Rounds to the nearest degree. 
  */
  public boolean isNear(double targetDegs, double valRads) {
    double val = Math.round(Maths.radsToDegs(valRads));
    boolean result = (targetDegs == val);
    return result;
  }
  
  /**
   Return true only if valRads is near targetHour 
   CAREFUL: one val is hours, one is rads. Rounds to the nearest degree. 
  */
  public boolean isNearHour(Double targetHour, double valRads) {
    double targetDegs = targetHour * 15.0;
    return isNear(targetDegs, valRads);
  }
  
  /** 
   Render text on a white rectangle. 
   Use to ensure that the given text is always shown clearly.
   Use sparingly.
   The idea is that writing a white rectangle will wash out things, before writing text on the same spot. 
  */
  public void overwritingWhiteRect(String text, Point2D.Double target, Graphics2D g) {
    Rectangle rect = getTextBounds(text, target.x, target.y, g);
    Consumer<Graphics2D> drawer = x -> {
      x.fill(rect);
    }; 
    drawColor(Color.WHITE, g, drawer);
    g.drawString(text, Maths.round(target.x), Maths.round(target.y));
  }
  
  /** Clip all drawing to the interior of the chart, where the stars and lines are. */
  public void clippingOn(Projection projection, Graphics2D g) {
    Shape clip = projection.innerBoundary();
    g.setClip(clip);
  }
  
  /** Turn off all clipping. */
  public void clippingOff(Graphics2D g) {
    g.setClip(null);
  }

  /** 
   Render a wee dot at the given location.
   Useful for investigating exact positions.
   Intended for debugging only.
   Nice: this remains a single pixel at all scales! 
  */
  public void debuggingDot(Point2D.Double target, Graphics2D g) {
    g.drawLine(Maths.round(target.x), Maths.round(target.y), Maths.round(target.x), Maths.round(target.y));
  }

  // PRIVATE
  
  private double width;
  private double height;
  
  /** Do a drawing operation in a given color. Template method. */
  private void drawColor(Color color, Graphics2D g, Consumer<Graphics2D> drawer) {
    Color origColor = g.getColor();
    g.setColor(color);
    drawer.accept(g);
    g.setColor(origColor);
  }

  /**
   Return a rectangle that bounds the given text at the given position.
 
   The xy correspond to what is passed to drawString().
   When you just want the height and width, you can just pass 0,0 for x,y, because you don't really 
   care about where it is on the canvas.
  
   <P>With the rectangle, one can compute the exact width and height of the text. 
   https://stackoverflow.com/questions/368295/how-to-get-real-string-height-in-java
   https://stackoverflow.com/questions/34690321/java-graphics-random-text
 
   Q: this seems to trim the text.
   Q: this seems to not work for the declination numbers in a polar chart (bottom only), where I'm trying to 
   overwrite with a white rectangle.
  */
  private Rectangle getTextBounds(String str, double x, double y, Graphics2D g) {
    FontRenderContext frc = g.getFontRenderContext();
    GlyphVector gv = g.getFont().createGlyphVector(frc, str);
    return gv.getPixelBounds(null, (float)x, (float)y);
  }
}