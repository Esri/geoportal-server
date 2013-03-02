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
package com.esri.gpt.catalog.harvest.clients;

import com.esri.gpt.catalog.harvest.clients.exceptions.HRConnectionException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import java.util.ArrayList;

/**
 * Harvest repository client.
 * Provides unified way to communicate with the remote repository.
 */
public interface HRClient {

/**
 * Checks if <i>ping</i> operation is supported.
 * @return <code>true</code> if <i>ping</i> operation is supported
 * @see HRClient#ping
 */  
boolean isPingSupported();

/**
 * Checks and verifies connection to the remote repository.
 * @throws HRInvalidProtocolException if provided connection 
 * definition is incomplete
 * @throws HRConnectionException if connection to the remote repository can not
 * be established at this moment
 * @throws UnsupportedOperationException if operation is not supported
 */  
void ping() 
  throws HRInvalidProtocolException,
         HRConnectionException,
         UnsupportedOperationException;  
}
