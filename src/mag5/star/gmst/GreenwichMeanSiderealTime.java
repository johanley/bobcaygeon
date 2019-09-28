package mag5.star.gmst;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mag5.util.Maths;

/**
 Calculate GMST (Greenwich Mean Sidereal Time) at 20h, for various days of the year.
 
 <P>This calculation is necessarily a bit vague. 
 Each year, on a given day, the exact GMST at 20h will be different.
 As well, it very gradually changes because of precession.
 However, in the given context, there's no real need to be exact. 
*/
public class GreenwichMeanSiderealTime {

  /** For testing only, for examining the data. */
  private static void main(String... args) {
    GreenwichMeanSiderealTime gmst = new GreenwichMeanSiderealTime();
    List<GmstMonth> months = gmst.monthLimits();
    for(GmstMonth item : months) {
      System.out.println(item);
    }
    List<GmstFiveDay> days = gmst.everyFiveDays();
    for(GmstFiveDay item : days) {
      System.out.println(item);
    }
  }
  
  /** GMST at 20h for all months, ordered by right ascension at the end of the month. */
  public List<GmstMonth> monthLimits(){
    List<GmstMonth> result = new ArrayList<>();
    for(Month month : EnumSet.range(Month.JANUARY, Month.DECEMBER)){
      result.add(buildGmstMonth(month));
    }
    Collections.sort(result);
    return result;
  }

  /** 
   For each chart, GMST at 20h for month-end and mid-month.
   Map key: the chart number.
   Map value: the GMSTs at 20h that are needed for that chart.
   Polar charts get all months, for the whole year.
   Equatorial charts get only the 3-4 months that are needed for that chart.
  */
  public Map<Integer, List<GmstMonth>> forCharts(){
    Map<Integer, List<GmstMonth>> result = new LinkedHashMap<>();
    //this hard-coding avoids issues with cyclical data; and has 'finer control' (ahem)
    result.put(1, buildGmstMonths(Month.values()));
    result.put(2, buildGmstMonths(Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER));
    result.put(3, buildGmstMonths(Month.DECEMBER, Month.JANUARY, Month.FEBRUARY, Month.MARCH));
    result.put(4, buildGmstMonths(Month.FEBRUARY, Month.MARCH, Month.APRIL, Month.MAY)); //drop Jan: too thin!
    result.put(5, buildGmstMonths(Month.APRIL, Month.MAY, Month.JUNE, Month.JULY));
    result.put(6, buildGmstMonths(Month.JULY, Month.AUGUST, Month.SEPTEMBER, Month.OCTOBER));
    
    //repeat for the southern hemisphere charts
    result.put(7, buildGmstMonths(Month.values()));
    result.put(8, buildGmstMonths(Month.SEPTEMBER, Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER));
    result.put(9, buildGmstMonths(Month.DECEMBER, Month.JANUARY, Month.FEBRUARY, Month.MARCH));
    result.put(10, buildGmstMonths(Month.FEBRUARY, Month.MARCH, Month.APRIL, Month.MAY)); //drop Jan: too thin!
    result.put(11, buildGmstMonths(Month.APRIL, Month.MAY, Month.JUNE, Month.JULY));
    result.put(12, buildGmstMonths(Month.JULY, Month.AUGUST, Month.SEPTEMBER, Month.OCTOBER));
    return result;
  }
  
  /** 
   The GMST at 20h00 for every five days in each month (5,10,15,20,25...) 
   Ordered by increasing right ascension.
   At the end of the month, the day is chosen so that it never goes beyond month-end.  
  */
  public List<GmstFiveDay> everyFiveDays(){
    List<GmstFiveDay> result = new ArrayList<>();
    for(Month month : EnumSet.range(Month.JANUARY, Month.DECEMBER)){
      //result.add(buildGmstFiveDay(month, 1));
      result.add(buildGmstFiveDay(month, 5));
      result.add(buildGmstFiveDay(month, 10));
      result.add(buildGmstFiveDay(month, 15));
      result.add(buildGmstFiveDay(month, 20));
      result.add(buildGmstFiveDay(month, 25));
      if (month.minLength() == 28) {
        result.add(buildGmstFiveDay(month, 28)); //Feb only 
      }
      if (month.minLength() >= 30) {
        result.add(buildGmstFiveDay(month, 30)); 
      }
      if (month.minLength() == 31) {
        result.add(buildGmstFiveDay(month, 31));
      }
    }
    Collections.sort(result);
    return result;
  }
  
  // PRIVATE 
  
  /**
   For the start of the year.
   https://dc.zah.uni-heidelberg.de/apfs/times/q/form
   2000-01-01 20:00 : 2h 43m 09.4s
   2019-01-01 20:00 : 2h 44m 43.7s (this agrees with the Observer's Handbook) 
  */
  private static double GMST_AT_20H00_START_DATE =  Maths.rightAscensionToRads(2, 43, 9.4);
  
  /**
   The amount by which the GMST grows each day.  
  */
  private static double TROPICAL_YEAR = 365.242189;
  
  /** Radians. */
  private static double DAILY_CHANGE = Maths.degToRads(360) / TROPICAL_YEAR;
  
  private static LocalDate START_DATE = LocalDate.parse("2000-01-01");

  /** 
   Return a value in radians, in the range 0..2pi
   Example values (in degrees):
    Jan 1 degs: 40.78916666666667
    Jan 2 degs: 41.7748140284505
  */
  private double gmstAt20h00ForDayOfTheYear(int month, int dayOfTheMonth) {
    LocalDate theDate = LocalDate.of(START_DATE.getYear(), month, dayOfTheMonth);
    Long numDays = ChronoUnit.DAYS.between(START_DATE, theDate);
    double result = GMST_AT_20H00_START_DATE + numDays * DAILY_CHANGE;
    double TWO_PI = 2*Math.PI;
    if (result > TWO_PI) {
      result = result - TWO_PI;
    }
    return result;
  }
 
  private GmstMonth buildGmstMonth(Month month) {
    double ra = gmstAt20h00ForDayOfTheYear(month.getValue(), month.minLength());
    int MIDDLE_OF_THE_MONTH = 15;
    double raMidMonth = gmstAt20h00ForDayOfTheYear(month.getValue(), MIDDLE_OF_THE_MONTH);
    GmstMonth result = new GmstMonth(ra, month.getValue(), month.minLength(), raMidMonth);
    return result;
  }
  
  private List<GmstMonth> buildGmstMonths(Month... months) {
    List<GmstMonth> result = new ArrayList<>();
    for(Month month : months) {
      result.add(buildGmstMonth(month));
    }
    return result;
  }
  
  private GmstFiveDay buildGmstFiveDay(Month month, int dayOfTheMonth) {
    double ra = gmstAt20h00ForDayOfTheYear(month.getValue(), dayOfTheMonth);
    GmstFiveDay result = new GmstFiveDay(ra, dayOfTheMonth);
    return result;
  }
}