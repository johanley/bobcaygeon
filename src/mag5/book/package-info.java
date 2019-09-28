/** 
 Generate a 5th-magnitude star atlas from the Yale Bright Star catalog (and other secondary data sources).
 
 <P>This project was inspired by my love for the Edmund Mag 5 Star Atlas (out of print since the 1970s).
 The format and general layout of that atlas was ideal for learning the constellations.
 You can print a version of these charts, for a small fee,  
 using files I have uploaded to <a href='http://www.lulu.com/content/paperback-book/bobcaygeon-constellations/25078661'>lulu.com</com>
 
 <P>This atlas is not really suited to any other purpose than learning the constellations.
 Experienced observers will not find this atlas to be useful for observing deep-sky objects.
 
 <P>
 This code was originally written by John O'Hanley in the summer of 2019, in Moncton, New Brunswick, Canada.
 
 <P>General notes about the charts:
 <ul>
  <li>the atlas can be used anywhere, in the northern or southern hemisphere
  <li>the output file is a single pdf file, two-sided, greyscale
  <li>the number of pages is evenly divisible by four (for two-sided printing)
  <li>there are 12 charts in all
  <li>the star data comes from a slightly modified version of the Yale Bright Star catalog
  <li>data for deep sky objects is taken from selected data in the Observer's Handbook of the Royal Astronomical Society of Canada
  <li>the deep sky objects are Messier's list, plus a few selected NGC objects
  <li>the epoch used is J2000
  <li>the equatorial charts use a sinusoidal projection (same as the Edmund atlas)
  <li>no lines are included to delineate the approximate border of the Milky Way
  <li>the Magellanic clouds are delineated only as simple ellipses
 </ul>
 
 <P>Charts 1-6 are biased for a northern hemisphere observer, and charts 7-12 are biased for a southern hemisphere observer.
 That is, charts 7-12 are 'upside down' versions of charts 1-6.
 Thus, these charts as a whole can be used equally well in both northern and southern hemispheres. 
 
 <P>Notes about the implementation:
 <ul>
  <li>the top-level main class is GeneratePdf.java
  <li>all text files, and all .java files, use the UTF-8 encoding; your dev environment must also treat them as UTF-8
  <li>the code is not suitable for use as a library. It's used as a glorified script, to generate the desired pdf file as output.
  <li>the project uses an old version of the <a href='https://itextsupport.com/apidocs/iText5/5.5.9/>'iText java library (5.5.13)</a>
  <li>the code is structured such that translation from English to some other target language should be possible with modest effort
  <li>vector graphics are used (not raster graphics)
  <li>the pdf properties are compatibile with what's needed by lulu.com (a printing service)
  <li>the fonts are embedded in the pdf
  <li>WARNING: different printers may render thin lines differently
  <li>the author has run this code only on Windows OS, not on Linux
 </ul>

  <P>A <a href='http://www.javapractices.com/topic/TopicAction.do?Id=205'>package-by-feature</a> design is used here. 
  Text data files are placed in the same directory as the code that uses it.
  Because of this, you will need to set up your dev environment to output compiled .class  
  files beside your .java files, in the same directory, and not in a separate 'bin' directory.
 
 <h2>Labels</h2>
 
 The hardest part of constructing a star chart is the placement of labels. 
 The labels are the text identifiers and whatnot, that are associated with stars and nebulae.
 A mature tool set would include cartographic tools, which can manipulate various different layers, and allow for 
 quick manual adjustments to label placement, according to context.
 If labels are placed only using code, the result is often poor. 
 In this project, the implementation is part in code, part manual.  
 The resulting charts are usually satisfactory, but not as esthetically pleasing as that which could be produced with robust cartographic tools. 
 Nevertheless, the results are still useful for the intended purpose of learning the constellations.
 
 <P>Labels are placed to the north, south, east or west of the target object.
 The default location is south. 
 By manual inspection, I have changed that default in each case, to whatever produces the best result. 
 In most, but not all, cases, the result is satisfactory.
 
 <P>In a few cases, where no suitable placement could be found, some labels have been 
 suppressed from the output. In the context of a chart intended solely to teach the constellations, 
 that seems acceptable. 
 
 <h2>Problems / To-Do List</h2>
 
 Possible items to work on in the future:
 <ul>
  <li>the text centering methods in ChartUtil are not pixel-perfect. The techniques I found on the web seem to be erroneous.
  <li>translations into other languages (French, Italian)
  <li>bigger tick-mark for each 5° of declination
  <li>can I find a better font than Times New Roman?
  <li>should there be more equatorial charts? every 2 hours?
  <li>ecliptic: maybe show the name of the month, in addition to the current tick marks
  <li>the preamble text could likely use some diagrams to clarify matters
  <li>maybe show locations: galactic centre, galactic poles, ecliptic poles, direction of the Sun's movement, 
  radiants of the largest meteor showers
 </ul>
*/
package mag5.book;