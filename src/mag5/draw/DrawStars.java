package mag5.draw;

import static java.util.Comparator.comparing;
import static mag5.util.Maths.inRange;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mag5.star.Star;
import mag5.util.Maths;

/** 
 Render the stars attached to a chart.
 By the time this class is used, the positions of the stars have already been found (but not yet rendered). 
*/
public class DrawStars {
  
  DrawStars(List<Star> stars, Map<Integer, Point2D.Double> starPoints, Graphics2D g) {
    this.stars = stars;
    this.starPoints = starPoints;
    this.g = g;
  }
  
  /** 
   The star positions have already been found.
   Sort the stars first by magnitude, so that smaller star-dots will overwrite larger star-dots in the background.
   Black circle, with a small white border around it.
   The small white border looks good when 2 stars are near each other.
   It also looks better for the constellation lines.
  */
  void draw() {
     Collections.sort(stars, comparing(Star::getMagnitude));
     for (Star star : stars) {
       drawStarDot(star);
       //drawStarNamesIndexOnly(star);
     }
  }
  
  public static int starSize(Star star) {
    int MAX = 7;
    int STEP_SIZE = 1; 
    int result = 0;
    
    //do it like this, so that it can be easily tweaked
    double mag = star.MAG;
    int steps = 0;
    if (inRange(-5,0.49, mag)) {
      //mag 0 or less
      steps = 0;
    }
    else if (inRange(0.50, 1.49, mag)) {
      //mag 1: 0.50..1.49
      steps = 1;
    }
    else if (inRange(1.5, 2.49, mag)) {
      //mag 2
      steps = 2;
    }
    else if (inRange(2.5, 3.49, mag)) {
      //mag 3
      steps = 3;
    }
    else if (inRange(3.50, 4.49, mag)) {
      //mag 4
      steps = 4;
    }
    else if (inRange(4.50, 10, mag)) {
      //mag 5 or more
      steps = 5;
    }
    result = MAX - steps*STEP_SIZE; 
    return result;
  }

  //PRIVATE 
  
  private List<Star> stars;
  private Map<Integer, Point2D.Double> starPoints;
  private Graphics2D g;

  private void drawStarDot(Star star) {
    Point2D.Double where = starPoints.get(star.INDEX); 
    int radius = starSize(star)+1;
    //careful: uses a bounding rectangle! the xy denotes the top left.
    //white filled circle, as a border to the black filled circle; slightly bigger
    int w = radius*2;
    int h = w; //circular
    Color originalColor = g.getColor();
    g.setColor(Color.WHITE);
    g.fillOval(Maths.round(where.x - radius), Maths.round(where.y - radius), w, h);

    radius = radius - 1;
    g.setColor(Color.BLACK);
    w = radius*2;
    h = w;
    g.fillOval(Maths.round(where.x - radius), Maths.round(where.y - radius), w, h);
    g.setColor(originalColor);
  }
  
  /** Used for development only, to easily see the index of stars. */
  @SuppressWarnings("unused")
  private void drawStarNamesIndexOnly(Star star) {
    Point2D.Double where = starPoints.get(star.INDEX);
    int tweak = starSize(star) + 3;
    String starId = star.INDEX.toString();
    Point2D.Double pName = new Point2D.Double(where.x, where.y + 2*tweak);
    g.drawString(starId, Maths.round(pName.x), Maths.round(pName.y)); 
  }
}
