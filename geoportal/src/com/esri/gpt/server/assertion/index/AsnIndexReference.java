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
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.components.AsnContext;

/**
 * Represents a configuration reference to a Lucene based assertion index.
 */
public class AsnIndexReference {

  /** instance variables ====================================================== */
  private boolean enabled = true;
  private String  indexAdapterClass;
  private String  indexLocation;
  private String  name;
  private boolean useNativeFSLockFactory = true;
  private int     writeLockTimeout = 0;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnIndexReference() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnIndexReference(AsnIndexReference objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setEnabled(objectToDuplicate.getEnabled());
      this.setIndexAdatperClass(objectToDuplicate.getIndexAdapterClass());
      this.setIndexLocation(objectToDuplicate.getIndexLocation());
      this.setName(objectToDuplicate.getName());
      this.setUseNativeFSLockFactory(objectToDuplicate.getUseNativeFSLockFactory());
      this.setWriteLockTimeout(objectToDuplicate.getWriteLockTimeout());
    } 
  }
    
  /** properties ============================================================== */

  /**
   * Gets the status indicating whether or not the index is enabled.
   * @return <code>true</code> if enabled
   */
  public boolean getEnabled() {
    return this.enabled;
  }
  /**
   * Sets the status indicating whether or not the index is enabled.
   * @param enabled <code>true</code> if enabled
   */
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  /**
   * Gets the fully qualified class name of the index adapter.
   * <br/>(must extend AsnIndexAdapter)
   * @return the index adapter class 
   */
  public String getIndexAdapterClass() {
    return this.indexAdapterClass;
  }
  /**
   * Sets the fully qualified class name of the index adapter.
   * <br/>(must extend AsnIndexAdapter)
   * @param indexAdapterClass the index adapter class 
   */
  public void setIndexAdatperClass(String indexAdapterClass) {
    this.indexAdapterClass = indexAdapterClass;
  }
  
  /**
   * Gets the location of the index folder (system path).
   * @return the location
   */
  public String getIndexLocation() {
    return this.indexLocation;
  }
  /**
   * Sets the location of the index folder (system path).
   * @param location the location
   */
  public void setIndexLocation(String location) {
    this.indexLocation = location;
  }
  /**
   * Sets the location of the index folder (system path).
   * @param rootIndexRef the index reference associated with the assertion root
   * @param name the index folder name
   * @param location the location
   */
  public void setIndexLocation(AsnIndexReference rootIndexRef, String name, String location) {
    location = Val.chkStr(location);
    this.setName(name);
    if (location.length() > 0) {
      this.setIndexLocation(location);
    } else {
      this.setIndexLocation(rootIndexRef.getIndexLocation()+"/"+name);
    }
  }
  
  /**
   * Gets the name associated with the index.
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  /**
   * Sets the name associated with the index.
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Gets the flag indicating if a NativeFSLockFactory should be used.
   * @return true if a NativeFSLockFactory should be used, otherwise use a SimpleFSLockFactory
   */
  public boolean getUseNativeFSLockFactory() {
    return this.useNativeFSLockFactory;
  }
  /**
   * Sets the flag indicating if a NativeFSLockFactory should be used.
   * @param useNative true if a NativeFSLockFactory should be used, otherwise use a SimpleFSLockFactory 
   */
  public void setUseNativeFSLockFactory(boolean useNative) {
    this.useNativeFSLockFactory = useNative;
  }
  
  /**
   * Gets the write lock timeout in milli-seconds.
   * @return the write lock timeout 
   */
  public int getWriteLockTimeout() {
    return this.writeLockTimeout;
  }
  /**
   * Sets the write lock timeout in milli-seconds.
   * <br/>If the timeout is less than zero, the timeout will be set to 60000.
   * @param millis the write lock timeout  the write lock timeout 
   */
  public void setWriteLockTimeout(int millis) {
    this.writeLockTimeout = millis;
    if (this.writeLockTimeout < 0) this.writeLockTimeout = 60000;
  }
  
  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnIndexRef(this);
   * @return the duplicated object
   */
  public AsnIndexReference duplicate() {
    return new AsnIndexReference(this);
  }
  
  /**
   * Makes an index adapter based upon the associated indexAdapterClass.
   * @param context the assertion operation context
   * @return the index adapter
   * @throws ClassNotFoundException if the class was not found
   * @throws InstantiationException if the class could not be instantiated
   * @throws IllegalAccessException if the class could not be accessed
   */
  public AsnIndexAdapter makeIndexAdapter(AsnContext context) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    if (!this.getEnabled()) {
      String msg = "The index is not enabled: "+this.getName();
      throw new ConfigurationException(msg);
    }
    String className = Val.chkStr(this.getIndexAdapterClass());
    if ((className.length() == 0) || className.equals(AsnIndexAdapter.class.getName())) {
      AsnIndexAdapter broker = new AsnIndexAdapter();
      broker.configure(this);
      return broker;
    } else {
      Class<?> cls = Class.forName(className);
      Object obj = cls.newInstance();
      if (obj instanceof AsnIndexAdapter) {
        AsnIndexAdapter broker = (AsnIndexAdapter)obj;
        broker.configure(this);
        return broker;
      } else {
        String msg = "The configured indexAdapterClass is invalid: "+ className;
        throw new ConfigurationException(msg);
      }
    }
  }
  
}
