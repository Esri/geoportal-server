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
<% // dataMigrationForm.jsp - migrate data page %>
<%@page import="com.esri.gpt.framework.context.*"%>
<%@page import="com.esri.gpt.framework.security.principal.Publisher"%>
<%@page import="com.esri.gpt.framework.security.identity.NotAuthorizedException"%>
<html>
	<head>
		<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/catalog/skins/themes/red/main.css"  />
		<link rel="icon" type="image/x-icon"   href="<%=request.getContextPath()%>/catalog/images/favicon.ico" />
		<link rel="shortcut icon" type="image/x-icon" href="<%=request.getContextPath()%>/catalog/images/favicon.ico" />	
		<script>
			function checkUserInput(name){
				if(!trim(document.getElementById(name).value).length > 0){
					alert('Please enter value for '+name);
					return false;
				}
				return true;
			}
			function trim (value){
				 return value.replace(/^\s+|\s+$/, ''); 
			}
			function show_confirm()
			{
				status  = true;
				status = checkUserInput("jdbcDriver");
				status = checkUserInput("jdbcUrl");
				status = checkUserInput("dbUserName");
				status = checkUserInput("dbPassword");
				status = checkUserInput("tablePrefix");
				status = checkUserInput("metaDataTableName");
				status = checkUserInput("geoportalVersion");
				status = checkUserInput("useMetadataServer");
				if(trim(document.getElementById("useMetadataServer").value) == 'Yes'){
					status = checkUserInput("geoportalUserName");
					status = checkUserInput("geoportalPassword");
					status = checkUserInput("serviceUrl");
				}
		
				if(!status)
					 return false;	
				var r=confirm("Press a button to continue");
				if (r==true)
				  {
					document.getElementById("migrate").disabled = true;  
				  }
				else
				  {
				  	alert("Data Migration not started!");
				  	return false;
				  }
			}
			function setUseMetadataServer(){
				document.getElementById("useMetadataServer").value = "Yes";
				document.getElementById("geoportalUserName").disabled = false;
				document.getElementById("geoportalPassword").disabled = false;
				document.getElementById("serviceUrl").disabled = false;
				document.getElementById("metaDataTableName").value="META";
			}
			function resetUseMetadataServer(){
				document.getElementById("useMetadataServer").value = "No";
				document.getElementById("geoportalUserName").value = "";
				document.getElementById("geoportalPassword").value = "";
				document.getElementById("serviceUrl").value = "";
				document.getElementById("geoportalUserName").disabled = true;
				document.getElementById("geoportalPassword").disabled = true;
				document.getElementById("serviceUrl").disabled = true;
				document.getElementById("metaDataTableName").value="METADATA";
			}
			function resetGeoportalVersion(){		
				if(document.getElementById("useMetadataServer").value == "No"){
					document.getElementById("geoportalVersion").value = "931";
					document.getElementById("geoportalUserName").value = "";
					document.getElementById("geoportalPassword").value = "";
					document.getElementById("serviceUrl").value = "";
					document.getElementById("geoportalUserName").disabled = true;
					document.getElementById("geoportalPassword").disabled = true;
					document.getElementById("serviceUrl").disabled = true;
					document.getElementById("metaDataTableName").value="METADATA";
				}else {
					//document.getElementById("geoportalVersion").value = "93";
					document.getElementById("geoportalUserName").disabled = false;
					document.getElementById("geoportalPassword").disabled = false;
					document.getElementById("serviceUrl").disabled = false;
					document.getElementById("metaDataTableName").value="META";
				}
			}
			function switchUseMetadataServer(value){
				if(value == "931"){
					resetUseMetadataServer();
				}
				else{
					setUseMetadataServer();
				}
			}
			function init(){
				document.getElementById("migrate").disabled = false; 
			} 
		</script>
	</head>
	<body onload="init()">
	<img  style="z-index:1; background: #450200 url(../skins/themes/red/images/banner.jpg) no-repeat scroll 0pt 0px;
	height: 65px;
	position: relative;width:100%;"/>
	<div style="z-index:2; position: absolute;
	top: 10px;
	left: 10px;
	font-size: 2em;
	font-family: 'Trebuchet MS',Helvetica,Arial,Geneva,sans-serif;
	color: #FFF;
    text-decoration: none !important;">
		Geoportal 10 - Data Migration
	</div>

<%		            
    RequestContext context = RequestContext.extract(request);
	try{
		Publisher p = new Publisher(context);
		if (p.getIsAdministrator()) {
		} else {
			response.sendRedirect("../main/home.page");
		}	
	}catch(NotAuthorizedException e){
		response.sendRedirect("../main/home.page");
	}
%>
	<form name="frmMigrate" action="./dataMigration.jsp" method="post" onSubmit="return show_confirm()">
			<h2>GPT database connection parameters</h2><br/>      
			<table columns="2" 
			  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">
				<tr>
				  <td><label>JDBC driver:</label></td>
				  <td><input type="text"  id="jdbcDriver" name="jdbcDriver" size="50" maxlength="128"/></td>
				 </tr>
				  <tr>
				  <td><label>JDBC url:</label></td>
				  <td>
				  <input type="text" size="50" name="jdbcUrl" id="jdbcUrl" maxlength="128" /></td>
				 </tr>
				 
				 	<tr>
				  <td>
				  <label>Database Schema:</label></td>
				  <td>
				  <input type="text" size="50" name="dbSchemaName" id="dbSchemaName" maxlength="128" /></td>
				 </tr>
				 
				<tr>
				  <td>
				  <label>Database User name:</label></td>
				  <td>
				  <input type="text" size="50" name="dbUserName" id="dbUserName" maxlength="128" /></td>
				 </tr>
				      
				<tr>
				  <td>
				  <label>Database Password:</label></td>
				  <td>
				  <input type="password"  size="50" name="dbPassword" id="dbPassword" maxlength="128" /></td>
				 </tr>
				                 
				<tr>
				  <td>
				  <label>Table Prefix:</label></td>
				  <td>
				  <input type="text" size="50" name="tablePrefix" id="tablePrefix" maxlength="128" value="GPT_"/></td>
				 </tr>
				 
				 <tr>
				  <td>
				  <label>Metadata Table Name:</label></td>
				  <td>
				  <input type="text" size="50" name="metaDataTableName" id="metaDataTableName" maxlength="128" value="METADATA"/></td>
				 </tr>
				                 
				<tr>
				  <td>
				  <label>Geoportal Version:</label></td>
				  <td>
				  <select name="geoportalVersion" id="geoportalVersion" onchange="switchUseMetadataServer(this.options[this.selectedIndex].value);" onclick="switchUseMetadataServer(this.options[this.selectedIndex].value);">
						<option value="931" selected onclick="resetUseMetadataServer();">9.3.1 SP1</option>
						<option value="931" onclick="resetUseMetadataServer();">9.3.1</option>
						<option value="93" onclick="setUseMetadataServer();">9.3</option>
				  </select></td>
				 </tr>
				  
				<tr>
				  <td>
				  <label>Uses Metadata Server:</label></td>
				  <td>
				   <select name="useMetadataServer" id="useMetadataServer" onchange="resetGeoportalVersion();" onclick="resetGeoportalVersion();">
						<option value="No" selected onclick="resetGeoportalVersion();">No</option>
						<option value="Yes" onclick="resetGeoportalVersion();">Yes</option>
				  </select></td>
				 </tr>
				 
				 <tr>
				  <td>
				  <label>Geoportal Instance Url:</label></td>
				  <td>
				  <input type="text" size="50" disabled="true" id="serviceUrl" name="serviceUrl" maxlength="128" title="Hint:- http://serverName:port/GPT9/com.esri.esrimap.Esrimap?serviceName=GPT_Publish_Metadata"/>
				  </td>
				 </tr>
				      
				 <tr>
				  <td>
				  <label>Geoportal User name:</label></td>
				  <td>
				  <input type="text" size="50" disabled="true" id="geoportalUserName" name="geoportalUserName" maxlength="128" /></td>
				 </tr>
				      
				 <tr>
				  <td>
				  <label>Geoportal Password:</label></td>
				  <td>
				  <input type="password"  size="50" disabled="true" id="geoportalPassword" name="geoportalPassword" maxlength="128" /></td>
				 </tr>
				                 
				<tr>
				  <td>
				  <label value=""/></td>
				  <td>
				  <input type="submit" value="Migrate"  id="migrate"  />
				    </td>
				 </tr>     
			</table>
		</form>
	</body>
</html>