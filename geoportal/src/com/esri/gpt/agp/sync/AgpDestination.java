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
package com.esri.gpt.agp.sync;
import com.esri.gpt.agp.client.AgpConnection;

/**
 * The destination Portal for the synchronization push.
 */
public class AgpDestination {
  
  /** instance variables ====================================================== */
  private AgpConnection connection;
  private String        destinationFolderID;
  private String        destinationOwner;
     
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpDestination() {
    super();
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the connection.
   * @return the connection
   */
  public AgpConnection getConnection() {
    return this.connection;
  }
  /**
   * Sets the connection.
   * @param connection the connection
   */
  public void setConnection(AgpConnection connection) {
    this.connection = connection;
  }
  
  /**
   * Gets the destination folder ID.
   * @return the folder ID
   */
  public String getDestinationFolderID() {
    return this.destinationFolderID;
  }
  /**
   * Sets the destination folder ID.
   * @param destinationFolderID the destination folder ID
   */
  public void setDestinationFolderID(String destinationFolderID) {
    this.destinationFolderID = destinationFolderID;
  }
  
  /**
   * Gets the destination owner.
   * @return the destination owner
   */
  public String getDestinationOwner() {
    return this.destinationOwner;
  }
  /**
   * Sets the destination owner.
   * @param destinationOwner the destination owner
   */
  public void setDestinationOwner(String destinationOwner) {
    this.destinationOwner = destinationOwner;
  }
  
}