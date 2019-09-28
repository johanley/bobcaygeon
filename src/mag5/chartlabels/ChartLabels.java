package mag5.chartlabels;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import mag5.draw.ChartUtil;
import mag5.draw.Hemisphere;
import mag5.translate.Label;
import mag5.util.DataFileReader;
import mag5.util.Maths;

/** 
 All labels for all charts, as a single data structure.
 Both the labels, and where to place them on each chart.
*/
public class ChartLabels {

  public ChartLabels(Label labels) {
    this.labels = labels;
  }
  
  /** 
   Map returning all chart label data, for all charts.
   
   <P>Map key is simply the chart number.
   
   <P>IMPORTANT: the data is duplicated for the equatorial charts intended for the southern hemisphere readers.
   Date for the southern charts 8..12 are the same as charts 2..6. 
  */
  public Map<Integer, List<ChartLabel>> readData(){
    Map<Integer, List<ChartLabel>> result = new LinkedHashMap<>();
    for(int idx = 1; idx <= SOUTH_POLE_CHART; ++idx) {
      String fileName = "chart_" + idx + "_labels.utf8";
      DataFileReader reader = new DataFileReader();
      List<String> lines = reader.readFile(this.getClass(), fileName);
      List<ChartLabel> chartLabels = new ArrayList<>();
      for(String line: lines) {
        process(line, chartLabels);
      }
      result.put(accountForHemisphere(idx), chartLabels);
      if (NORTHERN_EQUATORIAL_CHARTS_START <= idx && idx <= NORTHERN_EQUATORIAL_CHARTS_END) {        
        //northern equatorial charts
        //need the same data for the southern-hem chart!
        result.put(accountForHemisphere(idx+6), chartLabels);
      }
    }
    return result;
  }
  
  // PRIVATE 
  
  private Label labels;
  private Map<String, String> abbrToTranslatedName = new LinkedHashMap<>();
  private static final Integer NUM_CONSTELLATIONS = 88;
  private static final String BASE_KEY = "constellation-list-";
  
  //some hard-coding here: would need to change if the number of charts is altered someday
  private static final Integer NORTH_POLE_CHART = 1;
  private static final Integer NORTHERN_EQUATORIAL_CHARTS_START = 2;
  private static final Integer NORTHERN_EQUATORIAL_CHARTS_END = 6;
  private static final Integer SOUTH_POLE_CHART = 7;
  
  /** 
   Example of input lines:
   <pre>
     Peg=23.40:21.5,22.25:17.5
     asterism-circlet=23.30:3.5 
     star-deneb=20.30:45.1
   </pre>
   
   In general, <code>identifier=hh.mm:dd.d,hh.mm:dd.d</code>
  */
  private void process(String line, List<ChartLabel> names) {
    if (!line.trim().startsWith(DataFileReader.COMMENT)) {
      String[] parts = line.split(Pattern.quote("="));
      String translatedName = getTranslatedName(parts[0]);
      ChartLabelType type = chartLabelType(parts[0]);
      String[] coords = parts[1].split(Pattern.quote(","));
      for(String coord : coords) {
        addConstellationName(translatedName, coord, names, type);
      }
    }
  }
  
  /** 
   Translate a key into translated text. 
   For example, 'And' is translated into 'Andromeda'.
   
   <P>The labels file has a list of constellations, which uses the abbreviation as a key, 
   in an <em>ad hoc</em> data structure.
   For asterisms and stars, the <code>rawKey</code> is the key, and there's no secondary lookup.
  */
  private String getTranslatedName(String rawKey) {
    if (abbrToTranslatedName.size() == 0) {
      populateAbbrToName();
    }
    String result = abbrToTranslatedName.get(rawKey);
    if (result == null) {
      //asterisms and stars: just use the rawKey directly as the label key
      result = labels.text(rawKey, ChartUtil.lang());
    }
    return result;
  }
  
  private ChartLabelType chartLabelType(String rawKey) {
    ChartLabelType result = ChartLabelType.CONSTELLATION; //default
    if (abbrToTranslatedName.get(rawKey) == null) {
      //it's not a constellation; override the default
      if (rawKey.startsWith("asterism")) {
        result = ChartLabelType.ASTERISM;
      }
      else if (rawKey.startsWith("star")) {
        result = ChartLabelType.STAR;
      }
    }
    return result;
  }

  private void populateAbbrToName() {
    for (int idx = 1; idx <=NUM_CONSTELLATIONS; ++idx) {
      String key = BASE_KEY + idx;
      String constellationLine = labels.text(key, ChartUtil.lang());
      String[] parts = constellationLine.split(",");
      abbrToTranslatedName.put(parts[1].trim(), parts[0].trim());
    }
  }

  /** 
   coord parameter is like this: 'hh.mm:dd.d'
   Chop both parts into rads.
  */
  private void addConstellationName(String translatedName, String coord, List<ChartLabel> chartLabels, ChartLabelType type) {
    String[] parts = coord.trim().split(Pattern.quote(":"));
    Double ra = parseHours(parts[0].trim()); 
    Double dec = parseDegrees(parts[1].trim());
    ChartLabel chartLabel = new ChartLabel(translatedName, ra, dec, type);
    chartLabels.add(chartLabel);
  }
  
  /** hh.mm, with leading zeros for minutes and hours. */
  private Double parseHours(String hhmm) {
    String[] parts = hhmm.split(Pattern.quote("."));
    Double hour = Double.valueOf(parts[0]);
    Double minutes = Double.valueOf(parts[1]);
    return Maths.hoursToRads(hour + minutes/60.0);
  }

  /** 'dd.d', with an optional decimal. Possible leading negative sign. */ 
  private Double parseDegrees(String dd) {
    return Maths.degToRads(Double.valueOf(dd));
  }
  
  /** In the southern hemisphere, the polar charts switch position! */
  private int accountForHemisphere(int idx) {
    int result = idx;
    if (Hemisphere.SOUTH == ChartUtil.HEMISPHERE) {
      if (idx == NORTH_POLE_CHART) {
        result = SOUTH_POLE_CHART;
      }
      else if (idx == SOUTH_POLE_CHART) {
        result = NORTH_POLE_CHART;
      }
    }
    return result;
  }
}