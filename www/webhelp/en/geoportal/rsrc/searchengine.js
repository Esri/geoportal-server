// By ESRI

$(document).ready(function(){	
	//l10n: set the label on the search button
	$("#searchbutton").attr ("value", txtSearchButton_se)
});

var SearchEngine = (function() {
    //private
    
    //weighted set union
    //modifies first argument
    function union(set, newSet) {
        for (var i in newSet) {
            set[i] = (set[i] || 0) + newSet[i];
        }
    }
    
    //weighted set intersection
    //modifies first argument
    //unused in phase 1
    function intersection(set, newSet) {
        for (var i in set) {
            if (newSet[i]) {
                set[i] += newSet[i];
            } else {
                delete set[i];
            }
        }
    }
    
    function doSearch(terms, combineFn) {
        var resultSet = {};
        for (var i=0, len=terms.length; i<len; i++) {
            //get a set of results for the current term (or the empty set)
            var currentSet = searchIndex.dict[terms[i].toLocaleUpperCase()] || {};
            
            //add the items into the result set
            combineFn(resultSet, currentSet);
        }
        return resultSet;
    }
    
    function orSearch(terms) {
        return doSearch(terms, union);
    }
    
    function andSearch(terms) {
        return doSearch(terms, intersection);
    }
    
    function createResultList(set) {
        var resultList = [];
        for (var i in set) {
            resultList.push({
                title: searchIndex.titles[i],
                link: searchIndex.links[i],
                weight: set[i]
            });
        }
        return resultList;
    }
    
    function sortResultList(list) {
        list.sort(function(a, b){
            return (b.weight - a.weight) || (a.title < b.title ? -1 : 1);
        });
        return list;
    }
    
	function chopTerms (terms) {
		var result = [];
		for (var i=0, len=terms.length; i<len; i++) {
			var term = terms[i];
			if (term.match (/\W+/) != null){
				for (var j=0, len1=term.length; j<len1; j++) {
					result.push (term.substr (j, 1));
				}
			} else {
				result.push (term);
			}
		}
		
		return result;
	}
	
    //public
    return {
        search: function(searchString) {
            //clean up search string and create array of terms
            var terms = searchString.replace(/[.,]/,' ').replace(/\s{2,}/,' ').split(' ');
            
			var lang = "en";
			if (getLang() === undefined) {
				lang = "en";
			} else {
				lang = getLang();
			}
			if (lang == "ja" || lang == "zh") {
				terms = chopTerms (terms);
			}
			
            var orSet = orSearch(terms);
			var andSet = andSearch(terms);
			
            for (var i in andSet) {
                delete orSet[i];
            }
            
            var finalList = sortResultList(createResultList(andSet));
            var orList = sortResultList(createResultList(orSet));
            
            for (var i=0, len=orList.length; i<len; i++) {
                finalList.push(orList[i]);
            }
            
            return finalList;
        }
    };
})();

//search UI
$(function() {
	$("#searchwindow").dialog({
		autoOpen : false,
		position: ["center", "bottom"],
		width: "50%",
		height: 250,
		title : "Search Results"
	});
		
	function toID (val) {
		var v =  val.split ("/")[1];
		return v.split (".")[0];
	}
		
	$("#searchbutton").click (function (){
		var kw = $("#searchdata").val();
		if (!kw || kw.length == 0) {
			$("#searchwindow").html("").dialog("open");
		} else {
			var valL = SearchEngine.search(kw);
			var s = new Array();
			for (var i=0, len=valL.length; i<len; i++) {
				s.push ('<span class="searchtext_num">' + (i+1).toString() + '. </span>' + '<a href="#//'+ toID(valL[i].link) + '" onclick="TOC.openToNode(\''+toID(valL[i].link)+'\');HelpDoc.getDoc(\''+ toID(valL[i].link)+'\');return false;">'+valL[i].title+'</a><br/>');
			}
			htmStr = s.join ("");
			if (htmStr.length>0) {
				$("#searchwindow").html(htmStr).dialog("open");
			} else {
				$("#searchwindow").html("No result found").dialog("open");
			}
		}
	});
});	
