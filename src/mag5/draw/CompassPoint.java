package mag5.draw;

/** 
 Used to place text on a chart, on a circle centered on some 'base' object.
 
 The idea is to avoid collisions with nearby items in the chart, by overriding default 
 locations with an alternate 'compass point' around the same object.
 
 <P>Note that the compass points retain their meaning when 
 an equatorial chart is 'upside down' in the southern hemisphere.
 That is, for the placement of text, the item is always place in the same position angle 
 with respect to the object, regardless of whether it's 'upside down'.
 Most of the time, you can get a satisfactory result with one of these 4 values; sometimes, 
 it's not possible to get a good result with any of these 4 values. That is, this implementation 
 is not entirely satisfactory.
 
 <P>However, a southern chart will implement the placement by changing the sign of delta's 
 with respect to the 'base' object.
*/
public enum CompassPoint {
  
  /** North */
  N,
  
  /** South */
  S,
  
  /** East */
  E,
  
  /** West */
  W;

}