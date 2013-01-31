/**
 * Search hint.
 */

var Hints = new Array();

function hint(name, count) {
	this.name = name;
	this.count = count;
}

$(document).ready(function(){  
	var ctxPath = $("#schContextPath").val();
	var hintPrompt = $("#schHintPrompt").val();
	var sUrl =  ctxPath + "/rest/index/stats/fields?field=keywords&max=5000&f=json";
	
	$.get(sUrl,
        function(response) {
		  $("#hints").html(response.toString());          

		  var responseObject = eval('(' + response + ')');

  		  jQuery.each(responseObject.terms, function() {
  			Hints[Hints.length] = new hint(this.name, this.documents);      			
  		  });

  		  $("#hints").html(hintPrompt);          
        }
	);
	
	$("input[id='frmSearchCriteria:scText']").keyup( function() {
		var searchText = this.value;
		var sMatches = "";
		var nMaxMatches = 10;
		var nCountMatches = 0;
		
		if (searchText.length>=2) {
			jQuery.each(Hints, function() {
		  	  if (this.name.indexOf(searchText)>-1) {
		  		  if (nCountMatches < nMaxMatches) {
		  			  var sText = "keywords:\""  + this.name + "\"";
			  		  sMatches += "<a href=\" " +ctxPath + "/rest/find/document?f=searchpage&searchText=" + encodeURIComponent(sText) + "\" target=\"_top\">" + this.name + " (" + this.count + ")</a><br/>";	  			  
		  		  }
		  		  nCountMatches++;
		  		  if (nCountMatches >= nMaxMatches) return false;
		  	  }
		    });
		}
	    $("#hints").html(sMatches);
	  });
});






