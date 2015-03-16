/*
 * Copyright 2015 Esri, Inc..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.cart;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * "/try" response
 * @author Esri, Inc.
 */
public class TryResponse {
  private final ArrayList<String> accepted = new ArrayList<String>();
  private final ArrayList<String> rejected = new ArrayList<String>();

  /**
   * Gets collection of accepted keys.
   * @return collection of accepted keys (read only)
   */
  public List<String> getAccepted() {
    return Collections.unmodifiableList(accepted);
  }

  /**
   * Gets collection of rejected keys.
   * @return collection of rejected keys (read only)
   */
  public List<String> getRejected() {
    return Collections.unmodifiableList(rejected);
  }
  
  /**
   * Accepts a key.
   * @param key key
   */
  public void accept(String key) {
    rejected.remove(key);
    accepted.add(key);
  }
  
  /**
   * Rejects a key.
   * @param key key
   */
  public void reject(String key) {
    accepted.remove(key);
    rejected.add(key);
  }
  
  /**
   * Addes a key.
   * @param key key
   * @param accept <code>true</code> to accept, <code>false</code> to reject
   */
  public void add(String key, boolean accept) {
    if (accept)
      accept(key);
    else
      reject(key);
  }
}
