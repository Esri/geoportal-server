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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.JspException;

/**
 * Date picker configuration JavaScript generator. Facilitiates date picker 
 * localization.
 */
public class DatePickerConfig extends SimpleTagSupport {

/**
 * Called by the container to invoke this tag. 
 * The implementation of this method is provided by the tag library developer,
 * and handles all tag processing, body iteration, etc.
 */
@Override
public void doTag() throws JspException {

  JspWriter out = getJspContext().getOut();
  
  HttpServletRequest request = 
    new FacesContextBroker().extractHttpServletRequest();
  
  try {
    
    out.println("<script  type=\"text/javascript\">");
    
    out.println("function DatePickerConfig() {");

    out.println("var _dpClearText       = \"Erase\"");
    out.println("var _dpClearStatus     = \"Erase and return\"");
    out.println("var _dpCloseText       = \"Close\"");
    out.println("var _dpCloseStatus     = \"Close without change\"");
    out.println("var _dpPrevText        = \"&lt;Prev\"");
    out.println("var _dpPrevStatus      = \"Show the previous month\"");
    out.println("var _dpNextText        = \"Next&gt;\"");
    out.println("var _dpNextStatus      = \"Show the next month\"");
    out.println("var _dpCurrentText     = \"Today\"");
    out.println("var _dpCurrentStatus   = \"Show the current month\"");
    out.println("var _dpMonthNames      = \"['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']\"");
    out.println("var _dpMonthStatus     = \"Show a different month\"");
    out.println("var _dpYearStatus      = \"Show a different year\"");
    out.println("var _dpWeakHeader      = \"Wk\"");
    out.println("var _dpWeakStatus      = \"Week of the year\"");
    out.println("var _dpDayNames        = \"['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']\"");
    out.println("var _dpDayNamesShort   = \"['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']\"");
    out.println("var _dpDayNamesMin     = \"['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa']\"");
    out.println("var _dpDayStatus       = \"Set DD as first week day\"");
    out.println("var _dpDateStatus      = \"Select DD, M d\"");
    out.println("var _dpInitStatus      = \"Select a date\"");
    
    out.println("this.initialize = function initialize() {");
    
    java.lang.Object gptMsgO = request.getAttribute("gptMsg");
    if (gptMsgO!=null && gptMsgO instanceof java.util.Map) {
      java.util.Map<String,String> gptMsg = (java.util.Map<String,String>)gptMsgO;
      
    out.println("_dpClearText       = '" + gptMsg.get("catalog.general.datepicker.clearText") + "'");
    out.println("_dpClearStatus     = '" + gptMsg.get("catalog.general.datepicker.clearStatus") + "'");
    out.println("_dpCloseText       = '" + gptMsg.get("catalog.general.datepicker.closeText") + "'");
    out.println("_dpCloseStatus     = '" + gptMsg.get("catalog.general.datepicker.closeStatus") + "'");
    out.println("_dpPrevText        = '" + gptMsg.get("catalog.general.datepicker.prevText") + "'");
    out.println("_dpPrevStatus      = '" + gptMsg.get("catalog.general.datepicker.prevStatus") + "'");
    out.println("_dpNextText        = '" + gptMsg.get("catalog.general.datepicker.nextText") + "'");
    out.println("_dpNextStatus      = '" + gptMsg.get("catalog.general.datepicker.nextStatus") + "'");
    out.println("_dpCurrentText     = '" + gptMsg.get("catalog.general.datepicker.currentText") + "'");
    out.println("_dpCurrentStatus   = '" + gptMsg.get("catalog.general.datepicker.currentStatus") + "'");
    out.println("_dpMonthNames      = "  + gptMsg.get("catalog.general.datepicker.monthNames"));
    out.println("_dpMonthStatus     = '" + gptMsg.get("catalog.general.datepicker.monthStatus") + "'");
    out.println("_dpYearStatus      = '" + gptMsg.get("catalog.general.datepicker.yearStatus") + "'");
    out.println("_dpWeakHeader      = '" + gptMsg.get("catalog.general.datepicker.weakHeader") + "'");
    out.println("_dpWeakStatus      = '" + gptMsg.get("catalog.general.datepicker.weakStatus") + "'");
    out.println("_dpDayNames        = "  + gptMsg.get("catalog.general.datepicker.dayNames"));
    out.println("_dpDayNamesShort   = "  + gptMsg.get("catalog.general.datepicker.dayNamesShort"));
    out.println("_dpDayNamesMin     = "  + gptMsg.get("catalog.general.datepicker.dayNamesMin"));
    out.println("_dpDayStatus       = '" + gptMsg.get("catalog.general.datepicker.dayStatus") + "'");
    out.println("_dpDateStatus      = '" + gptMsg.get("catalog.general.datepicker.dateStatus") + "'");
    out.println("_dpInitStatus      = '" + gptMsg.get("catalog.general.datepicker.initStatus") + "'");
      
    }
    
    out.println("}");
    
    out.println("this.options = function options() {");
    out.println("return { dateFormat:     'yy-mm-dd',");
    out.println("showOn:         'button',");
    out.println("clearText:      this._dpClearText,");
    out.println("clearStatus:    this._dpClearStatus,");
    out.println("closeText:      this._dpCloseText,");
    out.println("closeStatus:    this._dpCloseStatus,");
    out.println("prevText:       this._dpPrevText,");
    out.println("prevStatus:     this._dpPrevStatus,");
    out.println("nextText:       this._dpNextText,");
    out.println("nextStatus:     this._dpNextStatus,");
    out.println("currentText:    this._dpCurrentText,");
    out.println("currentStatus:  this._dpCurrentStatus,");
    out.println("monthNames:     this._dpMonthNames,");
    out.println("monthStatus:    this._dpMonthStatus,");
    out.println("yearStatus:     this._dpYearStatus,");
    out.println("weakHeader:     this._dpWeakHeader,");
    out.println("weakStatus:     this._dpWeakStatus,");
    out.println("dayNames:       this._dpDayNames,");
    out.println("dayNamesShort:  this._dpDayNamesShort,");
    out.println("dayNamesMini:   this._dpDayNamesMin,");
    out.println("dayStatus:      this._dpDayStatus,");
    out.println("dateStatus:     this._dpDateStatus,");
    out.println("initStatus:     this._dpInitStatus");
    out.println("};");
    out.println("}");

    out.println("this.attach = function attach(node) {");
    out.println("var target = $(\"#\"+node.replace(/\\:/,'\\\\:'));");
    out.println("target.datepicker(this.options());");
    out.println("target.unbind('keypress', this._doKeyPress);");
    out.println("}");
    
    out.println("}");
    
    out.println("</script>");
    
    JspFragment f = getJspBody();
    if (f != null) {
      f.invoke(out);
    }
  } catch (java.io.IOException ex) {
    throw new JspException(ex.getMessage());
  }

}
}