define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/topic',
  'esri/request',
  'widgets/GeoportalSearch/common/QueryTask'
],function(declare,lang,
           topic,
           esriRequest,
           QueryTask){
    return declare(null, {

    query: null,
    results: null,
    nls: null,
    nItemsPerPage: 10,

    paginateResults: function(params){


      if(!params.query || !params.results || !params.container || !params.nls){
        return;
      }

      this.query = params.query;
      this.results = params.results;
      this.nls = params.nls;
      var container = params.container;

      if(params.nItemsPerPage){
       this.nItemsPerPage = params.nItemsPerPage;
      }

      var elHdr = container;
      var nStartIndex = this.query.start;
      var nTotalResults = this.results.totalResults;

      var currentPageNumber = Math.ceil(nStartIndex / this.nItemsPerPage);
      var nOfPages = Math.ceil(nTotalResults / this.nItemsPerPage);
      var iFrom = (currentPageNumber - 2) > 1 ? (currentPageNumber - 2) : 1;
      var iTo = (currentPageNumber + 2) > nOfPages ? nOfPages : (currentPageNumber + 2);
      if (iTo < 6) {
        iTo = nOfPages >= 5 ? 5 : nOfPages;
        iFrom = 1;
      }else if (iTo == nOfPages){
        iFrom = nOfPages - 4;
      }
      var nEndIndex = nStartIndex + this.nItemsPerPage - 1;
      if (nEndIndex > nTotalResults) nEndIndex = nTotalResults;

      if(this.elPageControl){
        elHdr.removeChild(this.elPageControl);
      }
      this.elPageControl = document.createElement("div");
      this.elPageControl.className = "gp-search__nav";

      var sPageSummary = this.nls.pageSummaryPattern;
      sPageSummary = sPageSummary.replace("{0}",nStartIndex);
      sPageSummary = sPageSummary.replace("{1}",nEndIndex);
      var elPageSummary = document.createElement("div");
      elPageSummary.className = "gp-search__result";
      elPageSummary.appendChild(document.createTextNode(sPageSummary));
      this.elPageControl.appendChild(elPageSummary);

      var elPageNumbers = document.createElement("div");
      elPageNumbers.className = "gp-search__page-numbers";
      this.elPageControl.appendChild(elPageNumbers);
      if (iFrom > 1) {
        var elPage = document.createElement("a");
        elPage.setAttribute("href","javascript:void(0);");
        elPage.pageNumber = 1;
        elPage.appendChild(document.createTextNode(this.nls.first));
        elPageNumbers.appendChild(elPage);
        dojo.connect(elPage,"onclick",this,"_onPageClicked");

         var elPage = document.createElement("a");
         elPage.setAttribute("href","javascript:void(0);");
         elPage.pageNumber = iFrom;
         elPage.appendChild(document.createTextNode("<"));
         elPageNumbers.appendChild(elPage);
         dojo.connect(elPage,"onclick",this,"_onPageClicked");
      }
      if (iTo > 1) {
        for (var i=iFrom; i<=iTo; i++) {
          var elPage = document.createElement("a");
          elPage.setAttribute("href","javascript:void(0);");
          elPage.pageNumber = i;
          elPage.appendChild(document.createTextNode(""+i));
          if (i == currentPageNumber) {
            elPage.className = "current";
          }
          elPageNumbers.appendChild(elPage);
          dojo.connect(elPage,"onclick",this,"_onPageClicked");

        }
      }

      if (iTo < nOfPages) {
        var elPage = document.createElement("a");
        elPage.setAttribute("href","javascript:void(0);");
        elPage.pageNumber = iTo;
        elPage.appendChild(document.createTextNode(">"));
        elPageNumbers.appendChild(elPage);
        dojo.connect(elPage,"onclick",this,"_onPageClicked");
        var elPage = document.createElement("a");
        elPage.setAttribute("href","javascript:void(0);");
        elPage.pageNumber = nOfPages;
        elPage.appendChild(document.createTextNode(this.nls.last));
        elPageNumbers.appendChild(elPage);
        dojo.connect(elPage,"onclick",this,"_onPageClicked");
      }

       elHdr.appendChild(this.elPageControl);

    },

  _onPageClicked: function(e){
    if (!e) e = window.event;
    var el = (window.event) ? e.srcElement : e.target;
    if ((el != null) && (el.pageNumber != null)) {
      this.query.start = ((el.pageNumber - 1) * this.nItemsPerPage) + 1;
      var queryTask = new QueryTask();
      queryTask.execute(this.query);
    }
  }

   });
});