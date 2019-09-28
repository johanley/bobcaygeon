package mag5.star.gmst;

/** The GMST (Greenwich Mean Sidereal Time) at 20h, for both the end of the month, and on the 15th of each month. */
public class GmstMonth implements Comparable<GmstMonth>{
  
  public GmstMonth(Double ra, int month, int day /*end of month*/, Double raMidMonth) {
    this.ra = ra;
    this.month = month;
    this.day = day;
    this.raMidMonth = raMidMonth;
  }
  
  /** Right ascension in radians at the end of the month. */
  public Double getRa() {
    return ra;
  }
  
  /** Right ascension in radians on the 15th of the month. */
  public Double getRaMidMonth() {
    return raMidMonth;
  }
  
  /** In the range 1..12 */
  public Integer getMonth() {
    return month;
  }
  
  /** End of the month, 28..31 */
  public Integer getDay() {
    return day;
  }

  /** Sorts by right ascension at the end of the month. */
  @Override public int compareTo(GmstMonth that) {
    return this.ra.compareTo(that.ra);
  }
  
  @Override public String toString() {
    return "ra:" + ra + " month:" + month + " day:" + day + " ra midmonth: " + raMidMonth;
  }
  
  //PRIVATE
  
  private Double ra;
  private Double raMidMonth;
  private Integer month; //1..12; use a number to allow for translation
  private Integer day; //the end of the month 28..31
}