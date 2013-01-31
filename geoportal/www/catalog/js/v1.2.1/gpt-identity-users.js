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
/**
 * gpt-identity-users.js (v1.2.1)
 * Manage User Role support
 */

dojo.declare("gpt.identity.Users", null, {  
  contextPath: "",
  currentUser: "user-current",
  currentUserHeader: "user-current-header",
  managedUser: "user-managed",
  searchTextId: "user-searchText",
  searchResults: "user-search-results",
  searchResultsHeader: "user-search-results-header",
  currentUserTitle : "user-current-title",
  listId : "user-list",
  usersEndPoint: "/rest/identity/users",
  groupsEndPoint: "/rest/identity/groups",
  hasDeleteUser: false,
  currentUserName: "",
  currentGroupName:"",
  requestTimeOut: 60000,
  resources: {
    noItemsSelected: "Select user and click change role.",
    noResults: "Returned no users",
    addRole: "Add user to role",
    removeRole: "Remove user from role",
    modifyRole : "Add/Remove current user from role.",
    memberOf: "Group Membership",
    configurableRoles: "Roles",
    userAttributes: "Attributes",
    deleteUser:"Delete User",
    addConfirmation: "Do you want to add {0} to the {1} role ?",
    selfAddConfirmation: "You are about to make a change to your own user role. Are you really sure you want to do this?",
    removeConfirmation: "Do you want to remove {0} from the {1} role ?",
    selfRemoveConfirmation: "You are about to make a change to your own user role. Are you really sure you want to do this?",
    deleteUserConfirmation: "Do you want to delete {0} from system ?",
    selfDeleteUserConfirmation: "You are about to make a change to your own user role. Are you really sure you want to do this?",
    searchResultsSummary: "Showing {0} of {1} match(es)",
    searchUser: "Search users by username",
    searchResults: "Showing {0} of {1} matches",
    search: "Search",
    listAGroup: "Or show the members of a group"
  },

  init: function () {
    var elUsersDiv = dojo.byId("users-div");

    var elUsersTbl = document.createElement("table");
    elUsersTbl.width = "100%";

    var elBody = document.createElement("tbody");
    elUsersTbl.appendChild(elBody);

    var elHdrTr = document.createElement("tr");
    elBody.appendChild(elHdrTr);

    var elBodyTr = document.createElement("tr");
    elBody.appendChild(elBodyTr);

    var elSearchTd = document.createElement("td");
    dojo.style(elSearchTd, { verticalAlign: "top" });
    elSearchTd.id = "user-search";
    elHdrTr.appendChild(elSearchTd);

    var elSearchUsrLbl = document.createElement("label");
    elSearchUsrLbl.innerHTML = this.resources.searchUser;
    elSearchTd.appendChild(elSearchUsrLbl);

    var elSearchUsrInp = document.createElement("input");
    elSearchUsrInp.type = "text";
    elSearchUsrInp.id = "user-searchText";
    elSearchUsrInp.size = "20";
    elSearchTd.appendChild(elSearchUsrInp);

    var elUsrListBtn = document.createElement("input");
    elUsrListBtn.type = "button";
    elUsrListBtn.id = "user-list";
    elUsrListBtn.value = this.resources.search;
    elSearchTd.appendChild(elUsrListBtn);

    var elLoadingImg = document.createElement("img");
    elLoadingImg.id = "user-loadingGif";
    elLoadingImg.src = this.contextPath + "/catalog/images/loading.gif";
    elLoadingImg.style.visibility = "hidden";
    elLoadingImg.style.verticalAlign = "middle";
    elLoadingImg.style.width = "25px";
    elSearchTd.appendChild(elLoadingImg);

    var elGroups = document.createElement("div");
    dojo.style(elGroups, { marginTop: "5px", marginBottom: "10px" });
    elSearchTd.appendChild(elGroups);
    var elGroupsLbl = document.createElement("label");
    elGroupsLbl.appendChild(document.createTextNode(this.resources.listAGroup));
    elGroups.appendChild(elGroupsLbl);
    var elConfiguredUsersTd = document.createElement("div");
    elConfiguredUsersTd.id = "configured-users";
    elGroups.appendChild(elConfiguredUsersTd);
    this.fetchConfiguredRoles(elConfiguredUsersTd);

    var elSrchRsltsHdrDiv = document.createElement("div");
    elSrchRsltsHdrDiv.id = "user-search-results-header";
    elSrchRsltsHdrDiv.style.height = "15px";
    elSearchTd.appendChild(elSrchRsltsHdrDiv);

    var elSrchRsltsDiv = document.createElement("div");
    elSrchRsltsDiv.id = "user-search-results";
    elSearchTd.appendChild(elSrchRsltsDiv);

    var elUserTd = document.createElement("td");
    dojo.style(elSearchTd, { verticalAlign: "top" });
    elUserTd.id = "user-current";
    elHdrTr.appendChild(elUserTd);

    var elUserTitleDiv = document.createElement("span");
    elUserTitleDiv.id = "user-current-title";
    elUserTd.appendChild(elUserTitleDiv);

    var elUserHdrDiv = document.createElement("div");
    elUserHdrDiv.id = "user-current-header";
    elUserTd.appendChild(elUserHdrDiv);

    var elUserBodyTd = document.createElement("div");
    elUserBodyTd.id = "user-managed-td";
    var elUserMngDiv = document.createElement("div");
    elUserMngDiv.id = "user-managed";
    elUserBodyTd.appendChild(elUserMngDiv);
    elUserTd.appendChild(elUserBodyTd);

    elUsersDiv.appendChild(elUsersTbl);

    var searchTextId = dojo.byId(this.searchTextId);
    dojo.connect(searchTextId, "onkeyup", this, "executeSearch");

    var listId = dojo.byId(this.listId);
    dojo.connect(listId, "onclick", this, "search");
  },
  
  fetchConfiguredRoles: function(elConfiguredUsersTd){	  
	  var readConfiguredUrl = this.contextPath + this.groupsEndPoint + "/configured";
	  dojo.xhrGet({
      url: readConfiguredUrl,     
      preventCache: true,
      timeout: this.requestTimeOut,
      handleAs: "json",
      load: dojo.hitch(this,function(response, ioArgs) {
    	this.clearSearch();
      	
   	    this.addConfiguredRoles(elConfiguredUsersTd,response);      	        
        return response;
      }),
  
      error: dojo.hitch(this,function(response, ioArgs) {
        this.showMessage(response,"errorMessage");
        return response;
      })
    });  
  },
  
  addConfiguredRoles : function (elConfiguredUsersTd,response){
    var configuredRoles = response.configuredRoles;	   	  			
	for(var i in configuredRoles){ 			
	  var elConfigureRole = document.createElement("a");
	  elConfigureRole.id = configuredRoles[i].roleName + "-link";
	  elConfigureRole.title = configuredRoles[i].roleName;
	  elConfigureRole.label = configuredRoles[i].roleKey;
	  elConfigureRole.innerHTML = configuredRoles[i].roleName;		  
	  elConfigureRole.href = "#";
	  elConfigureRole.value= configuredRoles[i].roleKey;
	  dojo.connect(elConfigureRole,"onclick",this,"fetchGroupMembers");	
	  elConfiguredUsersTd.appendChild(elConfigureRole);
	  this.addLineBreak(elConfiguredUsersTd);			
	}  
  },
 
  fetchGroupMembers:function(el){
	  this.clearSearch();
	  var srch = el.target.value;
	  var attributeName = el.target.label;
	  var searchMemberUrl = this.contextPath + this.usersEndPoint + "/searchMembers?q="+encodeURIComponent(dojo.trim(srch))+"&a="+encodeURIComponent(dojo.trim(attributeName));
	  this.executeSearch(searchMemberUrl);
  },
     
  search: function(){
	  var searchTextId = dojo.byId(this.searchTextId);
	  if(searchTextId != null && dojo.trim(searchTextId.value).length == 0){
		  searchTextId.value = "*";
	  }
	  var srh = dojo.byId(this.searchResultsHeader);
	  srh.innerHtml = "";
	  this.executeSearch(null);
  },
     
  executeSearch: function(searchUrl) {
	  var elLoadingGif = dojo.byId("user-loadingGif");
	  elLoadingGif.style.visibility = "visible";
	  var searchTextId = dojo.byId(this.searchTextId);	
	  if(searchUrl == null || searchUrl.target != null){
		  if(searchTextId == null || dojo.trim(searchTextId.value).length ==0){
			  elLoadingGif.style.visibility = "hidden";
			  return;
		  }
		  var manageUsersUrl = this.contextPath + this.usersEndPoint + "/search?q="+encodeURIComponent(dojo.trim(searchTextId.value));
		  searchUrl = manageUsersUrl;
	  }
	  dojo.xhrGet({
        url: searchUrl,
        handleAs: "json",
        timeout: this.requestTimeOut,
        preventCache: true,
        load: dojo.hitch(this,function(response, ioArgs) {
          this.clearUserInfo();
          var elSearchRslts = dojo.byId(this.searchResults);
          elSearchRslts.innerHTML ="";
          var elSearchRsltsHdr = dojo.byId(this.searchResultsHeader);
          elSearchRsltsHdr.innerHTML ="";
  	      dojo.byId(this.currentUserHeader).innerHTML = "";  	    
          var nTotalResults = response.totalUsers;          
          if (nTotalResults > 0) {  
        	 var nTopUsers = response.topUsers;
        	 var sPageSummary = this.resources.searchResultsSummary;
    	     sPageSummary = sPageSummary.replace("{0}",nTopUsers);
    	     sPageSummary = sPageSummary.replace("{1}",nTotalResults);
        	 elSearchRsltsHdr.appendChild(document.createTextNode(sPageSummary));
          	 var users = response.users;
          	 var elUsersList = document.createElement("ul");
          	 for(var i in users){          		      			 
      			 var elUserItem = document.createElement("li");
      			 var elUserLink = document.createElement("a");
      			 elUserLink.appendChild(document.createTextNode(users[i].userName));
      			 elUserLink.title = users[i].userName;
      			 elUserLink.href = "#";
      			 var elDnInput = document.createElement("input");      		  
     			 elDnInput.type = "hidden";
     			 elDnInput.value = users[i].dn;
     			 elUserLink.appendChild(elDnInput);
     			 elUserItem.appendChild(elUserLink);
     			 elUsersList.appendChild(elUserItem); 
	          	 dojo.connect(elUserLink,"onclick",this,"onUserClicked");
              }   
          	  elSearchRslts.appendChild(elUsersList); 
          }else {        		
  	        dojo.byId(this.searchResults).innerHTML = "";  	        
          }
          elLoadingGif.style.visibility = "hidden";
          return response;
        }),
    
        error: dojo.hitch(this,function(response, ioArgs) {
          elLoadingGif.style.visibility = "hidden";
          this.showMessage(response,"errorMessage");
          return response;
        })
      });
	  
  },
  
  onUserMouseOver: function(e) {
	  if (!e) e = window.event;
	  var target = (window.event) ? e.srcElement : e.target; 
	  if (target != null){
		 target.className = "selectedResultRow";
	  }
  },
  
  onUserMouseOut: function(e) {
	  if (!e) e = window.event;
	  var target = (window.event) ? e.srcElement : e.target; 
	  if (target != null){
		 target.className = "noneSelectedResultRow";
	  }
  },
  
  onUserClicked: function(e) {	
	  var elMsg = dojo.byId(this.currentUserHeader);
	  if(elMsg!=null){
		  elMsg.innerHTML ="";
	  }
	  if (!e) e = window.event;
	  var target = (window.event) ? e.srcElement : e.target; 
	  var elInputVal = "";
	  if (target != null && target.children.length > 0) {
	   	elInputVal = target.children[0].value;
	   	this.currentUserName = target.title;
	   	target.className = "current";
	  }
	  if(elInputVal.length > 0){
	    this.fetchUserInfo(elInputVal);
	  }
  },
  
  clearUserInfo: function(){
	  var elUserTitleDiv = dojo.byId(this.currentUserTitle);
	  elUserTitleDiv.innerHTML = "";
	  var elManagedUser = dojo.byId(this.managedUser);
	  elManagedUser.innerHTML ="";
  },
  
  clearSearch: function(){
	  var elSearchRslts = dojo.byId(this.searchResults);
	  elSearchRslts.innerHTML ="";
	  var searchTextId = dojo.byId(this.searchTextId);
	  searchTextId.value ="";
	  var elMsg = dojo.byId(this.currentUserHeader);
	  if(elMsg!=null){
		  elMsg.innerHTML ="";
	  }
  },
  
  addUserTitle : function (){
	 var elUserTitleDiv = dojo.byId(this.currentUserTitle);
	 elUserTitleDiv.innerHTML = "";
	 elUserTitleDiv.className = "user-Name";
	 elUserTitleDiv.appendChild(document.createTextNode(this.currentUserName));
  },
  
  fetchUserInfo: function(userDn){	  
	  var elLoadingGif = dojo.byId("user-loadingGif");
	  elLoadingGif.style.visibility = "visible";
	  var readUserUrl = this.contextPath + this.usersEndPoint + "/"+encodeURIComponent(userDn) + "/profile";
	  dojo.xhrGet({
      url: readUserUrl,     
      preventCache: true,
      timeout: this.requestTimeOut,
      handleAs: "json",
      load: dojo.hitch(this,function(response, ioArgs) {
    	this.clearUserInfo();
      	var elManagedUser = dojo.byId(this.managedUser);
        
      	this.addUserTitle();
      	if(this.hasDeleteUser){
       	 this.addDeleteUserIcon();	     
   		}
      	var elUserTbl = document.createElement("table");
  	    elUserTbl.width = "100%";
  	    
	  	var elBody = document.createElement("tbody");
		elUserTbl.appendChild(elBody);
  	    
  	    var elRoleTr = document.createElement("tr");
  	    elBody.appendChild(elRoleTr);
  	    
  	   var elRoleTd = document.createElement("td");
  	   elRoleTr.appendChild(elRoleTd);
	    
	    var elInfoTr = document.createElement("tr");
	    elBody.appendChild(elInfoTr);
	    
	    var elInfoTd = document.createElement("td");
	    elInfoTr.appendChild(elInfoTd);
	    
	    elManagedUser.appendChild(elUserTbl);
	  	          	
      	this.addLineBreak(elManagedUser);
   	    this.addGeoportalRoles(elRoleTd,response);
   	    this.addClearDiv(elManagedUser);
   	    this.addLineBreak(elManagedUser);
		this.parseUserInfo(elInfoTd,response);
		this.highlightRequestAccess("um-attr-Geo.Data.gov access");
		this.highlightRequestAccess("um-attr-Geoplatform.gov access");
		elLoadingGif.style.visibility = "hidden";	          	        
        return response;
      }),
  
      error: dojo.hitch(this,function(response, ioArgs) {
    	elLoadingGif.style.visibility = "hidden";
        this.showMessage(response,"errorMessage");
        return response;
      })
    });  
  },
  
  highlightRequestAccess : function(id){
	var oa = dojo.byId(id);
	if(oa != null){
		var oaVal = this.getNodeText(oa);
		if(oaVal != null){ 
		   var rtd = dojo.byId("roleTd-" + oaVal);
		   if(rtd != null){
			   var addImage = dojo.byId("removeRole-" + oaVal);
			   if(addImage != null){
				   var aid = addImage.style.display;
				   if((typeof (aid) != 'undefined') && (aid == "inline")){
					   rtd = null;
				   }
			   }
		   }
		   if(rtd != null){
			   dojo.style(rtd,{color:"red"});
		   }
		}		
	}
  },
  
  getNodeText: function(domNode) {
    var s;
    if (domNode.nodeType == 1) {
      var children = domNode.childNodes;
      if ((children != null) && (children.length > 0)) {
        var n = children.length;
        for (var i=0; i<n; i++) {
          var child = children[i];
          if (child.nodeType == 3) {
            s = child.nodeValue;
            if (s != null) s = dojo.trim(s);
            return s;
          }
        }
      }
      return "";
    } else {
      s = domNode.nodeValue;
      if (s != null) s = dojo.trim(s);
      return s;
    }  
    return null;
  },
  
  addClearDiv:function(parent){
	var elDiv = document.createElement("div");
	elDiv.style.clear = "both";
	parent.appendChild(elDiv);
  },
  
  parseUserInfo : function (elInfoTr,response){
	var attributes = response.attributes; 
  	var groups = response.groups;    	
  	var userDn = response.userDn;

	elInfoTr.id = "um-userInformation";
	var elInfoTbl = document.createElement("table");
	elInfoTbl.width = "100%";
	   
	var elBody = document.createElement("tbody");
	elInfoTbl.appendChild(elBody);
    
	var elTr = document.createElement("tr");
	elBody.appendChild(elTr);
    
    var elTd = document.createElement("td");
    elTr.appendChild(elTd);
    
	var elUserDnInput = document.createElement("input");      		  
  	elUserDnInput.type = "hidden";
  	elUserDnInput.value = userDn;
  	elTd.appendChild(elUserDnInput);
		
  	var elUserHeader = document.createElement("h4");
  	elUserHeader.innerHTML = this.resources.userAttributes;
	elTd.appendChild(elUserHeader);
	for(var i in attributes){
		elTr = document.createElement("tr");
		elBody.appendChild(elTr);
	    
	    elTd = document.createElement("td");
	    elTr.appendChild(elTd);

		var elAttrLabel = document.createElement("label");
		elAttrLabel.appendChild(document.createTextNode(attributes[i].key));
		
		var elAttrVal = document.createElement("span");
		elAttrVal.appendChild(document.createTextNode(attributes[i].value));
		elAttrVal.id = "um-attr-" + attributes[i].key;
		
		elTd.appendChild(elAttrLabel);
		
		elTd = document.createElement("td");
	    elTr.appendChild(elTd);
	    elTd.appendChild(elAttrVal);

	}
	
	this.addClearDiv(elTr);
	
	elTr = document.createElement("tr");
	elBody.appendChild(elTr);
    
    elTd = document.createElement("td");
    elTr.appendChild(elTd);
    
	var elGroupHeader = document.createElement("h4");
	elGroupHeader.innerHTML = this.resources.memberOf;
	this.addLineBreak(elTd);
	elTd.appendChild(elGroupHeader); 
	for(var i in groups){
		elTr = document.createElement("tr");
		elBody.appendChild(elTr);
	    
	    elTd = document.createElement("td");
	    elTr.appendChild(elTd);
	    
		var elGroupLabel = document.createElement("label");
		elGroupLabel.className = "user-attrLeftDiv";
		elGroupLabel.appendChild(document.createTextNode(groups[i].name));

		
		var elGroupVal = document.createElement("span");
		elGroupVal.id ="um-group-" + groups[i].name;
		elGroupVal.className = "user-attrRightDiv";
		elGroupVal.appendChild(document.createTextNode(groups[i].key));
		
		elTd.appendChild(elGroupLabel);
		
		elTd = document.createElement("td");
	    elTr.appendChild(elTd);
	    elTd.appendChild(elGroupVal);
	    
	} 

	elInfoTr.appendChild(elInfoTbl);
  },
  
  addGeoportalRoles : function (elRoleTr,response){
    var selectableRoles = response.selectableRoles;
    
    var elRoleTbl = document.createElement("table");

    var elBody = document.createElement("tbody");
    elRoleTbl.appendChild(elBody);
    
	var elTr = document.createElement("tr");
	elBody.appendChild(elTr);
    
    var elTd = document.createElement("td");
    elTr.appendChild(elTd);
    
	var elRolesHeader = document.createElement("h4");
	elRolesHeader.innerHTML = this.resources.configurableRoles;	
	elTd.appendChild(elRolesHeader);   			  			
	for(var i in selectableRoles){ 
		
		elTr = document.createElement("tr");
		elBody.appendChild(elTr);
	    	    		
		var hasRole = selectableRoles[i].hasRole;
		
		elTd = document.createElement("td");
	    elTr.appendChild(elTd);
	
	    /* checkbox mode 
	    var elModifyRole = document.createElement("input");
	    elModifyRole.type="checkbox";
	    elModifyRole.id= "modifyRole-" + dojo.trim(selectableRoles[i].roleKey);
	    elModifyRole.title = this.resources.modifyRole;
	    if(hasRole == "true") {
	    	elModifyRole.checked = true;
	    }else{
	    	elModifyRole.checked = false;
	    }
		elTd.appendChild(elModifyRole);
		dojo.connect(elModifyRole,"onclick",this,"modifyRole");*/
	    
		var elAddRole = document.createElement("img");
		elAddRole.id= "addRole-" + dojo.trim(selectableRoles[i].roleKey);
		elAddRole.src = this.contextPath + "/catalog/images/emptyBox.jpg";
		elAddRole.alt = this.resources.addRole;
		elAddRole.title = this.resources.addRole;		
		if(hasRole == "true") {
			elAddRole.style.display = "none";
		}else{
			elAddRole.style.display = "inline";
		}
	    elTd.appendChild(elAddRole);
		dojo.connect(elAddRole,"onclick",this,"addUserToGroup");
	    
		var elRemoveRole = document.createElement("img");
		elRemoveRole.id= "removeRole-" + dojo.trim(selectableRoles[i].roleKey);
		elRemoveRole.src = this.contextPath + "/catalog/images/checkBox.jpg";
		elRemoveRole.alt = this.resources.removeRole; 
		elRemoveRole.title = this.resources.removeRole; 
		if(hasRole  == "true") {
			elRemoveRole.style.display = "inline";
		}else{
			elRemoveRole.style.display = "none";
		}		
	    elTd.appendChild(elRemoveRole);
		dojo.connect(elRemoveRole,"onclick",this,"removeUserFromGroup");
											
		var elRoleKeyInput = document.createElement("input");      		  
		elRoleKeyInput.type = "hidden";
		elRoleKeyInput.title = selectableRoles[i].roleName;
		elRoleKeyInput.value = selectableRoles[i].roleDn;
		elTd = document.createElement("td");
	    elTr.appendChild(elTd);
	    elTd.appendChild(elRoleKeyInput);
	    
	    elTd = document.createElement("td");
	    elTd.id = "roleTd-" + dojo.trim(selectableRoles[i].roleKey);
	    elTr.appendChild(elTd);		
	    elTd.appendChild(document.createTextNode(selectableRoles[i].roleName));
		
	}  
	
	elRoleTr.appendChild(elRoleTbl);
  },
  
  modifyRole : function(el){
	  var checked = el.target.checked;
	  if(checked == true){
		  this.addUserToGroup(el);
	  }else{
		  this.removeUserFromGroup(el);
	  }
  },
  
  addDeleteUserIcon : function(){
	 var elUserTitleDiv = dojo.byId(this.currentUserTitle);
 	 var elDeleteUserImg = document.createElement("img");
 	 elDeleteUserImg.src = this.contextPath + "/catalog/images/asn-delete.png";
 	 elDeleteUserImg.title = this.resources.deleteUser;
 	 elDeleteUserImg.alt = this.resources.deleteUser;
 	 elDeleteUserImg.style.verticalAlign = "middle";
 	 elUserTitleDiv.appendChild(elDeleteUserImg);
	 dojo.connect(elDeleteUserImg,"onclick",this,"deleteUser"); 
  },
  
  addLineBreak: function(parent){
	  var elLineBreak = document.createElement("br");
	  parent.appendChild(elLineBreak);	  
  },
  
  findUserDn: function(){
	  var elUserDn = "";
	  dojo.query("#um-userInformation input[ type $= hidden ]").forEach(function(item) {
	    	elUserDn = item.value;
	  });
	  return dojo.trim(elUserDn);
  },
  
  findGroupDn:function(e){
	  var elGroupDn = "";
	  if (!e) e = window.event;
	  var target = (window.event) ? e.srcElement : e.target;
	  var qScope = target.parentNode.parentNode;
	  var gName = "";
	  dojo.query(" > td > input[ type $= hidden ]",qScope).forEach(function(item) {
		  elGroupDn = item.value;
		  gName = item.title;
	  });
	  this.currentGroupName = gName;
	  return dojo.trim(elGroupDn);	    
  },
  
  addUserToGroup: function(e) {	
    var elGroupDn = this.findGroupDn(e);
    var elUserDn = this.findUserDn();
    var sAddConfirmation = this.resources.addConfirmation;
    sAddConfirmation = sAddConfirmation.replace("{0}",this.currentUserName);
    sAddConfirmation = sAddConfirmation.replace("{1}",this.currentGroupName);
    var response = confirm(sAddConfirmation);
	if(response){
		var sSelfAddConfirmation = this.resources.selfAddConfirmation;
		sSelfAddConfirmation = sSelfAddConfirmation.replace("{0}",this.currentUserName);
		sSelfAddConfirmation = sSelfAddConfirmation.replace("{1}",this.currentGroupName);
	    var url = this.contextPath + this.groupsEndPoint + "/" + encodeURIComponent(elGroupDn) + "/addMember?member="+encodeURIComponent(elUserDn);
	    this.performAction(url,elUserDn,sSelfAddConfirmation);  
	}else{
		return;
	}
  },
  
  removeUserFromGroup: function(e) {    
	var elGroupDn = this.findGroupDn(e);
	var elUserDn = this.findUserDn();
	var sRemoveConfirmation = this.resources.removeConfirmation;
	sRemoveConfirmation = sRemoveConfirmation.replace("{0}",this.currentUserName);
	sRemoveConfirmation = sRemoveConfirmation.replace("{1}",this.currentGroupName);
    var response = confirm(sRemoveConfirmation);
	if(response){
		var sSelfRemoveConfirmation = this.resources.selfRemoveConfirmation;
		sSelfRemoveConfirmation = sSelfRemoveConfirmation.replace("{0}",this.currentUserName);
		sSelfRemoveConfirmation = sSelfRemoveConfirmation.replace("{1}",this.currentGroupName);
		var url = this.contextPath + this.groupsEndPoint + "/" + encodeURIComponent(elGroupDn) + "/removeMember?member="+encodeURIComponent(elUserDn);
		this.performAction(url,elUserDn,sSelfRemoveConfirmation);
	}else{
		return;
	}
  },
  
  performAction: function(url,userDn,sSelfConfirmation){	 	    
	    dojo.xhrGet({
        url: url,
	    preventCache: true,
	    timeout: this.requestTimeOut,
	    handleAs: "text",
	    load: dojo.hitch(this,function(response, ioArgs) {  
	    	if(response == "prompt" && sSelfConfirmation != ""){
	    		var response = confirm(sSelfConfirmation);
	    		if(response){
		    	  url = url + "&attempt=2";
		    	  this.performAction(url, userDn,"");
		    	  return;
	    		}
	    	}else{
		    	if(userDn != null && userDn.length > 0){
		    		return this.fetchUserInfo(userDn);
		    	}else{
		    		this.clearUserInfo();
		    		this.clearSearch();
		    		return;
		    	}	
	    	}
	    }),
	
	    error: dojo.hitch(this,function(response, ioArgs) {
	      this.showMessage(response,"errorMessage");
	      return response;
	    })
	  });	  	  
  },
  
  deleteUser: function(e) {
	var sDeleteConfirmation = this.resources.deleteUserConfirmation;
	sDeleteConfirmation = sDeleteConfirmation.replace("{0}",this.currentUserName);
	var response = confirm(sDeleteConfirmation);
	if(response){
		var sSelfDeleteUserConfirmation = this.resources.selfDeleteUserConfirmation;
		sSelfDeleteUserConfirmation = sSelfDeleteUserConfirmation.replace("{0}",this.currentUserName);
	    var elUserDn = this.findUserDn();    
	    var url = this.contextPath + this.usersEndPoint + "/" + encodeURIComponent(elUserDn) + "/delete";
	    this.performAction(url,null,sSelfDeleteUserConfirmation);
	}else{
		return;
	}
  },
        
  showMessage: function(msg,className) {    
    var elMsg = document.createElement("div");
    elMsg.id = this.currentUserHeader+"-messageText";
    if (className != null) {
      elMsg.className = className;
    }
    elMsg.appendChild(document.createTextNode(msg));
    dojo.byId(this.currentUserHeader).innerHTML = "";
    dojo.byId(this.currentUserHeader).appendChild(elMsg);
  }
  
});
	  
	  