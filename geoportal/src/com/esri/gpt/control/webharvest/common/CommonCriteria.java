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
package com.esri.gpt.control.webharvest.common;

import com.esri.gpt.catalog.search.ISearchFilterSpatialObj.OptionsBounds;
import com.esri.gpt.catalog.search.SearchEngineCSW.AimsContentTypes;
import com.esri.gpt.catalog.search.SearchFilterSort.OptionsSort;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.NodeListAdapter;
import com.esri.gpt.framework.xml.XmlIoUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Common implementation of criteria.
 */
public class CommonCriteria implements Criteria {

private static final Logger LOGGER = Logger.getLogger(CommonCriteria.class.getCanonicalName());
private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
private Integer maxRecords;
private String searchText;
private Date fromDate;
private Date toDate;
private Envelope bBox;
private AimsContentTypes contentType;
private String[] dataCategory;
private OptionsBounds bBoxOption;
private OptionsSort sortOption;

@Override
public Integer getMaxRecords() {
  return maxRecords;
}

/**
 * Sets maximum records.
 * @param maxRecords maximum records
 */
public void setMaxRecords(Integer maxRecords) {
  this.maxRecords = maxRecords;
}

@Override
public String getSearchText() {
  return searchText;
}

/**
 * Sets search text.
 * @param searchText search text
 */
public void setSearchText(String searchText) {
  this.searchText = searchText;
}

@Override
public Date getFromDate() {
  return fromDate;
}

/**
 * Sets FROM date.
 * @param fromDate FROM date
 */
public void setFromDate(Date fromDate) {
  this.fromDate = fromDate;
}

@Override
public Date getToDate() {
  return toDate;
}

/**
 * Sets TO date
 * @param toDate TO date
 */
public void setToDate(Date toDate) {
  this.toDate = toDate;
}

@Override
public Envelope getBBox() {
  return bBox;
}

/**
 * Sets bounding box.
 * @param bBox bounding box
 */
public void setBBox(Envelope bBox) {
  this.bBox = bBox;
}

  @Override
public AimsContentTypes getContentType() {
  return contentType;
}

/**
 * Sets content type.
 * @param contentType content type
 */
public void setContentType(AimsContentTypes contentType) {
  this.contentType = contentType;
}

  @Override
public String[] getDataCategory() {
  return dataCategory;
}

/**
 * Sets data category
 * @param dataCategory data category
 */
public void setDataCategory(String[] dataCategory) {
  this.dataCategory = dataCategory;
}

  @Override
public OptionsBounds getBBoxOption() {
  return bBoxOption;
}

/**
 * Sets bounding box options.
 * @param bBoxOption bounding box options
 */
public void setBBoxOption(OptionsBounds bBoxOption) {
  this.bBoxOption = bBoxOption;
}

  @Override
public OptionsSort getSortOption() {
  return this.sortOption;
}

/**
 * Sets sort options.
 * @param sortOption sort options.
 */
public void setSortOption(OptionsSort sortOption) {
  this.sortOption = sortOption;
}

@Override
public String toString() {
  StringBuilder sb = new StringBuilder();

  if (maxRecords != null && maxRecords > 0) {
    if (sb.length() > 0)
      sb.append(",");
      sb.append(" maxRecords: ").append(maxRecords);
  }
  if (searchText != null && searchText.trim().length() > 0) {
    if (sb.length() > 0)
      sb.append(",");
      sb.append(" searchText: ").append(searchText);
  }
  if (fromDate != null) {
    if (sb.length() > 0)
      sb.append(",");
      sb.append(" fromDate: ").append(fromDate);
  }
  if (toDate != null) {
    if (sb.length() > 0)
      sb.append(",");
      sb.append(" toDate: ").append(toDate);
  }
  if (bBox != null) {
    if (sb.length() > 0)
      sb.append(",");
      sb.append(" bbox: ").append(bBox);
  }
  if (contentType != null) {
    if (sb.length() > 0)
      sb.append(",");
      sb.append(" contentType: ").append(contentType);
  }
  if (dataCategory != null && dataCategory.length > 0) {
    if (sb.length() > 0)
      sb.append(",");
      sb.append(" dataCategory: ").append(dataCategory);
  }
  if (bBoxOption != null) {
    if (sb.length() > 0)
      sb.append(",");
      sb.append(" bBoxOption: ").append(bBoxOption);
  }
  if (sortOption != null) {
    if (sb.length() > 0)
      sb.append(",");
      sb.append(" sortOption: ").append(sortOption);
  }

  return "{" + sb.toString() + "}";
}

/**
 * Converts criteria into XML string.
 * @return criteria as XML string
 */
public String toXmlString() {
  try {
    Document doc = DomUtil.newDocument();
    Element crt = doc.createElement("criteria");
    doc.appendChild(crt);

    if (maxRecords != null && maxRecords > 0) {
      Element el = doc.createElement("maxRecords");
      el.setTextContent(maxRecords.toString());
      crt.appendChild(el);
    }

    if (searchText != null && searchText.trim().length() > 0) {
      Element el = doc.createElement("searchText");
      el.setTextContent(searchText.toString());
      crt.appendChild(el);
    }

    if (fromDate != null) {
      Element el = doc.createElement("fromDate");
      el.setTextContent(DF.format(fromDate));
      crt.appendChild(el);
    }

    if (toDate != null) {
      Element el = doc.createElement("toDate");
      el.setTextContent(DF.format(toDate));
      crt.appendChild(el);
    }

    if (bBox != null) {
      Element el = doc.createElement("bBox");
      el.setAttribute("minx", Double.toString(bBox.getMinX()));
      el.setAttribute("miny", Double.toString(bBox.getMinY()));
      el.setAttribute("maxx", Double.toString(bBox.getMaxX()));
      el.setAttribute("maxy", Double.toString(bBox.getMaxY()));
      crt.appendChild(el);
    }

    if (contentType != null) {
      Element el = doc.createElement("contentType");
      el.setTextContent(contentType.name());
      crt.appendChild(el);
    }

    if (dataCategory != null && dataCategory.length > 0) {
      Element el = doc.createElement("dataCategory");
      for (String code : dataCategory) {
        Element dc = doc.createElement("code");
        dc.setTextContent(code);
        el.appendChild(dc);
      }
      crt.appendChild(el);
    }

    if (bBoxOption != null) {
      Element el = doc.createElement("bBoxOption");
      el.setTextContent(bBoxOption.name());
      crt.appendChild(el);
    }

    if (sortOption != null) {
      Element el = doc.createElement("sortOption");
      el.setTextContent(sortOption.name());
      crt.appendChild(el);
    }

    return XmlIoUtil.domToString(doc);
  } catch (Exception ex) {
    LOGGER.log(Level.WARNING, "Error turning criteria into XML.", ex);
    return "";
  }
}

/**
 * Parses criteria from XML string.
 * @param str criteria as XML string
 * @return criteria
 */
public static CommonCriteria parseXmlString(String str) {
  CommonCriteria crt = new CommonCriteria();

  if (str.length() > 0) {
    try {

      XPathFactory xPathFactory = XPathFactory.newInstance();
      XPath xPath = xPathFactory.newXPath();

      Document doc = DomUtil.makeDomFromString(str, false);
      Node root = (Node) xPath.evaluate("criteria", doc, XPathConstants.NODE);

      if (root != null) {
        String maxRecords = (String) xPath.evaluate("maxRecords", root, XPathConstants.STRING);
        if (maxRecords!=null && maxRecords.length()>0) {
          try {
            crt.setMaxRecords(Integer.parseInt(maxRecords));
          } catch (NumberFormatException ex) {
          }
        }

        String searchText = (String) xPath.evaluate("searchText", root, XPathConstants.STRING);
        crt.setSearchText(searchText);

        String fromDate = (String) xPath.evaluate("fromDate", root, XPathConstants.STRING);
        if (fromDate!=null && fromDate.length()>0) {
          try {
            crt.setFromDate(DF.parse(fromDate));
          } catch (ParseException ex) {
          }
        }

        String toDate = (String) xPath.evaluate("toDate", root, XPathConstants.STRING);
        if (toDate!=null && toDate.length()>0) {
          try {
            crt.setToDate(DF.parse(toDate));
          } catch (ParseException ex) {
          }
        }

        if (xPath.evaluate("bbox", root, XPathConstants.NODE) != null) {
          String minx = (String) xPath.evaluate("bbox/minx", root, XPathConstants.STRING);
          String miny = (String) xPath.evaluate("bbox/miny", root, XPathConstants.STRING);
          String maxx = (String) xPath.evaluate("bbox/maxx", root, XPathConstants.STRING);
          String maxy = (String) xPath.evaluate("bbox/maxy", root, XPathConstants.STRING);
          try {
            Envelope bbox = new Envelope();
            bbox.setMinX(Double.parseDouble(minx));
            bbox.setMinY(Double.parseDouble(miny));
            bbox.setMaxX(Double.parseDouble(maxx));
            bbox.setMaxY(Double.parseDouble(maxy));
            crt.setBBox(bbox);
          } catch (NumberFormatException ex) {
          }
        }

        String contentType = (String) xPath.evaluate("contentType", root, XPathConstants.STRING);
        if (contentType!=null && contentType.length()>0) {
          try {
            crt.setContentType(AimsContentTypes.valueOf(contentType));
          } catch (IllegalArgumentException ex) {
          }
        }

        if (xPath.evaluate("dataCategory", doc, XPathConstants.NODE) != null) {
          ArrayList<String> aCodes = new ArrayList<String>();
          NodeList codes = (NodeList) xPath.evaluate("dataCategory/code/#text()", root, XPathConstants.NODESET);
          for (Node code : new NodeListAdapter(codes)) {
            aCodes.add(code.getNodeValue());
          }
          crt.setDataCategory(aCodes.toArray(new String[aCodes.size()]));
        }

        String bBoxOption = (String) xPath.evaluate("bBoxOption", root, XPathConstants.STRING);
        if (bBoxOption!=null && bBoxOption.length()>0) {
          try {
            crt.setBBoxOption(OptionsBounds.valueOf(bBoxOption));
          } catch (IllegalArgumentException ex) {
          }
        }

        String sortOption = (String) xPath.evaluate("sortOption", root, XPathConstants.STRING);
        if (sortOption!=null && sortOption.length()>0) {
          try {
            crt.setSortOption(OptionsSort.valueOf(sortOption));
          } catch (IllegalArgumentException ex) {
          }
        }
      }
    } catch (Exception ex) {
      LOGGER.log(Level.WARNING, "Error parsing criteria.", ex);
    }
  }

  return crt;
}

}
