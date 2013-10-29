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

package com.esri.gpt.server.usage.common;

import java.io.Writer;
import java.sql.ResultSet;

import com.esri.gpt.control.rest.writer.JsonResultSetWriter;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.usage.api.IStatisticsWriter;

/**
 * This is class is used to write json response.
 * 
 */
public class JSONWriter extends JsonResultSetWriter implements
		IStatisticsWriter {

	// class variables
	// =============================================================

	// instance variables
	// ==========================================================
	private StringBuilder sb;

	// constructors
	// ================================================================

	public JSONWriter(Writer underlyingWriter, StringBuilder sb) {
		super(underlyingWriter);
		this.sb = sb;
	}

	// properties
	// ==================================================================

	// methods
	// ==================================================================

	/**
	 * Adding spaces for pretty print.
	 * 
	 * @param depth
	 */
	private void addSpaces(int depth) {
		if (depth == 0) {
			sb.append(getNewline());
		} else {
			sb.append(getNewline()).append(makeTabs(depth));
		}
	}
	
	/**
	 * Serializes key value pair to json object.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param hasMore
	 *            true if has more elements in an JSON array
	 * @param isNumber
	 *            true if value is number.
	 */
	@Override
	public void writeElement(String key, String value, boolean hasMore,
			boolean isNumber) {
		String element = "\"" + key + "\":";
		if (isNumber) {
			element += value;
		} else {
			element += "\"" + value + "\"";
		}
		if (hasMore) {
			element += ",";
		}
		sb.append(element);
	}

	/**
	 * Serializes key value pair to json object.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	@Override
	public void writeElement(String name, String value) throws Exception {
		writeElement(name, value, false, false);
	}
	
	/**
	 * This method is used to serialize resultset rows as json.
	 * 
	 * @param tableName
	 *            the tableName for which rows are serialized
	 * @param rs
	 *            the resultset
	 * @param columnTags
	 *            the column names
	 */
	@Override
	public void writeResultSet(String tableName, ResultSet rs,
			String[] columnTags) throws Exception {
		sb.append("\"" + tableName + "\":{");
		addSpaces(4);
		sb.append("\"rows\":[");
		addSpaces(5);
		boolean firstRow = true;
		while (rs.next()) {
			if (!firstRow) {
				sb.append(",");
				addSpaces(5);
			}
			sb.append("{");
			int cnt = 0;
			for (String column : columnTags) {
				addSpaces(6);
				String value = rs.getString(column);
				if (cnt == columnTags.length - 1) {
					sb.append("\"" + column + "\":\""
							+ Val.escapeStrForJson(value) + "\"");
				} else {
					sb.append("\"" + column + "\":\""
							+ Val.escapeStrForJson(value) + "\",");
				}
				cnt++;
			}
			addSpaces(5);
			sb.append("}");
			firstRow = false;
		}
		addSpaces(4);
		sb.append("]}");
	}
}
