package mag5.star;

import static java.util.Comparator.comparing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mag5.util.DataFileReader;
import mag5.util.Maths;

/** 
 A custom, ad hoc star catalog, based on the Yale Bright Star Catalog (r5), but with modifications (see below).
 
 <P>The catalog is generated in memory; but, as a side-effect for developer convenience, the catalog 
 is dumped by this class into a text file. (See logging output for the file's location.)
 
 <P>WARNING: the IDs used here need to match the IDs used by the data for the constellation lines and other items.
 
 <P>No changes for precession are made. The YBS epoch of J2000 is retained.
*/
public final class StarCatalog {

  /**
   These items are discarded from the underlying catalog.
   14 items in the YBS are novae, and are simply DISCARDED; they really shouldn't be there. 
  */
  public static final List<Integer> NOVAE_ETC = Arrays.asList(92,95,182,1057,1841,2472,2496,3515,3671,6309,6515,7189,7539,8296);
 
  /**
   Generate the star catalog data, using YBSr5 as the base.
   WARNING: this method must be called first!
   The chart is 'intermediate' in the sense that it changes slightly the underlying YBS data.
   
   <P>Actions taken on the data:
   <ul>
    <li>discard novae
    <li>reduce the magnitude of T CrB in order to make it invisible
    <li>fuse close doubles (about 75 in number) into a single star of appropriate magnitude 
    <li>add proper names to stars ('Vega', for instance)
   </ul>
   
   <P>T CrB is retained with a lower magnitude. The reason it's not discarded is because doing so would 
   throw off the data for constellation lines (and similar data).  
   The data for constellation lines was taken from a previous project, that mistakenly retained T CrB. 
    
   <P>As a side effects, this method saves the resulting star catalog as a file. 
   This is meant for developer convenience, so that they data can be easily examined.   
  */
  public void generateIntermediateStarCatalog() throws IOException {
    readInRawStandardCatalogWhileDiscardingUnwantedItems();
    tweakCatalogData();
    addProperNamesToStars();
    //sortByRightAscension(); 
    //sortByMagnitude(); 
    saveToIntermediateFile();
  }
  
  /**
   Filter the catalog into a subset, in a way suited for the equatorial charts.
   Angular params are in degrees, except for the hours.
   Overlap (in degrees) is about the issue of constellation lines near the EDGES of the chart.
   Since weird things can happen with projections when you are far from the center of projection, it's 
   likely prudent to filter the set of stars, to ignore those that are definitely way outside the chart. 
  */
  public List<Star> filterEquatorial(Double limitingMag, Double minDec, Double maxDec, Double minHour, Double maxHour, Integer overlap){
    List<Star> result = new ArrayList<>();
    Double minDecl = Maths.degToRads(minDec - overlap);
    Double maxDecl = Maths.degToRads(maxDec + overlap);
    Double minRa = Maths.hoursToRads(minHour) - Maths.degToRads(overlap);
    Double maxRa = Maths.hoursToRads(maxHour) + Maths.degToRads(overlap);
    for (Star star : stars) {
      if (Maths.inRange(minDecl, maxDecl, star.DEC) &&
          Maths.inRangeRa(minRa, maxRa, star.RA) &&
          Maths.inRange(-5.0, limitingMag, star.MAG)) {
        result.add(star);
        //there's no real need to make a copy of the star object, since the data is treated as read-only
      }
    }
    return result;
  }

  /**
   Filter the star catalog in a way suitable for a polar chart, where the right ascension changes rapidly.
   All angular params are in degrees. 
   Overlap (in degrees) is about the issue of constellation lines near the EDGES of the chart.
  */
  public List<Star> filterPolar(Double limitingMag, Double minDec, Double maxDec, Integer overlap){
    List<Star> result = new ArrayList<>();
    Double minDecl = Maths.degToRads(minDec - overlap);
    Double maxDecl = Maths.degToRads(maxDec + overlap);
    for (Star star : stars) {
      if (Maths.inRange(minDecl, maxDecl, star.DEC) &&
          Maths.inRange(-5.0, limitingMag, star.MAG)) {
        result.add(star);
        //there's no real need to make a copy of the star object, since the data is treated as read-only
      }
    }
    return result;
  }

  /** Return all of the stars in the catalog, with no filter. */
  public List<Star> all(){
    return Collections.unmodifiableList(stars);
  }
  
  /**
   If the combined magnitude is higher than the limit, then it's very likely that 
   the 2 should be amalgamated into 1 star, for the purposes of this chart.
   WARNING: this method is called only at dev-time, in order to find doubles that should be fused into one.
   That work is then incorporated manually into this class, for later use at runtime.
  */
  public Set<DoubleTrouble> findCloseDoublesInConstellations(List<Star> targets, double arcSeconds, double limitingTotalMag, String... constellationAbbrs) {
    double arcSecRads = Maths.degToRads(arcSeconds/3600.00);
    Set<DoubleTrouble> result = new LinkedHashSet<>();
    for (String constellationAbbr : constellationAbbrs) {
      log("Scanning for doubles in " + constellationAbbr + " with mag limit " + limitingTotalMag + " and sep limit " + arcSeconds);
      for (Star a : targets) {
        for(Star b : targets) {
          if (!a.INDEX.equals(b.INDEX ) &&
              a.getConstellationAbbr().equals(constellationAbbr) && 
              b.getConstellationAbbr().equals(constellationAbbr)) {
            DoubleTrouble dt = new DoubleTrouble(a, b);
            if (dt.isTrouble(limitingTotalMag, arcSecRads)) {
              boolean alreadyFound = false;
              for(DoubleTrouble found : result) {
                if (found.pairHash().equals(dt.pairHash())) {
                  alreadyFound = true;
                }
              }
              if (!alreadyFound) {
                result.add(dt);
              }
            }
          }
        }
      }
    }
    return result;
  }
  
  // PRIVATE 

  private List<Star> stars = new ArrayList<>();
  private static final Double DIM = 7.0;
  
  private void readInRawStandardCatalogWhileDiscardingUnwantedItems() {
    log("Read in raw catalog. Discard unwanted items.");
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(this.getClass(), "yale_bright_star_catalog_5_raw.txt");
    int lineCount = 0;
    int discardedLineCount = 0;
    Star star = null;
    for(String line : lines) {
      ++lineCount;
      if (NOVAE_ETC.contains(Integer.valueOf(lineCount))){
        ++discardedLineCount;
        log("Discarding unwanted line: '" + line + "'");
        continue;
      }
      else {
        star = processLine(line, stars.size());
        stars.add(star);
      }
    }
    log("Read this many lines: " + lineCount);
    log("Discarded this many lines: " + discardedLineCount);
    log("Number of stars in output: " + stars.size());
  }
  
  private void tweakCatalogData() {
    log("Tweaking data!!");
    log("Suppress T CrB. Don't want to see it. Make it dim.");
    changeMag(5948, DIM);
    fuseCloseDoubles();
    //Beta Equ is mag 5.1; would be nice to have, for the shape of the lines
  }

  /**
   Some stars that appear as 1 to the human eye are in the catalog as 2 separate stars.
   This method fuses the brightness into one of the stars, and suppresses the brightness of the other by making it very dim (mag 7).
   That way, the chart sees them only as 1 object, of an amalgamated brightness.
   This is a more accurate representation of what a human actually sees.
   The following was generated using limiting-separation=210 arcseconds, limiting-mag=5.01. 
  */
  private void fuseCloseDoubles() {
    log("Fusing close doubles whose combined mag is greater than 5.01, and separation less than 210 arcsecs.");
    //WARNING: be careful of stars that are part of a constellation polyline! don't suppress them!

    //north pole - chart 1
    doubleTrouble("16 Dra", 6175, 3.92, 6174, 6176); //triple
    doubleTrouble("ν Dra", 6542, 4.12, 6543); //polyline
    doubleTrouble("ψ Dra", 6624, 4.27, 6625); 
    doubleTrouble("ξ UMa", 4364, 3.86, 4365); //polyline 
    doubleTrouble("ζ UMa", 5044, 2.06, 5045); //polyline 
    doubleTrouble("11 Cam", 1617, 4.72, 1618); //could be added to polyline
    
    //chart 2
    doubleTrouble("γ And", 599, 2.16, 600); //polyline 
    doubleTrouble("β Cyg", 7404, 2.92, 7405); //polyline 
    doubleTrouble("61 Cyg", 8071, 4.79, 8072);  
    doubleTrouble("μ Cyg", 8294, 4.45, 8295);  
    doubleTrouble("γ Del", 7934, 3.87, 7933);  
    doubleTrouble("ζ Aqr", 8543, 3.75, 8544); //polyline
    doubleTrouble("α Psc", 591, 3.94, 592); //591 is in a polyline
    doubleTrouble("ζ Psc", 357, 4.89, 358); //could be added to a polyline
    doubleTrouble("ψ1 Psc", 306, 4.69, 307); //could be added to a polyline
    
    //chart 3
    doubleTrouble("γ Ari", 541, 4.04, 542); //polyline
    doubleTrouble("ε Ari", 883, 3.88, 884); //polyline
    doubleTrouble("δ Ori", 1846, 2.21, 1845);  
    doubleTrouble("λ Ori", 1873, 3.39, 1874); //polyline
    doubleTrouble("ζ Ori", 1942, 1.91, 1943); //polyline  
    doubleTrouble("θ Eri", 893, 2.91, 894); //polyline  
    doubleTrouble("32 Eri", 1207, 4.51, 1206);  
    doubleTrouble("θ Ori", 1889,3.69, 1887, 1890, 1891, 1888);  //near M42: 4 stars in one!

    //chart 4
    doubleTrouble("β Mon", 2350, 3.17, 2351, 2352);   //triplet, poly
    doubleTrouble("α Gem", 2882, 1.59, 2883);   // poly
    doubleTrouble("ι Cnc", 3467, 3.92, 3466);   //poly   
    doubleTrouble("γ Leo", 4047, 2.3, 4048);   // poly
    doubleTrouble("54 Leo", 4249, 4.31, 4250);   // poly
    doubleTrouble("γ Vel", 3199, 1.68, 3198);  //poly   
    doubleTrouble("υ Car", 3880, 2.96, 3881);   

    //chart 5
    doubleTrouble("α CVn", 4905, 2.79, 4904); //poly   
    doubleTrouble("24 Com", 4782, 4.78, 4781);    
    doubleTrouble("α Com", 4958, 4.47, 4959); //poly   
    doubleTrouble("24 Com", 5319, 4.40, 5318);    
    doubleTrouble("κ Boo", 5319, 4.40, 5318);    
    doubleTrouble("π Boo", 5465, 4.56, 5466); //poly    
    doubleTrouble("ζ Boo", 5467, 3.86, 5468); //poly    
    doubleTrouble("ε Boo", 5496, 2.59, 5495); //poly    
    doubleTrouble("μ Boo", 5723, 4.17, 5724); //poly    
    doubleTrouble("γ Vir", 4815, 2.91, 4816); //poly    
    doubleTrouble("3 Cen", 5200, 4.32, 5201); //poly    
    doubleTrouble("α Cen", 5449, -0.29, 5450); //poly    
    doubleTrouble("α Cru", 4720, 0.76, 4721); //poly    
    doubleTrouble("γ Cru", 4753, 1.62, 4754); //poly    
    doubleTrouble("μ Cru", 4888, 3.7, 4889);    
    doubleTrouble("π Lup", 5595, 4.02, 5596); //poly    
    doubleTrouble("κ Lup", 5636, 3.68, 5637); //poly    
    doubleTrouble("ξ Lup", 5915, 4.59, 5916);
    
    //chart 6
    doubleTrouble("ζ CrB", 5824, 4.69, 5823); //should be in a poly
    doubleTrouble("κ Her", 5998, 4.70, 5999); 
    doubleTrouble("α Her", 6395, 3.31, 6396); //poly 
    doubleTrouble("ρ Her", 6474, 4.14, 6473); 
    doubleTrouble("95 Her", 6718, 4.31, 6717); 
    doubleTrouble("δ Ser", 5778, 3.05, 5779); //poly 
    doubleTrouble("θ Ser", 7129, 4.03, 7130); //poly 
    doubleTrouble("ξ Sco", 5968, 4.16, 5967); 
    doubleTrouble("β Sco", 5974, 2.50, 5975); //poly 
    doubleTrouble("ν Sco", 6017, 3.89, 6016); //poly 
    doubleTrouble("ρ Oph", 6102, 4.63, 6103); 
    doubleTrouble("36 Oph", 6391, 4.34, 6390); 
    doubleTrouble("ο Oph", 6413, 4.98, 6414); 
    doubleTrouble("τ Oph", 6722, 4.78, 6721); 
    doubleTrouble("γ CrA", 7213, 4.21, 7214); //poly 
    doubleTrouble("γ CrA", 7213, 4.21, 7214); //poly 
    doubleTrouble("ε Lyr", 7039, 3.83, 7040, 7041, 7042); //polyline
    doubleTrouble("ζ Lyr", 7044, 4.09, 7045); //polyline
    
    //south pole - chart 7
    doubleTrouble("β Tuc", 123, 3.70, 124); //polyline
    doubleTrouble("γ Vol", 2728, 3.61, 2727); //polyline
    doubleTrouble("κ Vol", 3293, 4.75, 3294); 
    doubleTrouble("δ Aps", 6010, 4.18, 6011); //polyline
  }
  
  private void addProperNamesToStars() {
    log("Add proper names to stars.");
    StarName starName = new StarName();
    starName.readData();
    int count = 0;
    for (Star star : stars) {
      String name = star.NAME; //can be blank
      if (name.length() > 0) {
        int firstBlank = name.indexOf(" ");
        String constellationAbbr = name.substring(firstBlank).trim();
        String bayerOrFlamsteed = name.substring(0, firstBlank);
        String properName = starName.nameFor(constellationAbbr, bayerOrFlamsteed);
        if (properName.length() > 0) {
          ++count;
          star.PROPER_NAME = properName;
        }
      }
    }
    log("Added " + count + " proper names for stars.");
  }

  private void sortByRightAscension() {
    log("Sort by increasing right ascension.");
    Collections.sort(stars, comparing(Star::getRightAscension));
  }
  
  private void sortByMagnitude() {
    log("Sort by magnitude, to better handle drawing-overlaps. Draw brighter/bigger first.");
    Collections.sort(stars, comparing(Star::getMagnitude));
  }

  private void saveToIntermediateFile() throws IOException {
    log("Save to an intermediate file. Only needed for dev/debugging purposes.");
    String outputFileName = "stars.utf8"; 
    finalOutput(stars, outputFileName);
  }
  
  private Star processLine(String line, int index){
    Star result = new Star();
    result.INDEX = index;
    //prefer bayer to flamsteed
    result.NAME = bayerDesignation(slice(line, 8, 7)); //possibly empty
    if (isEmpty(result.NAME)){
      result.NAME = flamsteedDesignation(slice(line, 5, 10)); //possibly empty
    }
    result.MAG = Double.valueOf(slice(line, 103, 5)); //possible leading minus sign; that's ok
    
    int ra_hour = sliceInt(line, 76, 2); //leading 0's for these
    int ra_min = sliceInt(line, 78, 2);
    double ra_sec = sliceDbl(line, 80, 4); 
    result.RA = round(rads((ra_hour + ra_min/60.0 + ra_sec/3600.0) * 15)); //no integer div
    
    int sign = slice(line, 84, 1).equals("+") ? 1 : -1;
    int dec_deg = sliceInt(line, 85, 2); //leading 0's for these
    int dec_min = sliceInt(line, 87, 2);
    int dec_sec = sliceInt(line, 89, 2);
    result.DEC = sign * round(rads(dec_deg + dec_min/60.0 + dec_sec/3600.0)); //no integer div
    
    return result;
  }
  
  private String slice(String line, int start /*1-based*/, int numchars){
    return line.substring(start-1, start-1+numchars).trim();
  }
  
  private Integer sliceInt(String line, int start, int numchars){
    return Integer.valueOf(slice(line, start, numchars));
  }
  
  private Double sliceDbl(String line, int start, int numchars){
    return Double.valueOf(slice(line, start, numchars));
  }
  
  private double rads(double degs){
    //avoid integer division!
    return degs * Math.PI * 2 / 360.0D;
  }
  
  private boolean isEmpty(String text){
    return text == null || text.trim().length() == 0; 
  }
  
  /** 1 arc sec is 5x10-6 rads. */
  private double round(double rads){
    double numdecimals = 10000000.0D;
    return Math.round(rads*numdecimals)/numdecimals; //avoid int division
  }
  
  /** Example input: 'Alp And', 'Kap1Scl'. Anything else is coerced to blank.  */
  private String bayerDesignation(String name){
    String result = "";
    if (name != null){
      if (name.length()>3){
        //it has both the Greek letter and the constellation name
        //the last 3 letters are the constellation abbr
        int len = name.length();
        String constellation = name.substring(len-3);
        String greekText = name.substring(0, len-3).trim();
        String greekLetter = greekLetter(greekText);
        result = greekLetter + " " + constellation; 
      }
    }
    return result;
  }
  
  /** Example input: '82    Psc'. Note the N spaces in the middle. Anything else is coerced to blank.  */
  private String flamsteedDesignation(String name){
    String result = "";
    if (name != null){
      if (name.length()>2){
        int len = name.length();
        String constellation = name.substring(len-3).trim();
        int firstBlank = name.indexOf(" ");
        String flamsteedNumber = name.substring(0, firstBlank).trim();
        result = flamsteedNumber + " " + constellation; 
      }
    }
    return result;
  }
  
  /** Translate Latin text abbreviations into the actual Greek letters. */
  private static final Map<String, String> GREEK = new LinkedHashMap<String, String>();
  static {
    add("Alp", "α");
    add("Bet", "β");
    add("Gam", "γ");
    add("Del", "δ");
    add("Eps", "ε");
    add("Zet", "ζ");
    add("Eta", "η");
    add("The", "θ");
    add("Iot", "ι");
    add("Kap", "κ");
    add("Lam", "λ");
    add("Mu", "μ");
    add("Nu", "ν");
    add("Xi", "ξ");
    add("Omi", "ο");
    add("Pi", "π");
    add("Rho", "ρ");
    add("Sig", "σ");
    add("Tau", "τ");
    add("Ups", "υ");
    add("Phi", "φ");
    add("Chi", "χ");
    add("Psi", "ψ");
    add("Ome", "ω");
  }
  private static void add(String in, String out){
    GREEK.put(in, out);
  }
  private String greekLetter(String text /*Alp2, for example*/){
    String input = text;
    int len = text.length();
    char lastChar = text.charAt(len-1);
    if (Character.isDigit(lastChar)){
      input = text.substring(0, len-1); //without the number
    }
    String output = GREEK.get(input.trim());
    if (output == null){
      log("Greek letter not found for: '"  + input + "'");
    }
    if (Character.isDigit(lastChar)){
      output = output + lastChar;
    }
    return output;
  }
  
  private void finalOutput(List<Star> brightstars, String filename) throws FileNotFoundException, IOException {
    File out = new File(filename);
    log("Writing to file. The file is for info/debugging purposes only. File name: " + out.getCanonicalPath());
    FileOutputStream fos = new FileOutputStream(out);
    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, DataFileReader.ENCODING));
    writer.write("# Source: Yale Bright Star Catalog r5. J2000. Generated on: " + new Date() + ". Index, Constellation, Right Ascension, Declination, Magnitude, Designation, and Proper name.");
    writer.newLine();
    for(Star nearbystar: brightstars){
      writer.write(nearbystar.toString());
      writer.newLine();
    }
    writer.newLine();
    writer.close();    
  }
  
  private static void log(Object text){
    System.out.println(text.toString());
  }

  /** Change the magnitude of the given star. */
  private void changeMag(int index, Double mag) {
    Star star = stars.get(index);
    log("Change mag to " + mag + " . Star: " + star);
    star.MAG = mag;
  }
  
  /** Change the name and magnitude of the given star. */
  private void changeMagAndName(int index, String newName, Double mag) {
    Star star = stars.get(index);
    log("Double trouble. Change mag to " + mag + ", name to " + newName + ". Star: " + star);
    star.MAG = mag;
    star.NAME = newName;
  }
  
  /** 
   Alter the magnitude and name of one star, while suppressing the rest.
   The suppressed have their magnitude changed to a dim value; they will be retained in the 
   catalog of stars, but won't show up on the chart since they are below the limiting mag. 
  */
  private void doubleTrouble(String newName,  int index, Double mag, Integer... suppressed) {
    changeMagAndName(index, newName, mag);
    for (Integer suppress : suppressed) {
      changeMag(suppress, DIM);
    }
  }
}