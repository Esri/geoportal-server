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
package com.esri.gpt.catalog.harvest.clients.exceptions;

/**
 * Invalid response exception.
 * Thrown when received response from the remote harvest repository has invalid
 * format.
 */
public class HRInvalidResponseException extends HRConnectionException {

/**
 * Constructs an instance of <code>HRInvalidResponseException</code> with the 
 * specified detail message.
 * @param msg the detail message
 */
public HRInvalidResponseException(String msg) {
    super(msg);
}

/**
 * Constructs an instance of <code>HRInvalidResponseException</code> with the 
 * specified detail message and cause.
 * @param msg the detail message
 * @param t cause of the exceptio (stack trace)
 */
public HRInvalidResponseException(String msg, Throwable t) {
    super(msg, t);
}

}
