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
package com.esri.gpt.framework.jsf;
import java.util.logging.Level;

import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.ServletRequest;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.LogUtil;

/**
 * Primary phase listener for the life cycle of a JSF based action.
 */
public class PrimaryPhaseListener implements PhaseListener {
  
// class variables =============================================================
 
// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public PrimaryPhaseListener() {
  LogUtil.getLogger().finer("Instantiating PrimaryPhaseListener");
}

// properties ==================================================================

/**
 * Gets the Faces PhaseId that this listener will listen for.
 * @return the PhaseId - PhaseId.ANY_PHASE
 */
public PhaseId getPhaseId() {
  return PhaseId.ANY_PHASE;
}

// methods =====================================================================

/**
 * Faces event fired after a PhaseEvent is processed.
 */
public void afterPhase(PhaseEvent event) {
  if (event.getPhaseId().equals(PhaseId.INVOKE_APPLICATION)) {
    onExecutionPhaseCompleted();
  }
  LogUtil.getLogger().finest("After phase: "+event.getPhaseId().toString());
  if(LogUtil.getLogger().isLoggable(Level.FINEST) &&
      event.getPhaseId().equals(PhaseId.RENDER_RESPONSE)){
    

    //LogUtil.getLogger().finest("Printing the tree");
    //DebugUtil.printTree( 
       // FacesContext.getCurrentInstance().getViewRoot(),System.out); 
    
  }
}

/**
 * Faces event fired before a PhaseEvent is processed.
 */
public void beforePhase(PhaseEvent event) {
  LogUtil.getLogger().finest("Before phase: "+event.getPhaseId().toString());  
  if (event.getPhaseId().equals(PhaseId.RESTORE_VIEW)) {
    // start of the process, nothing required at this time
  }
}

/**
 * Fired after the INVOKE_APPLICATION phase.
 */
private void onExecutionPhaseCompleted() {
  
  // ensure that the RequestContext.onExecutionPhaseCompleted() is fired
  // if a request context has been referenced during the cycle
  FacesContext fc = FacesContext.getCurrentInstance();
  if ((fc != null) && (fc.getExternalContext() != null)) {
    Object oReq = fc.getExternalContext().getRequest();
    if ((oReq != null) && (oReq instanceof ServletRequest)) {
      ServletRequest sr = (ServletRequest)oReq;
      String sKey = RequestContext.REFERENCEKEY;
      RequestContext rc = (RequestContext)sr.getAttribute(sKey);
      if (rc != null) {
        LogUtil.getLogger().finest("Ensuring RequestContext.onExecutionPhaseCompleted()");
        rc.onExecutionPhaseCompleted();
      }
      
      // check to see if an exception was thrown by the multipart filter
      Object oErr = sr.getAttribute("MultipartFilterException");
      if ((oErr != null) && (oErr instanceof Throwable)) {
        FacesContextBroker broker = new FacesContextBroker();
        broker.extractMessageBroker().addErrorMessage((Throwable)oErr);
      }
    }
  }
}

}
