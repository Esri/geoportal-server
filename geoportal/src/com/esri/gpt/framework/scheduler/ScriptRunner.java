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
package com.esri.gpt.framework.scheduler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.Val;

/**
 * Runs a script.
 */
public class ScriptRunner implements Runnable, IScheduledTask {

  /** class variables ========================================================= */
  private static final Logger LOGGER = Logger.getLogger(ScriptRunner.class.getName());
  /** instance variables ====================================================== */
  private StringAttributeMap parameters;

  /** constructors  =========================================================== */
  /** Default constructor. */
  public ScriptRunner() {
  }

  /** properties  ============================================================= */
	@Override
	public void setParameters(StringAttributeMap parameters) {
		this.parameters = parameters;
	}
	
	/**
   * Gets thread parameter.
   * @param name parameter name
   * @return parameter value
   */
  private String getParameter(String name) {
    return parameters != null ? Val.chkStr(parameters.getValue(name)) : "";
  }

  /** methods ================================================================= */
  /**
   * Run the synchronization process.
   */
	@Override
	public void run() {
		try {
      LOGGER.info("Starting script runner.");
      Process proc = Runtime.getRuntime().exec(getParameter("script"));
      BufferedReader stdInput = new BufferedReader(new 
          InputStreamReader(proc.getInputStream()));

     BufferedReader stdError = new BufferedReader(new 
          InputStreamReader(proc.getErrorStream()));

     String str;
     // read the output
     while ((str = stdInput.readLine()) != null) {
    	 LOGGER.log(Level.INFO,str);
     }

     // read any errors
     while ((str = stdError.readLine()) != null) {
    	 LOGGER.log(Level.SEVERE,str);
     }

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error in script runner.", e);
    } finally {
      LOGGER.info("Script runner completed.");
    }
		
	}

}
