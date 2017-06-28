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
package com.esri.gpt.control.webharvest;

import com.esri.gpt.framework.robots.Access;
import java.io.IOException;

/**
 * Access exception.
 */
public class AccessException extends IOException {
  private Access access;

  /**
   * Constructs an instance of <code>AccessException</code> with the specified
   * detail message.
   *
   * @param msg the detail message.
   * @param access access
   */
  public AccessException(String msg, Access access) {
    super(msg);
    this.access = access;
  }

  /**
   * Constructs an instance of <code>AccessException</code> with the specified
   * detail message.
   *
   * @param msg the detail message.
   * @param cause cause
   */
  public AccessException(String msg, Throwable cause) {
    super(msg, cause);
  }

  /**
   * Gets access.
   * @return access or <code>null</code> if access information unavailable
   */
  public Access getAccess() {
    return access;
  }
}
