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
package com.esri.gpt.server.assertion.components;
import com.esri.gpt.framework.collection.CaseInsensitiveMap;
import com.esri.gpt.framework.util.Val;
import java.util.Collection;
import java.util.StringTokenizer;

/**
 * Holds a domain values supported by an assertion operation.
 */
public class AsnSupportedValues {
  
  /** instance variables ====================================================== */
  private CaseInsensitiveMap<String> supported = new CaseInsensitiveMap<String>(false);

  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnSupportedValues() {}
  
  /**
   * Constructs the set of supported values from a delimited string.
   * @param tokens the delimited string to tokenize
   * @param delimiter the delimiter
   */
  public AsnSupportedValues(String tokens, String delimiter) {
    tokens = Val.chkStr(tokens);
    if (delimiter == null) {
      this.supported.put(tokens,tokens);
    } else {
      StringTokenizer st = new StringTokenizer(tokens,delimiter);
      while (st.hasMoreElements()) {
        String token = Val.chkStr((String)st.nextElement());
        this.supported.put(token,token);
      }
    }
  }
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnSupportedValues(AsnSupportedValues objectToDuplicate) {
    if (objectToDuplicate != null) {
      Collection<String> values = objectToDuplicate.values();
      if (values != null) {
        for (String value: values) {
          this.add(value);
        }
      }
    }
  }
  
  /** methods ================================================================= */
  
  /**
   * Adds a supported value to the domain.
   * @param value the value to add
   */
  public void add(String value) {
    this.supported.put(value,value);
  }
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnSupportedValues(this);
   * @return the duplicated object
   */
  public AsnSupportedValues duplicate() {
    return new AsnSupportedValues(this);
  }
  
  /**
   * Gets the supported value associated with a requested value.
   * @param requestedValue the requested value
   * @return the supported value (null if unsupported)
   */
  public String getSupportedValue(String requestedValue) {
    return this.supported.get(requestedValue);
  }
  
  /**
   * Determines if a requested value is supported.
   * @param requestedValue the requested value
   * @return <code>true</code> if the value is supported
   */
  public boolean isValueSupported(String requestedValue) {
    return this.supported.containsKey(requestedValue);
  }
  
  /**
   * Returns the collection of values.
   * @return the supported values
   */
  public Collection<String> values() {
    return this.supported.values();
  }

}
