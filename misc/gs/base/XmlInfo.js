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

  gs.base.XmlInfo = gs.Object.create(gs.Proto,{

    dom: {writable: true, value: null},
    root: {writable: true, value: null},

    forEachAttribute: {writable:true,value:function(node) {
      throw new Error("XmlInfo::forEachAttribute must be implemented");
    }},

    forEachChild: {writable:true,value:function(node) {
      throw new Error("XmlInfo::forEachChild must be implemented");
    }},

    getAttributes: {writable:true,value:function(node) {
      throw new Error("XmlInfo::getAttributes must be implemented");
    }},

    getAttributeValue: {writable:true,value:function(node,localName,namespaceURI) {
      throw new Error("XmlInfo::getAttributeValue must be implemented");
    }},

    getChildren: {writable:true,value:function(node) {
      throw new Error("XmlInfo::getChildren must be implemented");
    }},

    /*
     * nodeInfo:
     * {
     *   node: ,
     *   nodeText: , (if requested)
     *   nodeName: ,
     *   localName: ,
     *   namespaceURI: ,
     *   isAttributeNode: ,
     *   isElementNode: ,
     *   isTextNode:
     * }
     */
    getNodeInfo: {writable:true,value:function(node,withText) {
      throw new Error("XmlInfo::getNodeInfo must be implemented");
    }},

    getNodeText: {writable:true,value:function(node) {
      throw new Error("XmlInfo::getNodeText must be implemented");
    }}

  });

}());
