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

import java.util.logging.Logger;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.LogUtil;

/**
 * Document cleanup background thread.
 * <p/>
 * This class is designed to be registered within scheduler facility.
 */
public class FileCleanupThread implements Runnable, IScheduledTask {

	// class variables =============================================================

	// instance variables ==========================================================
	private Logger _logger = LogUtil.getLogger();
	private StringAttributeMap _parameters = null;

	// constructors ================================================================

	// properties ==================================================================

	// methods =====================================================================

	/**
	 * Sets the parameters for File clean up
	 * @param parameters StringAttributeMap
	 */
	public void setParameters(StringAttributeMap parameters) {
		_parameters = parameters;
	}

	/**
	 * Performs cleaning up task
	 */
	public void run() {

		try {

			_logger.info("Performing file cleanup...");

			FileCleanup task = new FileCleanup(_parameters);

			// do cleanup task
			boolean status = task.cleanup();

			if (status)
				_logger.info("File cleanup completed.");
			else
				_logger.info("No files cleaned up.");

		} catch (Throwable t) {
			_logger.severe("Error cleaning up files: " + t.getMessage());
		}
	}

}
