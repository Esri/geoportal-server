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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.LimitExceededException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapContext;

import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.security.principal.Group;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.UserAttribute;
import com.esri.gpt.framework.security.principal.UserAttributeMap;
import com.esri.gpt.framework.security.principal.Users;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

/**
 * Handles functionality related to querying an LDAP identity store.
 */
public class LdapQueryFunctions extends LdapFunctions {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
protected LdapQueryFunctions() {
  super();
}

/**
 * Construct with a supplied configuration.
 * @param configuration the configuration
 */
protected LdapQueryFunctions(LdapConfiguration configuration) {
  super(configuration);
}

// properties ==================================================================


// methods =====================================================================

/**
 * Appends attribute values to a map (keyed on attribute id).
 * @param attributes the attributes to append (from)
 * @param values the map of values to populate (to)
 * @param stringsOnly if true, only attributes values of type
 *        String will be appended
 * @throws NamingException if an exception occurs
 */
protected void appendAttributeValues(Attributes attributes,
                                     Map<String,Object> values,
                                     boolean stringsOnly)
  throws NamingException {
  NamingEnumeration<?> enAttr = null;
  try {
    if (attributes != null) {
      enAttr = attributes.getAll();
      while (enAttr.hasMore()) {
        Object oAttr = enAttr.next();
        if (oAttr instanceof Attribute) {
          Attribute attr = (Attribute)oAttr;
          String sId = attr.getID();
          Object oVal = attr.get();
          if (!stringsOnly || (oVal instanceof String)) {
            values.put(sId,oVal);
          } else if (stringsOnly && (oVal == null)) {
            //values.put(sId,"");
          }
          //System.err.println(sId+"="+oVal+" cl="+oVal.getClass().getName());
        }
      }
      enAttr.close();
    }
  }catch (PartialResultException pre) {
	 LogUtil.getLogger().finer(pre.toString());
  } catch (LimitExceededException lee) {
	 LogUtil.getLogger().finer(lee.toString());
  } finally {
    closeEnumeration(enAttr);
  }
}

/**
 * Appends a collection of sub-string attribute values to a list.
 * <br/>The sub-attributes are determined by attribute.getAll().
 * <br/>Only sub-attributes of type String will be appended.
 * @param attribute the attribute containing values to append (from)
 * @param values the list of values to populate (to)
 * @throws NamingException if an exception occurs
 */
protected void appendSubStringValues(Attribute attribute, StringSet values)
  throws NamingException {
  NamingEnumeration<?> enAttr = null;
  try {
    if (attribute != null) {
      enAttr = attribute.getAll();
      while (enAttr.hasMore()) {
        Object oAttr = enAttr.next();
        if (oAttr instanceof String) {
          values.add((String)oAttr);
        }
      }
    }
  } catch (PartialResultException pre) {
	 LogUtil.getLogger().finer(pre.toString());
  } catch (LimitExceededException lee) {
	 LogUtil.getLogger().finer(lee.toString());
  } finally {
    closeEnumeration(enAttr);
  }
}

/**
 * Determines group membership.
 * <br/>This method does not use the dynamic group member attribute.
 * <br/>This method doesn't work in all cases and is not used.
 * @param memberDN the distinguished name of the member
 * @param groupDN the distinguished name of the group
 * @return true if the member belongs to the group
 * @throws NamingException if an exception occurs
 */
private boolean isGroupMember(DirContext dirContext,
                              String memberDN, 
                              String groupDN)
  throws NamingException {
  boolean bIsMember = false;
  NamingEnumeration<SearchResult> enSearch = null;
  memberDN = Val.chkStr(memberDN);
  groupDN = Val.chkStr(groupDN);
  if ((memberDN.length() > 0) && (groupDN.length() > 0)) {
    try {
      String sFilter = getConfiguration().getGroupProperties().returnGroupMemberSearchFilter(memberDN);
      SearchControls controls = new SearchControls();
      controls.setSearchScope(SearchControls.OBJECT_SCOPE);
      enSearch  = dirContext.search(groupDN,sFilter,controls);
      bIsMember = enSearch.hasMore();
    } catch (NamingException e) {
      String sMsg = e.getMessage()+" groupDN:"+groupDN;
      throw new NamingException(sMsg);
    } finally {
      closeEnumeration(enSearch);
    }
  }
  return bIsMember;
}

/**
 * Reads the attribute values associated with an attribute name.
 * @param dirContext the directory context
 * @param attrubuteName attribute name.
 * @param objectDN the distinguished name of the object
 * @return the list attribute values (strings only are returned)
 * @throws NamingException if an exception occurs
 */
protected StringSet readAttribute(DirContext dirContext,
                                  String objectDN, 
                                  String attrubuteName)
  throws NamingException {
	StringSet values = new StringSet();
	try{	  
	  if ((objectDN.length() > 0) && (attrubuteName.length() > 0)) {
	    String[] aReturn = new String[1];
	    aReturn[0] = attrubuteName;
	    try{
	    Attributes attributes = dirContext.getAttributes(objectDN,aReturn);
	    if (attributes != null) {
	      appendSubStringValues(attributes.get(attrubuteName),values);
	    }
	    }catch(NameNotFoundException nnfe){
	    	LogUtil.getLogger().finer(nnfe.toString());
	    }
	  }	  
    } catch (PartialResultException pre) {
      LogUtil.getLogger().finer(pre.toString());
    } catch (LimitExceededException lee) {
        LogUtil.getLogger().finer(lee.toString());
    }
	return values;
}

/**
 * Reads directory object attributes into a HashMap (keyed on attribute id).
 * @param objectDN the distinguished name of the object
 * @param stringsOnly if true, consider strings only
 * @return the attribute HashMap
 * @throws NamingException if an exception occurs
 */
protected Map<String,Object> readAttributes(DirContext dirContext,
                                            String objectDN, 
                                            boolean stringsOnly)
  throws NamingException {
  Map<String,Object> map = new HashMap<String,Object>();
  Attributes attributes = dirContext.getAttributes(objectDN);
  appendAttributeValues(attributes,map,stringsOnly);
  return map;
}

/**
 * Reads group member name strings into a list.
 * @param dirContext the directory context
 * @param groupDN the distinguished name of the group
 * @return the list of group member strings
 * @throws NamingException if an exception occurs
 */
protected StringSet readGroupMembers(DirContext dirContext,
                                     String groupDN)
  throws NamingException {
  StringSet members = new StringSet();
  groupDN = Val.chkStr(groupDN);
  String sDynamic = getConfiguration().getGroupProperties().getGroupDynamicMembersAttribute();
  if (groupDN.length() > 0) {
    if (sDynamic.length() > 0) {
      members = readAttribute(dirContext,groupDN,sDynamic);
    } else {
      Attributes attributes = dirContext.getAttributes(groupDN);
      if (attributes != null) {
        String sMemberTag = getConfiguration().getGroupProperties().getGroupMemberAttribute();
        appendSubStringValues(attributes.get(sMemberTag),members);
      }
    }
  }
  return members;
}

/**
 * Retrieves this display name for a user.
 * @param dirContext the directory context
 * @param userDN the distinguished name for the user
 * @return the user display name
 * @throws NamingException 
 */
protected String readUserDisplayName(DirContext dirContext, String userDN) 
  throws NamingException {
  userDN = Val.chkStr(userDN);
  String sDisplayName = userDN;
  String sDisplayAttr = getConfiguration().getUserProperties().getUserDisplayNameAttribute();

  if ((userDN.length() > 0) && (sDisplayAttr.length() > 0)) {
    //try {
      StringSet ss = readAttribute(dirContext,userDN,sDisplayAttr);
      if (ss.size() > 0) {
        sDisplayName = ss.iterator().next();
      }
    //} catch (Exception e) {
    //  sDisplayName = userDN;
      //System.err.println("Error reading user display name for distinguished name: "+userDN);
      //e.printStackTrace(System.err);
    //}
  }
  return sDisplayName;
}

/**
 * Reads the groups to which a user belongs.
 * @param dirContext the directory context
 * @param user the subject user
 * @throws NamingException if an LDAP naming exception occurs
 */
protected void readUserGroups(DirContext dirContext, User user)
  throws NamingException {
  NamingEnumeration<SearchResult> enSearch = null;
  try {
    String sUserDN = user.getDistinguishedName();
    
    LdapGroupProperties props = getConfiguration().getGroupProperties();
    String sDynamicAttribute = props.getGroupDynamicMemberAttribute();
    String sNameAttribute = props.getGroupDisplayNameAttribute();
    String recursiveControlId = "";
    
    if (sDynamicAttribute.startsWith("controlid=")) {
      recursiveControlId = Val.chkStr(sDynamicAttribute.substring(10));
      sDynamicAttribute = "";
    }
    
    if (sUserDN.equals("*") || (sDynamicAttribute.length() == 0)) {
      
      // read group membership based upon a search filter
      String sBaseDN = props.getGroupSearchDIT();
      String sFilter = props.returnGroupMemberSearchFilter(sUserDN);
      if ((sUserDN.length() > 0) && (sFilter.length() > 0)) {
        
        // the is to handle an issue with activeDirectory recursion
        if (sUserDN.equals("*")) {
          sFilter = sFilter.replace("member:1.2.840.113556.1.4.1941:=","member=");
        }
        
        // supply a recursion control such as Oracle's CONNECT_BY (2.16.840.1.113894.1.8.3)
        if (!sUserDN.equals("*") && (recursiveControlId.length() > 0)) {
          if (dirContext instanceof LdapContext) {
            LdapContext ldapContext = (LdapContext)dirContext;
            Control[] aControls = ldapContext.getRequestControls();
            Control recursiveControl = new BasicControl(recursiveControlId);
            List<Control> lControls = new ArrayList<Control>();
            if (aControls != null) {
              for (Control ctl: aControls) {
                lControls.add(ctl);
              }
            } 
            lControls.add(recursiveControl);
            ldapContext.setRequestControls(lControls.toArray(new Control[0]));
          }
        }
        
        SearchControls controls = new SearchControls();
        controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        if (sNameAttribute.length() > 0) {
          String[] aReturn = new String[1];
          aReturn[0] = sNameAttribute;
          controls.setReturningAttributes(aReturn);
        }
        
        enSearch = dirContext.search(sBaseDN,sFilter,controls);
        try {
          while (enSearch.hasMore()) {
            SearchResult result = (SearchResult)enSearch.next();
            String sDN = buildFullDN(result.getName(),sBaseDN);
            if (sDN.length() > 0) {
              String sName = "";
              if (sNameAttribute.length() > 0) {
                Attribute attrName = result.getAttributes().get(sNameAttribute);
                if ((attrName != null) && (attrName.size() > 0)) {
                  sName = Val.chkStr(attrName.get(0).toString());
                }
              }
              Group group = new Group();
              group.setDistinguishedName(sDN);
              group.setKey(group.getDistinguishedName());
              group.setName(sName);
              user.getGroups().add(group);
            }
          }
        } catch (PartialResultException pre) {
          LogUtil.getLogger().finer(pre.toString());
        } catch (LimitExceededException lee) {
            LogUtil.getLogger().finer(lee.toString());
        }
      }
      
    } else {
      
      // read group membership based upon a dynamic attribute
      StringSet groupDNs = readAttribute(dirContext,sUserDN,sDynamicAttribute);
      for (String sDN: groupDNs) {
        sDN = sDN.toLowerCase();
        if (sDN.length() > 0) {
          String sName = "";
          if (sNameAttribute.length() > 0) {
            StringSet ss = readAttribute(dirContext,sDN,sNameAttribute);
            if (ss.size() > 0) {
              sName = ss.iterator().next();
            }
          }
          Group group = new Group(sDN);
          group.setDistinguishedName(sDN);
          group.setName(sName);
          user.getGroups().add(group);
        }
      }
    }
    
  } finally {
    closeEnumeration(enSearch);
  }
}

/**
 * Retrieves this username attribute for a user.
 * @param dirContext the directory context
 * @param userDN the distinguished name for the user
 * @return the username
 * @throws NamingException if the username attribute does not exist
 */
protected String readUsername(DirContext dirContext, String userDN) 
  throws NamingException {
  userDN = Val.chkStr(userDN);
  String sName = userDN;
  LdapNameMapping nameMap =  getConfiguration().getUserProperties().getUserProfileMapping();
  String sAttrName = nameMap.findLdapName(UserAttributeMap.TAG_USER_NAME);
  if (sAttrName.length() == 0) sAttrName = "cn";
  if ((userDN.length() > 0) && (sAttrName.length() > 0)) {
    //try {
      StringSet ss = readAttribute(dirContext,userDN,sAttrName);
      if (ss.size() > 0) {
        sName = ss.iterator().next();
      } else if (!sAttrName.equals("cn")) {
        ss = readAttribute(dirContext,userDN,"cn");
        if (ss.size() > 0) {
          sName = ss.iterator().next();
        }
      }
    //} catch (Exception e) {
    //  sName = userDN;
      //System.err.println("Error reading username for distinguished name: "+userDN);
      //e.printStackTrace(System.err);
    //}
  }
  return sName;
}

/**
 * Reads the profile attributes for a user.
 * @param dirContext the directory context
 * @param user the subject user
 * @throws NamingException if an LDAP naming exception occurs
 */
protected void readUserProfile(DirContext dirContext, User user)
  throws NamingException {
  LdapNameMapping nameMap = getConfiguration().getUserProperties().getUserProfileMapping();
  UserAttributeMap userProf = user.getProfile();
  UserAttributeMap configured = getConfiguration().getIdentityConfiguration().getUserAttributeMap();
  
  /* There were some issues with the initial integration of 
   * Apache's LDAP implementation. The section below ensures that a user's
   * profile contains all configured attributes, even if they do not exist on
   * the LDAP side. 
   */
  boolean bEnsureAllAttributes = true;
  if (bEnsureAllAttributes) {
    for (UserAttribute attr: configured.values()) {
      if (!userProf.containsKey(attr.getKey())) {
        userProf.set(attr.getKey(),"");
      }
    }
  }
  
  String sUserDN = user.getDistinguishedName();
  if (sUserDN.length() > 0) {
    
    // read current LDAP attribute values
    NamingEnumeration<?> enAttr = null;
    try {
      Attributes attributes = dirContext.getAttributes(sUserDN); 
            
      if (attributes != null) {
        enAttr = attributes.getAll();
        while (enAttr.hasMore()) {
          Object oAttr = enAttr.next();
          if (oAttr instanceof Attribute) {
            Attribute attr = (Attribute)oAttr;
            String sLdapKey = attr.getID();
            Object oVal = attr.get();
                        
            // set the corresponding application user attribute
            String sAppKey = Val.chkStr(nameMap.findApplicationName(sLdapKey));
            if ((sAppKey.length() > 0) && configured.containsKey(sAppKey)) {
              if (oVal instanceof String) {
                userProf.set(sAppKey,(String)oVal);
              } else if (oVal == null) {
                userProf.set(sAppKey,"");
              }
            }
            
          }
        }
        enAttr.close();
      }
    } finally {
      closeEnumeration(enAttr);
    }    
    
  }
}

/**
 * Builds list of ldap users matching filter.
 * @param dirContext the directory context
 * @param filter the user search filter for ldap
 * @return the list of users matching filter
 * @throws NamingException if an LDAP naming exception occurs
 */
protected Users readUsers(DirContext dirContext,String filter, String attributeName) throws NamingException{	
	Users users = new Users();
	NamingEnumeration<SearchResult> enSearch = null;
	try{
		LdapUserProperties userProps = getConfiguration().getUserProperties();
		String sNameAttribute = userProps.getUserDisplayNameAttribute();
	    String sBaseDN = userProps.getUserSearchDIT();
	    String sFilter = userProps.returnUserLoginSearchFilter(filter);
	    if(attributeName != null){	    
	    	sFilter = userProps.returnUserNewRequestSearchFilter(filter, attributeName);
	    }
	    SearchControls controls = new SearchControls();
	    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    if (sNameAttribute.length() > 0) {
	      String[] aReturn = new String[1];
	      aReturn[0] = sNameAttribute;
	      controls.setReturningAttributes(aReturn);
	    }
	    
	    enSearch = dirContext.search(sBaseDN,sFilter,controls);
	    try { 
	      while (enSearch.hasMore()) {
	        SearchResult result = (SearchResult)enSearch.next();
	        String sDN = buildFullDN(result.getName(),sBaseDN);
	        if (sDN.length() > 0) {
	          String sName = "";
	          if (sNameAttribute.length() > 0) {
	            Attribute attrName = result.getAttributes().get(sNameAttribute);
	            if ((attrName != null) && (attrName.size() > 0)) {
	              sName = Val.chkStr(attrName.get(0).toString());
	            }
	          }

		          User user = new User();
		          user.setDistinguishedName(sDN);
		          user.setKey(user.getDistinguishedName());
		          user.setName(sName);
		          users.add(user);
	        }
	      }
	    } catch (PartialResultException pre) {
	      LogUtil.getLogger().finer(pre.toString());
	    } catch (LimitExceededException lee) {
	        LogUtil.getLogger().finer(lee.toString());
	    }
	}finally {
	    closeEnumeration(enSearch);
	}
    return users;
}

/**
 * Builds list of ldap groups matching filter.
 * @param dirContext the directory context
 * @param filter the group search filter for ldap
 * @return the list of groups matching filter
 * @throws NamingException if an LDAP naming exception occurs
 */
protected Groups readGroups(DirContext dirContext,String filter) throws NamingException{	
	Groups groups = new Groups();
	NamingEnumeration<SearchResult> enSearch = null;
	try{
		LdapGroupProperties groupProps = getConfiguration().getGroupProperties();
		String sNameAttribute = groupProps.getGroupDisplayNameAttribute();
	    String sBaseDN = groupProps.getGroupSearchDIT();
	    String sFilter = groupProps.returnGroupNameSearchFilter(filter);
	    SearchControls controls = new SearchControls();
	    controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
	    if (sNameAttribute.length() > 0) {
	      String[] aReturn = new String[1];
	      aReturn[0] = sNameAttribute;
	      controls.setReturningAttributes(aReturn);
	    }
	    
	    enSearch = dirContext.search(sBaseDN,sFilter,controls);
	    try { 
	      while (enSearch.hasMore()) {
	        SearchResult result = (SearchResult)enSearch.next();
	        String sDN = buildFullDN(result.getName(),sBaseDN);
	        if (sDN.length() > 0) {
	          String sName = "";
	          if (sNameAttribute.length() > 0) {
	            Attribute attrName = result.getAttributes().get(sNameAttribute);
	            if ((attrName != null) && (attrName.size() > 0)) {
	              sName = Val.chkStr(attrName.get(0).toString());
	            }
	          }

		          Group group = new Group();
		          group.setDistinguishedName(sDN);
		          group.setKey(group.getDistinguishedName());
		          group.setName(sName);
		          groups.add(group);
	        }
	      }
	    } catch (PartialResultException pre) {
	      LogUtil.getLogger().finer(pre.toString());
	    } catch (LimitExceededException lee) {
	        LogUtil.getLogger().finer(lee.toString());
	    }
	}finally {
	    closeEnumeration(enSearch);
	}
    return groups;
}

/**
 * Returns a list of distinguished names resulting from a search.
 * <br/>The search is executed with SearchControls.SUBTREE_SCOPE.
 * @param dirContext the directory context
 * @param baseDN the baseBN for the search
 * @param filter the filter for the search
 * @return a collection of distinguished names
 * @throws NamingException if an exception occurs
 */
protected StringSet searchDNs(DirContext dirContext,
                              String baseDN, 
                              String filter) 
  throws NamingException {
  StringSet names = new StringSet(false,false,true);
  NamingEnumeration<SearchResult> enSearch = null;
  try {
    baseDN = Val.chkStr(baseDN);
    filter = Val.chkStr(filter);
    if (filter.length() > 0) {
      SearchControls controls = new SearchControls();
      controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      enSearch = dirContext.search(baseDN,filter,controls);
      try {
        while (enSearch.hasMore()) {
          SearchResult result = (SearchResult)enSearch.next();
          names.add(buildFullDN(result.getName(),baseDN));
        }
      } catch (PartialResultException pre) {
        LogUtil.getLogger().finer(pre.toString());
      } catch (LimitExceededException lee) {
          LogUtil.getLogger().finer(lee.toString());
      }
    }
  } finally {
    closeEnumeration(enSearch);
  }
  return names;
}

}

