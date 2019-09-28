package mag5.draw.polar;

import java.awt.geom.Point2D;

import mag5.draw.Projection;

/** 
 The rotation angle used needed to produce central symmetry with respect to the celestial pole.
 Usually combined with an Affine Transform. 
*/
class RotationAngle {
  
  RotationAngle(Projection projection, double dec, double ra) {
    this.target = projection.project(dec, ra);
    Point2D.Double ctr = projection.centerOfProj();
    double dxTheta = target.x - ctr.x;
    double dyTheta = target.y - ctr.y;
    this.rotationAngle = Math.atan2(dyTheta, dxTheta); 
  }
  
  /**
   Radians, measured from 3 o'clock to the hour angle; increase clockwise towards 6 o'clock. 
  */
  double rotationAngle() {
    return rotationAngle;
  }

  /** The coords of the given declination and right ascension passed to the constructor. */
  Point2D.Double target(){
    return target;
  }
  
  // PRIVATE 
  private double rotationAngle;
  private Point2D.Double target;
}