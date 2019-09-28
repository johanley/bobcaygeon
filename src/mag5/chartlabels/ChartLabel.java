package mag5.chartlabels;

/**
 The name of a constellation, asterims, or bright star, and where to place it.
 Note that the Bayer designation is not included here.
 <P>Simple struct to carry related data. 
  
 <P>WARNING: if the text is translated into another language, then 
 that will sometimes mean that the placement of the label will need to change as well. 
*/
public class ChartLabel {
  
  ChartLabel(String text, Double ra, Double dec, ChartLabelType type){
    this.TEXT = text;
    this.DEC = dec;
    this.RA = ra;
    this.TYPE = type;
  }
  
  /** Translated text. */
  public String TEXT;
  
  /** Rads. */
  public Double RA;
  
  /** Rads. */
  public Double DEC;

  /** What kind of object is being labeled.*/
  public ChartLabelType TYPE;

}
