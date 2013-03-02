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
package com.esri.gpt.control.webharvest.engine;

import java.util.Date;

/**
 * Statistics.
 */
public interface Statistics {

/**
 * Gets number of harvested documents.
 * @return number of harvested documents
 */
long getHarvestedCount();

/**
 * Gets number of validated documents.
 * @return number of validated documents
 */
long getValidatedCount();

/**
 * Gets number of added documents.
 * @return number of added documents
 */
long getAddedCount();

/**
 * Gets number of modified documents.
 * @return number of modified documents
 */
long getModifiedCount();

/**
 * Gets total number of published documents (added and modified).
 * @return total number of published documents
 */
long getPublishedCount();

/**
 * Gets total number of deleted documents.
 * @return total number of deleted documents
 */
long getDeletedCount();

/**
 * Gets start time.
 * @return start time
 */
Date getStartTime();

/**
 * Gets end time.
 * @return end time
 */
Date getEndTime();

/**
 * Gets duration.
 * @return duration
 */
long getDuration();

/**
 * Gets performance.
 * Performance is an average harvested records per minute.
 * @return performance
 */
double getPerformance();
}
