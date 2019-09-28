package mag5.star;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import mag5.draw.CompassPoint;
import mag5.util.DataFileReader;

/** Where to put the Bayer designation, in relation to the dot representing the star. */
public class BayerLetterPlacement {
  
  /**
   Set the compass-point for each star in the given list, if it's not the default (South). 
  */
  public void addBayerPlacementTo(List<Star> stars) {
    Map<Integer, CompassPoint> placements = getStarNamePlacements();
    for(Star star : stars) {
      CompassPoint compassPoint = placements.get(star.INDEX);
      if (compassPoint != null) {
        star.BAYER_COMPASS_POINT = compassPoint;
      }
    }
  }
  
  //PRIVATE
  
  /** Key: the index into the list of YBS stars. */
  private Map<Integer, CompassPoint> starNamePlacements = new LinkedHashMap<>();
  
  private Map<Integer, CompassPoint> getStarNamePlacements(){
    if (starNamePlacements.size() == 0) {
      init();
    }
    return starNamePlacements;
  }
  
  /** Read the file that states where the Bayer letter is placed, for stars having a Bayer letter. */
  private void init() {
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(this.getClass(), "star_name_placements.utf8");
    for(String line : lines) {
      if (!line.trim().startsWith(DataFileReader.COMMENT)) {
        process(line);
      }
    }
  }
  
  /** 1234=N */
  private void process(String line) {
    String[] parts = line.split(Pattern.quote("="));
    String idx = parts[0];
    String placement = parts[1];
    starNamePlacements.put(Integer.valueOf(idx), CompassPoint.valueOf(placement));
  }
}