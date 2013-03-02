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

import java.io.File;
import java.util.logging.Logger;

import com.esri.gpt.framework.collection.StringAttributeMap;

/**
 * Performs file clean up operation
 * server communication.
 */
public class FileCleanup {

	//class variables =============================================================
	private static Logger LOGGER = Logger.getLogger(FileCleanup.class.getName());

	//instance variables ==========================================================	
	private String _folderPath = null;
	private String _filePrefix = null;
	private String _fileSuffix = null;
	private long _expirationTimeMinutes = 0;

	//constructors ================================================================

	/**
	 * Sets the file clean up parameters
	 * @param parameters the key,value parameter mapping      
	 */
	protected FileCleanup(StringAttributeMap parameters) {
		_folderPath = parameters.get("folderPath").getValue();
		_filePrefix = parameters.get("filePrefix").getValue();
		_fileSuffix = parameters.get("fileSuffix").getValue();
		_expirationTimeMinutes = Long.parseLong(parameters.get(
				"expirationTimeMinutes").getValue());

		if (_filePrefix == null || _filePrefix.trim().length() < 1) {
			LOGGER.severe(" Invalid filePrefix parameter for file clean up task");
		}

		if (_fileSuffix == null || _fileSuffix.trim().length() < 1) {
			LOGGER.severe(" Invalid fileSuffix parameter for file clean up task");
		}

		if ((_expirationTimeMinutes * 60 * 1000) < 1) {
			LOGGER
					.severe(" Invalid expirationTimeMinutes parameter for file clean up task");
		}
	}

	//methods =====================================================================
	/**
	 * Performs cleaning up task.
	 * @return boolean file cleanup status
	 */
	protected boolean cleanup() {

		if (_folderPath == null || _folderPath.trim().length() < 1) {
			LOGGER.severe(" Invalid folder path for file clean up task");
			return false;
		}

		File directory = new File(_folderPath);

		if (!directory.exists()) {
			LOGGER.severe(_folderPath + " does not exist");
			return false;
		}

		String[] directoryContents = directory.list();

		if (directoryContents == null) {
			LOGGER.info(" No files inside folder " + _folderPath);
			return false;
		} else {
			LOGGER.finer(" Count of files/directory inside folder is "
					+ directoryContents.length);
		}

		boolean cleanUpFlag = false;

		for (int i = 0; i < directoryContents.length; i++) {

			if (Thread.interrupted()) {
	      break;
	    }
			
			File file = new File(_folderPath + directory.separator
					+ directoryContents[i]);
			
			String fileName = file.getName().toUpperCase().trim();

			if (!file.isFile()
					|| !checkPrefix(fileName)
					|| !checkSuffix(fileName)
					|| (System.currentTimeMillis() - file.lastModified()) < (_expirationTimeMinutes * 60 * 1000))
				continue;

			LOGGER.finer(" Cleaning up file " + file.getPath());

			if (!file.delete())
				LOGGER.severe("Couldn't remove file " + file.getPath());
			else
				cleanUpFlag = true;

		}

		return cleanUpFlag;
	}

	/**
	 * Checks file name for prefix match
	 * @param fileName
	 * @return true if file name matches prefix    
	 */
	private boolean checkPrefix(String fileName) {

		String prefixes[] = _filePrefix.split(",");

		for (int i = 0; i < prefixes.length; i++) {
			
			if (Thread.interrupted()) {
	      break;
	    }
			
			if (fileName.startsWith(prefixes[i].toUpperCase()))
				return true;
		}

		return false;
	}

	/**
	 * Checks file name for suffix match
	 * @param fileName
	 * @return true if file name matches suffix    
	 */
	private boolean checkSuffix(String fileName) {

		String suffixes[] = _fileSuffix.split(",");

		for (int i = 0; i < suffixes.length; i++) {
			
			if (Thread.interrupted()) {
	      break;
	    }
			
			if (fileName.endsWith(suffixes[i].toUpperCase()))
				return true;
		}

		return false;
	}
	
	/**
	 * Returns the string representation of the object.
	 * @return the string
	 */
	@Override
	public String toString() {
	  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
	  sb.append(" folderPath=\"").append(_folderPath).append("\"");
	  sb.append(" filePrefix=\"").append(_filePrefix).append("\"");
	  sb.append(" fileSuffix=\"").append(_fileSuffix).append("\"");
	  sb.append(" expirationTimeMinutes=\"").append(_expirationTimeMinutes).append("\"");
	  sb.append(") ===== end ").append(getClass().getName());
	  return sb.toString();
	}


}
