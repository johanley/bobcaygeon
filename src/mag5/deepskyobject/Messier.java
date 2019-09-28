package mag5.deepskyobject;

import java.util.ArrayList;
import java.util.List;

import mag5.util.DataFileReader;

/** Parse a dataset into a list of objects. */
class Messier {

  /** Read in the data file for Messier objects and parse the data. */
  List<DeepSkyObject> readData(){
    List<DeepSkyObject> result = new ArrayList<>();
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(this.getClass(), "messier.utf8");
    DeepSkyObject dso = null;
    for(String line : lines) {
      if (!line.startsWith("#")) {
        dso = processLine(line);
        result.add(dso);
      }
    }
    return result;
  }
  
  //PRIVATE
  
  /**
   messier[0]=["M1",1.4595316,0.3842633,8.4,"Tau","NB","!! famous Crab Neb. supernova remnant","Crab Nebula"];
  */
  private DeepSkyObject processLine(String line) {
    int equalsSign = line.indexOf("=");
    String line2 = line.substring(equalsSign+2,line.length()-2).trim(); //"M1",1.4595316,0.3842633,8.4,"Tau","NB","!! famous Crab Neb. supernova remnant","Crab Nebula",N
    String[] parts = line2.split(",");
    DeepSkyObject result = new DeepSkyObject(
      noQuote(parts[0]), 
      Double.valueOf(parts[1]), 
      Double.valueOf(parts[2]), 
      noQuote(parts[7]), 
      noQuote(parts[5]),
      parts[8]
    ); 
    return result;
  }

  private String noQuote(String text) {
    return text.substring(1, text.length()-1);
  }
}