package mag5.book;

import java.awt.Graphics2D;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.GrayColor;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import mag5.draw.ChartUtil;
import mag5.translate.Label;
import mag5.translate.Lang;
import sun.awt.image.ImageWatched.Link;

/** 
 Anything that's not a chart is generated here.
 
 <P>WARNING: Helvetica (the pdf default fault) cannot appear in the list of fonts 
 in the pdf properties. Lulu.com accepts only embedded fonts. 
*/
class TextContent {

  TextContent(Document document, Label labels, Lang lang, Graphics2D g){
    this.document = document;
    this.labels = labels;
    this.lang = lang;
    this.g = g;
  }
  
  /** Several pages of text at the start, after the {@link FrontMatter}. */
  void overview() throws DocumentException  {
    bigHeader("overview");
    paragraph("overview", 2);
    section("large-size", 3);
    section("easy-to-read", 2);
    section("show-whats-needed", 2);
    section("planispheres", 4);
    section("devices", 4);
    
    bigHeader("basic-astro-facts");
    paragraph("basic-astro-facts-a", 2);
    paragraph("basic-astro-facts-b", 6);
    paragraph("basic-astro-facts-c", 2);
    paragraph("basic-astro-facts-d", 3);
    
    section("declination", 4);
    section("right-ascension", 8);
    section("ecliptic", 7);
    section("stellar-magnitude", 7);
    section("constellations", 6);

    section("star-designations", 9); 
    
    section("asterism", 2);
    listOfThings("asterism-list", 11);
    
    section("deep-sky-objects", 4);
    listOfThings("deep-sky-objects-types", 4);
    
    //emptyLines(1);
    paragraph("deep-sky-objects-b", 2);
    
    bigHeader("how-to-use-the-charts");
    section("dark-site", 5);
    section("cardinal-directions", 7);
    section("dark-adapt", 2);
    section("averted-vision", 4);
    section("red-light", 3);
    section("date-scale", 2);
    paragraph("date-scale-a", 7);
    paragraph("lmt-a", 4);
    tableForLmtConversion();
    paragraph("lmt-b", 10);
    
    section("hints", 1);
    listOfThings("hints-list", 8);
  }

  /** Table listing all of the constellation names, with pronunciation hints. */
  void listOfConstellations() throws DocumentException {
    bigHeader("constellation-list");
    tableOfConstellations();
  }

  /** Small "About" section. */
  void about() throws DocumentException {
    section("about", 3);
  }
  
  // PRIVATE
  private Document document;
  private Label labels;
  private Lang lang;
  private Graphics2D g;
  private static final String NL = System.getProperty("line.separator");
  private static final String SPACE = " ";

  /**
   NAMING CONVENTION FOR chunk-labels keys used in labels_xxx.utf8.
   key (the header)
   key-1...key-n (n chunks in the body)
  */
  private void section(String key, int n) throws DocumentException {
    header(key);
    paraFromChunks(key, n);
  }
  
  /** Like section, but without a header*/
  private void paragraph(String key, int n) throws DocumentException {
    paraFromChunks(key, n);
  }
  
  private void header(String key) throws DocumentException {
    String text = labels.text(key, lang);
    Chunk chunk = new Chunk(text, boldFont());
    Paragraph result = new Paragraph();
    result.add(chunk);
    result.setAlignment(Element.ALIGN_LEFT);
    document.add(result);
 }
  
  private void bigHeader(String key) throws DocumentException {
    String text = labels.text(key, lang);
    Chunk chunk = new Chunk(text, biggerBoldFont());
    Paragraph result = new Paragraph();
    result.add(chunk);
    result.setAlignment(Element.ALIGN_LEFT);
    document.add(result);
    emptyLines(1);
 }
  
  private void paraFromChunks(String key, int numItems) throws DocumentException {
    Paragraph p = newPara();
    chunks(key, numItems, p);
    document.add(p);
    emptyLines(1);
  }
  
  private Paragraph newPara() {
    Paragraph result = new Paragraph();
    result.setAlignment(Element.ALIGN_JUSTIFIED);
    return result;
  }
  
  private void newPage() {
    document.newPage();
  }
  
  private void chunks(String baseKey, int lastKey, Paragraph p) {
    for(int idx = 1; idx <= lastKey; ++idx) {
      String text = labels.text(baseKey + "-" + idx, lang);
      Chunk chunk = new Chunk(text + SPACE, normalFont());
      p.add(chunk);
    }
  }
  
  private void emptyLines(int num) throws DocumentException {
    Chunk chunk = new Chunk(someEmptyLines(num), normalFont());
    Paragraph space = new Paragraph();
    space.add(chunk);
    document.add(space);
  }
  
  private void listOfThings(String key, int numThings) throws DocumentException {
    List list = new List(List.UNORDERED);
    list.setListSymbol("•");
    list.setIndentationLeft(PdfConfig.LIST_INDENT);
    for(int idx = 1; idx <= numThings; ++idx) {
      String text = labels.text(key+"-"+idx, lang);
      Chunk chunk = new Chunk(SPACE + text, normalFont());
      ListItem item = new ListItem(chunk);
      list.add(item);
    }
    document.add(list);
    emptyLines(1);
  }

  
  private Font normalFont() {
    //WARNING: I added BaseFont.IDENTITY_H to make the Greek letters appear; otherwise nothing showed
    /*
     * https://stackoverflow.com/questions/3858423/itext-pdf-greek-letters-are-not-appearing-in-the-resulting-pdf-documents
     * https://itextpdf.com/en/resources/faq/technical-support/itext-5-legacy/how-print-mathematical-characters
     */
    return FontFactory.getFont(PdfConfig.FONT, BaseFont.IDENTITY_H, PdfConfig.FONT_SIZE_NORMAL, Font.NORMAL);
  }
  
  private Font tinyFont() {
    return FontFactory.getFont(PdfConfig.FONT, BaseFont.IDENTITY_H, PdfConfig.FONT_SIZE_SMALL, Font.NORMAL);
  }
  
  private Font font(float size) {
    return FontFactory.getFont(PdfConfig.FONT, BaseFont.IDENTITY_H, size, Font.NORMAL);
  }

  private Font italicFont() {
    return FontFactory.getFont(PdfConfig.FONT_ITALIC, BaseFont.IDENTITY_H, PdfConfig.FONT_SIZE_NORMAL, Font.ITALIC);
  }
  
  private Font boldFont() {
    return FontFactory.getFont(PdfConfig.FONT, BaseFont.IDENTITY_H, PdfConfig.FONT_SIZE_NORMAL, Font.BOLD);
  }

  private Font biggerBoldFont() {
    return FontFactory.getFont(PdfConfig.FONT, BaseFont.IDENTITY_H, PdfConfig.FONT_SIZE_SECTION_HEADER, Font.BOLD);
  }
  
  // Font 'Times New Roman' with 'Identity-H' is not recognized.
  private Font font(String name, float size, int style)  {
    BaseFont bf = null;
    try {
      bf = BaseFont.createFont(name, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    } 
    catch (Throwable e) {
      log("Can't make this font: " + name);
      e.printStackTrace();
    }
    Font f = new Font(bf, size, style); 
    return f;
  }

  private static void log(String msg) {
    System.out.println(msg);
  }

  /** 
  WARNING: using Paragraph.setSpacingBefore/After injects Helvetica (unembedded) references in the the document!
  Those references cause rejection by lulu.com, because it requires all fonts to be embedded. 
  Hence this method, which just puts empty lines into a para, instead of calling the setSpacingXXX methods.
  */
  private String someEmptyLines(int n) {
    StringBuilder result = new StringBuilder();
    for (int idx = 0; idx < n; ++idx) {
      result.append(NL);
    }
    return result.toString();
  }
  
  private void tableOfConstellations() throws DocumentException {
    PdfPTable table = new PdfPTable(4);
    table.setWidthPercentage(100);
    table.setHorizontalAlignment(Element.ALIGN_LEFT);
    float[] relativeWidths = {4.0f, 10.0f, 10.0f, 16.0f};
    table.setWidths(relativeWidths);
    addHeaderCell(table, "constellation-list-header-1");
    addHeaderCell(table, "constellation-list-header-2");
    addHeaderCell(table, "constellation-list-header-3");
    addHeaderCell(table, "constellation-list-header-4");
    for(int idx = 1; idx <= 88; idx++) {
      String[] row = constellationRow(idx);
      addCell(table, row[1]); //abbr
      addCell(table, row[0]); //name
      addCell(table, row[2]); //descr
      addCell(table, row[3]); //pronunciation
    }
    table.setHeaderRows(1);
    document.add(table);
    emptyLines(1);
  }
  
  private  String[] constellationRow(int idx){
    String baseLabel = "constellation-list-";
    String row = labels.text(baseLabel + idx, ChartUtil.lang());
    return row.split(",");
  }

  private void addHeaderCell(PdfPTable table, String key) {
    String string = labels.text(key, ChartUtil.lang());
    Chunk chunk = new Chunk(string, boldFont());
    GrayColor grey = new GrayColor(0.8f);
    addChunk(table, chunk, grey);
  }

  private void addCell(PdfPTable table, String string) {
    Chunk chunk = new Chunk(string, normalFont());
    addChunk(table, chunk, null);
  }

  private void addChunk(PdfPTable table, Chunk chunk, BaseColor baseColor) {
    Phrase phrase = new Phrase(chunk);
    PdfPCell cell = new PdfPCell(phrase);
    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
    cell.setLeading(0f, 1.1f); // 0 + 1.5 times the font height
    cell.setPaddingBottom(4);
    cell.setPaddingLeft(4);
    if (baseColor != null) {
      cell.setBackgroundColor(baseColor);
    }
    table.addCell(cell);
  }
  
  private void tableForLmtConversion() throws DocumentException {
    PdfPTable table = new PdfPTable(3);
    table.setWidthPercentage(100);
    table.setHorizontalAlignment(Element.ALIGN_LEFT);
    float[] relativeWidths = {9.0f, 6.0f, 11.0f};
    table.setWidths(relativeWidths);
    addHeaderCell(table, "lmt-conversion-header-1");
    addHeaderCell(table, "lmt-conversion-header-2");
    addHeaderCell(table, "lmt-conversion-header-3");
    for(int idx = 1; idx <= 5; idx++) {
      String[] row = lmtConversionRow(idx);
      addCell(table, row[0]); //name
      addCell(table, row[1]); //value
      addCell(table, row[2]); //explanation
    }
    table.setHeaderRows(1);
    document.add(table);
    emptyLines(1);
  }
  
  private  String[] lmtConversionRow(int idx){
    String baseLabel = "lmt-conversion-list-";
    String row = labels.text(baseLabel + idx, ChartUtil.lang());
    return row.split(",");
  }
  
}
