<%--
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<% // service-wsdl.jsp - CSW service wsdl %>
<%@page language="java" contentType="text/xml; charset=UTF-8" session="false"%>
<%
  String url = com.esri.gpt.framework.context.RequestContext.resolveBaseContextPath(request);
%>
<wsdl:definitions targetNamespace="http://www.opengis.net/cat/csw/2.0.2/wsdl" xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" xmlns:http="http://schemas.xmlsoap.org/wsdl/http/" xmlns:csw-http="http://www.opengis.net/cat/csw/2.0.2/http" xmlns:csw-http-kvp="http://www.opengis.net/cat/csw/2.0.2/http/kvp" xmlns:csw-soap="http://www.opengis.net/cat/csw/2.0.2/soap" xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/">
  <wsdl:documentation xmlns:dc="http://purl.org/dc/elements/1.1/">
    <dc:date>2004-06-07</dc:date>
    <dc:description>
         This WSDL document defines the service-specific properties
         of a MyService CSW implementation; it specifies available
         endpoints and alternative bindings.
      </dc:description>
  </wsdl:documentation>
  <wsdl:import namespace="http://www.opengis.net/cat/csw/2.0.2/soap" location="./soap-binding.wsdl"/>
  <wsdl:import namespace="http://www.opengis.net/cat/csw/2.0.2/http" location="./http-binding.wsdl"/>
  <wsdl:import namespace="http://www.opengis.net/cat/csw/2.0.2/http/kvp" location="./kvp-binding.wsdl"/>
  <wsdl:service name="CSW">
    <wsdl:documentation>
         A CSW implementation. Includes alternative SOAP bindings
         for the CSW interfaces.
      </wsdl:documentation>
    <wsdl:port name="csw-SOAP-Port" binding="csw-soap:csw-SOAP">
      <soap:address location="<%=url%>/csw"/>
    </wsdl:port>
    <wsdl:port name="csw-SOAP-Publication-Port" binding="csw-soap:csw-SOAP">
      <soap:address location="<%=url%>/csw"/>
    </wsdl:port>    
  </wsdl:service>
</wsdl:definitions>





