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

    out.println("_dpClearText       : \"Erase\";");
    out.println("_dpClearStatus     : \"Erase and return\";");
    out.println("_dpCloseText       : \"Close\";");
    out.println("_dpCloseStatus     : \"Close without change\";");
    out.println("_dpPrevText        : \"&lt;Prev\";");
    out.println("_dpPrevStatus      : \"Show the previous month\";");
    out.println("_dpNextText        : \"Next&gt;\";");
    out.println("_dpNextStatus      : \"Show the next month\";");
    out.println("_dpCurrentText     : \"Today\";");
    out.println("_dpCurrentStatus   : \"Show the current month\";");
    out.println("_dpMonthNames      : ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];");
    out.println("_dpMonthStatus     : \"Show a different month\";");
    out.println("_dpYearStatus      : \"Show a different year\";");
    out.println("_dpWeakHeader      : \"Wk\";");
    out.println("_dpWeakStatus      : \"Week of the year\";");
    out.println("_dpDayNames        : ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];");
    out.println("_dpDayNamesShort   : ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];");
    out.println("_dpDayNamesMin     : ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'];");
    out.println("_dpDayStatus       : \"Set DD as first week day\";");
    out.println("_dpDateStatus      : \"Select DD, M d\";");
    out.println("_dpInitStatus      : \"Select a date\";");
    
    out.println("this.initialize = function initialize() {");
    
    java.lang.Object gptMsgO = request.getAttribute("gptMsg");
    if (gptMsgO!=null && gptMsgO instanceof java.util.Map) {
      java.util.Map<String,String> gptMsg = (java.util.Map<String,String>)gptMsgO;
      
    out.println("this._dpClearText       = \"" + gptMsg.get("catalog.general.datepicker.clearText") + "\";");
    out.println("this._dpClearStatus     = \"" + gptMsg.get("catalog.general.datepicker.clearStatus") + "\";");
    out.println("this._dpCloseText       = \"" + gptMsg.get("catalog.general.datepicker.closeText") + "\";");
    out.println("this._dpCloseStatus     = \"" + gptMsg.get("catalog.general.datepicker.closeStatus") + "\";");
    out.println("this._dpPrevText        = \"" + gptMsg.get("catalog.general.datepicker.prevText") + "\";");
    out.println("this._dpPrevStatus      = \"" + gptMsg.get("catalog.general.datepicker.prevStatus") + "\";");
    out.println("this._dpNextText        = \"" + gptMsg.get("catalog.general.datepicker.nextText") + "\";");
    out.println("this._dpNextStatus      = \"" + gptMsg.get("catalog.general.datepicker.nextStatus") + "\";");
    out.println("this._dpCurrentText     = \"" + gptMsg.get("catalog.general.datepicker.currentText") + "\";");
    out.println("this._dpCurrentStatus   = \"" + gptMsg.get("catalog.general.datepicker.currentStatus") + "\";");
    out.println("this._dpMonthNames      = " + gptMsg.get("catalog.general.datepicker.monthNames") + ";");
    out.println("this._dpMonthStatus     = \"" + gptMsg.get("catalog.general.datepicker.monthStatus") + "\";");
    out.println("this._dpYearStatus      = \"" + gptMsg.get("catalog.general.datepicker.yearStatus") + "\";");
    out.println("this._dpWeakHeader      = \"" + gptMsg.get("catalog.general.datepicker.weakHeader") + "\";");
    out.println("this._dpWeakStatus      = \"" + gptMsg.get("catalog.general.datepicker.weakStatus") + "\";");
    out.println("this._dpDayNames        = " + gptMsg.get("catalog.general.datepicker.dayNames") + ";");
    out.println("this._dpDayNamesShort   = " + gptMsg.get("catalog.general.datepicker.dayNamesShort") + ";");
    out.println("this._dpDayNamesMin     = " + gptMsg.get("catalog.general.datepicker.dayNamesMin") + ";");
    out.println("this._dpDayStatus       = \"" + gptMsg.get("catalog.general.datepicker.dayStatus") + "\";");
    out.println("this._dpDateStatus      = \"" + gptMsg.get("catalog.general.datepicker.dateStatus") + "\";");
    out.println("this._dpInitStatus      = \"" + gptMsg.get("catalog.general.datepicker.initStatus") + "\";");
      
    }
    
    out.println("};");
    
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
    out.println("};");

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