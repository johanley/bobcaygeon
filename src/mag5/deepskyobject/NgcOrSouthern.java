package mag5.deepskyobject;

import java.util.ArrayList;
import java.util.List;

import mag5.util.DataFileReader;
import mag5.util.Maths;

/** Parse a dataset into a list of objects. */
class NgcOrSouthern {

  NgcOrSouthern(String fileName) {
    this.fileName = fileName;
  }

  /** Read in the data file for NGC (New General Catalog) objects and parse the data. */
  List<DeepSkyObject> readData(){
    List<DeepSkyObject> result = new ArrayList<>();
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(this.getClass(), fileName);
    DeepSkyObject dso = null;
    for(String line : lines) {
      if (!line.startsWith("#")) {
        dso = processLine(line);
        result.add(dso);
      }
    }
    return result;
  }
  
  // PRIVATE 
  
  private String fileName;

  /**
      0  1       2    3   4  5
   7009,PN,21 04.2,-11 22,S,Saturn Nebula
   */
  private DeepSkyObject processLine(String line) {
    String[] parts = line.split(",");
    DeepSkyObject result = new DeepSkyObject(
      parts[0], 
      Maths.parseRa(parts[2]), 
      Maths.parseDec(parts[3]), 
      parts[5], 
      parts[1],
      parts[4]
    ); 
    return result;
  }
}