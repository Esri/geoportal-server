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
package com.esri.gpt.server.usage.harvester;

/**
 * This class is used to contain counts by harvesting protocol.
 * @author prat5814
 *
 */
public class ProtocolInfo {
	
	private int approvedCount = 0;
	private int registeredCount = 0;
	private int onScheduleCount = 0;
	private int documentCount = 0;
	
	/**
	 * Gets approved sites count
	 * @return count of approved records
	 */
	public int getApprovedCount() {
		return approvedCount;
	}
	/**
	 * Sets approved sites count
	 * @param approvedCount count of approved records
	 */
	public void setApprovedCount(int approvedCount) {
		this.approvedCount = approvedCount;
	}
	/**
	 * Gets registered sites count
	 * @return count of registered records
	 */
	public int getRegisteredCount() {
		return registeredCount;
	}
	/**
	 * Sets registered sites count
	 * @param registeredCount count of registered records
	 */
	public void setRegisteredCount(int registeredCount) {
		this.registeredCount = registeredCount;
	}
	/**
	 * Gets sites on a schedule count
	 * @return count of scheduled records
	 */
	public int getOnScheduleCount() {
		return onScheduleCount;
	}
    /**
     * Sets sites on a schedule count
     * @param onScheduleCount count of scheduled records
     */
	public void setOnScheduleCount(int onScheduleCount) {
		this.onScheduleCount = onScheduleCount;
	}
	/**
	 * Gets documents count
	 * @return count of documents
	 */
	public int getDocumentCount() {
		return documentCount;
	}
	/**
	 * Sets documents count
	 * @param documentCount count of documents
	 */
	public void setDocumentCount(int documentCount) {
		this.documentCount = documentCount;
	}
	
	
}
