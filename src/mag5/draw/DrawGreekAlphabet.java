package mag5.draw;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

import mag5.translate.GreekAlphabet;
import mag5.util.Maths;

/** 
 Draw the Greek alphabet on the chart.
 The Bayer designations for stars use the Greek alphabet. 
 Some beginners will be unfamiliar with it, so it is provided for reference. 
*/
class DrawGreekAlphabet {

  DrawGreekAlphabet( ChartUtil chartUtil, Projection projection, Graphics2D g) {
    this.projection = projection;
    this.g = g;
    this.chartUtil = chartUtil;
  }
  
  /**
   Two rows, vertically aligned, like a small table having cells with text centered in the text.
   <P>Row 1: letter. 
   <P>Row 2: the name of the letter, in the given language
   <P>Applies only to the bottom chart.
  */
  void draw() {
    if (!projection.getBounds().isTopChart()) {
      int y = Maths.round(chartUtil.percentHeight(100 - 1.50*ChartUtil.BORDER_WIDTH));
      int START = Maths.round(chartUtil.percentWidth(10)); 
      int x = START; 
      int Δ = Maths.round(chartUtil.percentWidth(ChartUtil.GREEK_ALPHABET_SPACING));
      
      //we need a bigger font
      Font originalFont = g.getFont();
      g.setFont(ChartUtil.modifiedBaseFont(12));
      //the letters
      for(String letter : GreekAlphabet.LETTERS_LIST) {
        Point2D.Double point = chartUtil.centerTextOn(x, y, letter, g);
        g.drawString(letter, Maths.round(point.x), Maths.round(point.y)); 
        x = x + Δ; 
      }
      g.setFont(originalFont);
      
      //the names of the letters (varies with language)
      GreekAlphabet greek = new GreekAlphabet();
      List<String> names = greek.namesOfLetters(ChartUtil.lang());
      //reset back to the left
      x = START; 
      y = y + Maths.round(chartUtil.percentHeight(2.5));
      for(String name : names) {
        Point2D.Double point = chartUtil.centerTextHorizontallyOn(x, y, name, g);
        g.drawString(name, Maths.round(point.x), Maths.round(point.y)); 
        x = x + Δ; 
      }
    }
  }
  
  // PRIVATE
  private ChartUtil chartUtil;
  private Projection projection;
  private Graphics2D g;

}
