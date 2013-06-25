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
package gc.base.util;
import java.util.UUID;

/**
 * UUID utilities.
 */
public class UuidUtil {
	
	/**
	 * Generates a uuid.
	 * @param lowCaseRemoveDashes if true, ensure lower case and remove dashes
	 * @return the uuid
	 */
  public static String makeUuid(boolean lowCaseRemoveDashes) {
  	String s = UUID.randomUUID().toString();
  	if (lowCaseRemoveDashes) {
  		s = s.toLowerCase().replaceAll("-","");
  	}
  	return s;
  }
  
	/**
	 * Normalize a GPT document uuid.
	 * <br/>(remove curly braces {}, remove dashes, ensure lower case)
	 * @return the uuid
	 */
  public static String normalizeGptUuid(String docuuid) {
		String id = docuuid;
		id = id.replaceAll("\\{","").replaceAll("}","").replaceAll("-","").toLowerCase();
		return id;
  }

}
