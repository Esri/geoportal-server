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
package com.esri.gpt.control.rest.writer;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import com.esri.gpt.control.rest.repositories.RepositoriesResultSet;
import com.esri.gpt.control.rest.repositories.RepositoriesResultSetWrapper;

import com.esri.gpt.framework.util.Val;

/**
 * Super-class for a rest response writer based upon a JDBC ResultSet.
 */
public abstract class ResultSetWriter extends ResponseWriter {
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with an underlying response writer.
   * @param underlyingWriter the underlying writer
   */
  public ResultSetWriter(Writer underlyingWriter) {
    super(underlyingWriter);
  }
  
  /** methods ================================================================= */
  
  /**
   * Ends a row.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  public abstract void endRow(int depth) throws IOException;
  
  /**
   * Ends a collection of rows.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  public abstract void endRows(int depth) throws IOException;
  
  /**
   * Starts a row.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  public abstract void startRow(int depth) throws IOException;
  
  /**
   * Starts a collection of rows.
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  public abstract void startRows(int depth) throws IOException;
  
  /**
   * Writes a cell property value.
   * @param name the property name
   * @param value the property value
   * @param depth the indent depth
   * @throws IOException if an I/O exception occurs
   */
  public abstract void writeCell(String name, Object value, int depth) throws IOException;
  
  /**
   * Writes a ResultSet to the response.
   * 
   * 
   * 
   * @param rs the ResultSet
   * @param depth the indent depth
   * @param columnTags optional, the tag names per column
   * @throws IOException if an I/O exception occurs
   * @throws SQLException if an SQL exception occurs
   */
  public void writeResultSet(ResultSet rs, int depth, String[] columnTags) 
      throws IOException, SQLException {
    if(rs instanceof RepositoriesResultSet) {
      writeResultSet1(rs, depth, columnTags);
      return;
    }
    this.startRows(depth);
    ResultSetMetaData md = rs.getMetaData();
    int nColumns = md.getColumnCount();
    while (rs.next()) {
      this.startRow(depth+1);
      for (int i=1;i<=nColumns;i++) {
        String name = md.getColumnName(i);
        if (columnTags != null) {
          name = columnTags[i-1];
        }
        this.writeCell(name,rs.getObject(i),depth+2);
      }
      this.endRow(depth+1); 
    }
    this.endRows(depth);
  }
  /**
   * Writes a ResultSet to the response.
   * 
   * 1.2.5 changed to combine values with gpt.xml.
   * 
   * @param rs the ResultSet
   * @param depth the indent depth
   * @param columnTags optional, the tag names per column
   * @throws IOException if an I/O exception occurs
   * @throws SQLException if an SQL exception occurs
   */
  private void writeResultSet1(ResultSet rs, int depth, String[] columnTags) 
    throws IOException, SQLException {
    //this.startRows(depth);
    
    Map<String, LinkedList<RecordElement>> mapIds = 
        new TreeMap<String, LinkedList<RecordElement>>(String.CASE_INSENSITIVE_ORDER);
        
    LinkedList<RecordElement> repLocal = null;
    LinkedList<RecordElement> repAgs = null;
    
    while (rs.next()) {
      ResultSetMetaData md = rs.getMetaData();// T.M. needed for ResultSetWrapper
      int nColumns = md.getColumnCount();
      //this.startRow(depth+1);
      
      LinkedList<RecordElement> lre = 
          new LinkedList<ResultSetWriter.RecordElement>();
      String repositoryName = null;
      String repositoryId = null;
      
      for (int i=1;i<=nColumns;i++) {
        String name = md.getColumnName(i);
        boolean isIteratingDb = true;
        if (rs instanceof RepositoriesResultSetWrapper) {
          RepositoriesResultSetWrapper reposRs = (RepositoriesResultSetWrapper) rs;
          isIteratingDb = reposRs == null
              || (reposRs != null && reposRs.isDbFinishedIterating() == false);
        }
        
        if (isIteratingDb && columnTags != null) {
          name = columnTags[i-1];
        } 
        if(Val.chkStr(name).toLowerCase().equals("id")) {
          repositoryId = rs.getObject(i).toString();        
        }
        if(Val.chkStr(name).toLowerCase().equals("name")) {
          repositoryName = rs.getObject(i).toString();
        }
        RecordElement re = 
            new RecordElement(name, rs.getObject(i), depth+2);
        lre.push(re);
        //this.writeCell(name,rs.getObject(i),depth+2);
      }
      if(nColumns < 1) {
        // ignore
      } else if(Val.chkStr(repositoryId).equals("local")) {
        repLocal = lre;
      } else if(Val.chkStr(repositoryId).equals("arcgis.com")) {
        repAgs = lre;
        mapIds.put(repositoryName, lre);
      } else if(repositoryName != null) {
        mapIds.put(repositoryName, lre);
      }
      //this.endRow(depth+1); 
    }
    //this.endRows(depth);
    
    
    this.startRows(depth);
    
    if(repLocal != null) { 
      mapIds.remove("local");
      writeResultSetRecord(repLocal, depth);
    }
      
    if(repAgs != null) {
      //mapIds.remove("arcgis.com");
      //writeResultSetRecord(repAgs, depth);
    }
    
        
    Iterator<String> iter =  mapIds.keySet().iterator();
    while(iter.hasNext()) {
      String key = iter.next();
      LinkedList<RecordElement> rep = mapIds.get(key);
      writeResultSetRecord(rep, depth);
      
    }
    this.endRows(depth);
  }
  
  private void writeResultSetRecord(LinkedList<RecordElement> lre, int depth) 
      throws IOException {
    
    this.startRow(depth+1);
    Iterator<RecordElement> iter = lre.iterator();
    while(iter.hasNext()) {
      RecordElement re = iter.next();
      this.writeCell(re.name,re.object,depth+2);
    }
    this.endRow(depth+1);
    
  }
  
  private class RecordElement {
    private String name;
    private Object object;
    private int depth;
    private RecordElement(String name, Object obj, int depth) {
      this.name = name;
      this.object = obj;
      this.depth = depth;
    }
  }

}
