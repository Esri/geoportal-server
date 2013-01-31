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
package com.esri.gpt.catalog.publication;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonResult;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.UrlUri;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.Result;
import com.esri.gpt.framework.xml.XmlIoUtil;
import java.io.File;
import java.io.IOException;
import javax.xml.transform.TransformerException;

/**
 * A processor compatible with a resource defined by a single XML.
 */
public class SingleXmlProcessor extends ResourceProcessor {
  
  /** instance variables ====================================================== */
  private File   file;
  private String resourceXml;
  private String systemId;

  /** constructors ============================================================ */
  
  /**
   * Constructs with an associated processing context and file.
   * @param context the processing context
   * @param file the file containing metadata for the resource to process
   */
  public SingleXmlProcessor(ProcessingContext context, File file) {
    super(context);
    this.file = file;
    this.getContext().setWasSingleSource(true);
    if (context.getTemplate() == null) {
      PublicationRecord template = new PublicationRecord();
      template.setUpdateOnlyIfXmlHasChanged(true);
      context.setTemplate(template);
    }
  }

  /**
   * Constructs with an associated processing context.
   * @param context the procesing context
   * @param systemId the system id of the resource (file path or URL)
   * @param resourceXml the resource XML to process
   */
  public SingleXmlProcessor(ProcessingContext context, String systemId, String resourceXml) {
    super(context);
    this.systemId = systemId;
    this.resourceXml = resourceXml;
    this.getContext().setWasSingleSource(true);
    if (context.getTemplate() == null) {
      PublicationRecord template = new PublicationRecord();
      template.setUpdateOnlyIfXmlHasChanged(true);
      context.setTemplate(template);
    }
  }
  
  /** methods ================================================================= */
  
  /**
   * Invokes processing against the resource.
   * @throws Exception if an exception occurs
   */
  @Override
  public void process() throws Exception {
    this.publishMetadata(this.systemId, readXml());
  }

  @Override
  public Query createQuery(IterationContext context, Criteria criteria) {
    Query query = new Query() {
      @Override
      public Result execute() {
        return new CommonResult(new NativeImpl());
      }
    };
    return query;
  }

  @Override
  public Native getNativeResource(IterationContext context) {
    return new NativeImpl();
  }


  /**
   * Reads XML.
   * @return XML
   * @throws Exception if reading XML fails
   * @throws TransformerException if processing response fails
   */
  private String readXml() throws IOException, TransformerException  {
    if (this.file != null) {
      this.systemId = this.file.getCanonicalPath();
      return XmlIoUtil.readXml(this.systemId);
    } else {
      return XmlIoUtil.transform(this.resourceXml);
    }
  }

  /**
   * Single XML specific implementation.
   */
  private class NativeImpl extends CommonPublishable implements Native {
    private UrlUri uri = new UrlUri(systemId);

    @Override
    public SourceUri getSourceUri() {
      return uri;
    }

    @Override
    public String getContent() throws IOException, TransformerException {
       return readXml();
    }

  };
}
