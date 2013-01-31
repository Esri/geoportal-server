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
package com.esri.gpt.server.assertion.index;
import com.esri.gpt.server.assertion.components.AsnContext;
import org.apache.lucene.document.Document;

/**
 * An assertion that can be indexed as a Lucene document.
 */
public class Assertion {
  
  /** instance variables ====================================================== */
  private AsnRdfPart    rdfPart = new AsnRdfPart();
  private AsnSystemPart systemPart = new AsnSystemPart();
  private AsnUserPart   userPart = new AsnUserPart();
  private boolean       wasReadFromIndex = false;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public Assertion() {} 
 
  /** properties ============================================================== */
  
  /**
   * Gets the part representing RDF fields associated with a assertion.
   * @return the RDF part
   */
  public AsnRdfPart getRdfPart() {
    return this.rdfPart;
  }
  /**
   * Sets the part representing RDF fields associated with a assertion.
   * @param rdfPart the RDF part
   */
  public void setRdfPart(AsnRdfPart rdfPart) {
    this.rdfPart = rdfPart;
  } 
  
  /**
   * Gets the part representing system fields associated with a assertion.
   * @return the system part
   */
  public AsnSystemPart getSystemPart() {
    return this.systemPart;
  }
  /**
   * Sets the part representing system fields associated with a assertion.
   * @param systemPart the system part
   */
  public void setSystemPart(AsnSystemPart systemPart) {
    this.systemPart = systemPart;
  } 
  
  /**
   * Gets the part representing user fields associated with a assertion.
   * @return the user part
   */
  public AsnUserPart getUserPart() {
    return this.userPart;
  }
  /**
   * Sets the part representing user fields associated with a assertion.
   * @param userPart the user part
   */
  public void setUserPart(AsnUserPart userPart) {
    this.userPart = userPart;
  } 
  
  /**
   * Gets the flag indicating whether or not this is an existing assertion 
   * read from an index.
   * @return <code>true</code> if existing and read from an index
   */
  public boolean getWasReadFromIndex() {
    return this.wasReadFromIndex;
  }
  /**
   * Sets the flag indicating whether or not this is an existing assertion 
   * read from an index.
   * @param wasRead <code>true</code> if existing and read from an index
   */
  public void setWasReadFromIndex(boolean wasRead) {
    this.wasReadFromIndex = wasRead;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   * @param depth the depth of the parent
   */
  public void echo(StringBuffer sb, int depth) {
    String pfx = "\n";
    for (int i=0;i<2*depth;i++) pfx += " ";
    sb.append(pfx).append(getClass().getSimpleName()).append(":");
    sb.append(pfx).append("  wasReadFromIndex=").append(this.getWasReadFromIndex());
    this.getSystemPart().echo(sb,depth+1);
    this.getUserPart().echo(sb,depth+1);
    this.getRdfPart().echo(sb,depth+1);
  }

  /**
   * Loads the assertion from based upon a previously indexed Lucene document.
   * @param document the previously indexed Lucene document
   */
  public void load(Document document) {
    this.getSystemPart().readFields(document);
    this.getUserPart().readFields(document);
    this.getRdfPart().readFields(document);
    this.setWasReadFromIndex(true);
  }
  
  /**
   * Makes a document suitable for writing to a Lucene index.
   * @param context the assertion operation context
   * @return the indexable document
   */
  public Document makeWritableDocument(AsnContext context) {
    Document document = new Document();
    this.getSystemPart().appendWritableFields(document);
    this.getUserPart().appendWritableFields(document);
    this.getRdfPart().appendWritableFields(document);
    return document;
  }

  /**
   * Returns a string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    this.echo(sb,0);
    return sb.toString();
  }
  
}
