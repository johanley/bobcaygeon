package mag5.draw;

/**
 The assumed hemisphere of the observer using the charts.
  
 Affects the orientation of the charts, according to the latitude of the observer.
 
 <P>Roughly speaking, the equatorial charts are 'upside down' in the southern hemisphere.
 The polar charts are unaffected by the observer's hemisphere: they are always the same. 
 
 <P>Here's a summary of what changes for the southern hemisphere (equatorial charts only):
 <ul>
   <li>the charts switch top and bottom (this affects the bounds used for each chart)
   <li>projection: declination increases going down, not up
   <li>projection: right ascension increases towards the left (not right)
   <li>date scale: increases towards the left (not right), the same as right ascension
 </ul>
*/
public enum Hemisphere {
  
  NORTH, SOUTH;
  
}
