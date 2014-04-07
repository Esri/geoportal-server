package com.esri.gpt.control.rest.repositories;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.esri.gpt.catalog.search.SearchConfig;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

/**
 * @author TM
 * 
 * The Class RepositoriesResultSetWrapper.
 * 
 * Generates csw repository records from the database and appends gpt.xml 
 * repositories
 */
public class RepositoriesResultSetWrapper extends RepositoriesResultSet {


// instance variables ==========================================================
/** The _rs. */
private ResultSet _rs;

/** The _next iter. */
private int _nextIter = -1;

/** The _search repos. */
private  LinkedHashMap<String, Map<String, String>> _searchRepos;

/** The repositories result metadata. */
private RepositoriesResultMetadata repositoriesResultMetadata;

// constructors ================================================================
/**
 * Instantiates a new repositories result set wrapper.
 *
 * @param rs the rs
 */
public RepositoriesResultSetWrapper(ResultSet rs) {
  this._rs = rs;
  
  boolean allowExt = SearchConfig.getConfiguredInstance().getAllowExternalSearch();
 
  _searchRepos = new  LinkedHashMap<String, Map<String, String>>();
  
 
  LinkedHashMap<String, Map<String, String>> tmpSearchRepos = 
      SearchConfig.getConfiguredInstance().getSearchFactoryRepos();
  Iterator<String>iter = tmpSearchRepos.keySet().iterator();
  while(iter.hasNext()) {
    String key = Val.chkStr(iter.next());
    if(key.equals("")) {
      continue;
    }
    Map<String, String> params = tmpSearchRepos.get(key);
    Iterator<String> keyParamsIter = params.keySet().iterator();
    while(keyParamsIter.hasNext()) {
      String paramsKey = keyParamsIter.next();
      if(paramsKey.equals("labelResourceKey") && 
         !Val.chkStr(params.get(paramsKey)).equals("")) {
        
        Map<String, String> params1 = new LinkedHashMap<String, String>();
        Iterator<String> iter1 = params.keySet().iterator();
        while(iter1.hasNext()) {
          String key1 = iter1.next();
          if(key1.toLowerCase().equals(("key"))) {
            params1.put("id", params.get(key1));
          } else if (key1.toLowerCase().equals(("labelresourcekey"))) {
            MessageBroker messageBroker = new MessageBroker();
            messageBroker.setBundleBaseName("gpt.resources.gpt");
            params1.put("name",
                messageBroker.retrieveMessage(params.get(key1)));
          }
        }
        if(allowExt || key.toLowerCase().equals("local"))
          _searchRepos.put(key, params1);
        break;
      }
          
    }
  }
  
}

// private methods =============================================================
/**
 * Gets the search repos.
 *
 * @return the search repos
 */
private LinkedHashMap<String, Map<String, String>> getSearchRepos() {
  return _searchRepos;
}

// public methods ==============================================================

/* (non-Javadoc)
 * @see com.esri.gpt.control.rest.repositories.RepositoriesResultSet#getMetaData()
 */
@Override
public ResultSetMetaData getMetaData() throws SQLException {
  if (_nextIter < 0) {
    return this._rs.getMetaData();
  } else {
 
    return new RepositoriesResultMetadata(this.getSearchRepos(), _nextIter);
  
  }
}



/* (non-Javadoc)
 * @see com.esri.gpt.control.rest.repositories.RepositoriesResultSet#next()
 */
public boolean next() throws SQLException {
  boolean rsNext = _rs.next();
  if(rsNext) {
    return true;
  } else {
    _nextIter++;
    return _nextIter < getSearchRepos().size();
    
  }

}

public boolean isDbFinishedIterating() {
  return _nextIter >= 0;
}

/* (non-Javadoc)
 * @see com.esri.gpt.control.rest.repositories.RepositoriesResultSet#getObject(int)
 */
public Object getObject(int i) throws SQLException {
  if (_nextIter < 0) {
    return this._rs.getObject(i);
  } else {
    i = i -1;
    String key = this.getSearchRepos().keySet().toArray()[_nextIter].toString();
    return this.getSearchRepos().get(key).values().toArray()[i];
    
  }
}

}
