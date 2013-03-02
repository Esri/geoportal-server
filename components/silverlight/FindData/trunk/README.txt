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
===========================================================================

This is the Silverlight widget tool that can be used by the geoportal to layer
services


Required tools for project compilation and testing:
============================================================================

1. Microsoft Silverlight Version 4

2) ArcGIS API for Microsoft Silverlight/WPF version 2.0

3) ESRI ArcGIS Sample for WMS Data Source (http://resources.esri.com/arcgisserver/apis/silverlight/index.cfm?fa=codeGalleryDetails&scriptID=16249)

5) Microsoft Visual Studio 2010 IDE

To compile/run this tool you need to.
=============================================================================

1) Download Silverlight Version 4 from Microsoft and install it;

2) Download ArcGIS API for Microsoft Silverlight/WPF version 2.0 from the esri resource center and install it;

3) Download ESRI ArcGIS Sample for WMS Data Source source code from the esri code gallery and unzip it to a directory;

4) Copy the GeoportalWidget folders in this directory into the parent directory of the above unzipped resource;  

5) Create a solution using Visual Studio 2010; 

6) Create two class library projects in the solution. These two projects correspond to two folders created in steps 3 and 4. Add the corresponding c sharp files into these two projects. 

7) Add references to WMS project in the GeoportalWidget project. 

8) Compile the solution to generate GeoportalWidget.dll assembly. This component can be used in other Silverlight applications. 