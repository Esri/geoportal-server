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

  gs.base.Response = gs.Object.create(gs.Proto,{

    MediaType_APPLICATION_ATOM_XML: {writable: true, value: "application/atom+xml"},
    MediaType_APPLICATION_JSON: {writable: true, value: "application/json"},
    MediaType_APPLICATION_KML_XML: {writable: true, value: "application/vnd.google-earth.kml+xml"},
    MediaType_APPLICATION_RSS_XML: {writable: true, value: "application/rss+xml"},
    MediaType_APPLICATION_XML: {writable: true, value: "application/xml"},
    MediaType_TEXT_CSV: {writable: true, value: "text/csv"},
    MediaType_TEXT_PLAIN: {writable: true, value: "text/plain"},
    MediaType_TEXT_XML: {writable: true, value: "text/xml"},

    Status_BAD_REQUEST: {writable: true, value: 400},
    Status_INTERNAL_SERVER_ERROR: {writable: true, value: 500},
    Status_NOT_FOUND: {writable: true, value: 404},
    Status_OK: {writable: true, value: 200},

    entity: {writable: true, value: null},
    headers: {writable: true, value: null},
    mediaType: {writable: true, value: null},
    status: {writable: true, value: null},

    addHeader: {writable:true,value:function(name,value) {
      if (!this.headers) this.headers= [];
      this.headers.push({
        name: name,
        value: value
      });
    }},

    put: {writable:true,value:function(status,mediaType,entity) {
      this.status = status;
      this.mediaType = mediaType;
      this.entity = entity;
    }}

  });

}());
