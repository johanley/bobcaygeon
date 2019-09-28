package mag5.deepskyobject;

import java.util.List;

/** List of all deep sky objects. */
public class DeepSkyObjects {

  /**
   The data is read from text files in the same directory as this class. 
  */
  public List<DeepSkyObject> list(){
    NgcOrSouthern ngc = new NgcOrSouthern("ngc.utf8");
    Messier messier = new Messier();
    NgcOrSouthern southern = new NgcOrSouthern("southern.utf8");
    
    List<DeepSkyObject> result = messier.readData();
    result.addAll(ngc.readData());
    result.addAll(southern.readData());
    return result;
  }
}
