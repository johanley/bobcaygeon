package mag5.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
  Read a file in the same directory as the calling class, and return it as a list of strings.
  
  <P>This class allows the code to follow the 
  <a href='http://www.javapractices.com/topic/TopicAction.do?Id=205'>package-by-feature</a> design principle.
*/
public class DataFileReader {

  /**
   Read a text file and return it as a list of (untrimmed) Strings.
   @param aClass the calling class
   @param fileName name of UTF-8 text file that resides in the same directory as the calling class. 
  */
  public List<String> readFile(Class<?> aClass, String fileName){
    List<String> result = new ArrayList<>();
    try (
      //uses the class loader search mechanism:
      InputStream input = aClass.getResourceAsStream(fileName);
      InputStreamReader isr = new InputStreamReader(input, ENCODING);
      BufferedReader reader = new BufferedReader(isr);
    ){
      String line = null;
      while ((line = reader.readLine()) != null) {
        result.add(line);
      }      
    }
    catch(IOException ex){
      System.out.println("CANNOT OPEN FILE: " + fileName);
    }
    return result;
  }
  
  public final static Charset ENCODING = StandardCharsets.UTF_8;
  public final static String COMMENT = "#";
}