import java.util.LinkedList;
import java.util.List;

import com.esri.gpt.catalog.search.ISearchFilter; 
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.catalog.search.SearchParameterMap; 
import com.esri.gpt.catalog.search.SearchParameterMap.Value;
import com.esri.gpt.framework.util.Val;
@SuppressWarnings("serial") 
public class SearchFilterHierarchy implements ISearchFilter { 

// key to be used to serialize class to a map 
private static String KEY_hierarchy = "apiso.Type"; 

// instance variable 
private String hierarchy;

@SuppressWarnings("rawtypes")
private List selectedHierachy;

/**
 * Gets the selected theme.
 * 
 * @return the selected theme
 */
@SuppressWarnings("unchecked")
public List getSelectedHierachy() {
  if(selectedHierachy == null) {
    selectedHierachy = new LinkedList();
  }
  return selectedHierachy;
}

/**
 * Sets the selected theme.
 * 
 * @param selectedHierachy the new selected theme
 */
@SuppressWarnings("unchecked")
public void setSelectedHierachy(List selectedHierachy) {
  this.selectedHierachy = selectedHierachy;
}

// property (Can be used by jsf(advanced search page) 
public String getHierarchy() { 
return Val.chkStr(hierarchy);
} 
// property (Can be used by jsf(advanced search page) 
public void setHierarchy(String hierarchy) { 
this.hierarchy = hierarchy; 
} 
// Serialize class instance into a map 
public SearchParameterMap getParams() { 
SearchParameterMap map = new SearchParameterMap(); 
map.put(KEY_hierarchy, map.new Value(this.getHierarchy(), ""));
return map;
}
// The class may receive a new map for deserialization (e.g. saved searches 
// can trigger this 
public void setParams(SearchParameterMap parameterMap) throws SearchException { 
Value val = parameterMap.get(KEY_hierarchy);
this.setHierarchy(val.getParamValue()); 
}
// Deep comparison of filters 
public boolean isEquals(Object obj) {
if (obj instanceof SearchFilterHierarchy) { 
return ((SearchFilterHierarchy) obj).getHierarchy().equals(this.getHierarchy()); 
} 
return false; 
} 
// This will be called by the clear button 
public void reset() { 
this.setHierarchy(""); 
} 
// Before search, validate will be called. An exception can be thrown 
// that will stop the search and the error is displayed on the search page 
public void validate() throws SearchException {
if (this.getHierarchy().equals("this should throw an exception")) {
throw new SearchException("this should throw an exception");
} 
} 
}