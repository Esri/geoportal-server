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
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.BaseActionListener;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;

/**
 * Handles a sample action.
 * <p>
 * This class simply serves as a template for a controller.
 */
public class SampleController extends BaseActionListener {

// class variables =============================================================

// instance variables ==========================================================
  
// constructors ================================================================
  
/** Default constructor. */
public SampleController() {}

// properties ==================================================================

// methods =====================================================================

/**
 * Handles a secondary action.
 * <br/>This is a template for processing an action that is not
 * entered through the normal JSF processAction method.
 * @param event the associated JSF action event
 * @throws AbortProcessingException if processing should be aborted
 */
public void processSecondaryAction(ActionEvent event) 
  throws AbortProcessingException {
  try {
    RequestContext context = onExecutionPhaseStarted();
    authorizeAction(context);
    
    // process the action
    
  } catch (AbortProcessingException e) {
    throw(e);
  } catch (Throwable t) {
    handleException(t);
  } finally {
    onExecutionPhaseCompleted();
  }
}

/**
 * Handles the primary action.
 * <br/>This is the default entry point for a sub-class of BaseActionListener.
 * <br/>This BaseActionListener handles the JSF processAction method and
 * invokes the processSubAction method of the sub-class.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws AbortProcessingException if processing should be aborted
 * @throws Exception if an exception occurs
 */
@Override
protected void processSubAction(ActionEvent event, RequestContext context) 
  throws AbortProcessingException, Exception {
}

}
