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
package com.esri.gpt.catalog.search;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Builds the query parameters for a rest query URL.
 */
public class RestUrlBuilder {
  
  /** class variables ========================================================= */
  private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public RestUrlBuilder() {}
  
  /** methods ================================================================= */
  
  /**
   * Appends a parameter to the query parameters buffer.
   * <br/>The parameter will not be appended if the value is null or empty.
   * <br/>The parameter value will be URLEncoded prior to appending.
   * @param parameters the query parameters buffer
   * @param name the parameter name
   * @param value the parameter value
   */
  protected void appendParam(StringBuffer parameters, String name, String value) {
    if ((value != null) && (value.length() > 0)) {
      if (parameters.length() > 0) parameters.append("&");
      parameters.append(name).append("=").append(this.encodeUrlParam(value));
    }
  }
  
  /**
   * Appends parameter value list to the query parameters buffer.
   * <br/>The parameter will not be appended if the value list is null or empty.
   * <br/>The parameter values will be concatenated with the delimiter prior to appending.
   * <br/>The parameter values will be URLEncoded prior to appending.
   * @param parameters the query parameters buffer
   * @param name the parameter name
   * @param delimiter the values delimiter 
   * @param values the parameter values
   */
  protected void appendParam(StringBuffer parameters, String name, String delimiter, List<String> values) {
    if ((values != null) && (values.size() > 0)) {
      StringBuffer sb = new StringBuffer();
      for (String value: values) {
        value = Val.chkStr(value);
        if (value.length() > 0) {
          if (sb.length() > 0) sb.append(delimiter);
          sb.append(value);
        }
      }
      if (sb.length() > 0) {
        this.appendParam(parameters,name,sb.toString());
      }
    }
  }
  
  /**
   * Builds the rest URL query parameters string.
   * @param criteria the search criteria from which the query parameters will be built.
   * @param format the response format
   * @param rid the repository id
   * @return the query parameters string
   */
  public String buildParameters(SearchCriteria criteria, String format, String rid) {
    StringBuffer sb = new StringBuffer();
        
    // keyword filter
    if (criteria.getSearchFilterKeyword() != null) {
      this.appendParam(sb,"searchText",criteria.getSearchFilterKeyword().getSearchText());
    }
    
    // spatial filter
    ISearchFilterSpatialObj fSpatial = criteria.getSearchFilterSpatial();
    if (fSpatial != null) {
      Envelope env = fSpatial.getEnvelope();
      if ((env != null) && env.isValid()) {
        String sSpatialRel = "";
        SearchFilterSpatial.OptionsBounds bounds = fSpatial.getSelectedBoundsAsEnum();
        switch (bounds) {
          case useGeogExtent:
            sSpatialRel = "esriSpatialRelOverlaps";
            break;
          case dataWithinExtent:
            sSpatialRel = "esriSpatialRelWithin";
            break;
        }
        if (sSpatialRel.length() > 0) {
          String sEnv = env.getMinX()+","+env.getMinY()+","+env.getMaxX()+","+env.getMaxY();
          this.appendParam(sb,"bbox",sEnv);
          if (sSpatialRel.equals("esriSpatialRelWithin")) {
            this.appendParam(sb,"spatialRel",sSpatialRel);
          }
        }
      }
    }
      
    // content type filter
    if (criteria.getSearchFilterContentTypes() != null) {
      this.appendParam(sb,"contentType",criteria.getSearchFilterContentTypes().getSelectedContentType());
    }

    // data category filter
    if (criteria.getSearchFilterThemes() != null) {
      this.appendParam(sb,"dataCategory",",",criteria.getSearchFilterThemes().getSelectedThemes());
    }
    
    // temporal filter
    ISearchFilterTemporal fTemporal = criteria.getSearchFilterTemporal();
    if (fTemporal != null) {
      Date dtAfter = fTemporal.getDateModifiedFromAsDate();
      Date dtBefore = fTemporal.getDateModifiedToAsDate();
      if (dtAfter != null) {
        this.appendParam(sb,"after",DF.format(dtAfter));
      }
      if (dtBefore != null) {
        this.appendParam(sb,"before",DF.format(dtBefore));
      }
    }
    
    // pagination filter (max only)
    if (criteria.getSearchFilterPageCursor() != null) {
      //this.appendParam(sb,"max",""+criteria.getSearchFilterPageCursor().getRecordsPerPage());
    }
    
    // sort filter    
    ISearchFilterSort fSort = criteria.getSearchFilterSort();
    if (fSort != null) {
      String sSort = fSort.getSelectedSort();
      if ((sSort != null) && !sSort.equalsIgnoreCase(SearchFilterSort.OptionsSort.relevance.name())) {
        this.appendParam(sb,"orderBy",sSort);
      }
    }
    
    // repository id
    this.appendParam(sb,"rid",Val.chkStr(rid));
    
    // format
    this.appendParam(sb,"f",Val.chkStr(format));
    
    return sb.toString();
  }
  
  /**
   * Encodes a URL parameter value.
   * @param value the URL parameter value to encode
   * @return the encoded parameter value
   */
  protected String encodeUrlParam(String value) {
    value = Val.chkStr(value);
    try {
      return URLEncoder.encode(value,"UTF-8");
    } catch (UnsupportedEncodingException ex) {
      LogUtil.getLogger().severe("Unsupported encoding: UTF-8");
      return value;
    }
  }
  
  /**
   * Instantiates a new rest url builder.
   * <p/>
   * By default, a new instance of 
   * com.esri.gpt.catalog.search.RestUrlBuilder is returned.
   * <p/>
   * This can be overridden by the configuration parameter:
   * /gptConfig/catalog/parameter@key="restUrlBuilder"
   * @param context the active request context
   * @param servletRequest the active HTTP servlet request
   * @param messageBroker the message broker
   * @return the rest url builder
   */
  public static RestUrlBuilder newBuilder(RequestContext context,
      HttpServletRequest servletRequest, MessageBroker messageBroker) {
  
    // initialize
    if (context == null) {
      context = RequestContext.extract(servletRequest);
    }
    if (messageBroker == null) {
      messageBroker = new MessageBroker();
      messageBroker.setBundleBaseName("gpt.resources.gpt");
    }
    CatalogConfiguration catCfg = context.getCatalogConfiguration();
  
    // look for a configured class name for the resource link builder
    String className = Val.chkStr(catCfg.getParameters().getValue("restUrlBuilder"));
    if (className.length() == 0) {
      className = com.esri.gpt.catalog.search.RestUrlBuilder.class.getName();
    }
  
    // instantiate the builder
    try {
      Class<?> cls = Class.forName(className);
      Object obj = cls.newInstance();
      if (obj instanceof RestUrlBuilder) {
        RestUrlBuilder builder = (RestUrlBuilder)obj;
        return builder;
      } else {
        String sMsg = "The configured restUrlBuilder parameter is invalid: "+className;
        throw new ConfigurationException(sMsg);
      }
    } catch (ConfigurationException t) {
      throw t;
    } catch (Throwable t) {
      String sMsg = "Error instantiating rest url builder: "+className;
      throw new ConfigurationException(sMsg, t);
    }
  }   

}
