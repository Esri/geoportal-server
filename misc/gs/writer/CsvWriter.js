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

  gs.writer.CsvWriter = gs.Object.create(gs.writer.Writer,{

    write: {writable:true,value:function(task,searchResult) {
      this.writeItems(task,searchResult);
    }},

    /* .......................................................................................... */

    esc: {writable:true,value:function(v) {
      if (typeof v !== "string") return v;
      v = v.replace(/"/g,'""');
      if (v.search(/("|,|\n)/g) >= 0) v = "\""+v+"\"";
      return v;
    }},

    writeEntry: {writable:true,value:function(task,stringBuilder,item,options) {
      var entry = task.target.itemToAtomEntry(task,item);

      var id = entry.id;
      var title = "Untitled";
      var description = "";
      var updated = options.now;
      if (typeof entry.title === "string" && entry.title.length > 0) {
        title = entry.title;
      }
      if (typeof entry.summary === "string") {
        description = entry.summary;
      }
      if (typeof entry.updated === "string" && entry.updated.length > 0) {
        updated = entry.updated;
      } else if (typeof entry.published === "string" && entry.published.length > 0) {
        updated = entry.published;
      }

      var line = this.esc(id);
      line += ","+this.esc(title);
      line += ","+this.esc(description);

      var xmin = "", ymin = "", xmax = "", ymax = "";
      if (gs.atom.BBox.isPrototypeOf(entry.bbox)) {
        xmin = entry.bbox.xmin;
        ymin = entry.bbox.ymin;
        xmax = entry.bbox.xmax;
        ymax = entry.bbox.ymax;
      }
      line += ","+xmin+","+ymin+","+xmax+","+ymax;

      var links = [], xmlLink = "", urlIdx = 0, urls = ["","","",""];
      if (Array.isArray(entry.link)) {
        links = entry.link;
      } else if (gs.atom.Link.isPrototypeOf(entry.link)) {
        links = [entry.link];
      }
      links.forEach(function(link){
        if (link.rel === "alternate" && link.type === "application/xml") {
          xmlLink = link.href;
        } else if (link.rel === "related") {
          if (urlIdx < 4) {
            urls[urlIdx] = link.href;
            urlIdx++;
          }
        }
        //console.log("link.rel",link.rel,link.href);
      });
      line += ","+this.esc(xmlLink);
      line += ","+this.esc(urls[0]);
      line += ","+this.esc(urls[1]);
      line += ","+this.esc(urls[2]);
      line += ","+this.esc(urls[3]);

      stringBuilder.append(options.NL).append(line);
    }},

    writeItems: {writable:true,value:function(task,searchResult) {
      var now = task.val.nowAsString();
      var options = {now: now, NL: task.val.NL};
      var header = "Id,Title,Description,West,South,East,North,Link_Xml,Link_1,Link_2,Link_3,Link_4";
      var stringBuilder = task.context.newStringBuilder();
      stringBuilder.append(header);
      var items = searchResult.items ? searchResult.items : [];
      for (var i=0;i<items.length;i++) {
        this.writeEntry(task,stringBuilder,items[i],options);
      }
      this.writeResponse(task,stringBuilder.toString());
    }},

    writeResponse: {writable:true,value:function(task,data) {
      var response = task.response;
      response.addHeader("Content-Disposition","filename="+Date.now()+".csv");
      response.put(response.Status_OK,response.MediaType_TEXT_CSV,data);
    }}

  });

}());
