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
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.esri.gpt.catalog.search.SearchParameterMap.Value;
import com.esri.gpt.framework.request.QueryCriteria;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;

/**
 * The class SearchCriteria. Class for composition of search criteria.
 */
@SuppressWarnings("serial")
public class SearchCriteria extends QueryCriteria {

// class variables =============================================================
/** The Constant TAG_FILTER. Used when <b>this</b> is serialized to XML. */
private final static String TAG_FILTER = "filter";

/** The Constant TAG_FILTER_INFO. Used when <b>this</b> is serialized to XML. */
private final static String TAG_FILTER_CLASS = "class";

/** The Constant TAG_FILTER_PARAM. Used when <b>this</b> is serialized to XML. */
private final static String TAG_FILTER_PARAM = "param";

/** The Constant TAG_FILTER_PARAM_NAME. Used when <b>this</b> is serialized to XML. */
private final static String TAG_FILTER_PARAM_NAME = "name";

/** The Constant TAG_FILTER_PARAM_VALUE. Used when <b>this</b> is serialized to XML. */
private final static String TAG_FILTER_PARAM_VALUE = "value";

/** The Constant TAG_FILTER_PARAM_INFO. Used when <b>this</b> is serialized to XML. */
private final static String TAG_FILTER_PARAM_INFO = "info";

/** Filters with attributes in the search criteria */
private final static String TAG_FILTER_ESSENTIAL = "essential";

/** Attribute where the saved search name will be put*/
private final static String ATTRIB_SAVEDSEARCH_NAME = "savedSearchName";

/** Element for saved states **/
private final static String ELEM_SAVEDSTATES = "savedStates";

/**
 * Where to save the search criteria to.
 */
public static enum OptionSaveLocation {

/** option to save to desktop. */
saveToDesktop,

/** option to save to online repository. */
saveToOnline
};
 
/** The class logger *. */
private static Logger LOG = 
  Logger.getLogger(SearchCriteria.class.getCanonicalName());

/** The saved search name. */
private String savedSearchName;

// instance variables  =========================================================
/** The list of all filters. */
private SearchFiltersList miscelleniousFilters;

/** The essential filter objects. */
private SearchFiltersList  essentialFilterObjects;

/** Flag indicating whether or not result records should be expanded by default. */
private boolean expandResultContent = false;

/** The search uri filter. */
private ISearchFilterURI searchFilterUri;

/** The page cursor filter. */
private ISearchFilterPagination searchFilterPageCursor;

/** The spatial search filter. */
private ISearchFilterSpatialObj searchFilterSpatial;

/** The search filter themes. */
private ISearchFilterThemes searchFilterThemes;

/** The search filter keyword. */
private ISearchFilterKeyword searchFilterKeyword;

/** The search filter content types. */
private ISearchFilterContentTypes searchFilterContentTypes;

/** The search filter temporal. */
private ISearchFilterTemporal searchFilterTemporal;

/** The search filter sort. */
private ISearchFilterSort searchFilterSort;

// constructor =================================================================
/**
 * Instantiates a new search criteria.
 * If class is extended, be sure to use super
 */
public SearchCriteria() {
  super();
}

/**
 * Instantiates a new search criteria with values of searchCriteriaDom.
 * 
 * @param searchCriteriaDom the search criteria dom (never null assumed)
 * 
 * @throws SearchException the search exception
 */
public SearchCriteria(Document searchCriteriaDom) throws SearchException {
  this.loadSearchCriteria(searchCriteriaDom);
}

/**
 * Instantiates a new search criteria with the values of the criteria param.
 * 
 * @param criteria the criteria (assumed never null)
 * 
 * @throws SearchException the search exception
 */
public SearchCriteria(SearchCriteria criteria) throws SearchException {
  this.loadSearchCriteria(criteria.toDom());
}

// properties ===================================================================

/**
 * Gets the flag indicating whether or not result records should be expanded by default.
 * @return true if results records should be expanded
 */
public boolean getExpandResultContent() {
  return expandResultContent;
}
/**
 * Sets the flag indicating whether or not result records should be expanded by default.
 * @param expand true if results records should be expanded
 */
public void setExpandResultContent(boolean expand) {
  this.expandResultContent = expand;
}

/**
 * Gets the saved search name.
 * 
 * @return the saved search name (never null)
 */
public String getSavedSearchName() {
  return Val.chkStr(savedSearchName);
}

/**
 * Sets the saved search name.
 * 
 * @param savedSearchName the new saved search name
 */
public void setSavedSearchName(String savedSearchName) {
  this.savedSearchName = savedSearchName;
}


/**
 * Gets the filters that do not have attributes in the search criteria.
 * 
 * @return the filter object list (never null )
 */
public final SearchFiltersList getMiscelleniousFilters() {
  if(this.miscelleniousFilters == null) {
    this.miscelleniousFilters = new SearchFiltersList();
  }
 return this.miscelleniousFilters;
}

/**
 * Sets the filter object list.
 * 
 * @param filterObjectList the new filter object list
 */
@SuppressWarnings("unchecked")
public final void setMiscelleniousFilters(SearchFiltersList filterObjectList) {
  this.miscelleniousFilters = filterObjectList;
}

/**
 * Gets the essential filter objects.
 * 
 * @return the essential filter objects (never null)
 */
private SearchFiltersList getEssentialFilters() {
  if(essentialFilterObjects == null){
    essentialFilterObjects = new SearchFiltersList();
  }
  return essentialFilterObjects;
}

/**
 * Updates the individual essential filter objects.
 * 
 * @param essentialFilterObjects the new essential filter objects
 */
private void hyrdateEssentialFilters(SearchFiltersList essentialFilterObjects) {
   
  for(ISearchFilter filter: essentialFilterObjects) {
    try {
      if(filter == null) {
        continue;
      }
      else if(filter instanceof ISearchFilterContentTypes){
        this.getSearchFilterContentTypes().setParams(filter.getParams());
      } else if(filter instanceof ISearchFilterKeyword){
        this.getSearchFilterKeyword().setParams(filter.getParams());
      } else if(filter instanceof ISearchFilterPagination){
        this.getSearchFilterPageCursor().setParams(filter.getParams());
      } else if(filter instanceof ISearchFilterSort){
        this.getSearchFilterSort().setParams(filter.getParams());
      } else if(filter instanceof ISearchFilterSpatialObj){
        this.getSearchFilterSpatial().setParams(filter.getParams());
      } else if(filter instanceof ISearchFilterTemporal){
        this.getSearchFilterTemporal().setParams(filter.getParams());
      } else if(filter instanceof ISearchFilterThemes){
        this.getSearchFilterThemes().setParams(filter.getParams());
      } else if(filter instanceof ISearchFilterURI){
        this.getSearchFilterThemes().setParams(filter.getParams());
      } 
    }catch(Exception e) {
      LOG.log(Level.WARNING, "Error while work", e);
    }

  }

}

/**
 * Hydrate miscellenious filters.
 * 
 * @param list the list
 */
@SuppressWarnings("unchecked")
private void hydrateMiscelleniousFilters(List list) {
  if(list == null) {
    LOG.info("Hydrating Miscellenious Filters got null list");
    return;
  }
  
  Iterator newListIter = list.iterator();
  Iterator curListIter = this.getMiscelleniousFilters().iterator();
  boolean newFilAdded = false;
  ISearchFilter curFil = null;
  ISearchFilter newFil = null;
  
  while(newListIter != null && newListIter.hasNext()) {
    Object newObj = newListIter.next();
    if(!(newObj instanceof ISearchFilter)) {
      continue;
    }
    newFilAdded = false;
    newFil = (ISearchFilter) newObj;
    
    while(curListIter != null && curListIter.hasNext()) {
      Object curObj = curListIter.next();
      if(!(curObj instanceof ISearchFilter)) {
        continue;
      }
      curFil = (ISearchFilter) curObj;
      if(newFil.getClass().isInstance(curObj)) {
        try {
          curFil.setParams(newFil.getParams());
          newFilAdded = true;
        } catch (SearchException e) {
          LOG.log(Level.WARNING, "Error while loading miscellenious filter", e);
        }
      }
    }
    if(newFilAdded == false) {
      this.getMiscelleniousFilters().add(newFil);
    }
    
  }
  
  
  
}

// filters ====
/**
 * Gets the search uri Filter.
 * 
 * @return the search uri filter (never null)
 */
public ISearchFilterURI getSearchFilterUri() {
  if(searchFilterUri == null) {
    searchFilterUri = new SearchFilterConnection();
  }
  return searchFilterUri;
}

/**
 * Sets the search uri.
 * 
 * @param searchURIFilter the new search uri filter
 */
public void setSearchFilterUri(ISearchFilterURI searchURIFilter) {
  this.getEssentialFilters().remove(this.searchFilterUri);
  this.searchFilterUri = searchURIFilter;
  this.getEssentialFilters().add(this.searchFilterUri);
}
/**
 * Gets the page cursor filter.
 * 
 * @return the page cursor filter (possibly null)
 */
public ISearchFilterPagination getSearchFilterPageCursor() {
  if(searchFilterPageCursor == null) {
    this.setSearchFilterPageCursor(new SearchFilterPagination());
  }
  return searchFilterPageCursor;
}

/**
 * Sets the page cursor filter.
 * CAUTION: If using faces context DO NOT SET THIS METHOD.
 * @param pageCursorFilter the new page cursor filter
 */
public void setSearchFilterPageCursor(ISearchFilterPagination pageCursorFilter) {
  this.getEssentialFilters().remove(this.searchFilterPageCursor);
  this.searchFilterPageCursor = pageCursorFilter;
  this.getEssentialFilters()
    .add(this.searchFilterPageCursor);
}

/**
 * Gets the search filter spatial.
 * 
 * @return the search filter spatial (possibly null)
 */
public ISearchFilterSpatialObj getSearchFilterSpatial() {
  if(searchFilterSpatial == null) {
    this.setSearchFilterSpatial(new SearchFilterSpatial());
  }
  return searchFilterSpatial;
}

/**
 * Sets the search filter spatial.
 * CAUTION: If using faces context DO NOT SET THIS METHOD.
 * @param searchFilterSpatial the new search filter spatial
 */
public void setSearchFilterSpatial(ISearchFilterSpatialObj searchFilterSpatial) {
  this.getEssentialFilters().remove(this.searchFilterSpatial);
  this.searchFilterSpatial = searchFilterSpatial;
  this.getEssentialFilters().add(this.searchFilterSpatial);
}

/**
 * Gets the search filter themes.
 * 
 * @return the search filter themes (never null)
 */
public ISearchFilterThemes getSearchFilterThemes() {
  if(this.searchFilterThemes == null) {
    this.setSearchFilterThemes(new SearchFilterThemeTypes());
  }
  return searchFilterThemes;
}

/**
 * Sets the search filter themes.
 * CAUTION: If using faces context DO NOT SET THIS METHOD. 
 * @param searchFilterThemes the new search filter themes
 */
public void setSearchFilterThemes(ISearchFilterThemes searchFilterThemes) {
  this.getEssentialFilters().remove(this.searchFilterThemes);
  this.searchFilterThemes = searchFilterThemes;
  this.getEssentialFilters().add(this.searchFilterThemes);
}

/**
 * Gets the search filter keyword.
 * 
 * @return the search filter keyword (never null)
 */
public ISearchFilterKeyword getSearchFilterKeyword() {
  if(this.searchFilterKeyword == null) {
    this.setSearchFilterKeyword(new SearchFilterKeyword());
  }
  return searchFilterKeyword;
}

/**
 * Sets the search filter keyword.
 * CAUTION: If using faces context DO NOT SET THIS METHOD.
 * @param searchFilterKeyword the new search filter keyword
 */
public void setSearchFilterKeyword(ISearchFilterKeyword searchFilterKeyword) {
  this.getEssentialFilters().remove(this.searchFilterKeyword);
  this.searchFilterKeyword = searchFilterKeyword;
  this.getEssentialFilters().add(this.searchFilterKeyword);
}

/**
 * Gets the search filter content types.
 * 
 * @return the search filter content types (never null)
 */
public ISearchFilterContentTypes getSearchFilterContentTypes() {
  if(this.searchFilterContentTypes == null){
    this.setSearchFilterContentTypes(new SearchFilterContentTypes());
  }
  return searchFilterContentTypes;
}

/**
 * Sets the search filter content types.
 * CAUTION: If using faces context DO NOT SET THIS METHOD.
 * @param searchFilterContentTypes the new search filter content types
 */
public void setSearchFilterContentTypes(
    ISearchFilterContentTypes searchFilterContentTypes) {
  this.getEssentialFilters().remove(this.searchFilterContentTypes);
  this.searchFilterContentTypes = searchFilterContentTypes;
  this.getEssentialFilters().add(this.searchFilterContentTypes);
  
}

/**
 * Gets the search filter sort.
 * 
 * @return the search filter sort (never null)
 */
public ISearchFilterSort getSearchFilterSort() {
  if(this.searchFilterSort == null) {
    this.setSearchFilterSort(new SearchFilterSort());
  }
  return searchFilterSort;
}

/**
 * Sets the search filter sort.
 * CAUTION: If using faces context DO NOT SET THIS METHOD.
 * @param searchFilterSort the new search filter sort
 */
public void setSearchFilterSort(ISearchFilterSort searchFilterSort) {
  this.getEssentialFilters().remove(this.searchFilterSort);
  this.searchFilterSort = searchFilterSort;
  this.getEssentialFilters().add(this.searchFilterSort);
}

/**
 * Gets the search filter temporal.
 * 
 * @return the search filter temporal (never null)
 */
public ISearchFilterTemporal getSearchFilterTemporal() {
  if(this.searchFilterTemporal == null) {
    this.setSearchFilterTemporal(new SearchFilterTemporal());
  }
  return searchFilterTemporal;
}

/**
 * Sets the search filter temporal.
 * CAUTION: If using faces context DO NOT SET THIS METHOD.
 * @param searchFilterTemporal the new search filter temporal
 */
public void setSearchFilterTemporal(ISearchFilterTemporal searchFilterTemporal) {
  this.getEssentialFilters().remove(this.searchFilterTemporal);
  this.searchFilterTemporal = searchFilterTemporal;
  this.getEssentialFilters().add(this.searchFilterTemporal);
}


// methods =====================================================================
/**
 * Reset <b>this</b>.  Resets all search filters & this.
 * to remove search filters w
 * 
 * @see com.esri.gpt.framework.request.QueryCriteria#reset()
 */
public final void resetFilters() {
  
  for(ISearchFilter filter : this.getMiscelleniousFilters()) {
    filter.reset();
  }
  for(ISearchFilter filter : this.getEssentialFilters()) {
    filter.reset();
  }
}

/**
 * Resets all search filters.
 */
@Override
public final void reset() {
  resetFilters();
}

/**
 * Validate input.
 * 
 * @throws SearchException thrown when validation has errors
 */
public final void validate() throws SearchException {
  for(ISearchFilter filter : this.getMiscelleniousFilters() ) {
    filter.validate();
  }
  for(ISearchFilter filter : this.getEssentialFilters() ) {
    filter.validate();
  }
}

/**
 * The DOM representation of Search Criteria.
 * <gpt>
 * <savedStates>
 * <filter class="com.esri.gpt...">
 * <param name="" info="" value=""/>
 * ...
 * ...
 * </filter>
 * <filter class="">
 * ...
 * ...
 * </filter>
 * ...
 * ...
 * <savedStates>
 * 
 * </gpt>
 * 
 * @return the Dom representing the criteria (possibly null)
 * 
 * @throws SearchException Especially if DOM document could not be initialized
 */
public final Document toDom() throws SearchException {
  
  return toDom(null);
}

/**
 * To dom.
 * 
 * @param extraInfo the extra info
 * @param dateFormat the date format
 * 
 * @return the document
 * 
 * @throws SearchException the search exception
 */
public final Document toDom(String extraInfo, SimpleDateFormat dateFormat) 
  throws SearchException {
  
  Document doc = null;

  try {
    doc = DomUtil.newDocument();
    Element eGpt = doc.createElement("gpt");
    doc.appendChild(eGpt);
    Element eSavedStates = doc.createElement(ELEM_SAVEDSTATES);
    if(extraInfo != null) {
      eSavedStates.setAttribute("info", extraInfo);
    }
    eSavedStates.setAttribute(ATTRIB_SAVEDSEARCH_NAME, 
        this.getSavedSearchName());
    eGpt.appendChild(eSavedStates);
    
    List<ISearchFilter> essentialFilters = this.getEssentialFilters();
    this.createDomFilters(doc, eSavedStates, true, essentialFilters,
           dateFormat);
    this.createDomFilters(doc, eSavedStates, false, 
           this.getMiscelleniousFilters(), dateFormat);
   
    if(LOG.isLoggable(Level.FINER)) {
       LOG.finer("SearchCriteria XML Representation\n" 
                  + XmlIoUtil.domToString(doc));
    }
  }
  catch(Exception e) {
    LOG.log(Level.WARNING, 
        "Could not convert searchCriteria to xml representation",
        e);
    throw new SearchException(e);
  }
  
  return doc;  
}

/**
 * Creates the dom filters.
 * 
 * @param doc the doc
 * @param isEssential the is essential
 * @param filters the filters
 * @param eSavedStates the e saved states
 * @param dateFormat the date format
 */
private void createDomFilters(Document doc, Element eSavedStates, 
    boolean isEssential, List<ISearchFilter> filters, 
    SimpleDateFormat dateFormat) {
  
  ISearchFilter filter = null;
  Element eFilter = null;
  Element params = null;
  
  Iterator<ISearchFilter> iter = filters.iterator();
  while(iter!= null && iter.hasNext()) {
    
    filter = iter.next();
    
    SearchParameterMap sMap = null;
    if(dateFormat == null  || !(filter instanceof ISearchFilterTemporal)) {
      sMap = filter.getParams();
    } else {
      sMap = ((ISearchFilterTemporal)filter).getParams(dateFormat);
    }
            
    if(sMap == null ) {
      continue;
    }
    Iterator<Entry<String, Value>> iter2 = sMap
    .entrySet().iterator();

    eFilter = doc.createElement(TAG_FILTER);
    eFilter.setAttribute(TAG_FILTER_CLASS, 
        filter.getClass().getCanonicalName());
    eFilter.setAttribute(TAG_FILTER_ESSENTIAL, 
        String.valueOf(isEssential));
    eSavedStates.appendChild(eFilter);      

    while(iter2 != null && iter2.hasNext()) {
      Entry<String, Value> vals = iter2.next();
      params = doc.createElement(TAG_FILTER_PARAM);
      params.setAttribute(TAG_FILTER_PARAM_NAME, 
          vals.getKey());
      params.setAttribute(TAG_FILTER_PARAM_VALUE, 
          vals.getValue().getParamValue());
      params.setAttribute(TAG_FILTER_PARAM_INFO,
          vals.getValue().getInfo());
      eFilter.appendChild(params);
    }


  }
  
}

/**
 * Transform <b>this</b> criteria to its DOM representation.
 * 
 * @param extraInfo Any extra info to be put included in this DOM (can be null)
 * 
 * @return the document (possibly null)
 * 
 * @throws SearchException Especially if DOM document could not be initialized
 */
public final Document toDom(String extraInfo) throws SearchException {
  return toDom(extraInfo, null);
}


/**
 * Convinience method to get DOM as a String.
 * 
 * @return the DOM string representation (never null)
 * 
 * @throws SearchException the search exception
 */
public String toDom2() throws SearchException {
  Document doc = this.toDom();
  String tmp = null;
  try {
    tmp = XmlIoUtil.domToString(doc);
    if(tmp == null || "".equals(tmp.trim())) {
      throw new SearchException("Dom conversion did not end up in xml string");
    }
  } catch (Exception e) {
    throw new SearchException("Could not turn criteria to DOM object: " 
        + e.getMessage(), e);
  }
  return tmp;
}

/**
 * Gets the mapped filters.  This will be used for translating
 * SearchCriteria DOMS to a new SearchCriteria
 * 
 * @return the mapped filters <filter.class.canonicalName, filter>
 */
private Map<String, ISearchFilter> getMappedFilters() {
  
  Map<String, ISearchFilter> map = new LinkedHashMap<String, ISearchFilter>();
  map.putAll(this.getMappedEssentialFilters());
  map.putAll(this.getMappedMiscelleniousFilters());
  return map;
}

/**
 * Gets the mapped essential filters.
 * 
 * @return the mapped essential filters
 */
private Map<String, ISearchFilter> getMappedEssentialFilters(){
  Map<String, ISearchFilter> map = new LinkedHashMap<String, ISearchFilter>();
  for(ISearchFilter filter : this.getMiscelleniousFilters()) {
    map.put(filter.getClass().getCanonicalName(), filter);
  }
  return map;
}

/**
 * Gets the mapped miscellenious filters.
 * 
 * @return the mapped miscellenious filters
 */
private Map<String, ISearchFilter> getMappedMiscelleniousFilters(){
  Map<String, ISearchFilter> map = new LinkedHashMap<String, ISearchFilter>();
  for(ISearchFilter filter : this.getMiscelleniousFilters()) {
    map.put(filter.getClass().getCanonicalName(), filter);
  }
  return map;
}

/**
 * Gets the new search filters.  Should be overridden by customized
 * SearchCriteria child.  The child should return a set containing all the
 * new filters implemented
 * 
 * @return the new search filters (possibly null)
 */
protected Set<ISearchFilter> getNewSearchFilters() {
  return null;
}


/**
 * Load search criteria.
 * 
 * @param criteriaDom the criteria XML DOM object (Exception on null)
 * 
 * @throws SearchException exception on error
 */
public void loadSearchCriteria(Document criteriaDom) throws SearchException {
  
  this.resetFilters();
  LOG.fine("Deserializing criteria");
  if(LOG.isLoggable(Level.FINER)) {
    try {
    LOG.finer("Criteria to deserialize = " +
        XmlIoUtil.domToString(criteriaDom));
    } catch(Exception e) {}
  }
  
  try {
    if(criteriaDom == null) {
      throw new SearchException(
          "Document to load not recieved by search criteria (null recieved)");
    }
    Map<String, ISearchFilter> miscelleniousFiltersMap = this.getMappedFilters();
    SearchFiltersList miscelleniousFilters = new SearchFiltersList();
    SearchFiltersList essentialFilters = new SearchFiltersList();
    
    NodeList savedSearch =   criteriaDom.getElementsByTagName(ELEM_SAVEDSTATES);
    if(savedSearch != null && savedSearch.getLength() > 0) {
      NamedNodeMap attribs = savedSearch.item(0).getAttributes();
      if(attribs != null) {
        Node attribNode = attribs.getNamedItem(
            ATTRIB_SAVEDSEARCH_NAME.toString());
        if(attribNode != null) {
          this.setSavedSearchName(attribNode.getNodeValue());
        }
      }
    }

    NodeList filterList = criteriaDom.getElementsByTagName(TAG_FILTER);
    for(int i = 0; i < filterList.getLength(); i++) {
      Node filterNode = filterList.item(i);
      NamedNodeMap attribs = filterNode.getAttributes();
      if(attribs == null) {
        LOG.warning("While deserializing search criteria, Expected " 
            + TAG_FILTER + " attributes but got null");
        continue;
      }
      
      Node essentialClass = attribs.getNamedItem(TAG_FILTER_ESSENTIAL);
      boolean isEssentialFilter = false;
      if(essentialClass != null 
          && Val.chkBool(essentialClass.getNodeValue(), false)) {
        isEssentialFilter = true;
      }
      
      Node attribNode = attribs.getNamedItem(TAG_FILTER_CLASS);
      if(attribNode == null){
        LOG.warning("While deserializing search criteria, Expected " 
            + TAG_FILTER_CLASS + " attribute but got null");
        continue;
      }
      String filterClass = attribNode.getNodeValue();
      if(filterClass == null || filterClass.equals("")){
         LOG.warning("While deserializing search criteria, Expected " 
            + TAG_FILTER_CLASS + " value but got got null or empty");
        continue;
      }
      
      ISearchFilter filter = null;
      
      if(isEssentialFilter) {
        Class<?> cls = Class.forName(filterClass);
        filter = (ISearchFilter) cls.newInstance();
        essentialFilters.add(filter);
      } else {
        // Attempting to track an object in the miscellenious filters.
        // This part shows the importance that no miscellenious should be 
        // repeated
        filter = miscelleniousFiltersMap.get(filterClass);
        if(filter == null){
          try {
            Class<?> cls = Class.forName(filterClass);
            filter = (ISearchFilter) cls.newInstance();
          } catch(Exception e) {
            LOG.log(Level.WARNING, 
                "While deserializing search criteria, did not find filter " 
                + " object with class " + filterClass + " ", e);
            continue;
          }

        }
        miscelleniousFilters.add(filter);
      }
      
      
      NodeList params  = filterNode.getChildNodes();
      SearchParameterMap paramMap = new SearchParameterMap();
      for(int j = 0; j < params.getLength(); j++) {
        Node elmParam = params.item(j);
        if(!elmParam.getNodeName().equals(TAG_FILTER_PARAM)) {
          LOG.warning("While deserializing search criteria, Expected " 
              + " element "+ TAG_FILTER_PARAM + " but found " +
              elmParam.getNodeName());
          continue;
        }
        NamedNodeMap attrParam = elmParam.getAttributes();
        Node paramName = attrParam.getNamedItem(TAG_FILTER_PARAM_NAME);
        Node paramValue = attrParam.getNamedItem(TAG_FILTER_PARAM_VALUE);
        Node paramInfo = attrParam.getNamedItem(TAG_FILTER_PARAM_INFO);
        if(paramName != null && paramValue != null 
            && paramName.getNodeName() != null 
            && !"".equals(paramName.getNodeName())){
          
          if(paramInfo != null) {
          paramMap.put(paramName.getNodeValue(), 
              paramMap.new Value(paramValue.getNodeValue(), 
                  paramInfo.getNodeValue()));
          } else {
            paramMap.put(paramName.getNodeValue(), 
                paramMap.new Value(paramValue.getNodeValue()));
          }
        }
      }
      filter.setParams(paramMap);
      
    }
    this.hyrdateEssentialFilters(essentialFilters);
    this.hydrateMiscelleniousFilters(miscelleniousFilters);
    //this.setFilterObjects(deSerializedFilters);
   
  
  } catch(Exception e) {
    throw new SearchException("catalog.search.error.noLoadCriteria ", e);
  }


}

/**
 * Gets the search uri.  If no ISearchCriteria object was found in filter
 * list then the gpt.xml search url will be used
 * 
 * @return the search uri (never null if configured properly)
 * 
 * @throws SearchException the search exception
 */
public URI getSearchUri() throws SearchException {
  ISearchFilterURI searchURI = this.getSearchFilterUri();
  if(searchURI == null) {
    return SearchConfig.getConfiguredInstance().getSearchUri();
  }
  try {
    
    URI uri = searchURI.getSearchURI();
    return uri;
  } catch(Exception e) {
    throw new SearchException("Invalid search URI input", e);
  }
}


/**
 * String representation of object.
 * 
 * @return String representation of object
 */
@Override
public String toString() {
  
  StringBuffer string = new StringBuffer();
  string.append("\n { SearchCriteria class ");
  
  for(ISearchFilter filter: this.getEssentialFilters()) {
    string.append(filter.toString());
  }
  for(ISearchFilter filter: this.getMiscelleniousFilters()) {
    string.append(filter.toString());
  }
 
  string.append("\n}\n");
  return string.toString(); 
}

/**
 * Tests equality of object.  Relies on the method {@link #toDom()}
 * 
 * @param obj the obj
 * 
 * @return true if equals, otherwise false
 */
@Override
public boolean equals(Object obj) {
  if(!(obj instanceof SearchCriteria)) {
    return false;
  }
  SearchCriteria argCriteria = (SearchCriteria) obj;
  try {
 
    Map<String, ISearchFilter> map1 = this.getMappedFilters();
    Map<String, ISearchFilter> map2 = argCriteria.getMappedFilters();
    if(map1.size() != map2.size()) {
      return false;
    }
    for(Map.Entry<String, ISearchFilter> entry: map1.entrySet()) {
      ISearchFilter searchFilter1 = entry.getValue();
      ISearchFilter searchFilter2 = map2.get(entry.getKey());
      if(searchFilter2 == null || !(searchFilter2.equals(searchFilter1))) {
        return false;
      }
    }
    
    return this.getSearchUri().toString()
        .equals(argCriteria.getSearchUri().toString());
    
  } catch (SearchException e) {
    LOG.log(Level.SEVERE, "Error while performing equals in SearchCriteria" +
    		"object", e);
  }
  return false;
}

/**
 * Gets the clone.
 * 
 * @return the clone of <code>this</code>
 * 
 * @throws SearchException the search exception
 */
public SearchCriteria getClone() throws SearchException {

  SearchCriteria criteria = new SearchCriteria();
  criteria.loadSearchCriteria(this.toDom());
  return criteria;
  
}

/**
 * Write rest parameters.
 * 
 * @param map parameters map
 */
public void writeRestParameters(Map<String, String> map) {
  if(map == null) {
    return;
  }
  this.reset();
  Set<String> keys = map.keySet();
  Iterator<String> iter = keys.iterator();
  String key = null;
  String value = null;
  SearchParameterMap sParamMap = new SearchParameterMap();
  SearchParameterMap.Value sParamValue = null;
  while(iter != null && iter.hasNext()) {
    key = iter.next();
    value = map.get(key);
    sParamValue = sParamMap.new Value(value);
    sParamMap.put(key, sParamValue);
  }
  Map<String, ISearchFilter> filterMap = this.getMappedFilters();
  keys = filterMap.keySet();
  iter = keys.iterator();
  ISearchFilter filter = null;
  while(iter != null && iter.hasNext()) {
    try {
      key = iter.next();
      filter = filterMap.get(key);
      filter.setParams(sParamMap);
    } catch (Throwable e) {
      LOG.log(Level.WARNING, "Could not set a filter param ", e);
    }
  }
}


} 
