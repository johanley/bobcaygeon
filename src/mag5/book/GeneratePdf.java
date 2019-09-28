 package mag5.book;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import mag5.chartlabels.ChartLabel;
import mag5.chartlabels.ChartLabels;
import mag5.constellation.ConstellationLines;
import mag5.deepskyobject.DeepSkyObject;
import mag5.deepskyobject.DeepSkyObjects;
import mag5.draw.Bounds;
import mag5.draw.ChartUtil;
import mag5.draw.Drawer;
import mag5.draw.Hemisphere;
import mag5.draw.equatorial.DrawEquatorialChart;
import mag5.draw.polar.DrawPolarChart;
import mag5.star.BayerLetterPlacement;
import mag5.star.Star;
import mag5.star.StarCatalog;
import mag5.translate.Label;


/**
Generate a pdf file suitable for printing by lulu.com.

<P>
<ul>
  <li>number of pages needs to be evenly divisible by 4
  <li>the last two pages need to be completely blank
  <li>the font needs to be embedded in the pdf. You can confirm that the font is embedded via the file's properties.
</ul>

<P>Constraints on images are <a href='http://connect.lulu.com/en/discussion/336486/text-boxes-and-pictures'>here</a>
 (jpg, png, gif).

<P>The lulu.com printing service used in the past are as follows. 
(WARNING: you only get these options in the 'US store', not the 'Canadian Store').
<ul>
 <li>8.5 x 11
 <li>premium paperback
 <li>saddle stitch (staples)
 <li>black and white on white (cream available only for smaller pages)
 <li>paper 104 gsm (70 pound) 
 <li>glossy cover
</ul>

<P>The cover is a separate (image) file. It's created outside of this project. 
Constraints from lulu.com:
<ul>
 <li>PDF, JPG, GIF, or PNG
 <li>image resolution should be 300dpi or better (this can be set using irfanview)
 <li>try working initially with a 24-bit bitmap; later save to the desired format (png), and ensure the dpi is 300 (irfanview can be used, for example)
 <li>spine width: 0 Postscript points wide; 0.000 cm; 0 px
 <li>spine begins 621 Postscript points from the left; 21.905 cm; 2588 px
 <li>total cover width: 1242 X 810 Postscript points; 43.810 cm X 28.571 cm; 5175px X 3375px
 <li>the yellow background I used has rgb = (239,228,176)
</ul>

<P>
WARNING: the length of the overview/instructions text at the start of the PDF affects other items. 
When the length of that text changes, those other items need to be checked manually, to make 
sure everything is hunky-dory. 
<ul>
 <li>the charts need to start on an even page number
 <li>page numbers are set only for the preamble text, and then they stop on the last page of text
</ul>
*/
public class GeneratePdf {

  /** 
   Default for the full file name for the output PDF - {@value}.
   To override the default, just use the command line argument 
   <code>-Dmag5output=my-full-file-name</code>, 
   and specify the full file name, including all directories.
  */
  static final String OUTPUT_FILE = "C:\\temp\\mag5.pdf";
  
  /** 
   Default directory that has all of the system's font files - {@value}.
   To override the default, just use the command line argument 
   <code>-Dmag5fonts=my-full-dir-name</code>, 
   and specify the full directory name, and end with a separator.
  */
  static final String FONT_DIR = "C:\\WINDOWS\\FONTS\\";
  
  /** UTF-8 everywhere. */
  static final Charset ENCODING = StandardCharsets.UTF_8;
  
  /** Generate the star atlas as a single pdf file. */
  public static void main(String... args) throws DocumentException, IOException {
    log("Mag 5 Star Atlas: starting PDF generation.");
    GeneratePdf generator = new GeneratePdf();
    generator.outputPdf();
    log("Done.");
  }
  
  /** Build the output PDF file from scratch. */
  void outputPdf() throws DocumentException, MalformedURLException, IOException {
    String outputFile = outputFileName();
    initPdf(outputFile, fontDirectory());
    initChartData();
    addMetadata();
    frontMatter(); 
    mainContent();
    pageCountDivisibleByFour();
    finalizeIt();
    log("Output file: " + outputFile);
  }
  
  // PRIVATE

  private Document document;  
  private PdfWriter writer;
  
  private Graphics2D g;
  private PdfContentByte contentByte;
  private PdfTemplate template;
  
  private Label labels = new Label();
  private Map<Integer, List<ChartLabel>> chartLabelMap;
  private StarCatalog starCatalog;
  private ConstellationLines constellationLines;
  private List<DeepSkyObject> dsos;
  
  private String outputFileName() {
    return System.getProperty("mag5output", OUTPUT_FILE);
  }
  
  private String fontDirectory() {
    return System.getProperty("mag5fonts", FONT_DIR);
  }

  /**
   Read in settings.
   Set page size, margins, register fonts, etc.
   Fonts need to be in the system's hard drive somewhere.
   The font is not attached to the Document as a whole; it's attached to lower level items. 
  */
  private void initPdf(String fileName, String fontDir) throws FileNotFoundException, DocumentException {
    log("Initial setup of pdf Document. Setting page size, margins. Reading in fonts.");
    
    embedFonts();
    registerAllFontsIn(fontDir, false);
    
    document = new Document();
    Rectangle rect = new Rectangle(PdfConfig.WIDTH, PdfConfig.HEIGHT);
    document.setPageSize(rect);
    document.setMargins(PdfConfig.MARGIN_INNER, PdfConfig.MARGIN_OUTER, PdfConfig.MARGIN_TOP, PdfConfig.MARGIN_BOTTOM);

    //should this be passed an encoding, I wonder?
    writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
    writer.setPdfVersion(PdfConfig.PDF_VERSION); 
    writer.setPageEvent(new Header());
    writer.setViewerPreferences(PdfWriter.PageLayoutSinglePage);
    document.open(); //need to call this early!
  }
  
  private void initChartData() throws IOException {
    starCatalog = new StarCatalog();
    starCatalog.generateIntermediateStarCatalog();
    
    constellationLines = new ConstellationLines();
    constellationLines.readData();
    
    DeepSkyObjects dso = new DeepSkyObjects();
    dsos = dso.list();
    
    chartLabelMap = new ChartLabels(labels).readData();
    BayerLetterPlacement bayerLetterPlacement = new BayerLetterPlacement();
    bayerLetterPlacement.addBayerPlacementTo(starCatalog.all());
  }
  
  /** Text is defined in label-files, specific to each language. */
  private String text(String key) {
    return labels.text(key, ChartUtil.lang());
  }
  
  private void addMetadata() {
    document.addAuthor(text("author")); 
    document.addTitle(text("title"));
    document.addSubject(text("subject"));
  }

  /** Lulu requires absolutely all fonts to be embedded. */
  private void embedFonts() {
    FontFactory.defaultEmbedding = true;
  }
  
  private void registerAllFontsIn(String fontDir, boolean log) {
    log("Registering all fonts in " + fontDir);
    FontFactory.registerDirectory(fontDir);
    if (log) {
      Set<String> fonts = new TreeSet<String>(FontFactory.getRegisteredFonts());
      for (String fontname : fonts) {
          log(fontname);
      } 
    }
  }
  
  private void frontMatter() throws DocumentException, MalformedURLException, IOException {
    FrontMatter frontMatter = new FrontMatter(document, writer, labels);
    frontMatter.generate();
  }
  
  private void blankPage() {
    log("Start blank page.");
    document.newPage();
    writer.setPageEmpty(false); //otherwise the page is ignored! Bit of a wacky API.
    document.newPage(); 
  }
  
  private void mainContent() throws DocumentException, MalformedURLException, IOException {
    log("Main content...");
    overview();
    //CAREFUL: the charts need to start on an even page!
    document.newPage();
    bothHemispheres();
    //DEBUGGING ONLY: to save time, you may want to restrict to a subset of the full set of charts
    //oneHemisphere();
    //northPole(1);
    //equatorialChartForNorth(2, 20.0, 2.0);
    listConstellations();
    about();
  }
  
  /** For debugging. */
  @SuppressWarnings("unused")
  private void oneHemisphere() {
    if (Hemisphere.NORTH == ChartUtil.HEMISPHERE) {
      northPole(1);
      equatorialChartForNorth(2, 20.0, 2.0);
      equatorialChartForNorth(3, 1.0, 7.0);
      equatorialChartForNorth(4, 5.0, 11.0);
      equatorialChartForNorth(5, 10.0, 16.0);
      equatorialChartForNorth(6, 15.0, 21.0);
      southPole(7);
    }
    else {
      southPole(1);
      equatorialChartForSouth(2, 20.0, 2.0);
      equatorialChartForSouth(3, 1.0, 7.0);
      equatorialChartForSouth(4, 5.0, 11.0);
      equatorialChartForSouth(5, 10.0, 16.0);
      equatorialChartForSouth(6, 15.0, 21.0);
      northPole(7);
    }
  }

  /** 
   Nice idea: give the reader both hemispheres, not just one.
   People travel a lot. 
   You can use the charts to dream about traveling to the other hemisphere. 
  */
  private void bothHemispheres() {
    ChartUtil.HEMISPHERE = Hemisphere.NORTH;
    northPole(1);
    equatorialChartForNorth(2, 20.0, 2.0);
    equatorialChartForNorth(3, 1.0, 7.0);
    equatorialChartForNorth(4, 5.0, 11.0);
    equatorialChartForNorth(5, 10.0, 16.0);
    equatorialChartForNorth(6, 15.0, 21.0);
    
    ChartUtil.HEMISPHERE = Hemisphere.SOUTH;
    southPole(7);
    //these are 'upside down' with respect to the northern-hemisphere versions of these charts
    equatorialChartForSouth(8, 20.0, 2.0);
    equatorialChartForSouth(9, 1.0, 7.0);
    equatorialChartForSouth(10, 5.0, 11.0);
    equatorialChartForSouth(11, 10.0, 16.0);
    equatorialChartForSouth(12, 15.0, 21.0);
  }
  
  private void overview() throws DocumentException {
    textContent().overview();
  }
  
  private void listConstellations() throws DocumentException {
    textContent().listOfConstellations();
  }
  
  private void about() throws DocumentException {
    textContent().about();
  }
  
  private TextContent textContent() {
    TextContent text = new TextContent(document, labels, ChartUtil.lang(), g);
    return text;
  }
  
  private void drawThePage(Drawer drawer) {
    drawInit();
    drawer.draw();
    drawEnd();
    document.newPage();
  }

  private void drawInit() {
    log("Fresh graphics context.");
    contentByte = writer.getDirectContent();
    template = contentByte.createTemplate(PdfConfig.WIDTH, PdfConfig.HEIGHT);
    g = new PdfGraphics2D(template, PdfConfig.WIDTH, PdfConfig.HEIGHT, new MyFontMapper());
    BasicStroke thinStroke = new BasicStroke(ChartUtil.STROKE_WIDTH_DEFAULT);
    /*
    BasicStroke thinStroke2 = new BasicStroke(1.0f,
        BasicStroke.CAP_BUTT, //no decoration
        BasicStroke.JOIN_ROUND);
    */
    g.setStroke(thinStroke);
    g.setFont(ChartUtil.baseFont());
    log("Graphics font: " + g.getFont().getFontName());
    //g = template.createGraphics(PdfConfig.WIDTH, PdfConfig.HEIGHT, new DefaultFontMapper()); //watch out! : deprecated!
  }
  
  /** You need to call this to actually draw the items to the page. */
  private void drawEnd() {
    log("Flushing graphics.");
    g.dispose();
    contentByte.addTemplate(template, 0, 0); // x,y positioning of graphics in PDF page
  }
  
  private void northPole(int chartNum) {
    polarChartFor(chartNum, 50.0, 90.0, 14.0, 2.0);
    polarChartFor(chartNum, 50.0, 90.0, 2.0, 14.0);
  }
  
  private void southPole(int chartNum) {
    polarChartFor(chartNum, -90.0, -50.0, 0.0, 12.0);
    polarChartFor(chartNum, -90.0, -50.0, 12.0, 24.0);
  }
  
  private void polarChartFor(int chartNum, double decMin, double decMax, double raMin, double raMax) {
    drawThePage(new Drawer() {
      public void draw() {polarChart(chartNum, decMin, decMax, raMin, raMax);}
    });
  }
  
  private void polarChart(int chartNum, double decMin, double decMax, double raMin, double raMax) {
    Bounds bounds = new Bounds(decMin, decMax, raMin, raMax);
    List<Star> stars = filterPolarStarsFor(bounds);
    DrawPolarChart polarChart = new DrawPolarChart(chartNum, bounds, stars, constellationLines, dsos, labels, chartLabelMap.get(chartNum), g);
    polarChart.draw();
  }
  
  private static final boolean TOP = true;
  private static final boolean BOTTOM = false;
  private static final boolean NORTH = true;
  private static final boolean SOUTH = false;
  
  private void equatorialChartForNorth(Integer chartNum, double raStart, double raEnd) {
    drawThePage(new Drawer() {
      public void draw() { equatorialChartNorth(chartNum, TOP, raStart, raEnd); }
    });
    drawThePage(new Drawer() {
      public void draw() { equatorialChartNorth(chartNum, BOTTOM, raStart, raEnd); }
    });
  }
  
  private void equatorialChartForSouth(Integer chartNum, double raStart, double raEnd) {
    drawThePage(new Drawer() {
      public void draw() { equatorialChartSouth(chartNum, TOP, raStart, raEnd); }
    });
    drawThePage(new Drawer() {
      public void draw() { equatorialChartSouth(chartNum, BOTTOM, raStart, raEnd); }
    });
  }
  
  private void equatorialChartNorth(Integer chartNum, Boolean isTop, double raStart, double raEnd) {
    equatorialChart(chartNum, NORTH, isTop, raStart, raEnd);
  }
  
  private void equatorialChartSouth(Integer chartNum, Boolean isTop, double raStart, double raEnd) {
    equatorialChart(chartNum, SOUTH, isTop, raStart, raEnd);
  }
  
  /** 
   This gives the correct bounds, according to hemisphere/topness, but the projection needs to be sensitive to
   the hemisphere as well, since the stars positions are 'upside down' in the southern hemisphere.
   In the southern hem, the bounds are reversed with respect to the north. 
  */
  private void equatorialChart(Integer chartNum, Boolean isNorthernHem, Boolean isTop, double raStart, double raEnd) {
    double maxDec = equatorialMaxDec(); //degs, positive
    Bounds a = new Bounds(0.0, maxDec, raStart, raEnd);
    Bounds b = new Bounds(-maxDec, 0.0, raStart, raEnd);
    Bounds bounds = null;
    if (isNorthernHem) {
      bounds = isTop ? a : b;
    }
    else {
      bounds = isTop ? b : a;
    }
    List<Star> stars = filterEquatorialStarsFor(bounds);
    DrawEquatorialChart chart = new DrawEquatorialChart(chartNum, bounds, stars, constellationLines, dsos, labels, chartLabelMap.get(chartNum), g);
    chart.draw();
  }
  
  private double equatorialMaxDec() {
    return ChartUtil.EQUATORIAL_CHART_MAX_DEC_DEGS;
  }
  
  private List<Star> filterEquatorialStarsFor(Bounds bounds){
    return starCatalog.filterEquatorial(ChartUtil.LIMITING_MAG, bounds.minDecDeg, bounds.maxDecDeg, bounds.minRaHours, bounds.maxRaHours, ChartUtil.EDGE_OVERLAP_DEGS);
  }
  
  private List<Star> filterPolarStarsFor(Bounds bounds){
    return starCatalog.filterPolar(ChartUtil.LIMITING_MAG, bounds.minDecDeg, bounds.maxDecDeg, ChartUtil.EDGE_OVERLAP_DEGS);
  }
  
  private static Font tinyFont() {
    return FontFactory.getFont(PdfConfig.FONT, PdfConfig.FONT_SIZE_SMALL, Font.NORMAL);
  }
  
  private static void log(String msg) {
    System.out.println(msg);
  }
  
  /** Page header. */
  private class Header extends PdfPageEventHelper {
    @Override public void onStartPage(PdfWriter writer, Document document) {
      Margins margins = new Margins(writer);
      //only takes effect for the NEXT page!! Ouch!
      docMarginsSensitiveToParity(document, margins); 
    }
    /** Put the title and page number on the page. Avoid this for the front matter. */
    @Override public void onEndPage(PdfWriter writer, Document document) {
      // for the front matter, you usually don't want to show the title or page number 
      Integer displayedPageNum = writer.getPageNumber() - PdfConfig.FRONT_MATTER_NUM_PAGES;
      Margins margins = new Margins(writer);
      if (displayedPageNum > 0 && displayedPageNum <= PdfConfig.LAST_NUMBERED_PAGE) {
        writeTitleInMiddle(writer, margins);
        writePageNumberAwayFromSpine(displayedPageNum, margins.isLeftHandPage, writer, margins);
      }
    }
    private void writeTitleInMiddle(PdfWriter writer, Margins margins) {
      float middleOfText = (margins.LEFT + PdfConfig.WIDTH - margins.RIGHT)/2.0f;
      write(text("title").toUpperCase(), Element.ALIGN_CENTER, writer, middleOfText); 
    }
    private void writePageNumberAwayFromSpine(Integer pageNum, Boolean isLeftHandPage, PdfWriter writer, Margins margins) {
      String num = pageNum.toString();
      if (isLeftHandPage) {
        write(num, Element.ALIGN_LEFT, writer, margins.LEFT);
      }
      else {
        write(num, Element.ALIGN_RIGHT, writer, PdfConfig.WIDTH - margins.RIGHT); 
      }
    }
    /** 
     It's undesirable to have equal left-right margins.
     The sides are asymmetrical: one side, near the spine, is harder to read when the text approaches the edge.
     So, you want a bit more margin near the spine. This makes it more comfortable to read. You don't have to 
     bend the book that little extra bit, in order to read the text near the spine.
     */
    private void docMarginsSensitiveToParity(Document document, Margins margin) {
      //WARNING: I SWITCH LEFT AND RIGHT: THIS CHANGE ONLY TAKES EFFECT FOR THE FOLLOWING PAGE!!
       document.setMargins(margin.RIGHT, margin.LEFT, PdfConfig.MARGIN_TOP, PdfConfig.MARGIN_BOTTOM);
    }
    private void write(String text, Integer alignment, PdfWriter writer, Float x) {
      Paragraph para = new Paragraph(text, tinyFont());
      //para.setSpacingAfter(100.0f); //no effect
      ColumnText.showTextAligned(
        writer.getDirectContent(),
        alignment, 
        para, 
        x, 
        PdfConfig.HEIGHT - PdfConfig.HEADER_Y, 
        0
      );
    }
  }
  
  /** Fancy: the margins react to where the spine is relative to the page. */
  private static final class Margins {
    Margins(PdfWriter writer){
      Integer rawPageNum = writer.getPageNumber();
      this.isLeftHandPage = (rawPageNum % 2 == 0);
      if (this.isLeftHandPage) {
        this.LEFT = PdfConfig.MARGIN_OUTER;
        this.RIGHT = PdfConfig.MARGIN_INNER;
      }
      else {
        this.LEFT = PdfConfig.MARGIN_INNER;
        this.RIGHT = PdfConfig.MARGIN_OUTER;
      }
    }
    private float LEFT;
    private float RIGHT;
    private boolean isLeftHandPage;
  }
  
  /** 
   If needed, and N blank pages at the end, to make the total divisible by 4;
  */
  private void pageCountDivisibleByFour() {
    int numPages = writer.getPageNumber();
    log("Num pages: " + numPages);
    int deficit = 4 - (numPages % 4);
    if (deficit > 0) {
      log("Need to add this many pages to make the total num of pages divisible by 4:" + deficit);
      for(int count = 0; count < deficit; ++count) {
        blankPage();
      }
    }
  }
  
  private void finalizeIt() {
    document.close(); 
  }
}