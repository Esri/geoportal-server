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

  gs.provider.csw.OwsException = gs.Object.create(gs.Proto,{

    OWSCODE_InvalidFormat: {writable: true, value: "InvalidFormat"},
    OWSCODE_InvalidParameterValue: {writable: true, value: "InvalidParameterValue"},
    OWSCODE_MissingParameterValue: {writable: true, value: "MissingParameterValue"},
    OWSCODE_NoApplicableCode: {writable: true, value: "NoApplicableCode"},
    OWSCODE_OperationNotSupported: {writable: true, value: "OperationNotSupported"},
    OWSCODE_VersionNegotiationFailed: {writable: true, value: "VersionNegotiationFailed"},

    code: {writable: true, value: null},
    locator: {writable: true, value: null},
    text: {writable: true, value: null},

    getReport: {writable:true,value:function(task) {
      var p1 = task.val.NL;
      var p2 = p1+"\t";
      var p3 = p2+"\t";

      if (this.code === null) {
        this.code = this.OWSCODE_NoApplicableCode;
      }
      var version = "3.0.0";
      var owsUri = task.uris.URI_OWS2;
      if (task.isCsw2) {
        version = "1.2.0";
        owsUri = task.uris.URI_OWS;
      }
      var xml = task.val.XML_HEADER;
      xml += p1+"<ExceptionReport";
      xml += " version=\""+task.val.escXml(version)+"\"";
      xml += " xmlns=\""+task.val.escXml(owsUri)+"\"";
      xml += ">";

      xml += p2+"<Exception";
      xml += " exceptionCode=\""+task.val.escXml(this.code)+"\"";
      if (this.locator !== null && this.locator.length > 0) {
        xml += " locator=\""+task.val.escXml(this.locator)+"\"";
      }
      xml += ">";

      xml += p3+"<ExceptionText>";
      var txt = this.text;
      if (txt === null) txt = "";
      if (txt.indexOf("<![CDATA[") === 0) {
        xml += p1+txt;
      } else {
        xml += p1+task.val.escXml(txt);
      }
      xml += p3+"</ExceptionText>";

      xml += p2+"</Exception>";
      xml += p1+"</ExceptionReport>";
      return xml;
    }},

    put: {writable:true,value:function(task,code,locator,text) {
      this.code = code;
      this.locator = locator;
      this.text = text;
      this.toResponse(task);
    }},

    toResponse: {writable:true,value:function(task) {
      var xml = this.getReport(task);
      var response = task.response;
      if (response.status === null) response.status = response.Status_BAD_REQUEST;
      response.put(response.status,response.MediaType_APPLICATION_XML,xml);
      task.hasError = true;
    }}

  });

}());
