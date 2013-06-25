/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gc.solr.publish;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SolrDocSaxHandler extends DefaultHandler {
	
	private String            activeFieldName;
	private String            activeInstruction;
	private StringBuilder     characters = new StringBuilder();
	private SolrInputDocument solrDoc;
	
	public SolrDocSaxHandler(SolrInputDocument solrDoc) {
		this.solrDoc = solrDoc;
	}
	
  /**
   * Advances a calendar to the end of date interval.
   * <p/>
   * Example: 
   * <br/>A calendar has been previously set to the beginning 
   * millisecond of 2010-04
   * <br/>Calling this method will advance the calendar to the 
   * final millisecond of 2010-04
   * @param calendar the calendar to advance
   * @param the date string associated with the current calendar time
   */
  private void advanceToUpperBoundary(Calendar calendar, String date) {
    if (date.indexOf("T") == -1) {
      int nAddType = -1;
      String[] parts = date.split("-");
      int nParts = parts.length;
      for (String s: parts) {
        if ((s.indexOf(":") != -1) && (s.indexOf("+") == -1)) {
          nParts--;
        }
      }
      if (nParts == 1) {
        nAddType = Calendar.YEAR;
      } else if (nParts == 2) {
        nAddType = Calendar.MONTH;
      } else if (nParts == 3) { 
        nAddType = Calendar.DAY_OF_MONTH;
      }
      if (nAddType > 0) {
        calendar.add(nAddType,1);
        // TODO: Does removing a millisecond cause an issue?
        calendar.add(Calendar.MILLISECOND,-1);
      }
    }
  }
  
  public String calcKmResolution(String text) {
  	//System.err.println("****** calcKmResolution="+text);
  	if (text != null && !text.equals(";")) {
  		String[] a = text.split(";");
  		if (a.length == 2) {
  			String s1 = a[0].trim();
  			String s2 = a[1].trim();
  			if ((s1.length() > 0) && (s2.length() > 0)) {
  				try {
  					double d1 = Double.parseDouble(s1);
  					double d2 = Double.parseDouble(s2);
  					double d3 = Math.sqrt((d1*d1)+(d2*d2));
  					return ""+d3;
  				} catch (NumberFormatException nfe) {}
  			}
  		}
  	}
  	return null;
  }

	@Override
	public final void characters(char ch[], int start, int length)
	  throws SAXException {
	  if ((ch != null) && (length > 0)) {
	  	characters.append(ch,start,length);
	  }
	}
	
	public String checkGeoEnvelope(String text) {
		
		// Bad X value 260.0 is not in boundary Rect(minX=-180.0,maxX=180.0,minY=-90.0,maxY=90.0)
  	
  	if (text != null) {
  	  String[] a = text.split(" ");
  	  if (a.length == 4) {
  	  	try {
  	  	  double xmin = Double.parseDouble(a[0]);
  	  	  double ymin = Double.parseDouble(a[1]);
  	  	  double xmax = Double.parseDouble(a[2]);
  	  	  double ymax = Double.parseDouble(a[3]);
  	  	  
  	  	  if ((xmin < -180.0) && (xmax >= -180.0)) xmin = -180.0;
  	  	  if ((xmax > 180.0) && (xmin <= 180.0)) xmax = 180.0;
  	  	  if ((ymin < -90.0) && (ymax >= -90.0)) ymin = -90.0;
  	  	  if ((ymax > 90.0) && (ymin <= 90.0)) ymax = 90.0;
  	  	  
  	  	  // Error adding field 'geo'='-86.933 41.761 -86.486 41.237' msg=maxY must be >= minY: 41.761 to 41.237
  	  	  if ((xmax >= xmin) && (ymax >= ymin)) {
  	  	  	if ((xmin >= -180) && (xmax <= 180.0) && (ymin >= -90.0) && (ymax <= 90.0)) {
  	  	      text = xmin+" "+ymin+" "+xmax+" "+ymax;
  	  	      //System.err.println("envelope= "+text);
  	  	    
  	  	      //195.0 14.999826066195965 244.00024350732565 50.0
  	  	      return text;
  	  	  	}
  	  	    
  	  	  }
  	  	  
  	  	} catch (NumberFormatException nfe) {}
  	  }
  	  // TODO log this;
  	  //System.err.println("Bad Envelope: "+text);
  	}
  	return null;
  }
	
	public String checkFgdcDate(String text, boolean isEnd) {
		//System.err.println("***** checkFgdcDate: "+text);
		if (text != null) {
			text = text.trim();
			if (text.length() > 0) {
				try {
				   /*
	 if (sDate.indexOf("-") == -1) {
      if (sDate.length() == 8) {
        sDate = sDate.substring(0,4)+"-"+sDate.substring(4,6)+
               "-"+sDate.substring(6,8);
      } else if (sDate.length() == 6) {
        sDate = sDate.substring(0,4)+"-"+sDate.substring(4,6);
      } else if (sDate.length() == 4) {
        sDate = sDate.substring(0,4);
      }
    }
					 */
					boolean bSet = false;
          if (text.indexOf("-") == -1) {
          	if (text.length() == 8) {
          	  text = text.substring(0,4)+"-"+text.substring(4,6)+"-"+text.substring(6,8);
          	  // TODO ?????
          	  text += "Z";
          	  bSet = true;
          	}
          }
          if (bSet) {
        	  //System.err.println("***** checkFgdcDate: "+text);
  				  Calendar c1 = javax.xml.bind.DatatypeConverter.parseDateTime(text);
  				  GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
  				  c.setTimeInMillis(c1.getTimeInMillis());
  				  if (isEnd) {
  				  	advanceToUpperBoundary(c,text);
  				  }
  				  text = javax.xml.bind.DatatypeConverter.printDateTime(c);
  				  //System.err.println("***** checkFgdcDate: "+text);
  				  return text;
          }
				} catch (IllegalArgumentException e) {
					// TODO log this;
					System.err.println("Bad date: "+text);
					//e.printStackTrace();
					//throw e;
				}
			}
		}
		return null;
	}
	
	public String checkIsoDateTime(String text, boolean isEnd) {
		if (text != null) {
			text = text.trim();
			if (text.length() > 0) {
				try {
					
					// repair this pattern: 2013-04-15 17:11:00Z
					if (text.length() > 11) {
						if (text.substring(10,11).equals(" ")) {
							if ((text.indexOf("-") != -1) && (text.indexOf(":") != -1)) {
								if (text.endsWith("Z")) {
									text = text.substring(0,10)+"T"+text.substring(11);
								}
							}
						}
					}
					
			    // TODO:
			    // if a timezone is not specified within the date-time string, 
			    // the local time zone is assumed, this isn't such a good idea,
			    // e.g 2012-06-13  vs 2012-06-13Z		
					boolean bDateOnly = false;
					String uc = text.toUpperCase();
					if ((uc.indexOf(" ") == -1) && (uc.indexOf(":") == -1) &&
							(uc.indexOf("T") == -1) && (uc.indexOf("Z") == -1)) {
						String[] p = text.split("-");
						if (p.length == 1) {
							if (p[0].length() == 4) {
								bDateOnly = true;
								text = text+"Z";
							}
						} else if (p.length == 2) {
							if ((p[0].length() == 4) && (p[1].length() == 2)) {
								bDateOnly = true;
								text = text+"Z";
							}
						} else if (p.length == 3) {
							if ((p[0].length() == 4) && (p[1].length() == 2) && (p[2].length() == 2)) {
								bDateOnly = true;
								text = text+"Z";
							}
						}
					}
				  Calendar c1 = javax.xml.bind.DatatypeConverter.parseDateTime(text);
				  GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
				  c.setTimeInMillis(c1.getTimeInMillis());
				  if (isEnd) {
				  	advanceToUpperBoundary(c,text);
				  }
				  if (bDateOnly) {
				  	//text = javax.xml.bind.DatatypeConverter.printDate(c2);
				  	text = javax.xml.bind.DatatypeConverter.printDateTime(c);
				  } else {
				  	text = javax.xml.bind.DatatypeConverter.printDateTime(c);
				  }		
				  //System.err.println("****** "+text);
				  return text;
				} catch (IllegalArgumentException e) {
					// TODO log this;
					System.err.println("Bad ISO date: "+text);
					//e.printStackTrace();
					//throw e;
				}
			}
		}
		return null;
	}
	
	private void resetCharacters() {
		characters = new StringBuilder();
	}
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		
	}

	@Override
	public void endElement(String uri, String localName, String qName)
	  throws SAXException {
	  try {
	  	if ((localName != null) && localName.equals("field") && (activeFieldName != null)) {
	  		String value = characters.toString();
	  		
	  		if (activeInstruction != null) {
	  			//System.err.println("***** "+activeFieldName+"="+activeInstruction);
		  		if (activeInstruction.equals("checkGeoEnvelope")) {
		  			value = checkGeoEnvelope(value);
		  			
		  		} else if (activeInstruction.equals("calcKmResolution")) {
		  			value = calcKmResolution(value);
		  			
		  		} else if (activeInstruction.equals("checkFgdcDate")) {
		  			value = checkFgdcDate(value,false);
		  		} else if (activeInstruction.equals("checkFgdcDate.begin")) {
		  			value = checkFgdcDate(value,false);
		  		} else if (activeInstruction.equals("checkFgdcDate.end")) {
		  			value = checkFgdcDate(value,true);	
		  		} else if (activeInstruction.equals("checkIsoDateTime")) {
		  			value = checkIsoDateTime(value,false);
		  		} else if (activeInstruction.equals("checkIsoDateTime.begin")) {
		  			value = checkIsoDateTime(value,false);
		  		} else if (activeInstruction.equals("checkIsoDateTime.end")) {
		  			value = checkIsoDateTime(value,true);
		  		}
	  		}
	  		
	  		if (value != null) {
	  			//solrDoc.addField(activeFieldName,value);
	  			value = value.trim();
	  			if (value.length() > 0) {
	  				while (value.startsWith(",")) {
	  					value = value.substring(1).trim();
	  				}
	  				while (value.endsWith(",")) {
	  					value = value.substring(0,(value.length()-1)).trim();
	  				}
	  			}
	  			if (value.length() > 0) {
	  				boolean bAdd = true;
	  				if (activeFieldName.endsWith("_ss")) {
	  					SolrInputField ss = solrDoc.getField(activeFieldName);
	  					if (ss != null) {
	  						for (Object obj: ss.getValues()) {
	  							String s = (String)obj;
	  							if (s.equals(value)) {
	  								bAdd = false;
	  								break;
	  							}
	  						}
	  					}
	  				}
	  				if (bAdd) {
	  					solrDoc.addField(activeFieldName,value);
	  				}
	  			}
	  		}
	  	}
	  } finally {
	  	activeFieldName = null;
	  	activeInstruction = null;
	    resetCharacters();
	  }
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	  throws SAXException {
		activeFieldName = null;
		activeInstruction = null;
	  resetCharacters();
	  if ((localName != null) && localName.equals("field")) {
	  	activeFieldName = attributes.getValue("name");
	  	activeInstruction = attributes.getValue("gc-instruction");
	  }
	}


	
}
