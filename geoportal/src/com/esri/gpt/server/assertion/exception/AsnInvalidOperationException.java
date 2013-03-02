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
package com.esri.gpt.server.assertion.exception;
import com.esri.gpt.framework.context.ApplicationException;

/**
 * Thrown when a requested operation is invalid.
 */
@SuppressWarnings("serial")
public class AsnInvalidOperationException extends ApplicationException {
  
  /** Default constructor */
  public AsnInvalidOperationException() {
    this("Invalid operation: subject/perdicate inconsistency.");
  }

  /**
   * Construct based upon an error message.
   * @param msg the error message
   */
  public AsnInvalidOperationException(String msg) {
    super(msg);
  }

}
