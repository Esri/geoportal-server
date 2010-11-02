
//global var

var gMetaCache = {};


// --- general ---

function getCurrentID() {
    var token = HashManager.getHash().token;
    if (token == "" || typeof token == UNDEFINED) {
        token = tocMap.startNode;
    }
    
    return token;
}


function getCurrentUrl () {
	var curID = getCurrentID();
	var loc = window.location; 
	var url = loc.protocol + "//" + loc.host + loc.pathname.replace ("index.html", "");
	
	url =  url + curID.substr (0,4) + "/" + curID + ".htm";
		
	return url;
}

function getCurrentTitle () {
	var token = getCurrentID();
	if (token in tocMap.tree) {
		return tocMap.tree [token].l;
	} else {
		return txtUnknownTopicTitle_navbar;
	}
}

// -- prev/next

function goMap(mapId, firstlast) {
	if (mapId in gMetaCache) {
		val0 = gMetaCache[mapId][firstlast];
		TOC.openToNode(val0);
		HelpDoc.getDoc(val0);
	} 
}


function goAsk(mapId, direction, firstlast) {

	var childInfo = gMetaCache[mapId];
    var val = childInfo[direction];
	if (val == "none") {
		//noop
	
	} else if (val.substr(0, 4) == "map_") {
		goMap(val.substr(4, 4), firstlast);
    
	} else {
		TOC.openToNode(val);
		HelpDoc.getDoc(val);
    }
    
}

function binaryGoAsK (mapId, direction, firstlast) {
	//only need to know it is none or (not none)
	var val = "none";
	if (mapId in gMetaCache) {
		var childInfo = gMetaCache[mapId];
		val = childInfo[direction];
	} else {
		//inline map case
		val = "notnone";
	}
	
	return val;
}

function isNoPrev(obj) {
	var vcss = obj.attr ('class');
	return ( vcss == "noprevButton"  || vcss == "noprevButton_h");
}


function prevPageH(evt) {

	if (!isNoPrev($(this))) {
		var token = getCurrentID();
		var nodeInfo = tocMap.tree [token];
		var val = nodeInfo.u;
    
		if (val == "none") {
			//noop
		} else if (val.substr(0, 4) == "map_") {
			goMap(val.substr(4, 4), "last");
    
		} else if (val.substr(0, 6) == "askup_") {
			goAsk(val.substr(6, 4), "up", "last");
    
		} else if (val != "") {
			TOC.openToNode(val);
			HelpDoc.getDoc(val.substr(0, HelpDoc.tokenLength));
		}
	}
    
    return false;
}

function isNoNext(obj) {
	var vcss = obj.attr ('class');
	return ( vcss == "nonextButton"  || vcss == "nonextButton_h");
}

function nextPageH(evt) {

	if (!isNoNext($(this))) {
		var token = getCurrentID();
		var nodeInfo = tocMap.tree [token];
		var val = nodeInfo.d;
	
		if (val == "none") {
			//noop
		} else if (val.substr(0, 4) == "map_") {
			goMap(val.substr(4, 4), "first");
    
		} else if (val.substr(0, 8) == "askdown_") {
			goAsk(val.substr(8, 4), "down", "first");
    
		} else if (val != "") {
			TOC.openToNode(val);
			HelpDoc.getDoc(val.substr(0, HelpDoc.tokenLength));
		}
	}
	
	return false;
    
    
}

function navEnabled (nodeID, direction) {
	var val = "none";
	
	if (nodeID in tocMap.tree) {
		val = tocMap.tree [nodeID][direction];
			
		if (val.substr(0, 8) == "askdown_") {
			val = binaryGoAsK (val.substr(8, 4), "down", "first");
		} else if (val.substr(0, 8) == "askup_") {
			val = binaryGoAsk (val.substr(8, 4), "up", "last");
		}
	}
	return val != "none";
	
}

// ---- printing ----

function insertPrintBreadCrumb (txt) {
    var curID = getCurrentID();		
	var crumbMdl = genCrumbModel (curID);
	var breadcrumb = genPrintBreadCrumb (crumbMdl); 

	var begI = txt.search (/breadcrumb/);
	var newTxt = "";
	if (begI >=0) {
		var pat = 'breadcrumb">';
		newTxt = txt.slice (0, begI+pat.length) + breadcrumb +  txt.slice (begI+pat.length, txt.length);
	} else {
		newTxt = txt;
	}
	
	return newTxt;
}



function genPrintMetadata () {
	var buf = [];
	buf.push ('<div id="printmetadata">');
	buf.push (getCurrentUrl());
	buf.push ('</div>');	
	return buf.join ("");
}

function printPage(token) {
    
    var newwin = window.open(); 
    var result = "Missing Content";
    
    $.ajax({
        async: false,
		type : 'GET',
        url: token.substr(0, 4) + "/" + token + ".htm",
        success : function (data) {
            var startIndex = data.indexOf('<div');
            var endIndex = data.lastIndexOf('</div>') + 6;
            if (startIndex > - 1) {
                result = data.substring(startIndex, endIndex);
            }
        },
        dataType: "html"
    });
    
    var buf = [];
    buf.push('<html><body>');
    buf.push('<head>');
    buf.push('<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />');
    buf.push('<title>');
	if (token in tocMap.tree) {
		buf.push (tocMap.tree [token].l);
	} else {
		buf.push ("No Title");
	}
	
	buf.push('</title>');
	buf.push('<link rel="stylesheet" type="text/css" href="' + fpPrintCSS + '" />');
    buf.push('</head>');
	buf.push('<body>');

	result = insertPrintBreadCrumb(result);
    buf.push (result);
	
	buf.push (genPrintMetadata());
    
	buf.push('</body></html>');
    
    newwin.document.write(buf.join(""));
    newwin.document.close();
    newwin.focus();
}

function printPageH(evt) {
    var token = getCurrentID();
    printPage(token);
    evt.stopImmediatePropagation();
    return false;
}


// --- bread crumb -----

function askMap(mapId, firstlast) {
	if (mapId in gMetaCache) {
		return gMetaCache[mapId][firstlast];
	} else {
		return "none";
	}
}

function getFirstTopicToken(folderTkn) {
    var nodeInfo = tocMap.tree [folderTkn];
    var childL;
    
    if (nodeInfo.hasOwnProperty("c")) {
        
        childL = nodeInfo.c;
    } else if (nodeInfo.hasOwnProperty("m")) {
        
        return askMap(nodeInfo.m, "first");
    } else {
        return "none";
    }
    
	if (childL.length == 0) {
		return "none";
	}
	
    var firstKidId = childL[0];

    if (firstKidId.substr(0, 4) == "map_") {

        return askMap(firstKidId.substr(4, 4), "first");

    } else {
        var firstKid = tocMap.tree [firstKidId];
        if (firstKid.hasOwnProperty("c")) {
            
            return getFirstTopicToken(firstKidId);

        } else if (firstKid.hasOwnProperty("m")) {

            return askMap(firstKid.m, "first");

        } else {
            
            return firstKidId;
        }
    }
}


function genCrummy (folderID) {
	var l = [];

    l.push ({ 
			   label : tocMap.tree [folderID].l,
			   kidtoken : getFirstTopicToken(folderID)
			});
		
	return l;
}

function genCrumbModel (curID) {

	var crummyL = [];

	if(typeof genHomeCrumb != UNDEFINED) {
		crummyL.push (genHomeCrumb ());
	}

	
	var pathL = TOC.getPathFromRoot(curID);
    for (var i = 0; i <pathL.length; i++) {
		crummyL.push (genCrummy (pathL[i]));
    }
		
	if (typeof genHomeCrumb != UNDEFINED) {
		if (curID in tocMap.tree) {
			crummyL.push ([{
						label : tocMap.tree [curID].l,
						 kidtoken : ""
						}]);
		}				
	}
	
	return {crumbL : crummyL};
	
}

function genAHref(node) {
    var label = node.label;
	var kidtkn = node.kidtoken;
    var url = kidtkn;

    if (kidtkn == "none") {
        url = "#";
    } else if (kidtkn.substr(0,4).toLowerCase() != "http") {
		url = kidtkn.substr(0,4) +"/" + kidtkn + ".htm";
	}
    
    if (url == "#") {
        return "<span>" + label + "</span>";
    }
    
    return "<a href='" + url + "' rel='" + url+"' >" + label + "</a>";	
}

function genBreadCrumb (mdl) {
	var crumbL = mdl.crumbL;
	var buf = [];
	
	if (isWebHelp && crumbL.length == 1) {
		buf.push (genAHref(crumbL[0][0]));
	} else {
		for (var i=0; i< crumbL.length-1; i+=1) {			
			buf.push (genAHref(crumbL[i][0]));
		}
	}
	
	return buf.join (" &#187; ");
}


function genPrintBreadCrumb (mdl) {
	var crumbL = mdl.crumbL;
	var buf = [];
	
	if (isWebHelp && crumbL.length == 1) {
		buf.push (crumbL[0][0].label);
	} else {
		for (var i=0; i< crumbL.length-1; i+=1) {			
			buf.push (crumbL[i][0].label);
		}
	}
	
	return buf.join (" &#187; ");
}

// --- feedback ---
function feedbackH(evt) {

    var url = getCurrentUrl();
	

	var buf = [];
	
	
	buf.push ('<form  id="feedbackForm" action="http://support.esri.com/en/feedback/submit" method="post">');
	buf.push ('<div class="fdbk_head">' + txtFeedbackTitle_navbar + '</div>');
	buf.push ('<p>'+ txtFeedbackpara_navbar + '</p>');
	buf.push ('<table>');
	
	buf.push ('<tr>');
	buf.push ('<td><label class="txtlbl">'+ txtFeedbackLbl0_navbar + '</label></td>');
	buf.push ('<td><input type="text" name="url" id="url" class="txtbox" size="70" value="' + url + '" /></td>');
	buf.push ('</tr>');

	buf.push ('<tr>');
	buf.push ('<td><label class="txtlbl">'+ txtFeedbackLbl1_navbar + '</label></td>');
	buf.push ('<td><textarea name="comment" id="comment" class="txtbox" rows="4" cols="60"></textarea></td>');
	buf.push ('</tr>');

	buf.push ('<tr>');	
	buf.push ('<td><label class="txtlbl">'+ txtFeedbackLbl2_navbar + '</label></td>');
	buf.push ('<td><input type="text" name="email" id="email" class="txtbox" size="30" value="" /></td>');
	buf.push ('</tr>');

	
	buf.push ('<tr>');	
	buf.push ('<td>&nbsp;</td>');
	buf.push ('<td><input disabled id="submitButton" class="submit" type="submit" value="' + txtFeedbackSubmit_navbar + '"/></td>');
	buf.push ('</tr>');

	buf.push ('</table>');
	
	buf.push ('</form>');
	
	$.fn.colorbox({
		transition:"none", 
		width:"730px", 
		height:"400px",
		opacity: 0.65,
		html : buf.join ("")
	});

	return false;

}

function feedbackValidation (evt) {
	
	if ($("#feedbackForm #comment").val.length > 0){
		$("#feedbackForm #submitButton").attr ("disabled",false);
	} else {
		$("#feedbackForm #submitButton").attr ("disabled",true);
	}
}


// --- email ---
function emailH (evt) {
	
	var subjTxt = "subject=" + encodeURIComponent (txtEmailSubject_navbar+ getCurrentTitle());
    var bodyTxt = "body=" + 
              encodeURIComponent (txtEmailBody_navbar) + 
              encodeURIComponent (getCurrentUrl());

	window.location.href = "mailto:?"+ subjTxt + "&" + bodyTxt;

    return false;	
}

// --- Switch To ---
function genDropdownSelection (valL) {
	var buf = [];

	if (typeof txtSwithtoHoverText == "undefined") {
		txtSwithtoHoverText = "";
	}
	
	buf.push ('<div id="ddbox">');
	buf.push ('<select align="top" id="switchto" title="' + txtSwithtoHoverText + '">');

	if (valL.length>0) {
		buf.push ('<option value="'+valL[0].kidtoken+'" selected>'+ valL[0].label+ '</option>');
	}
	for (var i=1; i<valL.length; i++) {
		buf.push ('<option value="'+valL[i].kidtoken+'">'+ valL[i].label + '</option>');
	}
	
	buf.push ('</select>');

	if ($.browser.msie) {
		buf.push ('<a id="ddclick" title="'+ txtDDClickTitle_navbar + '" href="#">&nbsp;</a>');
  }  
	buf.push ('</div>');
	
	return buf.join ("");

	
}

function switchtoH (evt) {
	window.location.href = $("#switchto").val();
	return false;
}

// --- init ---
function initNavBar() {

    var curID = getCurrentID();	
	
	var crumbMdl = genCrumbModel (curID);
	
	var breadcrumb = genBreadCrumb (crumbMdl); 

	var hasDropdown = false;

	var buf = [];	

	
	buf.push ('<span id="navbuttons">');	
	
	if(typeof genSwithToCrumb != UNDEFINED) {
		hasDropdown = true;
		buf.push (genDropdownSelection(genSwithToCrumb()));
		buf.push ('<div class="navbarspacer">&nbsp;</div>');
	}
		
	if (isWebHelp) {
		buf.push ('<a href="#" id="feedbackButton" class="feedbackButton" title="'+ txtFeedback_navbar +'">&nbsp;</a>');
	}
	
	buf.push ('<a href="#" id="emailButton" class="emailButton" title="' + txtEmail_navbar + '">&nbsp;</a>');
	
	buf.push ('<a href="#" id="printButton" class="printButton" title="'+ txtPrint_navbar + '">&nbsp;</a>');
	
	buf.push ('<div class="navbarspacer">&nbsp;</div>');
	
	try {
		if (navEnabled (curID, "u")) {
			buf.push ('<a href="#" id="prevButton" class="prevButton" title="' + txtPrevious_navbar + '">&nbsp;</a>');
		} else {
			buf.push ('<a href="#" id="prevButton" class="noprevButton" title="' + txtNoPrevious_navbar + '">&nbsp;</a>');
		}
			
		if (navEnabled (curID, "d")) {
			buf.push ('<a href="#" id="nextButton" class="nextButton" title="' + txtNext_navbar + '">&nbsp;</a>');
		} else {
			buf.push ('<a href="#" id="nextButton" class="nonextButton" title="' + txtNoNext_navbar + '">&nbsp;</a>');
		}
	} catch (err) {
	}
	

	buf.push ('</span>');
	buf.push ('<div id="feedbackWin"></div>');
	
	$("#ubertoolbar").html(buf.join ("")); 

	if (isWebHelp) {
		if (hasDropdown) {
			$("#ubertoolbar").css ("right", "30px");
		} else {
			//use the setting from navbar.css
		}
	}

    $("#docWrapper #breadcrumb").html(breadcrumb);
}

function preloadJsonMap (mapcache) {
    $.ajax({
        url: 'meta.js',
        success: function (data) {
			for (var key in data) {
				mapcache[key] = data[key];
			}
        },
        dataType: "json"
    });

}

$(document) .ready(function () {
	
	if ($.browser.msie) {
		$("#ddclick").live("click", switchtoH);
	} else {
		$("#switchto").live("change", switchtoH);
	}
		
    $("#ubertoolbar #emailButton").live("click", emailH);
    
	$("#ubertoolbar #feedbackButton").live("click", feedbackH);
	$("#feedbackForm #comment").live("keyup", feedbackValidation);

    $("#ubertoolbar #printButton").live("click", printPageH);
    $("#ubertoolbar #prevButton").live("click", prevPageH);
    $("#ubertoolbar #nextButton").live("click", nextPageH);
	
	preloadJsonMap (gMetaCache);

});
