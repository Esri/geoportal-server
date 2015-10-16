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
package com.esri.gpt.control.webharvest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import org.apache.commons.io.IOUtils;

/**
 * Default iteration context.
 */
public class DefaultIterationContext implements IterationContext {
  protected static SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  protected final LinkedList<ExceptionInfo> exceptionInfos = new LinkedList<ExceptionInfo>();

  @Override
  public void onIterationException(Exception ex) {
    registerException(ex);
  }

  @Override
  public String toString() {
    ByteArrayOutputStream output = null;
    PrintWriter writer = null;
    try {
      output = new ByteArrayOutputStream();
      
      writer = new PrintWriter(output);
      
      for (ExceptionInfo ei: exceptionInfos) {
        writer.println(ei.toString());
      }
      
      writer.close();
      
      return output.toString("UTF-8");
    } catch (IOException ex) {
      return "";
    } finally {
      IOUtils.closeQuietly(writer);
      IOUtils.closeQuietly(output);
    }
  }
  
  /**
   * Gets exception infos.
   * @return linked list of all registered exception infos.
   */
  public LinkedList<ExceptionInfo> getExceptionInfos() {
    return exceptionInfos;
  }
  
  /**
   * Registers an exception.
   * @param ex exception
   */
  public void registerException(Exception ex) {
    exceptionInfos.addLast(new ExceptionInfo(ex));
  }
  
  /**
   * Exception information.
   */
  public static final class ExceptionInfo {
    private final Date timestamp = new Date();
    private final Exception exception;

    /**
     * Creates instance of the exception information.
     * @param exception exception
     */
    public ExceptionInfo(Exception exception) {
      this.exception = exception;
    }

    /**
     * Gets timestamp.
     * @return timestamp
     */
    public Date getTimestamp() {
      return timestamp;
    }

    /**
     * Gets exception.
     * @return exception
     */
    public Exception getException() {
      return exception;
    }
    
    @Override
    public String toString() {
      return String.format("%s %s", DF.format(timestamp), exception.getMessage());
    }
  }
}
