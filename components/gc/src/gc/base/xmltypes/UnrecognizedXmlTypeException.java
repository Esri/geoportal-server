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
package gc.base.xmltypes;

/**
 * Thrown during XML interrogation when an unrecognized type is
 * encountered.
 */
@SuppressWarnings("serial")
public class UnrecognizedXmlTypeException extends Exception {

	/** Default constructor. */
	public UnrecognizedXmlTypeException() {
		super();
	}
	
	/**
	 * Construct based upon an error message.
	 * @param msg the error message
	 */
	public UnrecognizedXmlTypeException(String msg) {
		super(msg);
	}

	/**
	 * Construct based upon an error message and a cause.
	 * @param msg the error message
	 * @param cause the cause
	 */
	public UnrecognizedXmlTypeException(String msg, Throwable cause) {
		super(msg,cause);
	}

	/**
	 * Construct based upon a cause.
	 * @param cause the cause
	 */
	public UnrecognizedXmlTypeException(Throwable cause) {
		super(cause);
	}

}
