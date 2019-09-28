package mag5.draw;

import java.awt.Shape;
import java.awt.geom.Point2D;

/** 
 The projection used to render the celestial sphere on a two-dimensional chart.
 The implementations used in this project match those used in the Edmund Mag 5 Star Atlas. 
*/
public interface Projection {
  
  /** The bounds of the chart. */
  Bounds getBounds();

  /** 
   Execute the projection. Both inputs are in rads.
   Translate declination, right ascension into xy coordinates on the chart. 
  */
  Point2D.Double project(Double dec, Double ra);

  /**
   The area that defines the interior of the chart, where stars are rendered.
   Use as a clip region. 
   The clip region removes stars and lines from outside the desired chart area.
   <P>Clipping can be removed using g.setClip(null);
  */
  Shape innerBoundary();
  
  /** The number of pixels on the chart corresponding to one radian of declination. */
  Double distancePerRad();
  
  /** The point on the chart corresponding to the center of projection. */
  Point2D.Double centerOfProj();
  

}
