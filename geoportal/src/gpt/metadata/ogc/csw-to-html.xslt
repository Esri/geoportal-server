<?xml version="1.0" encoding="UTF-8"?>
<!--
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
-->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xlink="http://www.w3.org/1999/xlink"
  xmlns:csw="http://www.opengis.net/cat/csw"
  xmlns:csw202="http://www.opengis.net/cat/csw/2.0.2"
  xmlns:ows="http://www.opengis.net/ows"
  xmlns:ows11="http://www.opengis.net/ows/1.1">
  <xsl:output method="html" indent="yes" encoding="UTF-8" 
    omit-xml-declaration="yes"/>
   
	<xsl:template match="/">
   
   <h4>
     <xsl:value-of select="//ows:ServiceIdentification/ows:Title | //ows11:ServiceIdentification/ows11:Title"/>
   </h4>
   <p>
     <xsl:value-of select="//ows:ServiceIdentification/ows:Abstract | //ows11:ServiceIdentification/ows11:Abstract"/>
   </p>

	</xsl:template>
  
</xsl:stylesheet>
