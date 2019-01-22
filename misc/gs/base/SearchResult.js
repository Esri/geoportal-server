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

(function(){

  gs.base.SearchResult = gs.Object.create(gs.Proto,{

    items: {writable: true, value: null},
    itemsPerPage: {writable: true, value: 10},
    jsonResponse: {writable: true, value: null},
    startIndex: {writable: true, value: 1},
    totalHits: {writable: true, value: 0},

    calcNextRecord: {writable:true,value:function(task) {
      var items = this.items ? this.items : [];
      var noNext = -1; // TODO 0 ?
      var totalHits = this.totalHits;
      var startIndex = this.startIndex;
      var itemsPerPage = this.itemsPerPage;
      var numReturned = items.length;
      if (itemsPerPage === 0) numReturned = 0;
      var nextRecord = noNext;
      if (numReturned === 0 && totalHits > 0) {
        if (task.request.queryIsZeroBased) {
          if (startIndex === 0) nextRecord = 0;
        } else {
          if (startIndex === 1) nextRecord = 1;
        }
      } else if (numReturned > 0) {
        nextRecord = startIndex + itemsPerPage;
        if (task.request.queryIsZeroBased) {
          if (nextRecord >= totalHits) nextRecord = noNext;
        } else {
          if (nextRecord > totalHits) nextRecord = noNext;
        }

      }
      return nextRecord;
    }},

    init: {writable:true,value:function(task) {
      this.startIndex = 1;
      if (task.request.queryIsZeroBased) this.startIndex = 0;
      var start = task.request.getStart();
      start = task.val.strToInt(start,null);
      if (typeof start === "number" && start >= 0) {
        if (start > 0 || task.request.queryIsZeroBased) {
          this.startIndex = start;
        }
      }
      var num = task.request.getNum();
      num = task.val.strToInt(num,null);
      if (typeof num === "number" && num >= 0) {
        this.itemsPerPage = num;
      } else if (typeof num === "number" && num < 0) {
        this.itemsPerPage = 0;
      }
      return this;
    }}

  });

}());
