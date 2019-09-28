/**
 The data for stars used to generate the chart.
 
 The data is a modified version of the Yale Bright Star Catalog (YBS).
 See <a href='http://cdsarc.u-strasbg.fr/viz-bin/Cat?V/50'>vizier</a>
 and <a href='http://tdc-www.harvard.edu/catalogs/bsc5.html'>here</a>.
 
 YBS is nearly complete to mag 7. 
 It uses J2000 as its epoch.
 
 <P>I have modified the data somewhat, because it has various entries 
 that shouldn't really be there. 
 For example, it has bright novae. I have removed them.

 <P>9110 original objects = 9096 stars + 14 novae etc
 
 <P>Double stars also represent an issue.
 There are a number of cases in which two nearby stars, although technically double, 
 don't appear as such to the naked eye. 
 In the context of a chart intended for naked-eye use, and for learning the 
 constellations, it seems reasonable to amalgamate such doubles into a single entity.
 When that amalgamation happens, one star is selected to represent the two.
 The two stellar magnitudes are also combined into one (using a 
 <a href='https://en.wikipedia.org/wiki/Apparent_magnitude#Magnitude_addition'>formula</a> 
 that combines the flux), because that's how they will appear to the naked eye.
  
 The raw YBS catalog is massaged in these ways:
 - order by right ascension, not id
 - discard bizarre entries (novae, and so on)
 - add proper names to stars (manually, using the underlying catalog's id)
 - amalgamate selected double stars
*/
package mag5.star;