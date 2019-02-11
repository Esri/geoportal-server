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
using System;
using System.Collections.Generic;
using System.Text;


namespace com.esri.gpt.csw {
   public class CswRecords :CswObjects {

        public new CswRecord this[string key] {
            get { return (CswRecord)base[key]; }
        }

        public new CswRecord this[int index] {
            get {
                return (CswRecord)base[index];
            }
        }
       /// <summary>
       /// Added record to records object collection
       /// </summary>
       /// <param name="key"></param>
       /// <param name="record"></param>
       public void AddRecord(object key, CswRecord record) {
           base.Add(key, record);
       }

    }
}
