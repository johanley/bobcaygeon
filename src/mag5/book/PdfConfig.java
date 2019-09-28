package mag5.book;

import com.itextpdf.text.pdf.PdfWriter;

/** Various settings for the PDF output. */
public class PdfConfig {

  /** See lulu.com's recommendation <a href='http://connect.lulu.com/en/discussion/33681'>here</a>. */
  static final char PDF_VERSION = PdfWriter.VERSION_1_3;

  /* default 72 points per inch (28.346 points per cm, 2.8346 points per mm) */
  public static final float WIDTH = pointsFromIn(8.5); //MUST MATCH THE TARGET PRINTING SERVICE
  public static final float HEIGHT = pointsFromIn(11);

  static final float MARGIN_LEFT = pointsFromIn(0.75f);
  static final float MARGIN_RIGHT = pointsFromIn(0.75f);
  static final float MARGIN_TOP = pointsFromIn(0.75f);
  static final float MARGIN_BOTTOM = pointsFromIn(0.5f);
  static final float HEADER_Y = pointsFromIn(0.6f);
  static final float MARGIN_INNER = pointsFromIn(0.87f);
  static final float MARGIN_OUTER = pointsFromIn(0.63f);

  
  /** Please see {@link mag5.book.MyFontMapper}. */
  static final String FONT = "Times New Roman"; 
  static final String FONT_ITALIC = "Times New Roman";

  /** When you change this, review LAST_NUMBERED_PAGE */ 
  static final float FONT_SIZE_NORMAL = 12F; 
  static final float FONT_SIZE_SMALL = 8F;
  static final float FONT_SIZE_SMALL_TITLE = 14;    
  static final float FONT_SIZE_SECTION_HEADER = 18;    
  static final float FONT_SIZE_LARGE_TITLE = 30;
  
  /** 
   Spacing between lines.
   Usually, <code>document.leading = document.lineHeight</code> 
  */
  static final float LINE_LEADING = 15f; //16f is the default; 12 too close
  
  /** No page numbers for the front matter. REVIEW THIS. SENSITIVE TO THE CONTENT. */
  static final int FRONT_MATTER_NUM_PAGES = 2;
  
  /** The last 2 pages need to be completely blank. REVIEW THIS. SENSITIVE TO THE CONTENT. */
  static final int LAST_NUMBERED_PAGE = 5;

  static final float LIST_INDENT = 10.0F;
  
  public static float pointsFromMm(float mm) {
    return mm * POINTS_PER_INCH / MM_PER_INCH;
  }
  
  //PRIVATE 
  
  private static final int POINTS_PER_INCH = 72; //the itext default is 72
  private static final float MM_PER_INCH = 2.54f;

  private static float pointsFromIn(double inches) {
    return (float) inches * POINTS_PER_INCH;
  }
}