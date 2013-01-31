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
package com.esri.gpt.catalog.lucene;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.Val;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.queryParser.ParseException;

/**
 * Parser adaptor info.
 * <p/>
 * Stores all the information neccesary to create instance of the parser adaptor.
 * Parser adaptor is a mechanizm to delegate query parsing to another mechanizm.
 * <p/>
 * Adaptor is never accessible directly. Instead, an instance of
 * {@link IParserProxy} is created and used to submit query through the adaptor
 * to the external mechanizm.
 * <p/>
 * Adaptor class has to have one mandatory method <code>String parse(String queryTerm)</code>,
 * and may have optional method <code>void init(Properties attributes)</code>.
 * <p/>
 * Typically, adaptors are defined in <i>Lucene</i> configuration node, which
 * may look like on the example below:<br/><br/>
 * <div style="border-style: solid; border-width: thin; border-color: #A4A4A4; background-color: #F5F6CE"><code>
 * <pre><code>
 *  &lt;lucene
 *     indexLocation="c:/gpt9/lucene-index/catalog1"
 *     analyzerClassName="org.apache.lucene.analysis.standard.StandardAnalyzer"&gt;
 *     &lt;adaptor name="ontology" className="com.esri.gpt.catalog.lucene.StandardParserAdaptor"&gt;
 *        &lt;attribute key="baseUrl" value="http://localhost:8080/OntologyService/query?term="/&gt;
 *     &lt;/adaptor&gt;
 *  &lt;/lucene&gt;
 * </pre></div>
 */
public class ParserAdaptorInfo {

  /** logger */
  private static Logger log = Logger.getLogger(ParserAdaptorInfo.class.getName());
  /** adaptor name */
  private String name = "";
  /** adaptor class name */
  private String className = "";
  /** attributes */
  private StringAttributeMap attributes = new StringAttributeMap();

  /**
   * Gets adaptor name.
   * @return adaptor name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets adaptor name.
   * @param name adaptor name
   */
  public void setName(String name) {
    this.name = Val.chkStr(name);
  }

  /**
   * Gets class name.
   * @return class name
   */
  public String getClassName() {
    return className;
  }

  /**
   * Sets class name.
   * @param className adaptor class name
   */
  public void setClassName(String className) {
    this.className = Val.chkStr(className);
  }

  /**
   * Gets attributes.
   * @return attributes
   */
  public StringAttributeMap getAttributes() {
    return attributes;
  }

  /**
   * Sets attributes.
   * @param attributes attributes
   */
  public void setAttributes(StringAttributeMap attributes) {
    this.attributes = attributes != null ? attributes : new StringAttributeMap();
  }

  @Override
  public String toString() {
    return "{name: \"" + getName() + "\", className=\"" + getClassName() + "\", attributes=" +getAttributes()+ "}";
  }

  /**
   * Creates instance of parser proxy corresponding to the definition.
   * @return parser proxy or <code>null</code> if unable to create parser proxy
   */
  public IParserProxy createParserProxy() {
    if (getClassName().length() > 0) {
      try {

        // create instance of the parser proxy adaptor
        final Object instance = Class.forName(getClassName()).newInstance();
        try {
          // if adaptor has "init" method - call it
          Method method =
            Class.forName(getClassName()).getMethod("init", Properties.class);
          Properties props = new Properties();
          for (StringAttribute sa : getAttributes().values()) {
            props.put(sa.getKey(), sa.getValue());
          }
          method.invoke(instance, props);
        } catch (NoSuchMethodException ex) {
          log.log(Level.WARNING, "Adaptor instance has not been initialized.", ex);
        }

        // adaptor implements IParserProxy, return it
        if (instance instanceof IParserProxy) {
          return (IParserProxy) instance;

        // if not, wrap it withing anonymous IParserProxy calling "parse" method
        // from the adaptor.
        } else {

          // get "parse" method
          final Method method =
            Class.forName(getClassName()).getMethod("parse", String.class);

          // create parser proxy
          return new IParserProxy() {

            public String parse(String term) throws ParseException {
              try {
                return method.invoke(instance, term).toString();
              } catch (IllegalAccessException ex) {
                Logger.getLogger(IParserProxy.class.getName()).
                  log(Level.SEVERE, null, ex);
                throw new ParseException(
                  "Unable to parse term: \"" + term + "\" due to internal error: "+ex.getMessage());
              } catch (IllegalArgumentException ex) {
                Logger.getLogger(IParserProxy.class.getName()).
                  log(Level.SEVERE, null, ex);
                throw new ParseException(
                  "Unable to parse term: \"" + term + "\" due to internal error: "+ex.getMessage());
              } catch (InvocationTargetException ex) {
                Logger.getLogger(IParserProxy.class.getName()).
                  log(Level.SEVERE, null, ex);
                throw new ParseException(
                  "Unable to parse term: \"" + term + "\" due to internal error: "+ex.getMessage());
              }
            }

            @Override
            public String toString() {
              return "{name: \"" + getName() + "\"; className=\"" + getClassName() + "\"}";
            }
          };
        }

      } catch (NoSuchMethodException ex) {
        log.log(Level.SEVERE, "Error initializing adaptor instance.", ex);
      } catch (InvocationTargetException ex) {
        log.log(Level.SEVERE, "Error initializing adaptor instance.", ex);
      } catch (IllegalArgumentException ex) {
        log.log(Level.SEVERE, "Error initializing adaptor instance.", ex);
      } catch (InstantiationException ex) {
        log.log(Level.SEVERE, "Error creating adaptor instance.", ex);
      } catch (IllegalAccessException ex) {
        log.log(Level.SEVERE, "Error creating adaptor instance.", ex);
      } catch (ClassNotFoundException ex) {
        log.log(Level.SEVERE, "Error creating adaptor instance.", ex);
      }
    }
    return null;
  }
}
