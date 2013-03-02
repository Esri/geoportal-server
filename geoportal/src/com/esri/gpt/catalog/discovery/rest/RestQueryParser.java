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
package com.esri.gpt.catalog.discovery.rest;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.catalog.discovery.*;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides functionality to parse a rest query URL.
 * <p/>
 * The primary goal is to build filter and response components associated 
 * with the query.
 */
public class RestQueryParser {
  
  /** instance variables ====================================================== */
  private AliasedDiscoverables  discoverables;
  private RequestContext        requestContext;
  private Map<String, String[]> requestParameterMap;
  private RestQuery             restQuery;
    
  /** constructors ============================================================ */
  
  /**
   * Constructs the parser.
   * @param request the active HTTP request
   * @param context the request context
   * @param query the query to populate
   */
  public RestQueryParser(HttpServletRequest request, RequestContext context, RestQuery query) {
    this.requestParameterMap = request.getParameterMap();
    this.requestContext  = context;
    this.restQuery = query;
    
    // determine the aliased discoverables
    CatalogConfiguration catCfg = this.requestContext.getCatalogConfiguration();
    PropertyMeanings propertyMeanings = catCfg.getConfiguredSchemas().getPropertyMeanings();
    setDiscoverables(propertyMeanings.getDcPropertySets().getAllAliased());
    
    // establish RSS provider and source URLs
    String basePath = RequestContext.resolveBaseContextPath(request);
    getQuery().setRssProviderUrl(catCfg.getParameters().getValue("rssProviderUrl"));
    if (getQuery().getRssProviderUrl().length() == 0) {
      getQuery().setRssProviderUrl(basePath);
    }
    String sourceURL = basePath+"/rest/find/document";
    String queryString = request.getQueryString();
    getQuery().setRssSourceUrl(sourceURL + (queryString != null? "?"+queryString: ""));
    getQuery().setMoreUrl(sourceURL + "?" + getMoreQueryString(Val.chkStr(queryString)));
  }
  
    
  /** properties ============================================================== */
  
  /**
   * Gets the aliased map of configured discoverable properties.
   * @return the discoverables
   */
  public AliasedDiscoverables getDiscoverables() {
    return this.discoverables;
  }
  /**
   * Sets the aliased map of configured discoverable properties.
   * @param discoverables the discoverables
   */
  public void setDiscoverables(AliasedDiscoverables discoverables) {
    this.discoverables = discoverables;
  }
  
  /**
   * Convenience method to return the filter associated with the query being populated.
   * @return the filter
   */
  public DiscoveryFilter getFilter() {
    return getQuery().getFilter();
  }
  
  /**
   * Gets the query being populated.
   * @return the query
   */
  public RestQuery getQuery() {
    return this.restQuery;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends a clause to the query filter.
   * <br/>The clause will not be appended if it or it's parent is null.
   * @param parent the parent to which the supplied discovery clause will be appended
   * @param clause the clause to append
   */
  public void appendClause(LogicalClause parent, DiscoveryClause clause) {
    if ((parent != null) && (clause != null)) {
      parent.getClauses().add(clause);
    }
  }
        
  /**
   * Extracts a property clause from the HTTP request.
   * <br/>This method checks the HTTP request parameter map for the value associated with a
   * supplied key. If found, the supplied PropertyClause is populated with an appropriate
   * literal and target, then returned.
   * @param clause the property clause to populate and return
   * @param restKey the URL key for the parameter
   * @param discoverableKey the key associated with the target discoverable
   * @return the property clause (null if not validly located within the request)
   */
  public PropertyClause extractProperty(PropertyClause clause, 
                                        String restKey, 
                                        String discoverableKey) {
    if ((clause == null) || 
        (clause instanceof PropertyClause.PropertyIsNull) ||
        (clause instanceof PropertyClause.PropertyIsBetween)) {
      return null; // can't be handled
    }
    String value = Val.chkStr(getRequestParameter(restKey));
    if(restKey.equals("q") && discoverableKey.equals("anytext")){
    	if(value.equalsIgnoreCase("(type:tool%20OR%20type:toolset%20OR%20type:toolbox)")){
    		value= "";
    	}
    }
    Discoverable target = findDiscoverable(discoverableKey);
    if (target != null) {
      clause.setTarget(target);
      if ((value != null) && (value.length() > 0)) {
        try {
          clause.getTarget().getMeaning().getValueType().evaluate(value);
        } catch (Exception e) {
          // TODO Log warning??, throw exception ??
          value = null;
        }
      }
      clause.setLiteral(value);
      String literal = clause.getLiteral();
      if ((literal != null) && (literal.length() > 0)) {
        if (clause instanceof PropertyClause.PropertyIsEqualTo) {
        } else if (clause instanceof PropertyClause.PropertyIsGreaterThan) {
        } else if (clause instanceof PropertyClause.PropertyIsGreaterThanOrEqualTo) {
        } else if (clause instanceof PropertyClause.PropertyIsLessThan) {
        } else if (clause instanceof PropertyClause.PropertyIsLessThanOrEqualTo) {
        } else if (clause instanceof PropertyClause.PropertyIsLike) {
        } else if (clause instanceof PropertyClause.PropertyIsNotEqualTo) {
        } else {
          return null;
        }
        return clause;
      }
    }
    /*
    private Date extractDate(HttpServletRequest request, String key) {
      Date date = null;
      String sDate = extractParameter(request, key);
      if (sDate.length() > 0) {
        try {
          date = DF.parse(sDate);
        } catch (ParseException ex) {
          LogUtil.getLogger().info("Invalid date: "+sDate);
        }
      }
      return date;
    */
    return null;
  }
  
  /**
   * Extracts a grouping of property clauses (delimited list) from the HTTP request.
   * <br/>This method checks the HTTP request parameter map for the delimited value 
   * associated with a supplied key. 
   * <br/>If found, the delimited value is tokenized into a collection of "equal to"
   * property clauses ({@link com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsEqualTo}).
   * <br/>If multiple values are found, they will be logically grouped based upon the 
   * supplied "orBased" parameter ({@link LogicalClause}).
   * @param restKey the URL key for the parameter
   * @param discoverableKey the key associated with the target discoverable
   * @param delimiter the value delimiter
   * @param orBased if true, group within a logical or, otherwise group within a logical and
   * @return an appropriate discovery clause (null if not validly located within the request)
   */
  public DiscoveryClause extractPropertyList(String restKey, 
                                             String discoverableKey,
                                             String delimiter, 
                                             boolean orBased) {
    Discoverable discoverable = findDiscoverable(discoverableKey);
    String delimitedValues = getRequestParameter(restKey);
    String[] values = Val.tokenize(delimitedValues,delimiter);
    if ((discoverable != null) && (values != null) && (values.length > 0)) {
      LogicalClause logical = null;
      if (orBased) {
        logical = new LogicalClause.LogicalOr();
      } else {
        logical = new LogicalClause.LogicalAnd();
      }
      for (String value: values) {
        value = Val.chkStr(value);
        if (value.length() > 0) {
          PropertyClause clause = new PropertyClause.PropertyIsEqualTo();
          clause.setTarget(discoverable);
          clause.setLiteral(value);
          logical.getClauses().add(clause);
        }
      }
      if (logical.getClauses().size() > 1) {
        return logical;
      } else if (logical.getClauses().size() == 1) {
        return logical.getClauses().get(0);
      }
    }
    return null;
  }
  
  /**
   * Extracts a range based discovery clause from the HTTP request.
   * <br/>This method checks the HTTP request parameter map for the lower and/or upper 
   * boundaries of a range based query. 
   * <br/>If found, the lower boundary will be associated with a 
   * {@link com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsGreaterThanOrEqualTo} clause, the upper boundary with a
   * {@link com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsLessThanOrEqualTo} clause.
   * <br/>If both are found, a {@link com.esri.gpt.catalog.discovery.LogicalClause.LogicalAnd} clause is returned.
   * @param restLowerKey the URL key for the lower value parameter of the range
   * @param restUpperKey the URL key for the upper value parameter of the range
   * @param discoverableKey the key associated with the target discoverable
   * @return an appropriate discovery clause (null if not validly located within the request)
   */
  public DiscoveryClause extractPropertyRange(String restLowerKey, 
                                              String restUpperKey, 
                                              String discoverableKey) {
    PropertyClause lower = this.extractProperty(
        new PropertyClause.PropertyIsGreaterThanOrEqualTo(),restLowerKey,discoverableKey);
    PropertyClause upper = this.extractProperty(
        new PropertyClause.PropertyIsLessThanOrEqualTo(),restUpperKey,discoverableKey);
    
    // this isn't ideal, but it's here to handle a circumstance where
    // a validBefore URL parameter has been passed as validTo
    if (upper == null) {
      if ((restLowerKey != null) && restLowerKey.equalsIgnoreCase("validAfter")) {
        if ((restUpperKey != null) && restUpperKey.equalsIgnoreCase("validBefore")) {
          upper = this.extractProperty(
              new PropertyClause.PropertyIsLessThanOrEqualTo(),"validTo",discoverableKey);
        }
      }
    }
    
    if ((lower != null) && (upper != null)) {
      LogicalClause logical = new LogicalClause.LogicalAnd();
      logical.getClauses().add(lower);
      logical.getClauses().add(upper);
      return logical;
    } else if (lower != null) {
      return lower;
    } else if (upper != null) {
      return upper;
    }
    return null;
  }
  
  /**
   * Extracts sort option parameters from the HTTP request.
   * <br/>This method checks the HTTP request parameter map for an order by 
   * parameter and constructs an appropriate Sortables component if located.
   * @param restKey the URL key for the sort option parameter
   * @return the sortables (null if not validly located within the request)
   */
  public Sortables extractSortables(String restKey) {
    Sortables sortables = null;
    Discoverable target = null;
    Sortable.SortDirection direction = null;
    String sortBy = Val.chkStr(getRequestParameter(restKey));
    if (sortBy.equalsIgnoreCase("dateAscending")) {
      target = this.findDiscoverable("dct:modified");
      direction = Sortable.SortDirection.ASC;
    } else if (sortBy.equalsIgnoreCase("dateDescending")) {
      target = this.findDiscoverable("dct:modified");
      direction = Sortable.SortDirection.DESC;
    } else if (sortBy.equalsIgnoreCase("relevance")) {
    } else if (sortBy.equalsIgnoreCase("title")) {
      target = this.findDiscoverable("dc:title");
      direction = Sortable.SortDirection.ASC;
    } else if (sortBy.equalsIgnoreCase("format")) {
      target = this.findDiscoverable("contentType");
      direction = Sortable.SortDirection.ASC;
    } else if (sortBy.equalsIgnoreCase("areaAscending")) {
      target = this.findDiscoverable("geometry");
      direction = Sortable.SortDirection.ASC;
    } else if (sortBy.equalsIgnoreCase("areaDescending")) {
      target = this.findDiscoverable("geometry");
      direction = Sortable.SortDirection.DESC;
    } else {
      if (sortBy.length() > 0) {
        if (sortBy.toLowerCase().endsWith(".asc")) {
          target = this.findDiscoverable(sortBy.substring(0,sortBy.length()-4));
          direction = Sortable.SortDirection.ASC; 
        } else if (sortBy.toLowerCase().endsWith(".ascending")) {
          target = this.findDiscoverable(sortBy.substring(0,sortBy.length()-10));
          direction = Sortable.SortDirection.ASC;  
        } else if (sortBy.toLowerCase().endsWith(".desc")) {
          target = this.findDiscoverable(sortBy.substring(0,sortBy.length()-5));
          direction = Sortable.SortDirection.DESC; 
        } else if (sortBy.toLowerCase().endsWith(".descending")) {
          target = this.findDiscoverable(sortBy.substring(0,sortBy.length()-11));
          direction = Sortable.SortDirection.DESC;     
        } else {
          target = this.findDiscoverable(sortBy);
          direction = Sortable.SortDirection.ASC;
        }
      }
    }
    if ((target != null) && (direction != null)) {
      Sortable sortable = target.asSortable();
      sortable.setDirection(direction);
      sortables = new Sortables();
      sortables.add(sortable);
    }
    return sortables;
  }
  
  /**
   * Extracts the spatial clause from the HTTP request.
   * <br/>This method checks the HTTP request parameter map for spatial related
   * parameters and constructs a SpatialClause if located (filter related).
   * @param restBBoxKey the URL key for the BBOX parameter
   * @param restOperatorKey the URL key for the spatial operator parameter
   * @param discoverableKey the key associated with the target discoverable
   * @return the spatial clause (null if not validly located within the request)
   */
  public SpatialClause extractSpatialClause(String restBBoxKey, 
                                            String restOperatorKey, 
                                            String discoverableKey) {
    
    // make the clause
    SpatialClause clause = null;
    String operator = Val.chkStr(getRequestParameter(restOperatorKey));
    if (operator.equalsIgnoreCase("BBOX")) {
      clause = new SpatialClause.GeometryBBOXIntersects();
    } else if (operator.equalsIgnoreCase("Contains")) {
      clause = new SpatialClause.GeometryContains();
    } else if (operator.equalsIgnoreCase("Disjoint")) {
      clause = new SpatialClause.GeometryIsDisjointTo();
    } else if (operator.equalsIgnoreCase("Equals")) {
      clause = new SpatialClause.GeometryIsEqualTo();
    } else if (operator.equalsIgnoreCase("Intersects")) {
      clause = new SpatialClause.GeometryIntersects();
    } else if (operator.equalsIgnoreCase("Overlaps")) {
      clause = new SpatialClause.GeometryOverlaps();
    } else if (operator.equalsIgnoreCase("Within")) {
      clause = new SpatialClause.GeometryIsWithin();
      
    } else if (operator.equalsIgnoreCase("esriSpatialRelWithin")) {
      clause = new SpatialClause.GeometryIsWithin();
    } else if (operator.equalsIgnoreCase("esriSpatialRelOverlaps")) {
      clause = new SpatialClause.GeometryOverlaps();
      
    } else {
      clause = new SpatialClause.GeometryBBOXIntersects();
    }
    
    // determine the target and envelope, check before returning
    Discoverable target = findDiscoverable(discoverableKey);
    if ((clause != null) && (target != null)) {
      clause.setTarget(target);
      String sBBox = Val.chkStr(getRequestParameter(restBBoxKey));
      String[] aBBox = sBBox.split(",");
      if (aBBox.length == 4) {
        clause.getBoundingEnvelope().put(aBBox[0],aBBox[1],aBBox[2],aBBox[3]);
      }
      if (!clause.getBoundingEnvelope().isEmpty()) {
        return clause;
      } 
    }
    return null;
  }
  
  /**
   * Finds the discoverable property associated with a discoverable key.
   * @param discoverableKey the discoverable key (or alias)
   * @return the discoverable property (null if none was found);
   */
  public Discoverable findDiscoverable(String discoverableKey) {
    return getDiscoverables().get(discoverableKey);
  }
  
  /**
   * Instantiates property clause from a supplied property clause class name.
   * <br/>A runtime exception will be thrown if the supplien class name is invalid.
   * @param class name the class name of the property clause to instantiate
   * @return the new property clause object
   */
  private PropertyClause makePropertyClause(String className) {
    try {
      Class<?>cls = Class.forName(className);
      Object obj = cls.newInstance();
      if (obj instanceof PropertyClause) {
        return (PropertyClause)obj;
      } else {
        String sMsg = "The configured PropertyClause class name is invalid: "+className;
        throw new ConfigurationException(sMsg);
      }
    } catch (ConfigurationException t) {
      throw t;
    } catch (Throwable t) {
      String sMsg = "Error instantiating PropertyClause: "+className;
      throw new ConfigurationException(sMsg,t);
    }
  }
  
  /**
   * Gets the HTTP request parameter value associated with a key.
   * @param parameterKey the parameter key
   * @return the parameter value (empty string if not found)
   */
  public String getRequestParameter(String parameterKey) {
    for (Map.Entry<String, String[]> e : this.requestParameterMap.entrySet()) {
      if (e.getKey().equalsIgnoreCase(parameterKey)) {
        if (e.getValue().length > 0) {
          return Val.chkStr(e.getValue()[0]);
        } else {
          return "";
        }
      }
    }
    return "";
  }
  
  /*
  // This is just an example of usage, for an implementation see: 
  // {@link com.esri.gpt.control.georss.RestQueryServlet#parseRequest(HttpServletRequest, RequestContext)}
  private void parse() {
    parseResponseFormat("f");
    parseResponseGeometry("geometryType");
    parseResponseStyle("style");
    parseResponseTarget("target");
    parseStartRecord("start",1);
    parseMaxRecords("max",10);
    parsePropertyIsEqualTo("uuid","uuid");
    parsePropertyIsLike("searchText","anytext");
    parsePropertyList("contentType","dc:type",",",true);
    parsePropertyList("dataCategory","dc:subject",",",true);
    parsePropertyRange("after","before","dct:modified");
    parseSpatialClause("bbox","spatialRel","geometry");
    parseSortables("orderBy");
  }
  */
    
  /**
   * Parses and sets the maximum number of return records for the query filter.
   * <br/>See: {@link DiscoveryFilter#setMaxRecords(int)}
   * @param restKey the URL key for the parameter
   * @param defaultValue the default value (if the parameter is not located on the URL)
   */
  public void parseMaxRecords(String restKey, int defaultValue) {
    getFilter().setMaxRecords(Val.chkInt(getRequestParameter(restKey),defaultValue));
  }
    
  /**
   * Parses and appends a PropertyIsEqualTo clause to the query filter if located.
   * <br/>See: {@link #extractProperty(PropertyClause, String, String)}
   * <br/>See: {@link com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsEqualTo}
   * <br/>See: {@link RestQuery#getFilter}
   * @param restKey the URL key for the parameter
   * @param discoverableKey the key associated with the target discoverable
   */
  public void parsePropertyIsEqualTo(String restKey, String discoverableKey) {
    appendClause(getFilter().getRootClause(),
        extractProperty(new PropertyClause.PropertyIsEqualTo(),restKey,discoverableKey));
  }
    
  /**
   * Parses and appends a PropertyIsLike clause to the query filter if located.
   * <br/>See: {@link #extractProperty(PropertyClause, String, String)}
   * <br/>See: {@link com.esri.gpt.catalog.discovery.PropertyClause.PropertyIsLike}
   * <br/>See: {@link RestQuery#getFilter}
   * @param restKey the URL key for the parameter
   * @param discoverableKey the key associated with the target discoverable
   */
  public void parsePropertyIsLike(String restKey, String discoverableKey) {
    appendClause(getFilter().getRootClause(),
        extractProperty(new PropertyClause.PropertyIsLike(),restKey,discoverableKey));
  }  
  
  /**
   * 
   * Parses and appends a grouping of property clauses (delimited list) to the 
   * query filter if located.
   * <br/>See: {@link RestQueryParser#extractPropertyList(String, String, String, boolean)}
   * <br/>See: {@link RestQuery#getFilter}
   * @param restKey the URL key for the parameter
   * @param discoverableKey the key associated with the target discoverable
   * @param delimiter the value delimiter
   * @param orBased if true, group within a logical or, otherwise group within a logical and
   */
  public void parsePropertyList(String restKey, 
                                String discoverableKey, 
                                String delimiter, 
                                boolean orBased) {
    appendClause(getFilter().getRootClause(),
        extractPropertyList(restKey,discoverableKey,delimiter,orBased));
  }
    
  /**
   * Parses and appends a range based discovery clause to the query filter if located.
   * <br/>See: {@link #extractPropertyRange(String, String, String)}
   * <br/>See: {@link RestQuery#getFilter}
   * @param restLowerKey the URL key for the lower value parameter of the range
   * @param restUpperKey the URL key for the upper value parameter of the range
   * @param discoverableKey the key associated with the target discoverable
   */
  public void parsePropertyRange(String restLowerKey, String restUpperKey, String discoverableKey) {
    appendClause(getFilter().getRootClause(),
        extractPropertyRange(restLowerKey,restUpperKey,discoverableKey));
  }
  
  /**
   * Parses and sets the repository ID.
   * <br/>See: {@link RestQuery#setRepositoryId(String)}
   * @param restKey the URL key for the parameter
   */
  public void parseRepositoryId(String restKey) {
    getQuery().setRepositoryId(getRequestParameter(restKey));
  }
  
  /**
   * Parses and sets the response format for the query.
   * <br/>See: {@link RestQuery#setResponseFormat(String)}
   * @param restKey the URL key for the parameter
   */
  public void parseResponseFormat(String restKey) {
    getQuery().setResponseFormat(getRequestParameter(restKey));
  }
  
  /**
   * Parses and sets the response geometry for the query.
   * <br/>See: {@link RestQuery#setResponseGeometry(String)}
   * @param restKey the URL key for the parameter
   */
  public void parseResponseGeometry(String restKey) {
    getQuery().setResponseGeometry(getRequestParameter(restKey));
  }
  
  /**
   * Parses and sets the response style for the query.
   * <br/>See: {@link RestQuery#setResponseStyle(String)}
   * @param restKey the URL key for the parameter
   */
  public void parseResponseStyle(String restKey) {
    getQuery().setResponseStyle(getRequestParameter(restKey));
  }
  
  /**
   * Parses and sets the response target for the query.
   * <br/>See: {@link RestQuery#setResponseTarget(String)}
   * @param restKey the URL key for the parameter
   */
  public void parseResponseTarget(String restKey) {
    getQuery().setResponseTarget(getRequestParameter(restKey));
  }
  
  /**
   * Parses sort option parameters and sets the query sortables if found.
   * <br/>See: {@link RestQueryParser#extractSortables(String)}
   * <br/>See: {@link RestQuery#setSortables(Sortables)}
   * @param restKey the URL key for the parameter
   */
  public void parseSortables(String restKey) {
    getQuery().setSortables(extractSortables(restKey));
  }
  
  /**
   * Parses and appends a spatial clause to the query filter if located.
   * <br/>See: {@link #extractSpatialClause(String, String, String)}
   * <br/>See: {@link RestQuery#getFilter}
   * @param restBBoxKey the URL key for the BBOX parameter
   * @param restOperatorKey the URL key for the spatial operator parameter
   * @param discoverableKey the key associated with the target discoverable
   */
  public void parseSpatialClause(String restBBoxKey, 
                                 String restOperatorKey, 
                                 String discoverableKey) {
    appendClause(getFilter().getRootClause(),
        extractSpatialClause(restBBoxKey,restOperatorKey,discoverableKey));
  }
  
  /**
   * Parses and sets the start record for the query filter.
   * <br/>See: {@link DiscoveryFilter#setStartRecord(int)}
   * @param restKey the URL key for the parameter
   * @param defaultValue the default value (if the parameter is not located on the URL)
   */
  public void parseStartRecord(String restKey, int defaultValue) {
    getFilter().setStartRecord(Val.chkInt(getRequestParameter(restKey),defaultValue));
  }

  /**
   * Gets query string to more results.
   * @param queryString initial query string
   * @return query string to more results
   */
  private String getMoreQueryString(String queryString) {
    String [] props = queryString.split("&");

    boolean found = false;
    for (int i=0; i<props.length; i++) {
      String prop = props[i];
      if (prop.toLowerCase().startsWith("start=")) {
        String [] kvp = prop.split("=");
        if (kvp.length==2) {
          int startRecord = Val.chkInt(kvp[1], 1);
          props[i] =  kvp[0] + "=" + Integer.toString(startRecord + getFilter().getMaxRecords());
          found = true;
          break;
        }
      }
    }

    StringBuilder sb = new StringBuilder();
    for (String prop : props) {
      if (sb.length()>0) sb.append("&");
      sb.append(prop);
    }
    if (!found) {
      if (sb.length()>0) sb.append("&");
      sb.append("start=" + Integer.toString(getFilter().getStartRecord() + getFilter().getMaxRecords()));
    }

    return sb.toString();
  }
  
}
