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
package com.esri.gpt.framework.security.identity.ldap;
import com.esri.gpt.framework.util.Val;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

/**
 * Defines the configured properties for LDAP group access.
 */
public class LdapGroupProperties extends LdapProperties {
  
// class variables =============================================================

// instance variables ==========================================================
private String    _groupDisplayNameAttribute = "";
private String    _groupDynamicMemberAttribute = "";
private String    _groupDynamicMembersAttribute = "";
private String    _groupMemberAttribute = "";
private String    _groupMemberSearchPattern = "";
private String    _groupNameSearchPattern = "";
private Attribute _groupObjectClasses;
private String    _groupSearchDIT = "";
  
// constructors ================================================================

/** Default constructor. */
public LdapGroupProperties() {
  super();
  setGroupObjectClasses(new BasicAttribute("objectclass"));
}

// properties ==================================================================

/**
 * Get the display name attribute for a group.
 * @return the attribute name
 */
public String getGroupDisplayNameAttribute(){
  return _groupDisplayNameAttribute;
}
/**
 * Set the display name attribute for a group.
 * @param attributeName the attribute name
 */
public void setGroupDisplayNameAttribute(String attributeName){
  _groupDisplayNameAttribute = Val.chkStr(attributeName);
}

/**
 * Gets the attribute used to dynamically determine the groups for a user.
 * <br/>If specified, a user's group membership is dynamically
 * determined by searching this attribute.
 * <br/>
 * Examples: ibm-allgroups for Tivoli, nsRole for IPlanet, etc
 * @return the attribute used for dynamic group searches (zero length if none)
 */
public String getGroupDynamicMemberAttribute(){
  return _groupDynamicMemberAttribute;
}
/**
 * Sets the attribute used to dynamically determine the groups for a user.
 * <br/>If specified, a user's group membership is dynamically
 * determined by searching this attribute.
 * <br/>
 * Examples: ibm-allgroups for Tivoli, nsRole for IPlanet, etc
 * @param attribute the attribute used for dynamic group searches
 */
public void setGroupDynamicMemberAttribute(String attribute){
  attribute = Val.chkStr(attribute);
  if ((attribute.length() > 0) &&
      !attribute.equalsIgnoreCase("none") &&
      !attribute.equalsIgnoreCase("n/a")) {
    _groupDynamicMemberAttribute = attribute;
  } else {
    _groupDynamicMemberAttribute = "";
  }
}

/**
 * Gets the attribute to dynamically determine the members of a group.
 * <br/>If specified, the members of a group are dynamically
 * determined by searching this attribute.
 * <br/>
 * Examples: ibm-allmembers for tivoli
 * @return the attribute used for dynamic group searches (zero length if none)
 */
public String getGroupDynamicMembersAttribute(){
  return _groupDynamicMembersAttribute;
}
/**
 * Sets the attribute used to dynamically determine the members of a group.
 * <br/>If specified, the members of a group are dynamically
 * determined by searching this attribute.
 * <br/>
 * Examples: ibm-allmembers for Tivoli
 * @param attribute the attribute used for dynamic group searches
 */
public void setGroupDynamicMembersAttribute(String attribute){
  attribute = Val.chkStr(attribute);
  if ((attribute.length() > 0) &&
      !attribute.equalsIgnoreCase("none") &&
      !attribute.equalsIgnoreCase("n/a")) {
    _groupDynamicMembersAttribute = attribute;
  } else {
    _groupDynamicMembersAttribute = "";
  }
}

/**
 * Gets the attribute name for a member within a group.
 * @return the attribute name
 */
public String getGroupMemberAttribute() {
  return _groupMemberAttribute;
}
/**
 * Sets the attribute name for a member within a group.
 * @param attributeName the attribute name
 */
public void setGroupMemberAttribute(String attributeName) {
  _groupMemberAttribute = Val.chkStr(attributeName);
}

/**
 * Gets the group member search pattern.
 * <br/>eg. (&(objectclass=groupOfNames)(member={0}))
 * <br/>The intent is to support querying for group membership based upon
 * a members distinguished name.
 * <br/>If a the member's DN is "cn=myname,cn=users,o=esri,c=us" , the {0}
 * section of the pattern will be replaced to produce:
 * <br/>(&(objectclass=groupOfNames)(member=cn=myname,cn=users,o=esri,c=us))
 * <br/>The search for groups will begin at the base DN defined by:
 * getGroupSearchDIT()
 * @return the group member search pattern
 */
public String getGroupMemberSearchPattern() {
  return _groupMemberSearchPattern;
}
/**
 * Sets the group member search pattern.
 * <br/>eg. (&(objectclass=groupOfNames)(member={0}))
 * <br/>The intent is to support querying for group membership based upon
 * a members distinguished name.
 * <br/>If a the member's DN is "cn=myname,cn=users,o=esri,c=us" , the {0}
 * section of the pattern will be replaced to produce:
 * <br/>(&(objectclass=groupOfNames)(member=cn=myname,cn=users,o=esri,c=us))
 * <br/>The search for groups will begin at the base DN defined by:
 * getGroupSearchDIT()
 * @param pattern the group member search pattern
 */
public void setGroupMemberSearchPattern(String pattern) {
  _groupMemberSearchPattern = Val.chkStr(pattern);
}

/**
 * Gets the group name search pattern.
 * <br/>eg. (&(objectclass=groupOfNames)(cn={0}))
 * <br/>The intent is to support querying for a group based upon
 * a supplied short name.
 * <br/>If the short name is "publishers", the {0}
 * section of the pattern will be replaced to produce:
 * <br/>(&(objectclass=groupOfNames)(cn=publishers))
 * <br/>The search for groups will begin at the base DN defined by:
 * getGroupSearchDIT()
 * @return the group name search pattern
 */
public String getGroupNameSearchPattern() {
  return _groupNameSearchPattern;
}
/**
 * Sets the group name search pattern.
 * <br/>eg. (&(objectclass=groupOfNames)(cn={0}))
 * <br/>The intent is to support querying for a group based upon
 * a supplied short name.
 * <br/>If the short name is "publishers", the {0}
 * section of the pattern will be replaced to produce:
 * <br/>(&(objectclass=groupOfNames)(cn=publishers))
 * <br/>The search for groups will begin at the base DN defined by:
 * getGroupSearchDIT()
 * @param pattern the group name search pattern
 */
public void setGroupNameSearchPattern(String pattern) {
  _groupNameSearchPattern = Val.chkStr(pattern);
}

/**
 * Gets the required group object classes.
 * @return the required group object classes
 */
public Attribute getGroupObjectClasses() {
  return _groupObjectClasses;
}
/**
 * Sets the required group object classes.
 * @param groupObjectClasses Object classes for a new group
 */
private void setGroupObjectClasses(Attribute groupObjectClasses) {
  _groupObjectClasses = groupObjectClasses;
}

/**
 * Gets the root directory where searching of groups will take place.
 * @return directory root under which all groups reside
 */
public String getGroupSearchDIT(){
  return _groupSearchDIT;
}
/** Sets the root directory where searching of groups will take place.
 * @param dit directory root under which all groups reside
 */
public void setGroupSearchDIT(String dit){
  _groupSearchDIT = Val.chkStr(dit).toLowerCase();
}

// methods =====================================================================

/**
 * Adds group objects that will be used for creating groups.
 * @param className Class to add to group object to be created
 */
public void addGroupObjectClass(String className) {
  className = Val.chkStr(className);
  if ((className.length() > 0) && !_groupObjectClasses.contains(className)) {
    _groupObjectClasses.add(className);
  }
}

/**
 * Returns the search filter for finding a member within a group.
 * <br/>The filter will be based upon the configured getGroupMemberSearchPattern().
 * @param memberDN the distinguished name of the member
 * @return the filter
 */
public String returnGroupMemberSearchFilter(String memberDN) {
  memberDN = Val.chkStr(memberDN);
  if (memberDN.length() > 0) {
    
    // for DN's containing a backslash, escape single backslash with double backslash
    memberDN = memberDN.replaceAll("\\\\","\\\\\\\\\\\\\\\\");
    
    return replace(getGroupMemberSearchPattern(),memberDN);
  } else {
    return memberDN;
  }
}

/**
 * Returns the search filter for finding a group based upon a name.
 * <br/>The filter will be based upon the configured getGroupNameSearchPattern().
 * @param name the group name
 * @return the filter
 */
public String returnGroupNameSearchFilter(String name) {
  name = Val.chkStr(name);
  if (name.length() > 0) {
    return replace(getGroupNameSearchPattern(),name);
  } else {
    return name;
  }
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" displayNameAttribute=\"").append(
      getGroupDisplayNameAttribute()).append("\"\n");
  sb.append(" dynamicMemberOfGroupsAttribute=\"").append(
      getGroupDynamicMemberAttribute()).append("\"\n");
  sb.append(" dynamicMembersAttribute=\"").append(
      getGroupDynamicMembersAttribute()).append("\"\n");
  sb.append(" memberAttribute=\"").append(
      getGroupMemberAttribute()).append("\"\n");
  sb.append(" memberSearchPattern=\"").append(
      getGroupMemberSearchPattern()).append("\"\n");
  sb.append(" nameSearchPattern=\"").append(
      getGroupNameSearchPattern()).append("\"\n");
  sb.append(" searchDIT=\"").append(
      getGroupSearchDIT()).append("\"\n");
  if (getGroupObjectClasses() != null) {
    sb.append(" ").append(getGroupObjectClasses()).append("\n");
  }
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}
