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
using System.Collections;
using System.Text;

namespace com.esri.gpt.csw {
    /// <summary>
    /// Abstract class for collections.
    /// </summary>
    /// <remarks>
    /// The collection support both sequential and random access objects based on key.
    /// </remarks>
    public abstract class CswObjects :IDictionary, IEnumerable{

        private ArrayList list = new ArrayList();
        private Hashtable map = new Hashtable();
        /// <summary>
        /// Overidden methods
        /// </summary>
        #region ICollection implementation
                
                public int Count {
                    get { return map.Count; }
                }

                public bool IsSynchronized {
                    get { return map.IsSynchronized; }
                }

                public object SyncRoot {
                    get { return map.SyncRoot; }
                }

                public void CopyTo(System.Array array, int index) {
                    map.CopyTo(array, index);
                }
        #endregion

        #region IDictionary implementation
                

                public void Add(object key, object value) {
                    if (!map.ContainsKey(key))//ignores duplicate keys
                    {
                        list.Add(key);
                        map.Add(key, value);
                    }
                }

                public bool IsFixedSize {
                    get { return map.IsFixedSize; }
                }

                public bool IsReadOnly {
                    get { return map.IsReadOnly; }
                }

                public ICollection Keys {
                    get { return map.Keys; }
                }

                public void Clear() {
                    list.Clear();
                    map.Clear();
                }

                public bool Contains(object key) {
                    return map.Contains(key);
                }

                public bool ContainsKey(object key) {
                    return map.ContainsKey(key);
                }

                public IDictionaryEnumerator GetEnumerator() {
                    return map.GetEnumerator();
                }

                public void Remove(object key) {
                    map.Remove(key);
                    list.Remove(key);
                }

                public object this[object key] {
                    get { return map[key]; }
                    set { map[key] = value; }
                }

                public ICollection Values {
                    get { return map.Values; }
                }
        #endregion

        #region IEnumerable implementation

                 IEnumerator IEnumerable.GetEnumerator() {
                    return map.GetEnumerator();
                }

        #endregion

        #region specialized indexer routines

                public object this[string key] {
                    get { return map[key]; }
                }

                public object this[int index] {
                    get { return map[list[index]]; }
                }
        #endregion

    }
}
