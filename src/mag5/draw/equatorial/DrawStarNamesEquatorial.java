package mag5.draw.equatorial;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mag5.draw.ChartUtil;
import mag5.draw.CompassPoint;
import mag5.draw.DrawPosition;
import mag5.draw.DrawStars;
import mag5.draw.Projection;
import mag5.star.Star;
import mag5.util.Maths;

/**
 Render the star's designation (Bayer letter) near the star's position. 
 
 <P>In general, this is a hard problem: a result which is pleasing to the eye, and in which
 there's no overlap of items, is not easy to do.
 This implementation is satisfactory, but not perfect. It uses simple 'compass-point' placement, 
 which has 4 choices for the position. Usually that gives an acceptable result, but not always.
 
 <P>When crowding is a problem, then some star designations are actually suppressed entirely (about 25 in number).
 This is deemed acceptable: again, the purpose of the charts is to teach the constellations, not 
 to be an authoritave or complete atlas.
 
  <P>PROBLEM: this centering of text seems to be off a bit. The code snippets found on the web 
  for vertical and horizontal centering of text don't seem to be robust.
*/
public class DrawStarNamesEquatorial {
 
  public DrawStarNamesEquatorial(List<Star> stars, Map<Integer, Point2D.Double> starPoints, ChartUtil chartUtil, Projection projection, Graphics2D g){
    this.stars = stars;
    this.starPoints = starPoints;
    this.chartUtil = chartUtil;
    this.projection = projection;
    this.g = g;
  }

  /** Render the designation of the star, near to the star's position. */
  public void draw() {
    for (Star star : stars) {
      //drawIndexIfBayerPresent(star);
      //drawName(star, star.getBayer());
      if (!IS_SUPPRESSED.contains(star.INDEX)) {
        drawNameOnCompassPoint(star, star.getBayer());
      }
    }
  }
  
  //PRIVATE
  
  private List<Star> stars;
  private Map<Integer, Point2D.Double> starPoints;
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  
  /**
   Some spots are very crowded.
   Suppresses the star designation when things get too crowded. 
  */
  private static List<Integer> IS_SUPPRESSED = new ArrayList<>();
  static {
    //Hyades
    IS_SUPPRESSED.add(1406);
    IS_SUPPRESSED.add(1407);
    IS_SUPPRESSED.add(1368);
    IS_SUPPRESSED.add(1375);
    IS_SUPPRESSED.add(1384);
    //M42 region
    IS_SUPPRESSED.add(1893);
    IS_SUPPRESSED.add(1889);
    IS_SUPPRESSED.add(8416); // mu PsA 
    IS_SUPPRESSED.add(5702); // phi-2 Lupus
    IS_SUPPRESSED.add(4932); // xi-1 Cen 
    IS_SUPPRESSED.add(4923); // xi-2 Cen
    IS_SUPPRESSED.add(7179); // lambda Lyr
    IS_SUPPRESSED.add(5952); // eta Nor
    IS_SUPPRESSED.add(7138); // xi2 Sgr
    IS_SUPPRESSED.add(7108); // nu2 Sgr  
    IS_SUPPRESSED.add(7104); // nu1 Sgr
    IS_SUPPRESSED.add(7740); // alpha2 Cap
    IS_SUPPRESSED.add(5983); // omega1 Sco
    IS_SUPPRESSED.add(5987); // omega2 Sco
    IS_SUPPRESSED.add(6242); // mu2 Sco
    IS_SUPPRESSED.add(261); //ups Cas
    IS_SUPPRESSED.add(249); //ups Cas
    IS_SUPPRESSED.add(6062); //gamma-2 Nor
    IS_SUPPRESSED.add(6730); //gamma-1 Sgr
  }
  
  private int targetSize(Star star) {
    return DrawStars.starSize(star);
  }
  
  private void drawNameOnCompassPoint(Star star, String name) {
    //if absent (the default), coerce to South
    CompassPoint compassPoint = star.BAYER_COMPASS_POINT == null ? CompassPoint.S : star.BAYER_COMPASS_POINT;
    DrawPosition pos = DrawPosition.findPosFrom(compassPoint, projection);
    
    if (DrawPosition.ABOVE == pos) {
      drawNameAbove(star, name);
    }
    else if (DrawPosition.BELOW == pos) {
      drawNameBelow(star, name);
    }
    else if (DrawPosition.LEFT == pos) {
      drawNameOnLeft(star, name);
    }
    else if (DrawPosition.RIGHT == pos){
      drawNameOnRight(star, name);
    }
  }

  /** 
   Simple delta-y with respect to the star's exact position.
   Problems with :
     crowding
     constellation lines (especially when the line is near vertical on the page)
     some letters are small, and render a bit too far away (would need custom overrides per letter) 
  */
  private void drawName(Star star, String name) {
    Point2D.Double where = starPoints.get(star.INDEX);
    //int tweak = 2*DrawStars.starSize(star) + 3; //bit too far
    //int tweak = Maths.round(chartUtil.percentHeight(0.25)); //way too close
    //int tweak = Maths.round(nameHeight + Maths.round(chartUtil.percentHeight(0.5))); //ok
    double tweak = tweakForBayer(star, name);
    Point2D.Double pName = chartUtil.centerTextOn(where.x, where.y + tweak, name, g);
    g.drawString(name, Maths.round(pName.x), Maths.round(pName.y)); 
  }
  
  private double tweakForBayer(Star star, String name) {
    double nameHeight = chartUtil.textHeight(name, g);
    return nameHeight + DrawStars.starSize(star); 
  }
  
  /** Used for development. Shows the stars index, not Bayer designation. */
  private void drawIndexIfBayerPresent(Star star) {
    if (star.getBayer() != null && star.getBayer().length()>0) {
      Point2D.Double where = starPoints.get(star.INDEX);
      String name = star.INDEX.toString();
      Font originalFont = g.getFont();
      //the text alignment depends on the font size, so that needs to be set early
      Font biggerFont = chartUtil.resizedFont(0.5f, g);
      g.setFont(biggerFont);
      double tweak = tweakForBayer(star, name);
      Point2D.Double pName = chartUtil.centerTextOn(where.x, where.y + tweak, name, g);
      g.drawString(name, Maths.round(pName.x), Maths.round(pName.y)); 
      g.setFont(originalFont);
    }
  }
  
  /** Simple dy with respect to the dso's exact position. */
  private void drawNameAbove(Star star, String name) {
    Point2D.Double where = starPoints.get(star.INDEX);
    double tweak = tweakForNameAbove(star, name);
    int sign = -1;
    Point2D.Double pName = chartUtil.centerTextOn(where.x, where.y + sign * tweak, name, g);
    g.drawString(name, Maths.asFloat(pName.x), Maths.asFloat(pName.y)); 
  }
  
  private double tweakForNameAbove(Star star, String name) {
    double nameHeight = chartUtil.textHeight(name, g);
    return 1.0*nameHeight + targetSize(star); 
  }

  private void drawNameBelow(Star star, String name) {
    Point2D.Double where = starPoints.get(star.INDEX);
    double tweak = tweakForNameBelow(star, name);
    int sign = +1;
    Point2D.Double pName = chartUtil.centerTextOn(where.x, where.y + sign * tweak, name, g);
    g.drawString(name, Maths.asFloat(pName.x), Maths.asFloat(pName.y)); 
  }
  
  private double tweakForNameBelow(Star star, String name) {
    double nameHeight = chartUtil.textHeight(name, g);
    return 1.1*nameHeight + targetSize(star); 
  }
  
  private void drawNameOnRight(Star star, String name) {
    Point2D.Double where = starPoints.get(star.INDEX);
    double tweak = tweakForNameOnRight(star);
    Point2D.Double pName = chartUtil.centerTextVerticallyOn(where.x + tweak, where.y, name, g);
    g.drawString(name, Maths.asFloat(pName.x), Maths.asFloat(pName.y)); 
  }
  
  private double tweakForNameOnRight(Star star) {
    return 1.3*DrawStars.starSize(star); 
  }
  
  private void drawNameOnLeft(Star star, String name) {
    Point2D.Double where = starPoints.get(star.INDEX);
    double tweak = tweakForNameOnLeft(star, name);
    Point2D.Double pName = chartUtil.centerTextOn(where.x - tweak, where.y, name, g);
    g.drawString(name, Maths.asFloat(pName.x), Maths.asFloat(pName.y)); 
  }
  
  private double tweakForNameOnLeft(Star star, String name) {
    double nameWidth = chartUtil.textWidth(name, g);
    return nameWidth + targetSize(star); 
  }
}