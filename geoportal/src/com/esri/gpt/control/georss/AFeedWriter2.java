package com.esri.gpt.control.georss;


import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.catalog.search.SearchResultRecords;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class FeedWriter2.
 */
public abstract class AFeedWriter2 implements FeedWriter2 {

// instance variables ==========================================================
/** The message broker. */
private MessageBroker messageBroker;

/** The query. */
private RestQuery query;

/** The response. */
private HttpServletResponse response;

/** The request. */
private HttpServletRequest request;

/** The context. */
private RequestContext context;

private Map<String, String> configParameters;


// properties ==================================================================
/**
 * Gets the message broker.
 *
 * @return the message broker
 */
@Override
public MessageBroker getMessageBroker() {
  return messageBroker;
}

/**
 * Sets the message broker.
 *
 * @param messageBroker the new message broker
 */
@Override
public void setMessageBroker(MessageBroker messageBroker) {
  this.messageBroker = messageBroker;
}

/**
 * Gets the query.
 *
 * @return the query
 */
@Override
public RestQuery getQuery() {
  return query;
}

/**
 * Sets the query.
 *
 * @param query the new query
 */
@Override
public void setQuery(RestQuery query) {
  this.query = query;
}

/**
 * Gets the response.
 *
 * @return the response
 */
@Override
public HttpServletResponse getResponse() {
  return response;
}

/**
 * Sets the response.
 *
 * @param response the new response
 */
@Override
public void setResponse(HttpServletResponse response) {
  this.response = response;
}

/**
 * Gets the request.
 *
 * @return the request
 */
@Override
public HttpServletRequest getRequest() {
  return request;
}

/**
 * Sets the request.
 *
 * @param request the new request
 */
@Override
public void setRequest(HttpServletRequest request) {
  this.request = request;
}

/**
 * Gets the context.
 *
 * @return the context
 */
@Override
public RequestContext getContext() {
  return context;
}

/**
 * Sets the context.
 *
 * @param context the new context
 */
@Override
public void setContext(RequestContext context) {
  this.context = context;
}

/**
 * Write error.
 *
 * @param err the err
 */
@Override
public abstract void writeError(Throwable err);


/**
 * Write the search result.
 *
 * @param result the result
 */
@Override
public  void write(SearchResult result) {
  write(new SearchResultRecordsAdapter(result.getRecords()));
}

/**
 * Gets the config parameters.
 *
 * @return the config parameters
 */
public Map<String, String> getConfigParameters() {
  return configParameters;
}

/**
 * Sets the config parameters.
 *
 * @param configParameters the config parameters
 */
public void setConfigParameters(Map<String, String> configParameters) {
  this.configParameters = configParameters;
}

}
