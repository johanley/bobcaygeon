package mag5.draw.equatorial;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mag5.deepskyobject.DeepSkyObject;
import mag5.deepskyobject.DsoType;
import mag5.draw.ChartUtil;
import mag5.draw.DrawDeepSkyObject;
import mag5.draw.DrawPosition;
import mag5.draw.Projection;
import mag5.util.Maths;

/** 
 Draw the deep sky object's name near the object itself.
 Can be drawn in different nearby places, to avoid collisions with other items.
 
<P>
   A small number of objects have their name actually suppressed, because rendering them causes 
   excessive crowding and overlap (with the current implementation). This happens 
   in the region near Virgo, in a crowded field of galaxies.
*/
public class DrawDsoNameEquatorial {
  
  public DrawDsoNameEquatorial(List<DeepSkyObject> dsos, Map<String, Point2D.Double> dsoPoints, ChartUtil chartUtil, Projection projection, Graphics2D g){
    this.dsos = dsos;
    this.dsoPoints = dsoPoints;
    this.chartUtil = chartUtil;
    this.projection = projection;
    this.g = g;
  }

  /** 
   Render the designation of the DSO, for example 'M20' (Messier objects) or 'N1234' (NGC objects).
  */
  public void draw() {
    for (DeepSkyObject dso : dsos) {
      if (!IS_SUPPRESSED.contains(dso.getDesig())) {
        drawNameOnCompassPoint(dso, dso.getDesig());
      }
    }
  }
  
  //PRIVATE 
  private List<DeepSkyObject> dsos;
  private Map<String, Point2D.Double> dsoPoints;
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;
  
  /** Items whose names are prevented from rendering in the usual way. */
  private static List<String> IS_SUPPRESSED = new ArrayList<String>();
  static {
    //Virgo cluster
    IS_SUPPRESSED.add("M86");
    IS_SUPPRESSED.add("M87");
    IS_SUPPRESSED.add("M89");
    IS_SUPPRESSED.add("M58");
    IS_SUPPRESSED.add("M59");
    IS_SUPPRESSED.add("M99");
  }

  private void drawNameOnCompassPoint(DeepSkyObject dso, String name) {
    DrawPosition pos = DrawPosition.findPosFrom(dso.getCompassPoint(), projection);
    if (DrawPosition.ABOVE == pos) {
      drawNameAbove(dso, name);
    }
    else if (DrawPosition.BELOW == pos) {
      drawNameBelow(dso, name);
    }
    else if (DrawPosition.LEFT == pos) {
      drawNameOnLeft(dso, name);
    }
    else {
      drawNameOnRight(dso, name);
    }
  }
  
  /** Simple delta-y with respect to the dso's exact position. */
  private void drawNameAbove(DeepSkyObject dso, String name) {
    Point2D.Double where = dsoPoints.get(dso.getDesig());
    double tweak = tweakForNameAbove(dso, name);
    int sign = -1;
    Point2D.Double pName = chartUtil.centerTextOn(where.x, where.y + sign * tweak, name, g);
    g.drawString(name, Maths.asFloat(pName.x), Maths.asFloat(pName.y)); 
  }
  
  private double tweakForNameAbove(DeepSkyObject dso, String name) {
    double nameHeight = chartUtil.textHeight(name, g);
    double factor = dso.getDsoType() == DsoType.GALAXY ? 0.50 : 0.75;
    if (name.equals("M45")) {
      factor = 2.0 * factor; //cheating! hard-coded! so crowded there...
    }
    return factor*nameHeight + DrawDeepSkyObject.sizeOfDso(projection);  
  }

  private void drawNameBelow(DeepSkyObject dso, String name) {
    Point2D.Double where = dsoPoints.get(dso.getDesig());
    double tweak = tweakForNameBelow(dso, name);
    int sign = +1;
    Point2D.Double pName = chartUtil.centerTextOn(where.x, where.y + sign * tweak, name, g);
    g.drawString(name, Maths.asFloat(pName.x), Maths.asFloat(pName.y)); 
  }
  
  private double tweakForNameBelow(DeepSkyObject dso, String name) {
    double nameHeight = chartUtil.textHeight(name, g);
    double factor = 1.1;
    if (name.equals("M45")) {
      factor = 2.0; //cheating! hard-coded; so crowded there...
    }
    return factor*nameHeight + DrawDeepSkyObject.sizeOfDso(projection); 
  }
  
  private void drawNameOnRight(DeepSkyObject dso, String name) {
    Point2D.Double where = dsoPoints.get(dso.getDesig());
    double tweak = tweakForNameOnRight(dso);
    Point2D.Double pName = chartUtil.centerTextVerticallyOn(where.x + tweak, where.y, name, g);
    g.drawString(name, Maths.asFloat(pName.x), Maths.asFloat(pName.y)); 
  }
  
  private void drawNameOnLeft(DeepSkyObject dso, String name) {
    Point2D.Double where = dsoPoints.get(dso.getDesig());
    double tweak = tweakForNameOnLeft(dso, name);
    Point2D.Double pName = chartUtil.centerTextOn(where.x - tweak, where.y, name, g);
    g.drawString(name, Maths.asFloat(pName.x), Maths.asFloat(pName.y)); 
  }
  
  private double tweakForNameOnLeft(DeepSkyObject dso, String name) {
    double nameWidth = chartUtil.textWidth(name.trim(), g);
    double tooWide = chartUtil.textWidth("w", g); //I have no clue why this is needed!
    if (name.startsWith("N")) {
      tooWide = 1.5 * tooWide;
    }
    return nameWidth - tooWide + DrawDeepSkyObject.sizeOfDso(projection); 
  }
  
  private double tweakForNameOnRight(DeepSkyObject dso) {
    return 1.3*DrawDeepSkyObject.sizeOfDso(projection); 
  }
}