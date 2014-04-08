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
 * gpt-asn.js
 * Assertion support (ratings, comments, ...).
 */

dojo.declare("AsnMain", null, {  
  enabled: false,
  baseContextPath: null,
  imagesPath: null,
  resourceId: null,  

  loadResources: function() {
    var ratings = new gpt.AsnRatings();
    ratings.main = this;
    ratings.loadResources();
    
    var comments = new gpt.AsnComments();
    comments.main = this;
    comments.loadResources();
  },
  
  makeAnchor: function(text) {
	  var el = document.createElement("a");
	  el.setAttribute("href","javascript:void(0);");
	  if (text != null) {
	    el.appendChild(document.createTextNode(text));
	  }
	  return el;
	},
	  
  makeImg: function(icon,tip,className) {
    var el = document.createElement("img");
    if (className != null) el.className = className;
		el.src = this.imagesPath+"/"+icon;
		el.title = tip;
		el.alt = tip;
		return el;
  },
  
  makeImgButton: function(icon,tip,subject,predicate,value) {
    var el = document.createElement("img");
    el.className = "asn-imgButton";
    el.src = this.imagesPath+"/"+icon;
    el.title = tip;
    el.alt = tip;
    el.asnSubject = subject;
    el.asnPredicate = predicate;
    el.asnValue = value;
    return el;
  },
	  
	makeSpan: function(text,className) {
	  var el = document.createElement("span");
	  if (className != null) el.className = className;
	  el.appendChild(document.createTextNode(text));
	  return el;
	},
	
	makeLabel: function(text, className, forId) {
	  var el = document.createElement("span");
	  if (className != null) el.className = className;
	  
	  var lbl = dojo.create("label", {"for": forId});
	  lbl.appendChild(document.createTextNode(text));
	  el.appendChild(lbl);
	  return el;	
	},
	    
	onPageModified: function() {}

});

dojo.provide("gpt.AsnAssertionSet");
dojo.declare("gpt.AsnAssertionSet",null,{
  main: null,
  resources: {},
  urnPrefix: null,
  userCanCreate: false,
  userCanQuery: true,
  
  handleValidResponse: function(responseObject,ioArgs,reqProperty,respProperty) {},
  
  loadResources: function() {
    var s = this.urnPrefix;
    var p = this.urnPrefix+":uiresources";
    var u = this.main.baseContextPath+"?s="+escape(s)+"&p="+escape(p)+"&f=json";
    dojo.xhrGet({
      handleAs: "json",
      preventCache: true,
      url: u,
      error: dojo.hitch(this,"onError"),
      load: dojo.hitch(this,"onOperationComplete")
    });
  },
    
  onError: function(error,ioArgs) {  
    var msg = null;
    if (ioArgs == null) {
      msg = error.message;
    } else {
      if ((ioArgs.xhr.status >= 200) && (ioArgs.xhr.status < 300)) {
        msg = error.message;
      } else {
        msg = " HTTP: " +ioArgs.xhr.status+", "+ioArgs.args.url;
      }
    }
    if (msg != null) alert(msg);
  },
    
  onOperationClicked: function(e) {
    if (!e) e = window.event;
    var el = (window.event) ? e.srcElement : e.target;
    if ((el != null) && (el.asnSubject != null) && (el.asnPredicate != null)) {
      var s = el.asnSubject;
      var p = el.asnPredicate;
      var v = el.asnValue;      
      var u = this.main.baseContextPath+"?s="+escape(s)+"&p="+escape(p);
      if (v != null) u += "&v="+escape(v);
      u += "&f=json";
      var bOk = true;
      if (el.asnPrompt != null) {
        bOk = confirm(el.asnPrompt);
      }
      if (bOk) {
        dojo.xhrGet({
          handleAs: "json",
          preventCache: true,
          url: u,
          asnSubject: s,
          asnPredicate: p,
          error: dojo.hitch(this,"onError"),
          load: dojo.hitch(this,"onOperationComplete")
        });
      }
    }
  },
  
  onOperationComplete: function(responseObject,ioArgs) {
    var root = responseObject;
    var reqProperty = null;
    var respProperty = null;
    
    if ((root.subject == null) || (root.predicate == null)) {
      var error = new Object();
      error.message = "Invalid server response.";
      this.onError(error,ioArgs);
    } else if (root.subject == "urn:esri:geoportal:operation:exception") {
      this.onError(root.value);
      var error = new Object();
      error.message = root.value;
      this.onError(error,ioArgs);
      
    } else if ((root.subject == this.urnPrefix) && 
               (root.predicate == this.urnPrefix+":uiresources:response")) {
      var uiProps = root.properties;
      var uiPfx = this.urnPrefix+":uiresource:";
      if (uiProps != null) {
        for (var i=0;i<uiProps.length; i++) {
          var uip = uiProps[i];
          if ((uip.predicate != null) && (uip.value != null) && (uip.value.length > 0)) {
            if (uip.predicate.indexOf(uiPfx) == 0) {
              var nm = uip.predicate.substring(uiPfx.length);
              eval("this.resources."+nm+"=uip.value;");
            }
          }
        }
      }
      this.onResourcesLoaded();
      
    } else { 
      reqProperty = root;
      if ((reqProperty.properties != null) && (reqProperty.properties.length == 1)) {
        respProperty = reqProperty.properties[0];
      }
      this.handleValidResponse(responseObject,ioArgs,reqProperty,respProperty);
    }
  },
  
  onPostClicked: function(e) {
    if (!e) e = window.event;
    var el = (window.event) ? e.srcElement : e.target;
    if ((el != null) && (el.asnSubject != null) && (el.asnPredicate != null) && 
        (el.asnTextAreaId != null)) {
      var s = el.asnSubject;
      var p = el.asnPredicate;
      var u = this.main.baseContextPath+"?s="+escape(s)+"&p="+escape(p)+"&f=json";
      var elText = dojo.byId(el.asnTextAreaId);
      if (elText != null) {
        var v = dojo.trim(elText.value);
        if (v.length == 0) {
          if (el.asnEmptyWaning != null) {
            alert(el.asnEmptyWaning);
          }
        } else {
          elText.value = "";
          dojo.xhrPost({
            handleAs: "json",
            preventCache: true,
            url: u,
            headers: {"Content-Type": "text/plain"},
            postData: v,
            asnSubject: s,
            asnPredicate: p,
            error: dojo.hitch(this,"onError"),
            load: dojo.hitch(this,"onOperationComplete")
          });
        }
      }
    }
  },
  
  onResourcesLoaded: function() {},
  
  onTextAreaChange: function(e) {
    var el = this;
    if ((el != null) && (el.value != null) && (el.asnMaxLength != null)) {
      var txt = el.value;
      if (txt.length > el.asnMaxLength) {
        el.value = txt.substring(0,el.asnMaxLength);
      }
    }
  },
  
  queryByAssertionId: function(s,p,isNew) {
    if (this.userCanQuery) {
      var u = this.main.baseContextPath+"?s="+escape(s)+"&p="+escape(p)+"&f=json";
      dojo.xhrGet({
        handleAs: "json",
        preventCache: true,
        url: u,
        asnSubject: s,
        asnPredicate: p,
        asnIsNew: isNew,
        error: dojo.hitch(this,"onError"),
        load: dojo.hitch(this,"onOperationComplete")
      });
    }
  },
  
  queryByResourceId: function(p,start,max) {
    if (this.userCanQuery) {
      var s = "urn:esri:geoportal:resourceid:"+this.main.resourceId;
      var u = this.main.baseContextPath+"?s="+escape(s)+"&p="+escape(p)+"&f=json";
      if (start != null) u += "&start="+escape(start);
      if (max != null) u += "&max="+escape(max);
      dojo.xhrGet({
        handleAs: "json",
        preventCache: true,
        url: u,
        asnSubject: s,
        asnPredicate: p,
        error: dojo.hitch(this,"onError"),
        load: dojo.hitch(this,"onOperationComplete")
      });
    }
  },
  
  toggleElement: function(sId,bDisplay) {
    var el = dojo.byId(sId);
    if (el != null) {
      if (bDisplay) el.style.display = "block";
      else el.style.display = "none";
    }
  }

});

dojo.provide("gpt.AsnRatings");
dojo.declare("gpt.AsnRatings", gpt.AsnAssertionSet, { 
  urnPrefix: "urn:esri:geoportal:rating",
  
  resources: {
    caption: "Ratings for this resource:",
    totalUpTip: "Up votes",
    totalDownTip: "Down votes",
    upTip: "Vote up",
    downTip: "Vote down",
    youVoted: "You voted:",
    youVotedUpTip: "Up",
    youVotedDownTip: "Down",
    youCan: "You can:",
    deleteTip: "Delete your vote",
    switchTip: "Switch your vote",
    upIcon: "asn-vote-up.png",
    downIcon: "asn-vote-down.png",
    deleteIcon: "asn-delete.png"
  },
  
  handleValidResponse: function(responseObject,ioArgs,reqProperty,respProperty) {
    var subjectResource = "urn:esri:geoportal:resourceid:"+this.main.resourceId;
    
    if ((reqProperty.predicate == "urn:esri:geoportal:rating:create:response") ||
        (reqProperty.predicate == "urn:esri:geoportal:rating:update:response") ||
        (reqProperty.predicate == "urn:esri:geoportal:rating:delete:response") ||
        (reqProperty.predicate == "urn:esri:geoportal:rating:enable:response") ||
        (reqProperty.predicate == "urn:esri:geoportal:rating:disable:response")) {
      this.queryByResourceId("urn:esri:geoportal:rating:query");
            
    } else if ((reqProperty.subject == subjectResource) &&
        (reqProperty.predicate == "urn:esri:geoportal:rating:query:response")) {
      var ratingResources = this.resources;
      this.userCanCreate = false;
      var nUp = 0;
      var nDown = 0;
      var nTotal = 0;
      var myVote = null;
      var myVoteId = null;
      var bShow = false;
      
      if (reqProperty.properties != null) {
        var props = reqProperty.properties;
        for (var i=0;i<props.length; i++) {
          var prop = props[i];
          if (prop.predicate == "urn:esri:geoportal:rating:value:up:count") {
            bShow = true;
            nUp = parseInt(prop.value);
          } else if (prop.predicate == "urn:esri:geoportal:rating:value:down:count") {
            bShow = true;
            nDown = parseInt(prop.value);
          } else if (prop.predicate == "urn:esri:geoportal:rating:count") {
            nTotal = parseInt(prop.value);   
          } else if (prop.predicate == "urn:esri:geoportal:rating:activeUser:canCreate") {
            this.userCanCreate = (prop.value == "true");
          } else if (prop.predicate == "urn:esri:geoportal:rating:activeUser:previousValue") {
            myVoteId = prop.subject;
            myVote = prop.value;
          }
        }
      }
    
      var elAnchor;
      var elImg;
      var elDiv = document.createElement("div");
      elDiv.id = "asn-rating-content";
      
      if (bShow) {
    
        // caption
        elDiv.appendChild(this.main.makeSpan(ratingResources.caption,"sectionCaption"));
        elDiv.appendChild(this.main.makeSpan("","separator"));
      
        // up votes
        elDiv.appendChild(this.main.makeSpan(""+nUp));
        if (this.userCanCreate && (myVote == null)) {
          elImg = this.main.makeImgButton(ratingResources.upIcon,ratingResources.totalUpTip,
              subjectResource,"urn:esri:geoportal:rating:create","urn:esri:geoportal:rating:value:up");
          dojo.connect(elImg,"onclick",this,"onOperationClicked");
          elDiv.appendChild(elImg);
        } else {
          elImg = this.main.makeImg(ratingResources.upIcon,ratingResources.totalUpTip,"asn-img");
          elDiv.appendChild(elImg);
        }
        elDiv.appendChild(this.main.makeSpan("","separator"));
        
        // down votes
        elDiv.appendChild(this.main.makeSpan(""+nDown));
        if (this.userCanCreate && (myVote == null)) {
          elImg = this.main.makeImgButton(ratingResources.downIcon,ratingResources.totalDownTip,
              subjectResource,"urn:esri:geoportal:rating:create","urn:esri:geoportal:rating:value:down");
          dojo.connect(elImg,"onclick",this,"onOperationClicked");
          elDiv.appendChild(elImg);
        } else {
          elImg = this.main.makeImg(ratingResources.downIcon,ratingResources.totalDownTip,"asn-img");
          elDiv.appendChild(elImg);
        }
        
      }
          
      // my vote
      if (bShow && (myVote != null)) {
        var sIcon = null;
        var sTip = null;
        var sSwitchIcon = null;
        var sSwitchPredicate = null;
        if (myVote == "urn:esri:geoportal:rating:value:up") {
          sIcon = ratingResources.upIcon;
          sTip = ratingResources.youVotedUpTip;
          sSwitchIcon = ratingResources.downIcon; 
          sSwitchPredicate = "urn:esri:geoportal:rating:value:down";
        } else if (myVote == "urn:esri:geoportal:rating:value:down") {
          sIcon = ratingResources.downIcon;
          sTip = ratingResources.youVotedDownTip;
          sSwitchIcon = ratingResources.upIcon; 
          sSwitchPredicate = "urn:esri:geoportal:rating:value:up";
        }
        if (sIcon != null) {
          
          // show my vote
          elDiv.appendChild(this.main.makeSpan("","separator"));
          elDiv.appendChild(this.main.makeSpan(ratingResources.youVoted,"subCaption"));
          elImg = this.main.makeImg(sIcon,sTip,"asn-img");
          elDiv.appendChild(elImg);
          
          if (myVoteId != null) {
            elDiv.appendChild(this.main.makeSpan("","separator"));
            elDiv.appendChild(this.main.makeSpan(ratingResources.youCan,"subCaption"));
            
            // switch
            elImg = this.main.makeImgButton(sSwitchIcon,ratingResources.switchTip,
                myVoteId,"urn:esri:geoportal:rating:update",sSwitchPredicate);
            dojo.connect(elImg,"onclick",this,"onOperationClicked");
            elDiv.appendChild(elImg);
            
            // delete
            elImg = this.main.makeImgButton(ratingResources.deleteIcon,ratingResources.deleteTip,
                myVoteId,"urn:esri:geoportal:rating:delete",null);
            dojo.connect(elImg,"onclick",this,"onOperationClicked");
            elDiv.appendChild(elImg);
          }
          
        }
      }
    
      var elAsnRating = dojo.byId("asn-rating");
      var elPrevious = dojo.byId("asn-rating-content");
      if (elPrevious != null) {
        elPrevious.parentNode.removeChild(elPrevious);
      }  
      elAsnRating.appendChild(elDiv);
      this.main.onPageModified();
      
    }
  },
  
  onResourcesLoaded: function() {
    this.queryByResourceId("urn:esri:geoportal:rating:query");
  }
  
});

dojo.provide("gpt.AsnComments");
dojo.declare("gpt.AsnComments", gpt.AsnAssertionSet, {  
  hitCount: 0,
  nextRecord: 0,
  urnPrefix: "urn:esri:geoportal:comment",
  
  resources: {
    maxLength: 4000,
    caption: "Comments",
    addComment: "Add a comment:",
    postComment: "Post",
    disabledComment: "Disabled",
    emptyComment: "Please enter a comment.",
    enableTip: "Enable",
    disableTip: "Disable",
    editTip: "Edit",
    editedTip: "Edited",
    deleteTip: "Delete",
    deletePrompt: "Are you sure you want to delte this comment?",
    more: "more",
    editIcon: "asn-edit.png",
    deleteIcon: "asn-delete.png",
    enableIcon: "asn-enable.png",
    disableIcon: "asn-disable.png"
  },
  
  handleValidResponse: function(responseObject,ioArgs,reqProperty,respProperty) {
    var subjectResource = "urn:esri:geoportal:resourceid:"+this.main.resourceId;
  
    if (reqProperty.predicate == "urn:esri:geoportal:comment:create:response") {
      if (reqProperty.value != null) {
        this.queryByAssertionId(reqProperty.value,"urn:esri:geoportal:comment:query",true);
      }
    
    } else if ((reqProperty.predicate == "urn:esri:geoportal:comment:update:response") ||
        (reqProperty.predicate == "urn:esri:geoportal:comment:enable:response") ||
        (reqProperty.predicate == "urn:esri:geoportal:comment:disable:response")) {
      this.queryByAssertionId(reqProperty.subject,"urn:esri:geoportal:comment:query",false);
      
    } else if (reqProperty.predicate == "urn:esri:geoportal:comment:delete:response") {
      var elExisting = dojo.byId(reqProperty.subject);
      if (elExisting != null) {
        elExisting.parentNode.removeChild(elExisting);
        this.hitCount = this.hitCount - 1;
        this.nextRecord = this.nextRecord - 1;
        this.updateHits(this.hitCount);
        this.main.onPageModified();
      }

    } else if ((reqProperty.predicate == "urn:esri:geoportal:comment:query:response") &&
               (respProperty != null) && (respProperty.predicate == "urn:esri:geoportal:comment")) {
      var isNew = false;
      if ((ioArgs != null) && (ioArgs.args != null)) {
        isNew = ioArgs.args.asnIsNew;
        if (isNew == null) isNew = false;
      }
      var elComment = this.makeCommentElement(respProperty,true);
      if ((elComment != null) && (elComment.id != null)) {
        var elExisting = dojo.byId(elComment.id);
        if (elExisting != null) {
          dojo.place(elComment,elExisting,"replace");
        } else if (isNew) {
          var elList = dojo.byId("asn-comments-list");
          if (elList != null) {
            dojo.place(elComment,elList,"first");
            this.hitCount = this.hitCount + 1;
            this.nextRecord = this.nextRecord + 1;
            this.updateHits(this.hitCount);
          }
        }
        this.main.onPageModified();
      }
        
    } else if ((reqProperty.subject == subjectResource) &&
               (reqProperty.predicate == "urn:esri:geoportal:comment:query:response")) { 
      this.userCanCreate = false;
      this.nextRecord = 0;
      this.initElements();
      this.toggleElement("asn-comments-more",false);
      var elCommentList = dojo.byId("asn-comments-list");
      
      var props = reqProperty.properties;
      if (props != null) {
        for (var i=0;i<props.length; i++) {
          var prop = props[i];
          if (prop.predicate == "urn:esri:geoportal:comment:query:hits") {
            this.hitCount = parseInt(prop.value);
            this.updateHits(this.hitCount);
          } else if (prop.predicate == "urn:esri:geoportal:comment:query:nextRecord") {
            this.nextRecord = parseInt(prop.value);
            if (this.nextRecord > 0) this.toggleElement("asn-comments-more",true);
          } else if (prop.predicate == "urn:esri:geoportal:comment:activeUser:canCreate") {
            this.userCanCreate = (prop.value == "true");
          } else if (prop.predicate == "urn:esri:geoportal:comment") {
            var elComment = this.makeCommentElement(prop);
            if (elComment != null) {
              elCommentList.appendChild(elComment);
            }
          }
        }
      }
      
      this.toggleElement("asn-comments-add",this.userCanCreate);
      this.main.onPageModified();
    }
  },
  
  initElements: function() {
    var subjectResource = "urn:esri:geoportal:resourceid:"+this.main.resourceId;
    var commentResources = this.resources;
    
    var elComments = dojo.byId("asn-comments");
    var elCaption = dojo.byId("asn-comments-caption");
    if (elCaption == null) {
      elCaption = document.createElement("div");
      elCaption.id = "asn-comments-caption";
      elCaption.appendChild(this.main.makeSpan(commentResources.caption,"sectionCaption"));
      var elHits = this.main.makeSpan("","asn-comments-count");
      elHits.id = "asn-comments-count";
      elCaption.appendChild(elHits);
      elComments.appendChild(elCaption);

      var elAdd = document.createElement("div");
      elAdd.id = "asn-comments-add";
      elAdd.className = "asn-subSection asn-comments-add";
      elAdd.style.display = "none";
      elAdd.appendChild(this.main.makeLabel(commentResources.addComment,"sectionCaption", "asn-comments-add-textarea"));
      var elEdit = this.makeEditArea("asn-comments-add-editarea","asn-comments-add-textarea",null);
      elAdd.appendChild(elEdit);
      elComments.appendChild(elAdd);
      
      var elList = document.createElement("div");
      elList.id = "asn-comments-list";
      elComments.appendChild(elList);

      var elMore = document.createElement("div");
      elMore.id = "asn-comments-more";
      elMore.className = "asn-comments-more";
      elMore.style.display = "none";
      var elMoreLink = this.main.makeAnchor(commentResources.more);
      dojo.connect(elMoreLink,"onclick",this,"onMoreClicked");
      elMore.appendChild(elMoreLink);
      elComments.appendChild(elMore);
    }
  },
  
  makeCommentElement: function(prop) {
    var subjectResource = "urn:esri:geoportal:resourceid:"+this.main.resourceId;
    var commentResources = this.resources;
    var elComment = null;
    var commentProps = prop.properties;
    if (commentProps != null) {
      var asnId = prop.subject;
      var username = "";
      var date = null;
      var editDate = null;
      var text = null;
      var isEnabled = true;
      var canEdit = false;
      var canDelete = false;
      var canEnable = false;
      var canDisable = false;
      for (var i=0; i<commentProps.length; i++) {
        var commentProp = commentProps[i];
        if (commentProp.predicate == "urn:esri:geoportal:comment:username") {
          username = commentProp.value;
        } else if (commentProp.predicate == "urn:esri:geoportal:comment:date") {
          date = commentProp.value;
        } else if (commentProp.predicate == "urn:esri:geoportal:comment:edit:date") {
          editDate = commentProp.value;
        } else if (commentProp.predicate == "urn:esri:geoportal:comment:enabled") {
          isEnabled = (commentProp.value == "true");
        } else if (commentProp.predicate == "urn:esri:geoportal:comment:value") {
          text = commentProp.value;
        } else if (commentProp.predicate == "urn:esri:geoportal:comment:activeUser:capabilities") {
          var userProps = commentProp.properties;
          if (userProps != null) {
            for (var j=0; j<userProps.length; j++) {
              var userProp = userProps[j];
              if (userProp.predicate == "urn:esri:geoportal:comment:activeUser:canUpdate") {
                canEdit = (userProp.value == "true");
              } else if (userProp.predicate == "urn:esri:geoportal:comment:activeUser:canDelete") {
                canDelete = (userProp.value == "true");
              } else if (userProp.predicate == "urn:esri:geoportal:comment:activeUser:canEnable") {
                canEnable = (userProp.value == "true");
              } else if (userProp.predicate == "urn:esri:geoportal:comment:activeUser:canDisable") {
                canDisable = (userProp.value == "true");
              }
            }
          }
        }
      }
      if (isEnabled) {
        canEnable = false;
      } else { 
        canEdit = false; 
        canDisable = false;
      }
      
      var elImg;
      var elSpan;
      elComment = document.createElement("div");
      elComment.id = asnId;
      elComment.className = "asn-subSection asn-comment";
      elComment.appendChild(this.main.makeSpan(username,"asn-comment-user"));
      elComment.appendChild(this.main.makeSpan(date,"asn-comment-date"));
      
      if (canEnable) {
        elImg = this.main.makeImgButton(commentResources.enableIcon,commentResources.enableTip,
            asnId,"urn:esri:geoportal:comment:enable",null);
        dojo.connect(elImg,"onclick",this,"onOperationClicked");
        elComment.appendChild(elImg);
      }
      
      if (canDisable) {
        elImg = this.main.makeImgButton(commentResources.disableIcon,commentResources.disableTip,
            asnId,"urn:esri:geoportal:comment:disable",null);
        dojo.connect(elImg,"onclick",this,"onOperationClicked");
        elComment.appendChild(elImg);
      }
      
      if (canEdit) {
        elImg = this.main.makeImgButton(commentResources.editIcon,commentResources.editTip,
            asnId,"urn:esri:geoportal:comment:update",null);
        dojo.connect(elImg,"onclick",this,"onEditClicked");
        elComment.appendChild(elImg);
      }
      
      if (canDelete) {
        elImg = this.main.makeImgButton(commentResources.deleteIcon,commentResources.deleteTip,
            asnId,"urn:esri:geoportal:comment:delete",null);
        elImg.asnPrompt = commentResources.deletePrompt;
        dojo.connect(elImg,"onclick",this,"onOperationClicked");
        elComment.appendChild(elImg);
      }
      
      if (editDate != null) {
        elSpan = this.main.makeSpan(editDate,"asn-comment-editdate");
        elSpan.title = commentResources.editedTip;
        elComment.appendChild(elSpan);
      }
      
      if (text != null) {
        var elText = document.createElement("div");
        elText.id = asnId+":comment:text";
        elText.className = "asn-comment-text";  
        elText.asnOriginalText = text;
        elText.appendChild(document.createTextNode(text));
        elComment.appendChild(elText);
      } else if (!isEnabled) {
        var elText = document.createElement("div");
        elText.className = "asn-comment-text-disabled";  
        elText.appendChild(document.createTextNode(commentResources.disabledComment));
        elComment.appendChild(elText);
      } 
    }
    
    return elComment;
  },
  
  makeEditArea: function(divId,textareaId,asnSubject) {
    var subjectResource = "urn:esri:geoportal:resourceid:"+this.main.resourceId;
    var commentResources = this.resources;
    var elDiv = document.createElement("div");
    elDiv.id = divId;
    elDiv.className = "asn-comment-editarea";
    var elText = document.createElement("textarea");
    elText.id = textareaId;
    elText.cols = 80;
    elText.rows = 4;
    elText.asnMaxLength = commentResources.maxLength;
    var elPost = document.createElement("button");
    elPost.onclick = function() {return false;}
    elPost.appendChild(document.createTextNode(commentResources.postComment));
    if (asnSubject == null) {
      elPost.asnSubject = subjectResource;
      elPost.asnPredicate = "urn:esri:geoportal:comment:create";
    } else {
      elPost.asnSubject = asnSubject;
      elPost.asnPredicate = "urn:esri:geoportal:comment:update";
    }
    elPost.asnTextAreaId = textareaId;
    elPost.asnEmptyWaning = commentResources.emptyComment;
    elDiv.appendChild(elText);
    elDiv.appendChild(elPost);
    dojo.connect(elPost,"onclick",this,"onPostClicked");
    dojo.connect(elText,"onchange",dojo.hitch(elText,this.onTextAreaChange));
    return elDiv;
  },
    
  onEditClicked: function(e) {
    if (!e) e = window.event;
    var el = (window.event) ? e.srcElement : e.target;
    if ((el != null) && (el.asnSubject != null) && (el.asnPredicate != null)) {
      var elText = dojo.byId(el.asnSubject+":comment:text");
      if (elText != null) {
        var elEdit = this.makeEditArea(el.asnSubject+":comment:editarea",
            el.asnSubject+":comment:textarea",el.asnSubject);
        if (elEdit != null) {
          elEdit.asnOriginalText = elText.asnOriginalText;
          for (var i=0;i<elEdit.childNodes.length; i++) {
            var nd = elEdit.childNodes[i];
            if ((nd != null) && (nd.nodeName != null)) {
              if (nd.nodeName.toLowerCase() == "textarea") {
                nd.value = elEdit.asnOriginalText;
                dojo.place(elEdit,elText,"replace");
                break;
              }
            }
          }  
        }
      } else {
        var elEdit = dojo.byId(el.asnSubject+":comment:editarea");
        if (elEdit != null) {
          elText = document.createElement("div");
          elText.id = el.asnSubject+":comment:text";
          elText.className = "asn-comment-text";
          elText.asnOriginalText = elEdit.asnOriginalText;
          elText.appendChild(document.createTextNode(elEdit.asnOriginalText));
          dojo.place(elText,elEdit,"replace");
        }
      }
      this.main.onPageModified();
    }
  },
  
  onMoreClicked: function(e) {
    if (this.nextRecord > 0) {
      this.queryByResourceId("urn:esri:geoportal:comment:query",this.nextRecord);
    }
  },
  
  onResourcesLoaded: function() {
    this.queryByResourceId("urn:esri:geoportal:comment:query");
  },
    
  updateHits: function(n) {
    var elHits = dojo.byId("asn-comments-count");
    if (elHits != null) {
      var s = ""+n;
      if (n <= 0) s = "";
      elHits.innerHTML = s;
    }
  }
  
});
