package mag5.draw.polar;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import mag5.draw.ChartUtil;
import mag5.draw.CompassPoint;
import mag5.draw.DrawPosition;
import mag5.draw.DrawStars;
import mag5.draw.Projection;
import mag5.star.Star;
import mag5.util.Maths;

/**
Place the star's designation near the star's position.

In general, this is a hard problem: a result which is pleasing to the eye, and in which
there's no overlap of items, is not easy to do.
*/
public class DrawStarNamesPolar {
  
   public DrawStarNamesPolar(List<Star> stars, Map<Integer, Point2D.Double> starPoints, ChartUtil chartUtil, Projection projection, Graphics2D g){
     this.stars = stars;
     this.starPoints = starPoints;
     this.chartUtil = chartUtil;
     this.projection = projection;
     this.g = g;
   }

   /**
    The designation of the star is rotated, to be axially symmetric with respect to the celestial pole.
    A small number of stars have their designation suppressed, because of crowding problems. 
   */
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
   
   private static List<Integer> IS_SUPPRESSED = new ArrayList<>();
   static {
     //theta crux
     IS_SUPPRESSED.add(4589);
     IS_SUPPRESSED.add(4593);
     //upsilon Cas
     IS_SUPPRESSED.add(261);
     IS_SUPPRESSED.add(249);
   }
   
   private int targetSize(Star star) {
     return DrawStars.starSize(star);
   }
   
   /** 
    WARNING: here, the compass-point is REINTERPRETED with respect to the direction of the celestial pole! 
   */
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
    Problems with:
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

   /** For development only, to see the star's index. */
   @SuppressWarnings("unused")
  private void drawIndexIfBayerPresent(Star star) {
     if (star.getBayer() != null && star.getBayer().length()>0) {
       Point2D.Double where = starPoints.get(star.INDEX);
       //int tweak = 2*DrawStars.starSize(star) + 3; //bit too far
       //int tweak = Maths.round(chartUtil.percentHeight(0.25)); //way too close
       //int tweak = Maths.round(nameHeight + Maths.round(chartUtil.percentHeight(0.5))); //ok
       
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
   
   /** Simple delta-y with respect to the dso's exact position. */
   private void drawNameAbove(Star star, String name) {
     Consumer<Graphics2D> drawer = x -> {
       double tweak = tweakForNameAbove(star, name);
       int sign = -1;
       Point2D.Double centered = chartUtil.centerTextOn(0, 0 + sign * tweak, name, g);
       x.drawString(name, Maths.asFloat(centered.x), Maths.asFloat(centered.y));
     };
     drawTextWrtPole(star, drawer);
   }
   
   private double tweakForNameAbove(Star star, String name) {
     double nameHeight = chartUtil.textHeight(name, g);
     return 1.0*nameHeight + targetSize(star); 
   }
   
   private void drawTextWrtPole(Star star, Consumer<Graphics2D> drawer) {
     Point2D.Double starPoint = starPoints.get(star.INDEX);
     double rotationAngle = chartUtil.rotationAngle(projection, starPoint);
     chartUtil.drawRotated(g, rotationAngle, starPoint, drawer);
   }
   
   private void drawNameBelow(Star star, String name) {
     Consumer<Graphics2D> drawer = x -> {
       double tweak = tweakForNameBelow(star, name);
       int sign = +1;
       Point2D.Double centered = chartUtil.centerTextOn(0, 0 + sign * tweak, name, g);
       x.drawString(name, Maths.asFloat(centered.x), Maths.asFloat(centered.y));
     };
     drawTextWrtPole(star, drawer);
   }
   
   private double tweakForNameBelow(Star star, String name) {
     double nameHeight = chartUtil.textHeight(name, g);
     return 1.1*nameHeight + targetSize(star); 
   }
   
   private void drawNameOnRight(Star star, String name) {
     Consumer<Graphics2D> drawer = x -> {
       double tweak = tweakForNameOnRight(star);
       Point2D.Double centered = chartUtil.centerTextVerticallyOn(0 + tweak, 0, name, g);
       x.drawString(name, Maths.asFloat(centered.x), Maths.asFloat(centered.y));
     };
     drawTextWrtPole(star, drawer);
   }
   
   private double tweakForNameOnRight(Star star) {
     return 1.3*DrawStars.starSize(star); 
   }
   
   private void drawNameOnLeft(Star star, String name) {
     Consumer<Graphics2D> drawer = x -> {
       double tweak = tweakForNameOnLeft(star, name);
       Point2D.Double centered = chartUtil.centerTextOn(0 - tweak, 0, name, g);
       x.drawString(name, Maths.asFloat(centered.x), Maths.asFloat(centered.y));
     };
     drawTextWrtPole(star, drawer);
   }
   
   private double tweakForNameOnLeft(Star star, String name) {
     double nameWidth = chartUtil.textWidth(name, g);
     return nameWidth + targetSize(star); 
   }  
}