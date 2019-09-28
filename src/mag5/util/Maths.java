package mag5.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Various utility methods of general use. */
public class Maths {
  
  public static double degToRads(double deg) {
    return deg * DEG_TO_RADS;   
  }
  
  public static double radsToDegs(double rad) {
    return rad * RADS_TO_DEG;   
  }
  
  public static double hoursToRads(double hours) {
    return degToRads(hours * HOURS_TO_DEGS);
  }
  
  public static double radsToHours(double rads) {
    return radsToDegs(rads) / HOURS_TO_DEGS;
  }
  
  public static double rightAscensionToRads(int hours, int minutes, double seconds) {
    double hoursDecimal = hours + (minutes/60.0) + (seconds/3600.0); // avoid int division
    return hoursToRads(hoursDecimal);
  }
  
  /** Inclusive. */
  public static boolean inRange(double min, double max, double val) {
    return min <= val && val <= max;
  }
  
  /** Inclusive. Ra is tricky because its cyclical. Things are different if the range straddles 0h. */
  public static boolean inRangeRa(Double min, Double max, Double val) {
    boolean result = false;
    if (min < max) {
      result = (val >= min && val <= max);
    }
    else {
      //overlaps 0h
      result = (val >= min || val <= max);
    }
    return result;
  }
  
  public static String roundMag(Double val) {
    BigDecimal num = new BigDecimal(val.toString());
    BigDecimal rounded = num.setScale(1, RoundingMode.HALF_EVEN);
    return rounded.toString();
  }
  
  public static Integer inchesToPixels(double inches) {
    return (int)(DOTS_PER_PIXEL * inches);
  }
  
  public static Double midpoint(Double a, Double b) {
    return (a + b)/2.0;
  }
  
  /** Format hh mm.m. No leading zeros for padding. */
  public static Double parseRa(String ra) {
    String[] parts = ra.trim().split(" ");
    double h = Double.parseDouble(parts[0]);
    double m = Double.parseDouble(parts[1]);
    h = h + m/60.0;
    return hoursToRads(h);
  }
  
  /** Format d m. Leading sign. For m, possible leading zeros for padding. */
  public static Double parseDec(String dec) {
    String[] parts = dec.trim().split(" ");
    double d = Double.parseDouble(parts[0]); //leading sign is ok
    double m = Double.parseDouble(parts[1]); //leading 0 is ok
    int sign = d < 0 ? -1 : 1;
    m = sign * m; //ensure the minutes has the same sign as the degrees
    d = d + m/60.0;
    return degToRads(d);
  }
  
  public static double radsToArcsecs(Double val) {
    double result = radsToDegs(val);
    result = result*3600;
    result = Math.round(result*100)/100;
    return result;
  }
  
  /**
   Round the given value. 
    
   <P>Many graphics operations take only an int.
   To preserve as much accuracy as possible when converting from double to int, 
   you need to call this method, instead of doing a cast.
   Casting simply abandons the decimal part. 
   I saw this cause a problem: in drawing a large circle, it was actually slightly oval, I believe. 
  */
  public static int round(double val) {
    long result = Math.round(val);
    return (int)result;
  }

  public static float asFloat(double val) {
    return (float)(val);
  }
  
  /** Ensure that the given value is in the range 0..2pi. */
  public static double in2pi(double rads){
    double TWO_PI = 2*Math.PI;
    double result = rads % TWO_PI;
    if (result < 0){
      result = result + TWO_PI;
    }
    return result;
  };  
  
  //PRIVATE 
  
  private static final Double DEG_TO_RADS = 2*Math.PI/360.0;
  private static final Double RADS_TO_DEG = 360.0/(2*Math.PI);
  private static final Double HOURS_TO_DEGS = 15.0;
  private static final Integer DOTS_PER_PIXEL = 72; //iText's default
}