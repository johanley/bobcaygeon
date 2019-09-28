package mag5.book;

import com.itextpdf.awt.FontMapper;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.BaseFont;

/** 
 Controls how a graphics context treats text.
 
 <P>Fonts are confusing in iText. 
 I'm not sure if I understand them well.
 
 <P>I think the main point is that Graphics2D.setFont(java.awt.Font) needs to really use an iText class instead.
 That is, the java.awt API accepts a java.awt.Font, but iText needs a com.itextpdf.text.Font object instead.  
 Hence the need for this mapper.
  
 <P>When outputting text only, outside the graphics context, this step isn't needed, since we're working 
 only with iText classes.

<pre>
  java.awt.Font
  com.itextpdf.text.pdf.BaseFont
    BaseFont bf = BaseFont.createFont(name, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
  com.itextpdf.text.Font
  com.itextpdf.text.FontFactory
      FontFactory.registerDirectory(fontDir);
      return FontFactory.getFont(PdfConfig.FONT, BaseFont.IDENTITY_H, PdfConfig.FONT_SIZE_NORMAL, Font.BOLD);
  com.itextpdf.awt.FontMapper 
    (java.awt.Font <--> BaseFont)
    similar fonts that can be used as replacements
    com.itextpdf.awt.DefaultFontMapper
      public int insertDirectory(String dir)
        Inserts all the fonts recognized by iText in the directory into the map. 
        The encoding will be BaseFont.CP1252 but can be changed later.
</pre>        
*/
class MyFontMapper implements FontMapper {

  /** 
   For the moment, I'm only using a single font in graphics-world.
   See {@link PdfConfig#FONT}.
  */
  @Override public BaseFont awtToPdf(java.awt.Font awtFont) {
    BaseFont result = null;
    try {
      //result = BaseFont.createFont(awtFont.getName(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
      //return FontFactory.getFont(PdfConfig.FONT, BaseFont.IDENTITY_H, PdfConfig.FONT_SIZE_NORMAL, Font.NORMAL);
      //int style = awtFont.isItalic() ? Font.ITALIC : Font.NORMAL;
      int style = 0;
      if (awtFont.isItalic()) {
        style = Font.ITALIC;
      }
      else if (awtFont.isBold()) {
        style = Font.BOLD;
      }
      else {
        style = Font.NORMAL;
      }
      com.itextpdf.text.Font itextFont = FontFactory.getFont(PdfConfig.FONT, BaseFont.IDENTITY_H, PdfConfig.FONT_SIZE_NORMAL, style);
      result =  itextFont.getBaseFont(); 
      //log("java.awt.Font:" + awtFont.toString() + " mapped to " + result.toString());
    } 
    catch (Throwable e) {
      e.printStackTrace();
    }
    return result;
  }

  /** Not implemented. */
  @Override public java.awt.Font pdfToAwt(BaseFont arg0, int arg1) {
    return null;
  }
  
  private void log(String msg) {
    System.out.println(msg);
  }
}

