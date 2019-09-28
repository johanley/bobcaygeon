package mag5.draw;

/** 
 The bounds of the area shown by a chart, and related data derived from the bounds.
 WARNING: the units are in degrees and hours only! 
*/
public class Bounds {

  public Bounds(Double minDecDeg, Double maxDecDeg, Double minRaHours, Double maxRaHours) {
    this.minDecDeg = minDecDeg;
    this.maxDecDeg = maxDecDeg;
    this.minRaHours = minRaHours;
    this.maxRaHours = maxRaHours;
  }
  
  public Double minDecDeg;
  public Double maxDecDeg;
  public Double minRaHours;
  public Double maxRaHours;

  /** Right ascension of the center of the equatorial chart.*/
  public Double raCenterHours() {
    double max = maxRaHours;
    if (maxRaHours < minRaHours) {
      //straddles 0h
      max = max + 24;
    }
    return (max + minRaHours)/2.0;
  }
  
  /** Half the width in right ascension of the equatorial chart.  */
  public Double raHalfWidthHours() {
    double w = maxRaHours - minRaHours;
    if (w < 0) {
      //straddles 0h
      w = w + 24; 
    }
    return w/2.0;
  }
  
  /** True only if the chart is for north of the celestial equator. */
  public boolean isNorth() {
    return maxDecDeg >= 0 && minDecDeg >= 0;
  }
  
  /** True only if the chart is equatorial, not polar. */
  public boolean isEquatorial() {
    return minDecDeg == 0 || maxDecDeg == 0;
  }
  
  /** True only if the chart is polar, not equatorial. */
  public boolean isPolar() {
    return ! isEquatorial();
  }

  /** 
   Return true only if the chart is an upper chart, not a lower chart.
   When using the charts, they are held sideways, with one upper chart, and one lower chart, across two pages.
  */
  public boolean isTopChart() {
    boolean result = false;
    if (isEquatorial()) {
      if (Hemisphere.NORTH == ChartUtil.HEMISPHERE) {
        result = maxDecDeg > 0;
      }
      else {
        result = maxDecDeg == 0;
      }
    }
    else {
      boolean topNorthPole = maxDecDeg == 90 && raCenterHours() > 12;
      boolean topSouthPole = maxDecDeg < 0 && raCenterHours() < 12;
      result = topNorthPole || topSouthPole;
    }
    return result;
  }
  
  /** Equatorial charts only. This situation requires special handling. */
  public boolean straddlesVernalEquinox() {
    return maxRaHours < minRaHours;
  }

  /** Return the declination that is furtherst from the pole. Retains the sign. ONLY WORKS for polar charts. */
  public double decFurthestFromPole() {
    double result = 0.0;
    if (isNorth()) {
      result = minDecDeg;
    }
    else {
      result = maxDecDeg;
    }
    return result;
  }

  /** Return the declination furthest from the celestial equator. Retains the sign. ONLY WORKS for equatorial charts. */
  public double decFurthestFromEq() {
    double result = 0.0;
    if (isNorth()) {
      result = maxDecDeg;
    }
    else {
      result = minDecDeg;
    }
    return result;
  }
  
  /** The maximum declination less the minumumb declination. Always positive! */
  public double decRange() {
    return maxDecDeg - minDecDeg;
  }
}
