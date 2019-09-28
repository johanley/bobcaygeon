package mag5.translate;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mag5.util.DataFileReader;

/** Translate a label key into a label value, in a given lang. */
public class Label {
  
  public String text(String key, Lang lang) {
    if (translations.size() == 0) {
      init();
    }
    String result = translations.get(lang).get(key);
    if (result == null) {
      //should not happen for production
      throw new IllegalArgumentException("Can't find label named: " + key + " for lang " + lang);
    }
    return result;
  }
  
  /** Short month names, translated, are available thru the JDK. No need for text files. */
  public String shortMonthName(Lang lang, Month month) {
    LocalDate someDate = LocalDate.of(1962, month.getValue(), 1);
    String result = DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag(lang.name())).format(someDate);
    return result;
  }

  // PRIVATE
  private static Map<Lang, Map<String, String>> translations = new LinkedHashMap<>();

  /** There's no need for the caller to init explicitly. */
  private static void init() {
    initAllLangs();
    readInAllTranslations();
  }
  
  static private void initAllLangs() {
    for(Lang lang : Lang.values()) {
      Map<String, String> emptyMap = new LinkedHashMap<>();
      translations.put(lang, emptyMap);
    }
  }
  
  /** Scan files in the current directory that start with 'labels_', and read their data. */
  static private void readInAllTranslations() {
    for(Lang lang : Lang.values()) {
      DataFileReader reader = new DataFileReader();
      String fileName = "labels_" + lang + ".utf8";
      log("Reading in translations from " + fileName);
      List<String> lines = reader.readFile(Label.class, fileName);
      for (String line : lines) {
        if (line.equals("STOP-PARSING")) {
          break;
        }
        if (!line.startsWith("#") && line.trim().length()>0) {
          String[] parts = line.split("=");
          translations.get(lang).put(parts[0].trim(), parts[1].trim());
        }
      }
      log("Translation: " + fileName + " has " + translations.get(lang).size() + " items");
    }
  }

  static private void log(String msg) {
    System.out.println(msg);
  }
}