package mag5.draw;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.List;

import mag5.deepskyobject.DeepSkyObject;
import mag5.deepskyobject.DsoType;
import mag5.util.Maths;

/** Draw the deep sky objects attached to a chart. Includes the Magellanic Clouds. */
public class DrawDeepSkyObject {
  
  DrawDeepSkyObject(List<DeepSkyObject> deepSkyObjects, Projection projection, Graphics2D g) {
    this.deepSkyObjects = deepSkyObjects;
    this.projection = projection;
    this.g = g;
  }

  /**
   Draws a symbol designating a type of DSO. 
   Symbols should be drawn with 'fill', so that items in the background (lines) are overdrawn. 
  */
  void draw() {
    int size = sizeOfDso(projection);
    for(DeepSkyObject dso : deepSkyObjects) {
      Point2D.Double target = projection.project(dso.getDec(), dso.getRa());
      if (DsoType.GLOBULAR_CLUSTER == dso.getDsoType()) {
        drawGlobularCluster(target, size, g);
      }
      else if (DsoType.OPEN_CLUSTER == dso.getDsoType()) {
        drawOpenCluster(target, size, g);
      }
      else if (DsoType.GALAXY == dso.getDsoType()) {
        drawGalaxy(target, size, g);
      }
      else {
        drawNebula(target, size, g);
      }
      //g.drawRect(target.x, target.y, 1, 1); //debugging only
    }
    drawCloudsOfMagellan();
  }

  /** In pixels. */
  public static int sizeOfDso(Projection projection) {
    double sizeRads = Maths.degToRads(ChartUtil.DEEP_SKY_OBJECT_SIZE);
    return Maths.round(sizeRads * projection.distancePerRad());
  }
  
  /** 
   Circle with a cross in the middle.
   The Edmund Mag 5 style looks too much like two stars on top of each other. 
  */
  public static void drawGlobularCluster(Point2D.Double target, int radius, Graphics2D g) {
    int w = radius*2;
    int h = w;
    int x = Maths.round(target.x);
    int y = Maths.round(target.y);
    g.drawOval(x - radius, y - radius, w, h);
    //cross over the center of the circle
    g.drawLine(x, y - radius, x, y + radius);
    g.drawLine(x - radius, y, x + radius, y);
  }
  
  /** Dashed circle. Centered on the target. */
  public static void drawOpenCluster(Point2D.Double target, int radius, Graphics2D g) {
    //dashed line https://docs.oracle.com/javase/tutorial/2d/geometry/strokeandfill.html
    float dashes[] = {1.0f};
    float dashPhaseOffset = 1.0f;
    BasicStroke dashed = new BasicStroke(1.0f/*width*/, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f /*miter*/, dashes, dashPhaseOffset);
    Stroke regularStroke = g.getStroke();
    g.setStroke(dashed);
    
    int w = radius*2;
    int h = w;
    g.drawOval(Maths.round(target.getX() - radius), Maths.round(target.getY() - radius), w, h);
    
    g.setStroke(regularStroke);
  }
  
  /** Oval. Centered on the target. */
  public static void drawGalaxy(Point2D.Double target, int radius, Graphics2D g) {
    int w = radius*2;
    int h = w/2;
    int x = Maths.round(target.getX() - w/2);
    int y = Maths.round(target.getY() - h/2);
    g.drawOval(x, y, w, h);
  }
  
  /** Small square centered on the given point. */
  public static void drawNebula(Point2D.Double target, int radius, Graphics2D g) {
    int rad = radius - 1;
    int w = rad*2;
    int h = w;
    double x = target.getX();
    double y = target.getY();
    g.drawRect(Maths.round(x-rad), Maths.round(y-rad), w, h);
  }
  
  // PRIVATE 
  private List<DeepSkyObject> deepSkyObjects;
  private Projection projection;
  private Graphics2D g;
  
  private void drawCloudsOfMagellan() {
    boolean isTopChartForSouthPole = projection.getBounds().minDecDeg.equals(-90.0) && projection.getBounds().isTopChart(); 
    if (isTopChartForSouthPole) {
      drawMagellanicCloud(0.917, -73, 4.5/2, 0.5, 80); //small 
      drawMagellanicCloud(5.25, -68, 6.7/2, 0.5, -20); //large 
    }
  }
  
  /** Rendered as a simple ellipse. */
  private void drawMagellanicCloud(double raHrs, double decDeg, double radiusDeg, double eccentricity, double rotation) {
    double ra = Maths.hoursToRads(raHrs);
    double dec = Maths.degToRads(decDeg);
    Point2D.Double cloudCenter = projection.project(dec, ra);
    
    double radius = projection.distancePerRad() * Maths.degToRads(radiusDeg);
    int w = Maths.round(radius*2);
    int h = Maths.round(eccentricity * w); //ellipse
    float dashes[] = {1.0f};
    float dashPhaseOffset = 1.0f;
    BasicStroke dashed = new BasicStroke(1.0f/*width*/, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f /*miter*/, dashes, dashPhaseOffset);
    Stroke regularStroke = g.getStroke();
    g.setStroke(dashed);

    ChartUtil chartUtil = new ChartUtil();
    chartUtil.drawRotated(
        g, Maths.degToRads(rotation), cloudCenter, 
        x -> x.drawOval(Maths.round(0 - radius), Maths.round(0 - radius), w, h)
    );
    g.setStroke(regularStroke);
  }
}