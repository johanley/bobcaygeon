package mag5.book;

import java.io.IOException;
import java.net.MalformedURLException;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import mag5.draw.ChartUtil;
import mag5.translate.Label;

/** Items that appear at the start of the book, before the main content. */
class FrontMatter {
  
  FrontMatter(Document document, PdfWriter writer, Label labels){
    this.document = document;
    this.writer = writer;
    this.labels = labels;
  }

  void generate() throws DocumentException, MalformedURLException, IOException {
    log("Front matter.");
    //blankPage();
    largeTitlePage();
    blankPage();
  }

  //PRIVATE
  private Document document;
  private PdfWriter writer;
  private Label labels;
  private static final String NL = System.getProperty("line.separator");
  private float SMALL = PdfConfig.FONT_SIZE_SMALL_TITLE;
  private float LARGE = PdfConfig.FONT_SIZE_LARGE_TITLE;

  private static void log(String msg) {
    System.out.println(msg);
  }
  
  private void largeTitlePage() throws DocumentException, MalformedURLException, IOException {
    log("Large title page.");
    emptyLines(2);
    someText("author", SMALL, false);
    emptyLines(4);
    
    someText("title-upper-case", LARGE, false);
    emptyLines(4);
    
    addLogoImage();
    
    emptyLines(2);
    someText("bold-orion", PdfConfig.FONT_SIZE_SMALL_TITLE, true);
    emptyLines(4);
    
    someText("date-published", PdfConfig.FONT_SIZE_SMALL_TITLE, false);
  }
  
  private String text(String key) {
    return labels.text(key, ChartUtil.lang());
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
  
  private void emptyLines(int numLines) throws DocumentException {
    Chunk chunk = new Chunk(someEmptyLines(numLines), normalFont());
    Paragraph para = new Paragraph();
    para.add(chunk);
    document.add(para);
  }
  
  private void blankPage() {
    log("Start blank page.");
    document.newPage();
    writer.setPageEmpty(false); //otherwise the page is ignored! wacky API
    document.newPage(); 
  }
  
  private void someText(String key, float size, boolean isItalic)  throws DocumentException {
    Chunk chunk = null;
    if (isItalic) {
      chunk = new Chunk(text(key), italicFont(size));
    }
    else {
      chunk = new Chunk(text(key), normalFont(size));
    }
    Paragraph para = new Paragraph();
    para.setAlignment(Element.ALIGN_CENTER);
    para.add(chunk);
    document.add(para);
  }
  
  private static Font normalFont() {
    return FontFactory.getFont(PdfConfig.FONT, PdfConfig.FONT_SIZE_NORMAL, Font.NORMAL);
  }
  
  private static Font normalFont(float size) {
    return FontFactory.getFont(PdfConfig.FONT, size, Font.NORMAL);
  }
  
  private Font italicFont() {
    return FontFactory.getFont(PdfConfig.FONT_ITALIC, PdfConfig.FONT_SIZE_NORMAL, Font.ITALIC);
  }
  
  private Font italicFont(float size) {
    return FontFactory.getFont(PdfConfig.FONT_ITALIC, size, Font.ITALIC);
  }
  
  private void addLogoImage() throws DocumentException, MalformedURLException, IOException {
    Image image = Image.getInstance("image/flammarion.jpg");
    image.scalePercent(55f); //the percent will depend on the image size
    image.setAlignment(Element.ALIGN_CENTER);
    document.add(image);
  }
}
