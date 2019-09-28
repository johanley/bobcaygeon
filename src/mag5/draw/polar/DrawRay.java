package mag5.draw.polar;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

import mag5.draw.Projection;
import mag5.util.Maths;

/**
 Draw a ray from the center to the circumference, corresponding to a given right ascension.
 This class was created because of problems seen when drawing the date-scale.
 
 I've tried different techniques that don't work well:
 <ul> 
  <li>calculating ra, dec for the endpoints of the line (crooked, not pointed towards the center)
  <li>drawing a line of fixed size using an affine transform to a rotated frame (doesn't match up with circles)
 </ul>
 
  <P>In this class, I'm centralizing the policy for drawing a ray from the pole 
  to the circumference. The ray is usually drawn only partially, as in the date-scale.
 */
class DrawRay {
  
  DrawRay(Projection projection, Graphics2D g) {
    this.projection = projection;
    this.g = g;
  }
  
  /**
   Draw a ray from the center to the circumference, corresponding to a given right ascension.
   The ray is drawn only between the two given radii. 
   This implementation uses shapes and clipping regions.
   
   <P>IMPORTANT: the radii passed in here need to be calculated in the EXACT same way as 
   when the corresponding circles were drawn.
  */
  void ray(double ra, double smallerRadius, double largerRadius) {
    Shape origClip = g.getClip();
    g.setClip(clippingRegion(smallerRadius, largerRadius));
    
    double raMid = Maths.hoursToRads(projection.getBounds().raCenterHours());
    //find a point on a big big circle corresponding to the given ra
    double R = veryLargeRadius(largerRadius);
    int sign = projection.getBounds().isNorth() ? 1 : -1;
    if (! projection.getBounds().isTopChart()) {
      sign = -1*sign;
    }
    double deltaX = R * Math.sin(ra - raMid) * sign;
    double deltaY = 0.0;
    if (projection.getBounds().isNorth()) {
      deltaY = R * Math.cos(ra - raMid) * (-1) * sign;
    }
    else {
      deltaY = R * Math.cos(ra - raMid) * sign;
    }
    //draw a line between the ctr and the point on the big big circle (but most of it will be clipped!)
    g.drawLine(
      Maths.round(projection.centerOfProj().getX()), 
      Maths.round(projection.centerOfProj().getY()), 
      Maths.round((projection.centerOfProj().getX() + deltaX)), 
      Maths.round((projection.centerOfProj().getY() + deltaY))
    );
        
    g.setClip(origClip);
  }
  
  // PRIVATE
  private Projection projection;
  private Graphics2D g;
  
  /** 
   Defines where all drawing is done.
   Outside of this shape, no drawing is done
   Two circles centered on the pole (center of proj).
   You can SUBTRACT the smaller circle from the other, to get the desired area. 
   (This will need to change if the center of projection is lifted from the edge of the page.) 
  */
  private Area clippingRegion(double smallerRadius, double largerRadius) {
    Area result = new Area();
    result.add(circle(largerRadius));
    result.subtract(circle(smallerRadius));
    //if a half-moon shape is needed, add that mod here (subtract a huge rectangle, representing half the world?)
    //or, you might alter to use: arc + line + arc + line, instead of 2 circles
    return result;
  }

  /** Centered on the ctr of projection. */
  private Area circle(double radius) {
    //the shapes are calculated in the EXACT same way as the when the circles were drawn.
    double w = Maths.round(radius * 2);
    double h = w;
    double x = Maths.round(projection.centerOfProj().getX() - radius);
    double y = Maths.round(projection.centerOfProj().getY() - radius);
    Ellipse2D.Double result = new Ellipse2D.Double(x, y, w, h);
    return new Area(result);
  }
  
  /**
   The intent here is to studiously avoid the kinds of small deviations I've seen with other algorithms. 
   The idea is to draw the ray with a really large R, scaled way up, but clip off most of it.  
  */
  private double veryLargeRadius(double radius){
    return radius * 10;
  }
}