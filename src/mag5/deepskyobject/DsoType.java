package mag5.deepskyobject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 The various kinds of deep sky object, as shown on these charts. 
*/
public enum DsoType {
  
  /** Emission nebula, planetary nebula, and reflection nebula. */
  NEBULA, 
  OPEN_CLUSTER, 
  GLOBULAR_CLUSTER, 
  GALAXY;
  
  /**
   Parse identifiers in various data text files into one of the dso-types. 
  */
  public static DsoType parse(String text) {
    DsoType result = map.get(text);
    if (result == null) {
      throw new IllegalArgumentException("Unknown type: '" + text + "'");
    }
    return result;
  }

  /**
   The various data sets designate types in different ways.
   Here, we follow the Edmund Mag 5 style, and collapse all of the 'fuzzy' objects 
   into the single type 'Nebula'.
  */
  private static final Map<String, DsoType> map = new LinkedHashMap<String, DsoType>();
  static {
    map.put("GC", DsoType.GLOBULAR_CLUSTER);
    map.put("OC", DsoType.OPEN_CLUSTER);
    map.put("GY", DsoType.GALAXY); //galaxy
    map.put("NB", DsoType.NEBULA);
    map.put("PN", DsoType.NEBULA); //planetary neb
    map.put("EN", DsoType.NEBULA); //emission neb
  }
}