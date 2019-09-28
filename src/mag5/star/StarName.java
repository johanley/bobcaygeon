package mag5.star;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import mag5.util.DataFileReader;

/** 
 Proper name for a star ('Vega', for example). 
 The star is usually bright. 
*/
public class StarName {

  /**
   For the given constellation and Bayer designation, return the proper name of the star ('Vega', for example). 
   Return empty string if not found. 
  */
  public String nameFor(String constellationAbbr, String greekLetter) {
    String result = "";
    Map<String, String> forConstellation = starNames.get(constellationAbbr);
    if (forConstellation != null) {
      String starName = forConstellation.get(greekLetter);
      result = starName != null ? starName : "";
    }
    return result;
  }

  /** Read the source data into memory. The file is in the same directory as this class. */
  public void readData() {
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(this.getClass(), "proper-names.utf8");
    int lineCount = 0;
    for (String line : lines) {
      if (line.startsWith(DataFileReader.COMMENT) || line.trim().length() == 0){
        continue;
      }
      else {
        ++lineCount;
        processLine(line.trim());
      }
    }
    log("Processed names for this many constellations: " + lineCount);
  }
  
  // PRIVATE
  
  private Map<String /*UMa*/, Map<String/*α*/, String /*Dubhe*/>> starNames = new LinkedHashMap<>();
  private static final String BLANK = " ";
  
  private void log(String msg) {
    System.out.println(msg);
  }
  
  /** line: 'Ari α=Hamal, β=Sheratan' */
  private void processLine(String line) {
    if (line.length() == 3) {
      starNames.put(line, new LinkedHashMap<>()); //no names for this constellation
    }
    else {
      int firstBlank = line.indexOf(BLANK);
      String constellationAbbr = line.substring(0, firstBlank);
      
      String[] names = line.substring(firstBlank).split(Pattern.quote(","));
      Map<String, String> namesMap = new LinkedHashMap<>();
      for(String name : names) {
          String[] parts = name.split(Pattern.quote("="));
          namesMap.put(parts[0].trim(), parts[1].trim());
      }
      starNames.put(constellationAbbr, namesMap);
    }
  }
}