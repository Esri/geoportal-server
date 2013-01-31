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
package com.esri.gpt.catalog.management;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.request.QueryCriteria;
import com.esri.gpt.framework.request.SortOption;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.DateRange;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

/**
 * Maintains the query criteria for a manage metadata request.
 */
public class MmdQueryCriteria extends QueryCriteria {

// class variables =============================================================
  private static final Logger LOGGER = Logger.getLogger(MmdQueryCriteria.class.getCanonicalName());
// instance variables ==========================================================
  private String _approvalStatus;
  private String _collectionUuid = "";
  private DateRange _dateRange;
  private String _owner = "";
  private String _pubMethod;
  private String _title = "";
  private String _uuid = "";
  private String _siteUuid = "";
  private String _protocolType = "";

// constructors ================================================================
  /** Default constructor. */
  public MmdQueryCriteria() {
    _approvalStatus = MmdEnums.ApprovalStatus.any.toString();
    _pubMethod = MmdEnums.PublicationMethod.any.toString();
    setDateRange(new DateRange());
    getSortOption().setColumnKey("updatedate");
    getSortOption().setDirection(SortOption.SortDirection.desc);
  }

// properties ==================================================================

  /**
   * Checks if query criteria are empty.
   * @return <code>true</code> if query criteria are empty
   */
  public boolean getIsEmpty() {
    return (getApprovalStatus().length()==0 || getApprovalStatus().equals(MmdEnums.ApprovalStatus.any.toString())) &&
           (getDateRange().getFromDate()==null || getDateRange().getFromDate().getDate().length()==0) &&
           (getDateRange().getToDate()==null || getDateRange().getToDate().getDate().length()==0) &&
           getOwner().length()==0 &&
           (getPublicationMethod().length()==0 || getPublicationMethod().equals(MmdEnums.PublicationMethod.any.toString())) &&
           getTitle().length()==0 &&
           getUuid().length()==0 &&
           getSiteUuid().length()==0 &&
           getCollectionUuid().length()==0 &&
           getProtocolType().length()==0;
  }

  /**
   * Gets query criteria as XML string.
   * @return query criteria as XML string
   */
  public String getContentAsXml() {
    StringBuilder sb = new StringBuilder();
    sb.append("<?xml version=\"1.0\"?>");
    sb.append("<MmdQueryCriteria>");
    if (getApprovalStatus().length()>0 && !getApprovalStatus().equals(MmdEnums.ApprovalStatus.any.toString()) ) {
      sb.append("<ApprovalStatus>").append(Val.escapeXml(getApprovalStatus())).append("</ApprovalStatus>");
    }
    if (getDateRange().getFromDate().getDate().length()>0) {
      sb.append("<FromDate>").append(Val.escapeXml(getDateRange().getFromDate().getDate())).append("</FromDate>");
    }
    if (getDateRange().getToDate().getDate().length()>0) {
      sb.append("<ToDate>").append(Val.escapeXml(getDateRange().getToDate().getDate())).append("</ToDate>");
    }
    if (getOwner().length()>0) {
      sb.append("<Owner>").append(Val.escapeXml(getOwner())).append("</Owner>");
    }
    if (getPublicationMethod().length()>0 && !getPublicationMethod().equals(MmdEnums.PublicationMethod.any.toString())) {
      sb.append("<PublicationMethod>").append(Val.escapeXml(getPublicationMethod())).append("</PublicationMethod>");
    }
    if (getTitle().length()>0) {
      sb.append("<Title>").append(Val.escapeXml(getTitle())).append("</Title>");
    }
    if (getUuid().length()>0) {
      sb.append("<Uuid>").append(Val.escapeXml(getUuid())).append("</Uuid>");
    }
    if (getSiteUuid().length()>0) {
      sb.append("<SiteUuid>").append(Val.escapeXml(getSiteUuid())).append("</SiteUuid>");
    }
    if (getCollectionUuid().length()>0) {
      sb.append("<CollectionUuid>").append(Val.escapeXml(getCollectionUuid())).append("</CollectionUuid>");
    }
    if (getProtocolType().length()>0) {
      sb.append("<ProtocolType>").append(Val.escapeXml(getProtocolType())).append("</ProtocolType>");
    }
    sb.append("</MmdQueryCriteria>");
    return sb.toString();
  }

  /**
   * Sets query criteria from XML string.
   * @param content query criteria from XML string
   */
  public void setContentAsXml(String content) {
    try {
      reset();

      Document doc = DomUtil.makeDomFromString(content, false);

      XPath xPath = XPathFactory.newInstance().newXPath();

      setApprovalStatus((String) xPath.evaluate("/MmdQueryCriteria/ApprovalStatus", doc, XPathConstants.STRING));
      getDateRange().getFromDate().setDate((String) xPath.evaluate("/MmdQueryCriteria/FromDate", doc, XPathConstants.STRING));
      getDateRange().getToDate().setDate((String) xPath.evaluate("/MmdQueryCriteria/ToDate", doc, XPathConstants.STRING));
      setOwner((String) xPath.evaluate("/MmdQueryCriteria/Owner", doc, XPathConstants.STRING));
      setPublicationMethod((String) xPath.evaluate("/MmdQueryCriteria/PublicationMethod", doc, XPathConstants.STRING));
      setTitle((String) xPath.evaluate("/MmdQueryCriteria/Title", doc, XPathConstants.STRING));
      setUuid((String) xPath.evaluate("/MmdQueryCriteria/Uuid", doc, XPathConstants.STRING));
      setSiteUuid((String) xPath.evaluate("/MmdQueryCriteria/SiteUuid", doc, XPathConstants.STRING));
      setCollectionUuid((String) xPath.evaluate("/MmdQueryCriteria/CollectionUuid", doc, XPathConstants.STRING));
      setProtocolType((String) xPath.evaluate("/MmdQueryCriteria/ProtocolType", doc, XPathConstants.STRING));

    } catch (Exception ex) {
      LOGGER.log(Level.FINER, "Invalid content.", ex);
    }
  }

  /**
   * Gets content as encrypted string.
   * @return content as encrypted string
   */
  public String getContentAsEncryptedString() {
    try {
      return com.esri.gpt.framework.security.codec.Base64.encode(getContentAsXml(), "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      return "";
    }
  }

  /**
   * Sets content as encrypted string.
   * @param content content as encrypted string
   */
  public void setContentAsEncryptedString(String content) {
    try {
      setContentAsXml(com.esri.gpt.framework.security.codec.Base64.decode(content, "UTF-8"));
    } catch (IOException ex) {
      setContentAsXml("");
    }
  }
  /**
   * Resets criteria.
   */
  @Override
  public void reset() {
    _approvalStatus = MmdEnums.ApprovalStatus.any.toString();
    _pubMethod = MmdEnums.PublicationMethod.any.toString();
    _title = "";
    _uuid = "";
    _siteUuid = "";
    _protocolType = "";
    _collectionUuid = "";
    setDateRange(new DateRange());
    getSortOption().setColumnKey("updatedate");
    getSortOption().setDirection(SortOption.SortDirection.desc);
  }

  /**
   * Gets the approval status.
   * @return the approval status
   */
  public String getApprovalStatus() {
    return _approvalStatus;
  }

  /**
   * Sets the approval status.
   * @param status the approval status
   */
  public void setApprovalStatus(String status) {
    status = Val.chkStr(status);
    try {
      _approvalStatus = MmdEnums.ApprovalStatus.valueOf(status).toString();
    } catch (IllegalArgumentException ex) {
      _approvalStatus = MmdEnums.ApprovalStatus.any.toString();
    }
  }
  
  /**
   * Gets the collection UUID.
   * @return the collection UUID
   */
  public String getCollectionUuid() {
    return _collectionUuid;
  }

  /**
   * Sets the collection UUID.
   * @param colUuid the collection UUID
   */
  public void setCollectionUuid(String colUuid) {
    this._collectionUuid = UuidUtil.addCurlies(colUuid);
  }

  /**
   * Gets the date range.
   * @return the date range
   */
  public DateRange getDateRange() {
    return _dateRange;
  }

  /**
   * Sets the date range.
   * @param dateRange date range
   */
  private void setDateRange(DateRange dateRange) {
    _dateRange = dateRange;
  }

  /**
   * Gets the document owner.
   * @return the document owner
   */
  public String getOwner() {
    return _owner;
  }

  /**
   * Sets the document owner.
   * @param owner the document owner
   */
  public void setOwner(String owner) {
    _owner = Val.chkStr(owner);
  }

  /**
   * Gets the publication method.
   * @return the publication method
   */
  public String getPublicationMethod() {
    return _pubMethod;
  }

  /**
   * Sets the publication method.
   * @param method the publication method
   */
  public void setPublicationMethod(String method) {
    method = Val.chkStr(method);
    try {
      _pubMethod = MmdEnums.PublicationMethod.valueOf(method).toString();
    } catch (IllegalArgumentException ex) {
      _pubMethod = MmdEnums.PublicationMethod.any.toString();
    }
  }

  /**
   * Gets the document title.
   * @return the document title
   */
  public String getTitle() {
    return _title;
  }

  /**
   * Sets the document title.
   * @param title the document title
   */
  public void setTitle(String title) {
    _title = Val.chkStr(title);
  }

  /**
   * Gets the document UUID.
   * @return the document UUID
   */
  public String getUuid() {
    return _uuid;
  }

  /**
   * Sets the document UUID.
   * @param uuid the document UUID
   */
  public void setUuid(String uuid) {
    _uuid = UuidUtil.addCurlies(uuid);
  }

  /**
   * Gets site UUID.
   * @return site UUID
   */
  public String getSiteUuid() {
    return _siteUuid;
  }

  /**
   * Sets site UUID.
   * @param siteUuid site UUID
   */
  public void setSiteUuid(String siteUuid) {
    this._siteUuid = UuidUtil.addCurlies(siteUuid);
  }

  /**
   * Gets protocol type.
   * @return protocol type
   */
  public String getProtocolType() {
    return _protocolType;
  }

  /**
   * Sets protocol type.
   * @param protocolType protocol type
   */
  public void setProtocolType(String protocolType) {
    this._protocolType = Val.chkStr(protocolType);
  }


  // methods =====================================================================
  
  /**
   * Appends WHERE phrase.
   * @param tableAlias alias of the table or <code>null</code> if no alias
   * @param wherePhrase where phrase holder where the phrase will be appended
   * @param publisher publisher which executes query
   * @return map of arguments to apply to the statement through {@link #applyArgs} method
   */
  public Map<String,Object> appendWherePhrase(String tableAlias, StringBuilder wherePhrase, Publisher publisher) {
    return this.appendWherePhrase(null,tableAlias,wherePhrase,publisher);
  }
  
  /**
   * Appends WHERE phrase.
   * @param context the active request context
   * @param tableAlias alias of the table or <code>null</code> if no alias
   * @param wherePhrase where phrase holder where the phrase will be appended
   * @param publisher publisher which executes query
   * @return map of arguments to apply to the statement through {@link #applyArgs} method
   */
  public Map<String,Object> appendWherePhrase(RequestContext context, String tableAlias, StringBuilder wherePhrase, Publisher publisher) {

    TreeMap<String,Object> args = new TreeMap<String,Object>();
    tableAlias = (tableAlias!=null? tableAlias+".": "");

    // determine if the database is case sensitive
    StringAttributeMap params  = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters();
    String s = Val.chkStr(params.getValue("database.isCaseSensitive"));
    boolean isDbCaseSensitive = !s.equalsIgnoreCase("false");
    
    // document title
    String sTitle = getTitle();
    if (sTitle.length() > 0) {
      boolean bForceLike = false;
      if (isDbCaseSensitive) {
        sTitle = appendValueFilter(wherePhrase, "UPPER(" +tableAlias+ "TITLE)",sTitle,bForceLike);
        args.put("sTitle", sTitle.toUpperCase());
      } else {
        sTitle = appendValueFilter(wherePhrase,tableAlias+"TITLE",sTitle,bForceLike);
        args.put("sTitle", sTitle);
      }
    }

    // document UUID
    String sDocUuid = getUuid();
    if (sDocUuid.length() > 0) {
      // search executed against GPT_ADMIN (B) table, because GPT_META (A) might
      // have DOCUUID as 'uniqueidentifier' type in case of SQL server, and that
      // doesn't work well with UUID with curlies.

      //sDocUuid = appendValueFilter(wherePhrase, "UPPER(" +tableAlias+ "DOCUUID)", sDocUuid, true);
      //args.put("sDocUuid", sDocUuid.toUpperCase());
      
      sDocUuid = appendValueFilter(wherePhrase, tableAlias+ "DOCUUID", sDocUuid, false);
      args.put("sDocUuid", sDocUuid);
    }

    // site UUID
    String sSiteUuid = getSiteUuid();
    if (sSiteUuid.length() > 0) {
      //sSiteUuid = appendValueFilter(wherePhrase, "UPPER(" +tableAlias+ "SITEUUID)", sSiteUuid, true);
      //wherePhrase.append(" AND UPPER(").append(tableAlias).append("DOCUUID) <> UPPER(").append(tableAlias).append("SITEUUID) ");
      //args.put("sSiteUuid", sSiteUuid.toUpperCase());
      
      sSiteUuid = appendValueFilter(wherePhrase,tableAlias+"SITEUUID",sSiteUuid,false);
      wherePhrase.append(" AND ").append(tableAlias).append("DOCUUID <> ").append(tableAlias).append("SITEUUID ");
      args.put("sSiteUuid", sSiteUuid);
    }

    // document owner
    Publisher owner = null;
    if (getOwner().length()>0) {
      RequestContext context2 = null;
      try {
        if (context != null) {
          owner = new Publisher(context, getOwner());
        } else {
          context2 = RequestContext.extract(null);
          owner = new Publisher(context2, getOwner());
        }
      } catch (Exception ex) {
        LOGGER.log(Level.FINER, "Error creating publisher", ex);
      } finally {
        if (context2 != null) context2.onExecutionPhaseCompleted();
      }
    } else {
      if (publisher!=null && !publisher.getIsAdministrator()) {
        owner = publisher;
      }
    }
    if (owner!=null) {
      appendExpression(wherePhrase, "" +tableAlias+ "OWNER = ?");
      args.put("nUserId", new Integer(owner.getLocalID()));
    }

    // approval status
    String sStatus = getApprovalStatus();
    if ((sStatus.length() > 0) && !sStatus.equals(MmdEnums.ApprovalStatus.any.toString())) {
      if (sStatus.equals(MmdEnums.ApprovalStatus.posted.toString())) {
        String sExpr = "" +tableAlias+ "APPROVALSTATUS = ? OR " +tableAlias+ "APPROVALSTATUS IS NULL";
        appendExpression(wherePhrase, sExpr);
      } else {
        sStatus = appendValueFilter(wherePhrase, "" +tableAlias+ "APPROVALSTATUS", sStatus, false);
      }
      args.put("sStatus", sStatus);
    }

    String sProtocolType = getProtocolType();
    if (sProtocolType.length()>0) {
      if (isDbCaseSensitive) {
        sProtocolType = appendValueFilter(wherePhrase, "UPPER(" +tableAlias+ "PROTOCOL_TYPE)", sProtocolType, false);
        args.put("sProtocolType", sProtocolType.toUpperCase());
      } else {
        sProtocolType = appendValueFilter(wherePhrase,tableAlias+"PROTOCOL_TYPE",sProtocolType, false);
        args.put("sProtocolType", sProtocolType);
      }
    }

    // date range
    Timestamp tsFrom = getDateRange().getFromTimestamp();
    Timestamp tsTo = getDateRange().getToTimestamp();
    if (tsFrom != null) {
      appendExpression(wherePhrase, "" +tableAlias+ "UPDATEDATE >= ?");
      args.put("tsFrom", tsFrom);
    }
    if (tsTo != null) {
      appendExpression(wherePhrase, "" +tableAlias+ "UPDATEDATE <= ?");
      args.put("tsTo", tsTo);
    }

    // publication method
    String sPubMethod = getPublicationMethod();
    if ((sPubMethod.length() > 0)
        && !sPubMethod.equals(MmdEnums.PublicationMethod.any.toString())) {
      if (sPubMethod.equals(MmdEnums.PublicationMethod.other.toString())) {
        String sExpr = "" +tableAlias+ "PUBMETHOD = ? OR " +tableAlias+ "PUBMETHOD IS NULL";
        appendExpression(wherePhrase, sExpr);
      } else {
        sPubMethod = appendValueFilter(wherePhrase,"" +tableAlias+ "PUBMETHOD",sPubMethod,false);
      }
      args.put("sPubMethod", sPubMethod);
    }

    return args;
  }

  /**
   * Applies arguments to the statement.
   * @param st statement to apply arguments
   * @param n initial index of the first argument
   * @param args map of arguments obtained through {@link #appendWherePhrase} method
   * @throws SQLException if arguments application fails
   */
  public int applyArgs(PreparedStatement st, int n, Map<String,Object> args) throws SQLException {

    // document title
    String sTitle = Val.chkStr(args.get("sTitle")!=null? args.get("sTitle").toString(): null);
    if (sTitle.length() > 0) {
      st.setString(n, sTitle);
      n++;
    }

    // document UUID
    String sDocUuid = Val.chkStr(args.get("sDocUuid")!=null? args.get("sDocUuid").toString(): null);
    if (sDocUuid.length() > 0) {
      st.setString(n, sDocUuid);
      n++;
    }

    // site UUID
    String sSiteUuid = Val.chkStr(args.get("sSiteUuid")!=null? args.get("sSiteUuid").toString(): null);
    if (sSiteUuid.length() > 0) {
      st.setString(n, sSiteUuid);
      n++;
    }

    // document owner
    int nUserId = args.get("nUserId") instanceof Integer? (Integer)args.get("nUserId"): -1;
    if (nUserId>=0) {
      st.setInt(n, nUserId);
      n++;
    }

    // approval status
    String sStatus = Val.chkStr(args.get("sStatus")!=null? args.get("sStatus").toString(): null);
    if (sStatus.length() > 0) {
      st.setString(n, sStatus);
      n++;
    }

    // protocol type
    String sProtocolType = Val.chkStr(args.get("sProtocolType")!=null? args.get("sProtocolType").toString(): null);
    if (sProtocolType.length() > 0) {
      st.setString(n, sProtocolType);
      n++;
    }

    // date range
    // ArcIMS stores dates as seconds from the epoch,
    // we need to convert from milli-seconds
    Timestamp tsFrom = args.get("tsFrom") instanceof Timestamp? (Timestamp)args.get("tsFrom"): null;
    if (tsFrom != null) {
      st.setTimestamp(n,tsFrom);
      n++;
    }
    Timestamp tsTo = args.get("tsTo") instanceof Timestamp? (Timestamp)args.get("tsTo"): null;
    if (tsTo != null) {
      st.setTimestamp(n,tsTo);
      n++;
    }

    // publication method
    String sPubMethod = Val.chkStr(args.get("sPubMethod")!=null? args.get("sPubMethod").toString(): null);
    if (sPubMethod.length() > 0) {
      st.setString(n, sPubMethod);
      n++;
    }
    return n;
  }
  
/**
 * Appends a value filter to an SQL where clause.
 * <p>
 * This is intended for use within a PreparedStatement. The appended
 * filter will have the following forms:
 * <br/>field = ?
 * <br/>field LIKE ?
 * <p>
 * The value returned should be bound within the PreparedStatement:
 * <br/>preparedStatement.setString(n,value)
 * @param whereClause the where clause
 * @param field the field
 * @param value the field value
 * @param forceLike if true force an SQL LIKE
 * @return the value - possible modified for LIKE (ie. "%"+value+"%")
 */
protected String appendValueFilter(StringBuilder whereClause,
                                   String field,
                                   String value,
                                   boolean forceLike) {
  value = value.replaceAll("\\*","%");
  String sExpression = "";
  if (value.indexOf("%") != -1) {
    sExpression = field+" LIKE ?";
  } else if (forceLike) {
    value = "%"+value+"%";
    sExpression = field+" LIKE ?";
  } else {
    sExpression = field+" = ?";
  }
  appendExpression(whereClause,sExpression);
  return value;
}

/**
 * Appends an expression to an SQL where clause.
 * @param whereClause the where clause
 * @param expression the expression
 */
protected void appendExpression(StringBuilder whereClause,
                                String expression) {
  if (whereClause.length() > 0) {
    whereClause.append(" AND ");
  }
  whereClause.append("(").append(expression).append(")");
}
}
