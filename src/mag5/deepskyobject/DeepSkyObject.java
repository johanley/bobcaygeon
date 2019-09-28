package mag5.deepskyobject;

import mag5.draw.CompassPoint;

/** 
 Data carrier for deep sky objects.
 Identifiers, coords, and how it's ID should be placed with respect to it.
*/
public class DeepSkyObject {

  /** 
   Build from incoming text data sets. 
   Dec and Ra are in radians. 
  */
  public DeepSkyObject(String desig, Double ra, Double dec, String nickName, String type, String compassPoint) {
    this.desig = desig;
    this.ra = ra;
    this.dec = dec;
    this.nickName = nickName;
    this.dsoType = DsoType.parse(type);
    this.compassPoint = CompassPoint.valueOf(compassPoint);
  }
  
  public String getDesig() {
    return desig;
  }
  
  /** Radians. */
  public Double getRa() {
    return ra;
  }
  
  /** Radians. */
  public Double getDec() {
    return dec;
  }
  
  public String getNickName() {
    return nickName;
  }
  public DsoType getDsoType() {
    return dsoType;
  }
  public CompassPoint getCompassPoint() {
    return compassPoint;
  }

  private String desig;
  private Double ra;
  private Double dec;
  private String nickName;
  private DsoType dsoType;
  private CompassPoint compassPoint;
}
