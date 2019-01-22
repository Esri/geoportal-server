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

  /* ============================================================================================ */

  gs.Object = {

    create: function(proto,properties) {
      var obj = Object.create(proto,properties);
      if (typeof obj.__init__ === "function") obj.__init__();
      return obj;
    }

  };

  /* ============================================================================================ */

  gs.Proto = Object.create(Object.prototype,{

    __init__: {writable:true,value:function() {}},

    /* Example
    __init__: {writable:true,value:function() {
      gs.base.Target.__init__.call(this); // call super.__init__
      print("PortalTarget::__init__"); // custom initialization
    }},
    */

    init: {writable:true,value:function() {
      return this;
    }},

    mixin: {writable:true,value:function(props) {
      if (typeof props === "object" && props != null) {
        for (var k in props) {
          if (props.hasOwnProperty(k)) {
            this[k] = props[k];
          }
        }
      }
      return this;
    }},

    safeMixin: {writable:true,value:function(props) {
      if (typeof props === "object" && props != null) {
        for (var k in props) {
          if (props.hasOwnProperty(k)) {
            if (typeof props[k] !== "function") {
              this[k] = props[k];
            }
          }
        }
      }
      return this;
    }}

  });

  /* ============================================================================================ */

}());
