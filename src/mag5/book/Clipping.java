package mag5.book;

/** 
 Clipping avoids drawing in unwanted areas.
 When clipping is used, and a clipping area is defined, then 
 any drawing operations outside the clipping area will be cut off automatically.
*/
public enum Clipping {
  
  /** Don't draw in outside the clipping area. */
  ON, 
  
  /** Allow drawing outside of the clipping area.*/
  OFF;
}
