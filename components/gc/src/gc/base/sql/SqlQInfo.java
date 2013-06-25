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
import java.util.List;

/**
 * Provides information for a query.
 */
public class SqlQInfo {
	
	/** instance variables */
	private int          fetchSize = -1;
	private String[]     fields;
	private int          maxRows = -1;
	private String       orderBy;
	private List<Object> queryBindings;
	private String       tableName;
	private String       tableSuffix;
	private String       where;
	
	/** Default constructor. */
	public SqlQInfo() {}
	
	/**
	 * Gets the fetch size.
	 * @return the fetch size
	 */
	public int getFetchSize() {
		return fetchSize;
	}
	/**
	 * Sets the fetch size.
	 * @param fetchSize the fetch size
	 */
	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}
	
	/**
	 * Gets the list of fields to select.
	 * @return the fields
	 */
	public String[] getFields() {
		return fields;
	}
	/**
	 * Sets the list of fields to select.
	 * @param fields the fields
	 */
	public void setFields(String[] fields) {
		this.fields = fields;
	}
	
	/**
	 * Gets the maximum number if rows to return.
	 * @return the max rows
	 */
	public int getMaxRows() {
		return maxRows;
	}
	/**
	 * Sets the maximum number if rows to return.
	 * @param maxRows the max rows
	 */
	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}
	
	/**
	 * Gets the order by clause.
	 * @return the order by clause
	 */
	public String getOrderBy() {
		return orderBy;
	}
	/**
	 * Sets the order by clause.
	 * @param orderBy the order by clause
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	
	/**
	 * Gets the query value bindings (for prepared statements - ?).
	 * @return the query value bindings
	 */
	public List<Object> getQueryBindings() {
		return queryBindings;
	}
	/**
	 * Sets the query value bindings (for prepared statements - ?).
	 * @param queryBindings the query value bindings
	 */
	public void setQueryBindings(List<Object> queryBindings) {
		this.queryBindings = queryBindings;
	}
	
	/**
	 * Gets the table name.
	 * @return the table name
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * Sets the table name.
	 * @param tableName the table name
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * Gets the table suffix (if applicable).
	 * @return the table suffix
	 */
	public String getTableSuffix() {
		return tableSuffix;
	}
	/**
	 * Sets the table suffix (if applicable).
	 * @param tableSuffix the table suffix
	 */
	public void setTableSuffix(String tableSuffix) {
		this.tableSuffix = tableSuffix;
	}

	/**
	 * Gets the where clause.
	 * @return the where clause
	 */
	public String getWhere() {
		return where;
	}
	/**
	 * Sets the where clause.
	 * @param where the where clause
	 */
	public void setWhere(String where) {
		this.where = where;
	}

}
