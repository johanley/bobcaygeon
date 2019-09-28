package mag5.draw.polar;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import mag5.deepskyobject.DeepSkyObject;
import mag5.draw.ChartUtil;
import mag5.draw.DrawDeepSkyObject;
import mag5.draw.DrawPosition;
import mag5.draw.Projection;
import mag5.util.Maths;

/** Render the star designation for a polar chart. */
public class DrawDsoNamePolar {

  public DrawDsoNamePolar(List<DeepSkyObject> dsos, Map<String, Point2D.Double> dsoPoints, ChartUtil chartUtil, Projection projection, Graphics2D g){
    this.dsos = dsos;
    this.dsoPoints = dsoPoints;
    this.chartUtil = chartUtil;
    this.projection = projection;
    this.g = g;
  }

  /** The star's designation is rotated, to be symmetric with respect to the celestial pole. */
  public void draw() {
    for (DeepSkyObject dso : dsos) {
      drawNameOnCompassPoint(dso, dso.getDesig());
    }
  }
  
  //PRIVATE 
  
  private List<DeepSkyObject> dsos;
  private Map<String, Point2D.Double> dsoPoints;
  private Projection projection;
  private Graphics2D g;
  private ChartUtil chartUtil;

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
    Consumer<Graphics2D> drawer = x -> {
      double tweak = tweakForNameAbove(dso, name);
      int sign = -1;
      Point2D.Double centered = chartUtil.centerTextOn(0, 0 + sign * tweak, name, g);
      x.drawString(name, Maths.asFloat(centered.x), Maths.asFloat(centered.y));
    };
    drawTextWrtPole(dso, drawer);
  }
  
  private double tweakForNameAbove(DeepSkyObject dso, String name) {
    double nameHeight = chartUtil.textHeight(name, g);
    return 1.0*nameHeight + DrawDeepSkyObject.sizeOfDso(projection); 
  }
  
  private void drawTextWrtPole(DeepSkyObject dso, Consumer<Graphics2D> drawer) {
    Point2D.Double starPoint = dsoPoints.get(dso.getDesig());
    double rotationAngle = chartUtil.rotationAngle(projection, starPoint);
    chartUtil.drawRotated(g, rotationAngle, starPoint, drawer);
  }

  private void drawNameBelow(DeepSkyObject dso , String name) {
    Consumer<Graphics2D> drawer = x -> {
      double tweak = tweakForNameBelow(dso, name);
      int sign = +1;
      Point2D.Double centered = chartUtil.centerTextOn(0, 0 + sign * tweak, name, g);
      x.drawString(name, Maths.asFloat(centered.x), Maths.asFloat(centered.y));
    };
    drawTextWrtPole(dso, drawer);
  }
  
  private double tweakForNameBelow(DeepSkyObject dso, String name) {
    double nameHeight = chartUtil.textHeight(name, g);
    return 1.1*nameHeight + DrawDeepSkyObject.sizeOfDso(projection); 
  }
  
  private void drawNameOnRight(DeepSkyObject dso, String name) {
    Consumer<Graphics2D> drawer = x -> {
      double tweak = tweakForNameOnRight(dso);
      Point2D.Double centered = chartUtil.centerTextVerticallyOn(0 + tweak, 0, name, g);
      x.drawString(name, Maths.asFloat(centered.x), Maths.asFloat(centered.y));
    };
    drawTextWrtPole(dso, drawer);
  }
  
  private double tweakForNameOnRight(DeepSkyObject dso) {
    return 1.3*DrawDeepSkyObject.sizeOfDso(projection); 
  }
  
  private void drawNameOnLeft(DeepSkyObject dso, String name) {
    Consumer<Graphics2D> drawer = x -> {
      double tweak = tweakForNameOnLeft(dso, name);
      Point2D.Double centered = chartUtil.centerTextOn(0 - tweak, 0, name, g);
      x.drawString(name, Maths.asFloat(centered.x), Maths.asFloat(centered.y));
    };
    drawTextWrtPole(dso, drawer);
  }
  
  private double tweakForNameOnLeft(DeepSkyObject dso, String name) {
    double nameWidth = chartUtil.textWidth(name.trim(), g);
    double tooWide = chartUtil.textWidth("m", g); //I have no clue why this is needed!
    if (name.startsWith("N")) {
      tooWide = 1.5 * tooWide;
    }
    return nameWidth - tooWide + DrawDeepSkyObject.sizeOfDso(projection); 
  }
}