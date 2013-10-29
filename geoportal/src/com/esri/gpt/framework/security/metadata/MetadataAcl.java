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
package com.esri.gpt.framework.security.metadata;
import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.AuthenticationStatus;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.principal.Group;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is used to metadata build access control lists.
 */
public class MetadataAcl {

  /** class variables ========================================================= */

  /** Logger */
  private static Logger  LOGGER = Logger.getLogger(MetadataAcl.class.getName());
  
  /** instance variables ====================================================== */
  private RequestContext _requestContext = null;
  
  /**
   * Constructor.
   * @param context the active request context
   */
  public MetadataAcl(RequestContext context) {
    _requestContext = context;
  }
    
  /** methods ================================================================= */
  
  /**
   * Build list of selected groups
   * @param groups groups
   * @param pickedGroups picked groups
   * @return acl
   */
  public String buildAclGroups(Groups groups, ArrayList<String> pickedGroups) {
    // TODO: Transfer ownership of documents is an issue since
    // user might not be member of a group for acl is set for a document 
    Collection<Group> grps = groups.values();
    Groups selectedGroups = new Groups();
    for (String o : pickedGroups) {
      for (Group group : grps) {
        if (group != null && group.getName().equals(o)) {
          selectedGroups.add(group);
          break;
        }
      }
    }
  
    if (selectedGroups.size() > 0) {
  
    } else {
      for (String o : pickedGroups) {
        Group group = groups.get(o);
        if (group != null) {
          selectedGroups.add(group);
        }
      }
    }
  
    StringBuffer sbAclXml = new StringBuffer();
    if (selectedGroups != null && selectedGroups.isEmpty()) {
      return null;
    }
    sbAclXml.append("<acl>");
    for (Group group : selectedGroups.values()) {
      sbAclXml.append("<principal type=\"groupDn\">");
      sbAclXml.append(Val.escapeXml(group.getDistinguishedName()));
      sbAclXml.append("</principal>");
    }
    sbAclXml.append("</acl>");
    return sbAclXml.toString();
  }
  
  /**
   * 
   * @param xml
   * @return document ACL
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  private HashMap<String, String> buildDocumentAcl(String xml)
      throws ParserConfigurationException, SAXException, IOException {
    Document document = null;
    HashMap<String, String> acls = new HashMap<String, String>();
    if (xml != null && xml.trim().length() > 0) {
      document = DomUtil.makeDomFromString(xml, false);
    } 
    
    if (document != null) {
      NodeList principalNodes = document.getElementsByTagName("principal");
      if (principalNodes != null && principalNodes.getLength() > 0) {
        for (int i = 0; i < principalNodes.getLength(); i++) {
          Node principalNode = principalNodes.item(i);
          NamedNodeMap attributes = principalNode.getAttributes();
          if (attributes.getNamedItem("type").getNodeValue().equalsIgnoreCase("groupDn")) {
            String acl = "g." + principalNode.getTextContent();
            acl = acl.toUpperCase().trim();
            acls.put(acl, acl);
          }
        }
      }
    }
    return acls;
  }
  
  /**
   * Indicates if the user has read access for the document with given UUID.
   * @param user the application user
   * @param uuid the document uuid
   * @return true if the user has read access
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   * @throws SQLException
   */
  public boolean hasReadAccess(User user, String uuid) throws SQLException,
      ParserConfigurationException, SAXException, IOException {
    AuthenticationStatus auth = user.getAuthenticationStatus();
    boolean bAdmin = auth.getAuthenticatedRoles().hasRole("gptAdministrator");
    if (bAdmin || isPolicyUnrestricted()) {
      return true;
    } else {
      if (uuid == null) {
        return true;
      }
      if (user == null) {
        return false;
      }
      HashMap<String, String> acls = makeDocumentAclFromUUID(uuid);
      if (acls != null && acls.size() > 0) {
        Groups groups = user.getGroups();
        for (Group group : groups.values()) {
          String userAclKey = makeAclString(group);
          userAclKey = userAclKey.toUpperCase().trim();
          if ((userAclKey.length() > 0) && acls.containsKey(userAclKey)) {
            return true;
          }
        }
      } else {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Indicates if the application metadata access policy is unrestricted.
   * @return true if the policy is unrestricted
   */
  public boolean isPolicyUnrestricted() {
    MetadataAccessPolicy cfg;
    cfg = _requestContext.getApplicationConfiguration().getMetadataAccessPolicy();
    return cfg.isPolicyUnrestricted();
  }
  
  /**
   * 
   * @param group
   * @return ACL string
   */
  private String makeAclString(Group group) {
    return "g." + group.getDistinguishedName();
  }
  
  /**
   * Builds array of acl strings
   * @param xml xml string
   * @return document acl string array
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public String[] makeDocumentAcl(String xml)
      throws ParserConfigurationException, SAXException, IOException {
    return buildDocumentAcl(xml).values().toArray(new String[0]);
  }
  
  /**
   * Builds hashed map of acl values
   * @param uuid the document uuid
   * @return document acl HashMap
   * @throws SQLException
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  private HashMap<String, String> makeDocumentAclFromUUID(String uuid)
      throws SQLException, ParserConfigurationException, SAXException, IOException {
    ImsMetadataAdminDao adminDao = new ImsMetadataAdminDao(_requestContext);
    String xml = adminDao.queryAclByUUID(uuid);
    return buildDocumentAcl(xml);
  }
  
  /**
   * Returns groups from acl xml string
   * @param groups group
   * @param xml xml
   * @return string representation of groups
   * @throws IOException
   * @throws SAXException
   * @throws ParserConfigurationException
   */
  public String makeGroupsfromXml(Groups groups, String xml)
      throws ParserConfigurationException, SAXException, IOException {
    StringBuffer selectedGroups = new StringBuffer();
    Document document = null;
  
    if (xml != null && xml.trim().length() > 0) {
      document = DomUtil.makeDomFromString(xml, true);
    } else {
      return null;
    }
  
    if (document != null) {
      NodeList principalNodes = document.getElementsByTagName("principal");
      if (principalNodes != null && principalNodes.getLength() > 0) {
        for (int i = 0; i < principalNodes.getLength(); i++) {
          Node principalNode = principalNodes.item(i);
          NamedNodeMap attributes = principalNode.getAttributes();
          if (attributes.getNamedItem("type").getNodeValue().equalsIgnoreCase(
              "groupDn")) {
            Group group = groups.get(principalNode.getTextContent());
            if (group != null)
              selectedGroups.append(group.getName()).append(",");
          }
        }
      }
    }
  
    return selectedGroups.length() > 0 ? selectedGroups.substring(0,
        selectedGroups.length() - 1) : null;
  }
  
  /**
   * Makes group keys from XML string.
   * @param groups groups
   * @param xml XML string
   * @return string representation of groups
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public String makeGroupsKeysfromXml(Groups groups, String xml)
    throws ParserConfigurationException, SAXException, IOException {
    StringBuffer selectedGroups = new StringBuffer();
    Document document = null;
    
    if (xml != null && xml.trim().length() > 0) {
      document = DomUtil.makeDomFromString(xml, true);
    } else {
      return null;
    }
    
    if (document != null) {
      NodeList principalNodes = document.getElementsByTagName("principal");
      if (principalNodes != null && principalNodes.getLength() > 0) {
        for (int i = 0; i < principalNodes.getLength(); i++) {
          Node principalNode = principalNodes.item(i);
          NamedNodeMap attributes = principalNode.getAttributes();
          if (attributes.getNamedItem("type").getNodeValue().equalsIgnoreCase("groupDn")) {
            Group group = groups.get(principalNode.getTextContent());
            if (group != null)
              selectedGroups.append(group.getKey()).append("\u2715");
          }
        }
      }
    }
    
    return selectedGroups.length() > 0 ? selectedGroups.substring(0,
      selectedGroups.length() - 1) : null;
  }
  
  
  /**
   * Builds array of acl strings
   * @return user acl string array
   * @throws SQLException
   * @throws NamingException
   * @throws IdentityException
   */
  public String[] makeUserAcl() {
    User user = _requestContext.getUser();
    
    /* groups have already been read for the user
    try {
      _requestContext.newIdentityAdapter().readUserGroups(user);
    } catch (IdentityException e) {
      LOGGER.severe(e.getMessage());
    } catch (NamingException e) {
      LOGGER.severe(e.getMessage());
    } catch (SQLException e) {
      LOGGER.severe(e.getMessage());
    }
    */
    
    Groups groups = user.getGroups();
    ArrayList<String> acls = new ArrayList<String>();
    for (Group group : groups.values()) {
      acls.add(makeAclString(group));
    }
    return acls.toArray(new String[0]);
  }
  


}
