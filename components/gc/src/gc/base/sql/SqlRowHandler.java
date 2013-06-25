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
package gc.base.sql;
import gc.base.task.TaskContext;
import java.sql.Connection;
import java.sql.ResultSet;

/**
 * Interface for processing the rows of a result set.
 */
public interface SqlRowHandler {

	/**
	 * Handle a row.
	 * @param context the task context
	 * @param con the database connection
	 * @param rs the result set
	 * @param rowNum the row number (based on a counter incremented by the application)
	 * @throws Exception if an exception occurs
	 */
	public void handleSqlRow(TaskContext context, 
			Connection con, ResultSet rs, long rowNum) throws Exception;
}
