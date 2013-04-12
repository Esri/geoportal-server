import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.discovery.rest.RestQueryParser;
import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.control.georss.RestQueryServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
public class CustomRestQueryServlet extends RestQueryServlet {
private static String REST_PARAM_KEY1 = "hierarchy";
//Relate the rest queryable to the CSW queryables 
protected RestQuery parseRequest(HttpServletRequest request, RequestContext context) {
Logger LOG = Logger.getLogger(RestQuery.class.getCanonicalName());
	RestQuery query = super.parseRequest(request, context);
RestQueryParser parser = new RestQueryParser(request,context,query);
// "hierarchy" will be the name of the rest queryable
parser.parsePropertyIsLike(REST_PARAM_KEY1, "apiso.Type");
/** The below is shown as an example
parser.parseRepositoryId("rid");
parser.parseResponseFormat("f");
parser.parseResponseGeometry("geometryType");
parser.parseResponseStyle("style");
parser.parseResponseTarget("target");
parser.parseStartRecord("start",1);
parser.parseMaxRecords("max",10);
parser.parsePropertyIsEqualTo("uuid","uuid");
parser.parsePropertyIsLike("searchText","anytext");
parser.parsePropertyList("contentType","dc:type",",",true);
parser.parsePropertyList("dataCategory","dc:subject",",",true);
parser.parsePropertyRange("after","before","dct:modified");
parser.parseSpatialClause("bbox","spatialRel","geometry");
parser.parseSortables("orderBy");
**/
LOG.log(Level.FINER, "In Custom Rest Query Servlet");

return query;
}
//Populate the searchCriteria with the rest queryable hierarchy
protected SearchCriteria toSearchCriteria(HttpServletRequest request, 
RequestContext context, RestQuery query) {
SearchCriteria criteria = super.toSearchCriteria(request, context, query);
RestQueryParser parser = new RestQueryParser(request,context, query);
String sHierarchy = Val.chkStr(parser.getRequestParameter(REST_PARAM_KEY1));
if (sHierarchy.length() > 0) {
SearchFilterHierarchy filterHierarchy = new SearchFilterHierarchy();
filterHierarchy.setHierarchy(sHierarchy);
criteria.getMiscelleniousFilters().add(filterHierarchy);
}

return criteria;
}
}