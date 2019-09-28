package mag5.draw;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;

/** Lines joining stars in a given constellation. */
class DrawConstellations {
  
  DrawConstellations(Map<String, List<List<Integer>>> lines, Map<Integer, Point2D.Double> starPoints, Graphics2D g) {
    this.lines = lines;
    this.starPoints = starPoints;
    this.g = g;
  }

  /**
   Only draw lines for which the positions of ALL of its star-points are known.
   Policy (easiest): only draws complete polylines; if any item is missing, then the polyline will not be drawn at all. 
  */
  void draw() {
    for (List<List<Integer>> polylines : lines.values()) {
      for (List<Integer> polyline : polylines) {
        if (allStarPointsAreKnownForThis(polyline)) {
          drawThe(polyline, g);
        }
      }
    }
  }
  
  // PRIVATE 
  private Map<Integer, Point2D.Double> starPoints;
  private Map<String, List<List<Integer>>> lines;
  private Graphics2D g;
  
   private boolean allStarPointsAreKnownForThis(List<Integer> polyline) {
     boolean result = true; //innocent until something is seen to be absent
     for (Integer index : polyline) {
       if (!starPoints.keySet().contains(index)) {
         result = false;
         break;
       }
     }
     return result;
   }
 
   /** Assumes that all points have already been found. */
   private void drawThe(List<Integer> polyline, Graphics2D g) {
     GeneralPath path = new GeneralPath();
     Point2D.Double start = starPoints.get(polyline.get(0));
     path.moveTo(start.x, start.y);
     for(Integer index : polyline.subList(1, polyline.size())) {
       Point2D.Double point = starPoints.get(index);
       path.lineTo(point.x, point.y);
       //find cases where it's a line that crosses the whole chart
       /*
       if (Math.abs(point.x - start.x) > 2500) {
         System.out.println("CROSSES THE WHOLE CHART>2500. Poly: " + polyline + " index:" + index + " point.x:" + point.x + " start.x:" + start.x);
       }
       */
     }
     Stroke orig = g.getStroke();
     //print seems to be finer than screen!
     /*
      * 0.00 is too thin (0 means the minimum possible, to make a mark)
      * 0.25 or 0.35 seem about right
      * let's take 0.25; it matches Edmund Mag 5
      * 1.00 is too thick
      */
     g.setStroke(new BasicStroke(ChartUtil.STROKE_WIDTH_CONSTELLATION_LINE)); 
     g.draw(path);
     g.setStroke(orig);
   }
}