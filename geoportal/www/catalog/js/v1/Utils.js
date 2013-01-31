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
/**
 * Utility functions
 */
function GptUtils() {
	
}

/** Logs the error 
 * @msg Message that should be logged
 * */
GptUtils.log = new GptUtils();

/**
 * Constants for log levels. do not change.  Made to march
 * java.util.log levels
 */
GptUtils.log.showLogs = false; // Set to true if you want visible debug logs on browswer
GptUtils.log.elementId ="gptUtilsLoggingelement";
GptUtils.log.Level = new Object();
GptUtils.log.Level.SEVERE = 1000; 
GptUtils.log.Level.WARN = 900;
GptUtils.log.Level.WARNING = 900;
GptUtils.log.Level.INFO = 800;
GptUtils.log.Level.CONFIG = 700;
GptUtils.log.Level.DEBUG = 500;
GptUtils.log.Level.FINE = 500;
GptUtils.log.Level.FINER = 400;
GptUtils.log.Level.FINEST = 300;
//This variable should be changed (Link it to the java.util.logging current 
// level on your page)
GptUtils.log.currentLevel = GptUtils.log.Level.FINE;

/*
 * Logs using levels
 * @param level Take from GptUtils.log.Level.*
 * @param msg String message
 */
GptUtils.logl = function(level, msg, alertUser) {

  var defaultLevel = GptUtils.valChkInt(GptUtils.log.currentLevel, 
    GptUtils.log.Level.WARN);
  level = GptUtils.valChkInt(level, GptUtils.log.Level.FINEST);
  alertUser = GptUtils.valChkBool(alertUser, false);
  if(defaultLevel > level) {
  	return;
  }
  var bgColor;
  if(level >= 1000){
  	level = "SEVERE: ";
  	bgColor = "#E67373"
  } else if (level >= 900) {
  	level = "WARNING: ";
  	bgColor = "#E6CFCF";
  } else if (level >= 800) {
  	level = "INFO: ";
  	bgColor = "#CFD6E6"
  } else if (level >= 700) {
    level = "CONFIG: ";
    bgColor = "#CFD6E6"
  } else if (level >= 500) {
  	level = "FINE: ";
  	bgColor = "#E6DCCF";
  } else if (level >= 400) {
    level = "FINER: ";
    bgColor = "#E6DCCF";
  } else {
    level = "FINEST: ";
    bgColor = "#E6DCCF";
  }
  
  // Creating table on page if necessary
  var eTable = document.getElementById(GptUtils.log.elementId);
  var eTableBody = document.getElementById(GptUtils.log.elementId + "body");
  
  if(!GptUtils.exists(eTable)) {
  	eTable = document.createElement('table');
  	eTable.setAttribute("id", GptUtils.log.elementId);
  	var eTableTr = document.createElement('tr');
  	var eTableTh = document.createElement('th');
  	eTableTh.appendChild(document.createTextNode("Javascript Logs"));
  	eTableTr.appendChild(eTableTh);
  	eTable.appendChild(eTableTr);
  	var eTableBody = document.createElement("tbody");
  	eTableBody.setAttribute("id", GptUtils.log.elementId + "body");
  	eTable.appendChild(eTableBody);
  	var eBody = document.getElementsByTagName("body").item(0);
  	eBody.appendChild(eTable);
  	eTable =  document.getElementById(GptUtils.log.elementId);
  	eTableBody = document.getElementById(GptUtils.log.elementId + "body");
  }
  var showLogs = GptUtils.valChkBool(GptUtils.log.showLogs, false);
  if(showLogs == true) {
  
  	eTable.style.visibility = "visible";
    eTable.style.display = "block";
  	eTable.setAttribute("style", "visibility: visible; display: block;");
  	
  } else {
  
  	eTable.style.visibility = "hidden";
    eTable.style.display = "none";
  	eTable.setAttribute("style", "visibility: hidden; display: none;");
  	
  }
  
  if(alertUser == true) {
    alert(level +  msg );
  }

  // Adding row & cells
  var eTr = document.createElement('tr');
  var currentTime = new Date();
 
  var eTd = document.createElement('td');
  eTd.style.backgroundColor = bgColor;
  var eTxtMsg = document.createTextNode(currentTime.toTimeString());
  eTd.appendChild(eTxtMsg);
  eTr.appendChild(eTd);
  
  eTd = document.createElement('td');
  eTd.style.backgroundColor = bgColor;
  eTxtMsg = document.createTextNode(level);
  eTd.appendChild(eTxtMsg);
  eTr.appendChild(eTd);
    
  eTd = document.createElement('td');
  eTd.style.backgroundColor = bgColor;
  eTxtMsg = document.createTextNode(msg);
  eTd.appendChild(eTxtMsg);
  eTr.appendChild(eTd);
  
  eTableBody.appendChild(eTr);
  
  
}


/**
 * @return true if variable exists & not null, false otherwise
 */ 
GptUtils.exists = function(variable) {
	 if(typeof(variable) != 'undefined' && variable != null){
       return true;
    }
   return false;
}

/**
 * Popup windows
 * @param url url to open
 * @param pageTitle page title of the popped up page
 * @param width popup width
 * @param height popup height
 * @return window pointer
 */
GptUtils.popUp = function(url, pageTitle, width, height) {
   
    var nw = window.open(url,
            pageTitle, 
            "width=" + width + ",height=" + height + ",resizable,scrollbars");
    nw.focus();
    return nw;
}

/*
 * Trims a string
 * @param sString string to be trimmed
 * @return trimmed string or if not string then empty string returned
 */
GptUtils.trim = function(sString) {
  if(typeof(sString) != 'string') {
  	return "";
  }
  return sString.replace(/^\s+|\s+$/g,"");
}

/*
 * Trims string if string is valid, otherwise returns default
 * @param sString String to be checked
 * @param sDefault String to return if problems with sString
 * @return "" or sDefault string if argument is null && trimmed
 * if string is true, 
 */
GptUtils.valChkStr = function(sString, sDefault) {
	if(!GptUtils.exists(sDefault) || typeof(sDefault) != 'string') {
		sDefault = "";
	}
	if(!GptUtils.exists(sString) || typeof(sString) != 'string' ) {
		return sDefault;
  }
  
  return GptUtils.trim(sString);
}

/*
 * Checks integer value
 * @param iInt Integer object to be checked
 * @param iDefault to return. if none existent or not number, 0 is returned
 */
GptUtils.valChkDouble = function(iDouble, iDefault) {
  if(!GptUtils.exists(iDefault) || typeof(iDefault) != 'number') {
    iDefault = 0;
  }
  if(!GptUtils.exists(iDouble)) {
    return iDefault;
  }
  if( typeof(iDouble) == 'number') {
    return iDouble;
  }
  var tmpiDouble = parseFloat(GptUtils.trim(iDouble.toString()));
  if(!isNaN(tmpiDouble)) {
    return tmpiDouble;
  }
  
  return iDefault;
}

/*
 * Checks integer value
 * @param iInt Integer object to be checked
 * @param iDefault to return. if none existent or not number, 0 is returned
 */
GptUtils.valChkInt = function(iInt, iDefault) {
	if(!GptUtils.exists(iDefault) || typeof(iDefault) != 'number') {
    iDefault = 0;
  }
  if(!GptUtils.exists(iInt)) {
    return iDefault;
  }
  if( typeof(iInt) == 'number') {
  	return iInt;
  }
  var tmpiInt = parseInt(GptUtils.trim(iInt.toString()));
  if(!isNaN(tmpiInt)) {
  	return tmpiInt;
  }
  
  return iDefault;
}

/*
 * Returns boolean value of first object if object has
 * boolean representation.  Otherwise returns bDefault
 * 
 * @param vBool variable to check for boolean ness
 * @param bDefault reverted to false if not exists or not boolean
 */
GptUtils.valChkBool = function( vBool , bDefault) {
  if(!GptUtils.exists(bDefault) || typeof(bDefault) != 'boolean' ) {
    bDefault = false;
  }
  if(!GptUtils.exists(vBool) || !GptUtils.exists(vBool.toString)) {
    return bDefault;
  }
  var tmp = GptUtils.trim(vBool.toString());
  
  if(tmp == 'true' || (typeof(vBool) == 'number' && vBool != 0) ) {
     return true;
  } else {
     return false;
  }
}

/*
 * 
 */
GptUtils.isFunction = function(fArg) {
	if(!GptUtils.exists(fArg)) {
		return false;
	}
	return (typeof(fArg) == 'function');
}

/*
 * 
 */
GptUtils.addLoadEvent = function(eventFunc) {
  var loadFunction = window.onload;

  window.onload = function() {
    if(typeof loadFunction == 'function') {
      loadFunction();
    }
    if(typeof eventFunc == 'function') {
      eventFunc();
    }
  }
}

/*
 * 
 */
GptUtils.addWindowResize = function(eventFunc) {
  var loadFunction = window.onresize;

  window.onresize = function() {
    if(typeof loadFunction == 'function') {
      loadFunction();
    }
    if(typeof eventFunc == 'function') {
      eventFunc();
    }
  }
}

GptUtils.ajxGetAjaxObject = function() {
  var ajaxObj = null;
  
  try{
    ajaxObj = new XMLHttpRequest();
  } catch(e){
  
    try{
      ajaxObj = new ActiveXObject("Msxml2.XMLHTTP");
    
    }catch(e){
     
      try{
         ajaxObj = new ActiveXObject("Microsoft.XMLHTTP");
       }catch(e){
       
         
           try{
             
             ajaxObj = new window.createRequest();
           
           }catch(e){
             ;
           }
         
       }
    
    }
  }
  return ajaxObj;
}

GptUtils.ajxSndGetRtrnXml = function(sUrl, fCalFunction) {
  
   var ajaxObj = GptUtils.ajxGetAjaxObject();
   ajaxObj.onreadystatechange = function() {
    var rState = ajaxObj.readyState;
    alert(rState);
    if(rState == 4) {
       debugger;
       fCalFunction(ajaxObj.responseXml);
    }   
   }
   try {
     ajaxObj.open("GET", sUrl, true);
     ajaxObj.send(null);
   }catch(err) {   
     
   }
}

GptUtils.checkImgError = function(elImg) {
  if(elImg == null || 
      (typeof(elImg.corrected) != 'undefined' && elImg.corrected == true)) {
    return;
  }
  elImg.corrected = true;
  elImg.style.visibility="hidden";
  elImg.style.display="none";
}



