package mag5.star.gmst;

/** The GMST (Greenwich Mean Sidereal Time) at 20h, for every 5 days of the year. */
public class GmstFiveDay implements Comparable<GmstFiveDay>{
  
  public GmstFiveDay(Double ra, Integer day) {
    this.ra = ra;
    this.day = day;
  }

  /** Right ascension in radians, for the given day. */
  public Double getRa() {
    return ra;
  }
  
  /** The day in the month, 1..31 */
  public Integer getDay() {
    return day;
  }

  /** Sorts by right ascension. */
  @Override public int compareTo(GmstFiveDay that) {
    return this.ra.compareTo(that.ra);
  }

  @Override public String toString() {
    return "ra:" + ra + " day:" + day;
  }
  
  private Double ra;
  private Integer day; //1..31
  //the month is not preserved here
}
