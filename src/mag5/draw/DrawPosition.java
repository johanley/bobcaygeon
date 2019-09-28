package mag5.draw;

import java.util.LinkedHashMap;
import java.util.Map;

/** 
 Where to render designations with respect to the object being designated.
 
 The {@link CompassPoint} has an absolute meaning, but the placement of the name, relative to an item on the page, 
  depends on different things.
  
  <P>There are 4 different cases: equatorial/polar and northern/southern hemisphere.
  The southern-hem cases can be treated as a reversal of the northern hem cases.
  
  <P>Equatorial Chart:
  <pre>
   Northern hem 
    North = above
    South = below
    East = left
    West = right
   Southern hem:
    North = below
    South = above
    East = right
    West = left
  </pre>
      
  <P>With polar charts, the sense is not the same as in equatorial charts. 
  Here, the idea is towards/away-from the pole. In addition, on polar charts the text is rotated, to 
  be symmetric with respect to the pole.
  
  <P>Polar chart:
  <pre>
   Northern hem 
    North = toward the pole
    South = away from the pole
    East = right (as seen from the pole)
    West = left
   Southern hem 
    North = away from the pole
    South = toward the pole
    East = left (as seen from the pole)
    West = right
  </pre>
*/
public enum DrawPosition {

  ABOVE, BELOW, RIGHT, LEFT;

  /**
   Map a {@link CompassPoint} into a corresponding DrawPosition. See class comment.
  */
  public static DrawPosition findPosFrom(CompassPoint compassPoint, Projection projection) {
    Map<CompassPoint, DrawPosition> map = projection.getBounds().isEquatorial() ? EQUATORIAL_NORTHERN_HEM : POLAR_NORTHERN_HEM;
    DrawPosition result = map.get(compassPoint);
    if (Hemisphere.SOUTH == ChartUtil.HEMISPHERE) {
      result = DrawPosition.reverseOf(result);
    }
    return result;
  }
  
  /** Swap above/below, left/right. */
  static DrawPosition reverseOf(DrawPosition pos){
    DrawPosition result = null;
    if (DrawPosition.ABOVE == pos) {
      result = DrawPosition.BELOW;
    }
    else if (DrawPosition.BELOW == pos) {
      result = DrawPosition.ABOVE;
    }
    if (DrawPosition.RIGHT == pos) {
      result = DrawPosition.LEFT;
    }
    if (DrawPosition.LEFT == pos) {
      result = DrawPosition.RIGHT;
    }
    return result;
  }

  //PRIVATE 
  
  private static final Map<CompassPoint, DrawPosition> EQUATORIAL_NORTHERN_HEM = new LinkedHashMap<>();
  private static final Map<CompassPoint, DrawPosition> POLAR_NORTHERN_HEM = new LinkedHashMap<>();
  static {
    EQUATORIAL_NORTHERN_HEM.put(CompassPoint.N, DrawPosition.ABOVE);
    EQUATORIAL_NORTHERN_HEM.put(CompassPoint.S, DrawPosition.BELOW);
    EQUATORIAL_NORTHERN_HEM.put(CompassPoint.E, DrawPosition.LEFT);
    EQUATORIAL_NORTHERN_HEM.put(CompassPoint.W, DrawPosition.RIGHT);
    POLAR_NORTHERN_HEM.put(CompassPoint.N, DrawPosition.BELOW);
    POLAR_NORTHERN_HEM.put(CompassPoint.S, DrawPosition.ABOVE);
    POLAR_NORTHERN_HEM.put(CompassPoint.E, DrawPosition.RIGHT);
    POLAR_NORTHERN_HEM.put(CompassPoint.W, DrawPosition.LEFT);
  }
}