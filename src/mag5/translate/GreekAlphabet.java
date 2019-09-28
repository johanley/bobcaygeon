package mag5.translate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mag5.util.DataFileReader;

/** 
 The letters of the Greek alphabet.
 The Bayer designation for stars uses the Greek alphabet. 
*/
public class GreekAlphabet {

  private static final String LETTERS = "α β γ δ ε ζ η θ ι κ λ μ ν ξ ο π ρ σ τ υ φ χ ψ ω";
  
  /** All letters of the Greek alphabet, in the usual order. */
  public static final List<String> LETTERS_LIST = Arrays.asList(LETTERS.split(" "));
  
  /**
   Return the names of the letters, for a given language.
   The symbols of the letters is a fixed list of 24 characters.
   The names, however, vary a bit from one language to another.
   The order of the returned list is significant, and matches the order of LETTERS_LIST. 
  */
  public List<String> namesOfLetters(Lang lang) {
    List<String> result = new ArrayList<>();
    DataFileReader reader = new DataFileReader();
    List<String> lines = reader.readFile(this.getClass(), "greek_" + lang + ".utf8");
    for (String line : lines) {
      result.add(line.trim());
    }
    return result;
  }
}
