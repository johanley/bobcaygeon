package mag5.constellation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mag5.star.Star;
import mag5.star.StarCatalog;
import mag5.util.DataFileReader;

/** WARNING: the line data is very brittle.  */
public class ConstellationLines {

  /** Read in the data file. The data file exists in the same directory as this class. */
  public void readData() {
    parseInputFile();
  }

  /**
   All polylines for all constellations. 
   The key is the abbreviation for the constellation name, for example Peg (for Pegasus).
   The value is a list of 'polylines'. Each polyline is a list of integers, indentifying the stars that 
   are to be connected together by lines.
  */
  public Map<String/*Ari*/ , List<List<Integer>> /*1..N polylines*/> all(){
    return lines;
  }

  /**
   For debugging only.
   The problem is that the polyline only gets drawn if ALL stars identified in the polyline are 
   actually present. For example, if the stars are filtered to only mag 4, and a polyline has a mag 4.5 star, 
   then the polyline will not show. 
    
   <P>As a diagnostic, this method finds the polyline points (as Stars) that are MISSING from the given starlist, 
   and returns them in a list.
  */
  public List<Star> scanForAnyMissingStarsInThe(List<Star> givenStarList, StarCatalog starCatalog){
    List<Star> result = new ArrayList<>();
    Set<String> constellations = lines.keySet();
    for (String constellation : constellations) {
      List<List<Integer>> polys = lines.get(constellation);
      for (List<Integer> poly : polys) {
        for (Integer id : poly) {
          boolean missing = true;
          for(Star star : givenStarList) {
            if (star.INDEX.equals(id)) {
              missing = false;
              break;
            }
          }
          if (missing) {
            result.add(lookUpStar(id, starCatalog));
          }
        }
      }
    }
    return result;
  }
  
  // PRIVATE 
  
  private Map<String/*Ari*/ , List<List<Integer>> /*1..N polylines*/> lines = new LinkedHashMap<>();
  
  private void parseInputFile() {
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(this.getClass(), "constellation-lines.utf8");
    for (String line : lines) {
      if (line.trim().startsWith(DataFileReader.COMMENT)) {
        continue;
      }
      else {
        processLine(line.trim());
      }
    }
  }
  
  /**
   Source data example (came from another project, in javascript-world):
     Ari = [[820,797,613,549,541],[968,883,613],[947,883],[834,797]];
   Each line is a single constellation, and almost every constellation is present with at least 1 polyline, with 
   the exception of a couple that are really faint, and have no stars to join.
  */
  private void processLine(String line) {
    int equals = line.indexOf("=");
    String constellationAbbr = line.substring(0, equals).trim();
    List<List<Integer>> polylinesIds = new ArrayList<>();
    
    String polylines = line.substring(equals+1).trim(); // [[820,797,613,549,541],[968,883,613],[947,883],[834,797]];
    //chop off the extra square brackets that were needed in javascript-land
    polylines = polylines.substring(1, polylines.length() - 2); // [820,797,613,549,541],[968,883,613],[947,883],[834,797]
    //use regexes to grab each single-line
    String A = Pattern.quote("[");
    String B = Pattern.quote("]");
    String COMMA = Pattern.quote(",");
    Pattern singleLine = Pattern.compile(A + "(.*?)" + B); //1 matching group: 820,797,613,549,541. Reluctant qualifier!
    
    //split the matching group around the comma
    Matcher matcher = singleLine.matcher(polylines);
    while (matcher.find()) {
      //String wholeMatch = matcher.group(0);
      String oneLine = matcher.group(1);
      String[] parts = oneLine.split(COMMA);
      List<Integer> polylineIds = new ArrayList<>();
      for(String part : parts) {
        polylineIds.add(Integer.valueOf(part));
      }
      polylinesIds.add(polylineIds);
    }
    lines.put(constellationAbbr, polylinesIds);
  }
  
  /** Returns null if not found. */
  private Star lookUpStar(Integer id, StarCatalog catalog) {
    Star result = null;
    for (Star star : catalog.all()) {
      if (star.INDEX.equals(id)) {
        result = star;
        break;
      }
    }
    return result;
  }
}