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
package com.esri.gpt.control.harvest;

import com.esri.gpt.catalog.harvest.history.HeRecord;
import com.esri.gpt.catalog.harvest.history.HeTransformReportRequest;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.jsf.PageContext;
import com.esri.gpt.framework.xml.XsltTemplate;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * Provides functionality to view report.
 * It parses the report and extracts two sections: <i>Summary</i>, and 
 * <i>Details</i>. Each section is capable to render appropriate HTML code.
 */
public class ReportViewer {

// class variables =============================================================
  /** Report summary transformation path (gpt/harvest/summary.xslt). */
  private static final String SUMMARY_PATH = "gpt/harvest/summary.xslt";
  /** Report details transformation path (gpt/harvest/details.xslt). */
  private static final String DETAILS_PATH = "gpt/harvest/details.xslt";
  /** Template used to perform summary transformation.
   * Created using {@link ReportViewer#SUMMARY_PATH}
   */
  private static XsltTemplate _summaryTemplate;
  /** Template used to perform details transformation.
   * Created using {@link ReportViewer#DETAILS_PATH}
   */
  private static XsltTemplate _detailsTemplate;
  /** Mapping between XSLT parameter name to resource string.
   * This is required to have localized harvest harvest reports. Each variable
   * used within the template has to be declared here.
   */
  private static final Map<String, String> _paramToResourceMap =
    new TreeMap<String, String>();

  static {
    _paramToResourceMap.put("parameter",
      "catalog.harvest.manage.report.xsltparam.parameter");
    _paramToResourceMap.put("value",
      "catalog.harvest.manage.report.xsltparam.value");
    _paramToResourceMap.put("sourceUri",
      "catalog.harvest.manage.report.xsltparam.sourceUri");
    _paramToResourceMap.put("validationStatus",
      "catalog.harvest.manage.report.xsltparam.validationStatus");
    _paramToResourceMap.put("publishStatus",
      "catalog.harvest.manage.report.xsltparam.publishStatus");
    _paramToResourceMap.put("validationError",
      "catalog.harvest.manage.report.xsltparam.validationError");
    _paramToResourceMap.put("publishError",
      "catalog.harvest.manage.report.xsltparam.publishError");
    _paramToResourceMap.put("statusOk",
      "catalog.harvest.manage.report.xsltparam.statusOk");
    _paramToResourceMap.put("statusFailed",
      "catalog.harvest.manage.report.xsltparam.statusFailed");
    _paramToResourceMap.put("recordsLimitation",
      "catalog.harvest.manage.report.xsltparam.recordsLimitation");
    _paramToResourceMap.put("errorsLimitation",
      "catalog.harvest.manage.report.xsltparam.errorsLimitation");
  }
// instance variables ==========================================================
  /** request context */
  private RequestContext context;
  /** message broker */
  private MessageBroker msgBroker;
  /** event record to view its report */
  private HeRecord record;
  /** summary */
  private Summary summary = new Summary();
  /** details */
  private Details details = new Details();
// constructors ================================================================

  /**
   * Creates instance of the viewer.
   * @param context request context
   * @param record event record
   */
  public ReportViewer(RequestContext context, HeRecord record) {
    this.context = context;
    this.record = record;
  }

  /**
   * Creates instance of the viewer.
   */
  public ReportViewer() {
    
  }
// properties ==================================================================

  /**
   * Gets request context
   * @return request context
   */
  public RequestContext getRequestContext() {
    return context;
  }

  /**
   * Sets request context
   * @param context request context
   */
  public void setRequestContext(RequestContext context) {
    this.context = context;
    this.summary.setRequestContext(context);
    this.details.setRequestContext(context);
  }

  /**
   * Gets message broker.
   * @return message broker.
   */
  public MessageBroker getMsgBroker() {
    if (msgBroker!=null) {
      return msgBroker;
    } else {
      MessageBroker mb = new MessageBroker();
      mb.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
      return mb;
    }
  }

  /**
   * Sets message broker.
   * @param msgBroker message broker
   */
  public void setMsgBroker(MessageBroker msgBroker) {
    this.msgBroker = msgBroker;
    this.summary.setMsgBroker(msgBroker);
  }

  /**
   * Gets record.
   * @return record
   */
  public HeRecord getRecord() {
    return record;
  }

  /**
   * Sets record.
   * @param record record
   */
  public void setRecord(HeRecord record) {
    this.record = record;
    this.summary.setRecord(record);
    this.details.setRecord(record);
  }

// methods =====================================================================
  /**
   * Gets all report sections.
   * @return array of sections
   */
  public ReportViewer.ISection[] getAllSections() {
    return new ReportViewer.ISection[]{getSummary(), getDetails()};
  }

  /**
   * Gets report summary.
   * @return report summary
   */
  public Summary getSummary() {
    return summary;
  }

  /**
   * Sets summary.
   * @param summary summary
   */
  public void setSummary(Summary summary) {
    this.summary = summary;
  }

  /**
   * Gets report details.
   * @return report details
   */
  public Details getDetails() {
    return details;
  }

  /**
   * Sets details.
   * @param details details
   */
  public void setDetails(Details details) {
    this.details = details;
  }
  
  /**
   * Gets summary template.
   * @return summary template
   * @throws IOException if reading configuration failed
   * @throws TransformerConfigurationException if processing configuration failed
   */
  private static XsltTemplate getSummaryTemplate()
    throws IOException, TransformerConfigurationException {
    if (_summaryTemplate == null) {
      _summaryTemplate = XsltTemplate.makeFromResourcePath(SUMMARY_PATH);
    }
    return _summaryTemplate;
  }

  /**
   * Gets details template.
   * @return details template
   * @throws IOException if reading configuration failed
   * @throws TransformerConfigurationException if processing configuration failed
   */
  private static XsltTemplate getDetailsTemplate()
    throws IOException, TransformerConfigurationException {
    if (_detailsTemplate == null) {
      _detailsTemplate = XsltTemplate.makeFromResourcePath(DETAILS_PATH);
    }
    return _detailsTemplate;
  }

  /**
   * Reports section.
   */
  public static interface ISection {

    /**
     * Gets inner HTML representing report section.
     * @return inner HTML representing report section
     */
    String getHtml();

    /**
     * Transform section and writes into writer
     * @param writer writer
     * @throws Exception if transforming fails
     */
    void transform(Writer writer) throws Exception;
  }

  /**
   * Report summary.
   */
  public static class Summary extends HtmlOutputText implements ISection {
    /** request context */
    private RequestContext context;
    /** message broker */
    private MessageBroker msgBroker;
    /** event record to view its report */
    private HeRecord record;

    /**
     * Sets request context.
     * @param context request context
     */
    public void setRequestContext(RequestContext context) {
      this.context = context;
    }

    /**
     * Sets record.
     * @param record record
     */
    public void setRecord(HeRecord record) {
      this.record = record;
    }

    /**
     * Gets message broker.
     * @return message broker.
     */
    public MessageBroker getMsgBroker() {
      if (msgBroker!=null) {
        return msgBroker;
      } else {
        MessageBroker mb = new MessageBroker();
        mb.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
        return mb;
      }
    }

    /**
     * Sets message broker.
     * @param msgBroker message broker
     */
    public void setMsgBroker(MessageBroker msgBroker) {
      this.msgBroker = msgBroker;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
      ResponseWriter writer = context.getResponseWriter();
      try {
        transform(writer);
      } catch (Exception ex) {
        throw new IOException("Error rendering summary: " + ex.getMessage());
      }
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
    }

    @Override
    public void transform(Writer writer) throws Exception {
      if (context!=null && record!=null) {
        HeTransformReportRequest request = new HeTransformReportRequest(context, record);
        try {
          request.execute(getSummaryTemplate(), writer, getParams());
        } catch (NullPointerException ex) {
          String msg = getMsgBroker().retrieveMessage("catalog.harvest.manage.history.message.readingError");
          writer.write(msg);
        } catch (TransformerException ex) {
          String msg = getMsgBroker().retrieveMessage("catalog.harvest.manage.history.message.readingError");
          writer.write(msg);
        }
      }
    }

    @Override
    public String getHtml() {
      final StringBuilder htmlBd = new StringBuilder();
      Writer writer = new Writer() {

        @Override
        public void close() throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
          for (int i = 0; i + off < cbuf.length && i < len; i++) {
            htmlBd.append(cbuf[i + off]);
          }
        }
      };
      try {
        transform(writer);
        return htmlBd.toString();
      } catch (Exception ex) {
        return "";
      }
    }
  }

  /**
   * Report details.
   */
  public static class Details extends HtmlOutputText implements ISection {
    /** request context */
    private RequestContext context;
    /** event record to view its report */
    private HeRecord record;

    /**
     * Sets request context.
     * @param context request context
     */
    public void setRequestContext(RequestContext context) {
      this.context = context;
    }

    /**
     * Sets record.
     * @param record record
     */
    public void setRecord(HeRecord record) {
      this.record = record;
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
      ResponseWriter writer = context.getResponseWriter();
      try {
        transform(writer);
      } catch (Exception ex) {
        throw new IOException("Error rendering summary: " + ex.getMessage());
      }
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
    }

    @Override
    public void transform(Writer writer) throws Exception {
      if (context!=null && record!=null) {
        HeTransformReportRequest request = new HeTransformReportRequest(context, record);
        try {
          request.execute(getDetailsTemplate(), writer, getParams());
        } catch (NullPointerException ex) {
        } catch (TransformerException ex) {
          
        }
      }
    }

    @Override
    public String getHtml() {
      final StringBuilder htmlBd = new StringBuilder();
      Writer writer = new Writer() {

        @Override
        public void close() throws IOException {
        }

        @Override
        public void flush() throws IOException {
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
          for (int i = 0; i + off < cbuf.length && i < len; i++) {
            htmlBd.append(cbuf[i + off]);
          }
        }
      };
      try {
        transform(writer);
        return htmlBd.toString();
      } catch (Exception ex) {
        return "";
      }
    }
  }

  /**
   * Gets XSLT parameters.
   * These parameters will be used when producing reports. They holds localized
   * strings to display on the screen.
   * @return map of parameters
   */
  private static Map<String, String> getParams() {
    Map<String, String> params = new TreeMap<String, String>();

    MessageBroker mb = PageContext.extractMessageBroker();
    for (Map.Entry<String, String> e : _paramToResourceMap.entrySet()) {
      String key = e.getKey();
      String value = mb.retrieveMessage(e.getValue());
      params.put(key, value);
    }

    return params;
  }
}
