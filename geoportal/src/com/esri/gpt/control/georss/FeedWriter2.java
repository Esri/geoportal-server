package com.esri.gpt.control.georss;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;

/**
 * Extended feed writer.
 */
public interface FeedWriter2 extends FeedWriter {

// properties ==================================================================
/**
 * Gets the message broker.
 *
 * @return the message broker
 */
public abstract MessageBroker getMessageBroker();

/**
 * Sets the message broker.
 *
 * @param messageBroker the new message broker
 */
public abstract void setMessageBroker(MessageBroker messageBroker);

/**
 * Gets the query.
 *
 * @return the query
 */
public abstract RestQuery getQuery();

/**
 * Sets the query.
 *
 * @param query the new query
 */
public abstract void setQuery(RestQuery query);

/**
 * Gets the response.
 *
 * @return the response
 */
public abstract HttpServletResponse getResponse();

/**
 * Sets the response.
 *
 * @param response the new response
 */
public abstract void setResponse(HttpServletResponse response);

/**
 * Gets the request.
 *
 * @return the request
 */
public abstract HttpServletRequest getRequest();

/**
 * Sets the request.
 *
 * @param request the new request
 */
public abstract void setRequest(HttpServletRequest request);

/**
 * Gets the context.
 *
 * @return the context
 */
public abstract RequestContext getContext();

/**
 * Sets the context.
 *
 * @param context the new context
 */
public abstract void setContext(RequestContext context);

/**
 * Write error.
 *
 * @param err the err
 */
public abstract void writeError(Throwable err);

/**
 * Write the search result.
 *
 * @param result the result
 */
public void write(SearchResult result);

/**
 * Gets the config parameters.
 *
 * @return the config parameters
 */
public Map<String, String> getConfigParameters();


/**
 * Sets the config parameters.
 *
 * @param configParameters the config parameters
 */
public void setConfigParameters(Map<String, String> configParameters); 

}