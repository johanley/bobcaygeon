package mag5.star;

import java.util.Objects;

import mag5.util.Maths;

/**
 Determine if two stars should be amalgamated into one by this project.
  
 Two stars close together can appear as one to the human eye.
 Stars in catalogs don't take this into account; they list the separate components.
   
 <P>So, to produce a chart for the human eye, one needs to amalgamate some pairs into a 
 single entity. There's a <a href='https://en.wikipedia.org/wiki/Apparent_magnitude#Magnitude_addition'>formula</a> 
 for calculating the total magnitude out of the components.
 
 <P>This class is not used at runtime. Rather, it's used during development, to 
 find cases in which the underlying star data will be manually massaged.
*/
public class DoubleTrouble {
  
  /** 
   Dev tool. 
   Change to public in order to run. 
   Explore how the formula for fused magnitudes works with specific values. 
  */
  private static void main(String... args) {
   double mag = fusedMagnitudeForCloseDoubleStar(5.58, 5.99);
   System.out.println(mag);
  }

  public DoubleTrouble(Star a, Star b) {
    this.a = a;
    this.b = b;
    this.separation = separation(a, b);
  }
  
  /** Rounded to 2 decimals. */
  public Double fusedMag() {
    if (fusedMag == null) {
      fusedMag = fusedMagnitudeForCloseDoubleStar(a.MAG, b.MAG);
    }
    return fusedMag;
  }

  /**
   Determine of the two stars should be considered a double in the context of this project.
   Returns true only if the mag and separation are less than or equal to the given values. 
  */
  public boolean isTrouble(Double limitingMag, Double limitingSepRads) {
    return fusedMag() <= limitingMag && sep() <= limitingSepRads; 
  }

  /**
   The separation of the two stars in radians. 
   WARNING: this is only accurate for small angles (its intended purpose here)! 
  */
  public Double sep() {
    return separation;
  }
  
  /** Used to determine if a pair has already been found. */
  public Integer pairHash() {
    return a.INDEX + b.INDEX;
  }
  
  @Override public String toString() {
    return "Sep: " + sepAsArcSec() + "arcsec. Fused mag: "+ fusedMag + NL 
        + " A:" + a + NL 
        + " B:" + b; 
  }
  
  public Star getA() { return a;}
  public Star getB() { return b;}

  /** I'M STUPID: this is not being using when objects are added to a Set. Don't know why. */
  @Override public boolean equals(Object aThat) {
    if (this == aThat) return true;
    if(!(aThat instanceof DoubleTrouble)) return false;
    DoubleTrouble that = (DoubleTrouble) aThat;
    boolean regular = a.INDEX.equals(that.a.INDEX) && b.INDEX.equals(that.b.INDEX);
    boolean reverse = a.INDEX.equals(that.b.INDEX) && b.INDEX.equals(that.a.INDEX);
    return regular || reverse;
  }
  
  @Override public int hashCode() {
    return Objects.hash(a.INDEX, b.INDEX);
  }

  // PRIVATE 
  private Star a;
  private Star b;
  private Double separation; //radians
  private Double fusedMag;
  private static final String NL = System.getProperty("line.separator");
  
  /**
   Two dimmer stars close together are unresolved by the human eye, 
   and appear as a single, slightly brighter star.
   Returns the magnitude of the 'combined' star.
   https://en.wikipedia.org/wiki/Apparent_magnitude#Magnitude_addition
  */
  private static Double fusedMagnitudeForCloseDoubleStar(Double m1, Double m2) {
   double a = Math.pow(10, -0.4*m1) + Math.pow(10, -0.4*m2);
   double b = Math.log10(a);
   double result = -2.5 * b;
   return Math.round(result*100)/100.0;
  } 
 
 /** 
  Separation in radians. Uses an approximation suitable only for small angles.
  Find out if two stars form a double or not.
  They say that the human eye can resolve 8 arcseconds. That might depend on the brightness (I don't know). 
  */
  private double separation(Star a, Star b) {
   double dDec = a.DEC - b.DEC;
   double dRa = a.RA - b.RA;
   double result = Math.sqrt(  Math.pow(dRa * Math.cos(a.DEC),2) + Math.pow(dDec, 2) ); //APPROXIMATELY, FOR SMALL ANGLES
   return result;
  }
  
  private double sepAsArcSec() {
    return Maths.radsToArcsecs(separation);
  }
}
