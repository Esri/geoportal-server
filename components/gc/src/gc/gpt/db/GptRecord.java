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
package gc.gpt.db;
import gc.base.sql.SqlQInfo;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a GPT record (SQL database).
 */
public abstract class GptRecord {
	
	/** instance variables */
	private SqlQInfo sqlQInfo = new SqlQInfo();

	/** Default constructor. */
	public GptRecord() {}
	
	/**
	 * Gets the query information.
	 * @return the query information
	 */
	public SqlQInfo getSqlQInfo() {
		return sqlQInfo;
	}
	/**
	 * Sets the query information.
	 * @param sqlQInfo the query information
	 */
	public void setSqlQInfo(SqlQInfo sqlQInfo) {
		this.sqlQInfo = sqlQInfo;
	}
	
	/**
	 * Reads values from a result set.
	 * @param rs the result set
	 * @throws SQLException if an exception occurs
	 */
	public void readFields(ResultSet rs) throws SQLException {}
	
	/**
	 * Writes values to a statement.
	 * @param pst the statement
	 * @throws SQLException if an exception occuts
	 */
	public void write(PreparedStatement pst) throws SQLException {}
	
	/**
	 * Reads values from a binary data source.
	 * @param in the source
	 * @throws IOException if an exception occurs
	 */
	public void readFields(DataInput in) throws IOException {}
	
	/**
	 * Writes values to a binary data destination.
	 * @param out the destination
	 * @throws IOException if an exception occurs
	 */
	public void write(DataOutput out) throws IOException {}
	
}
