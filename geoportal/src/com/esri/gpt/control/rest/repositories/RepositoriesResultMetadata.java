package com.esri.gpt.control.rest.repositories;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class RepositoriesResultMetadata implements ResultSetMetaData {

private LinkedHashMap<String, Map<String, String>> _repos;

private Entry<String, Map<String, String>> _entrySet;

public RepositoriesResultMetadata(LinkedHashMap<String, Map<String, String>> repos, int index) {
  this._repos = repos;
  this._entrySet = (Entry<String, Map<String, String>>) repos.entrySet().toArray()[index];
}

private Entry<String, Map<String, String>> getEntrySet() {
  return this._entrySet;
}


@Override
public <T> T unwrap(Class<T> iface) throws SQLException {
  // TODO Auto-generated method stub
  return null;
}

@Override
public boolean isWrapperFor(Class<?> iface) throws SQLException {
  // TODO Auto-generated method stub
  return false;
}

@Override
public int getColumnCount() throws SQLException {
  return this.getEntrySet().getValue().size();
}

@Override
public boolean isAutoIncrement(int column) throws SQLException {
   return false;
}

@Override
public boolean isCaseSensitive(int column) throws SQLException {
  return false;
}

@Override
public boolean isSearchable(int column) throws SQLException {
  return false;
}

@Override
public boolean isCurrency(int column) throws SQLException {
  return false;
}

@Override
public int isNullable(int column) throws SQLException {
  return 0;
}

@Override
public boolean isSigned(int column) throws SQLException {
  return false;
}

@Override
public int getColumnDisplaySize(int column) throws SQLException {
   return 0;
}

private String getEntryName(int column) {
 
  column--;
  if(column < this.getEntrySet().getValue().keySet().toArray().length )
      return this.getEntrySet().getValue().keySet().toArray()[column].toString();
  return "";
}

@Override
public String getColumnLabel(int column) throws SQLException {
  return getEntryName(column);
}

@Override
public String getColumnName(int column) throws SQLException {
  return getEntryName(column);
}

@Override
public String getSchemaName(int column) throws SQLException {
  return getEntryName(column);
}

@Override
public int getPrecision(int column) throws SQLException {
   return 0;
}

@Override
public int getScale(int column) throws SQLException {
   return 0;
}

@Override
public String getTableName(int column) throws SQLException {
  return "";
}

@Override
public String getCatalogName(int column) throws SQLException {
   return "";
}

@Override
public int getColumnType(int column) throws SQLException {
  
  return 0;
}

@Override
public String getColumnTypeName(int column) throws SQLException {
  // TODO Auto-generated method stub
  return null;
}

@Override
public boolean isReadOnly(int column) throws SQLException {
  return true;
}

@Override
public boolean isWritable(int column) throws SQLException {
  return false;
}

@Override
public boolean isDefinitelyWritable(int column) throws SQLException {
  return false;
}

@Override
public String getColumnClassName(int column) throws SQLException {
  // TODO Auto-generated method stub
  return null;
}

}
