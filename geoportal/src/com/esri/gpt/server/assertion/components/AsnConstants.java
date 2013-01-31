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
package com.esri.gpt.server.assertion.components;

/**
 * Assertion constants.
 */
public class AsnConstants {
  
  /** class variables ========================================================= */
  
  /** Anonymous username = "anonymous" */
  public static final String ANONYMOUS_USERNAME = "anonymous";
  
  /** Application URN prefix = "urn:esri:geoportal" */
  public static final String APP_URN_PREFIX = "urn:esri:geoportal";
  
  /** Field: RDF subject = "rdf.subject" */
  public static final String FIELD_RDF_SUBJECT = "rdf.subject";
  
  /** Field: RDF predicate = "rdf.predicate" */
  public static final String FIELD_RDF_PREDICATE = "rdf.predicate";
  
  /** Field: RDF value = "rdf.value" */
  public static final String FIELD_RDF_VALUE = "rdf.value";  
  
  /** Field: system assertion id = "sys.assertionid" */
  public static final String FIELD_SYS_ASSERTIONID = "sys.assertionid";
  
  /** Field: system enabled status = "sys.enabled" */
  public static final String FIELD_SYS_ENABLED = "sys.enabled";
  
  /** Field: system resource id (associated metadata document id) = "sys.resourceid" */
  public static final String FIELD_SYS_RESOURCEID = "sys.resourceid";

  /** Field: system timestamp = "sys.timestamp" */
  public static final String FIELD_SYS_TIMESTAMP = "sys.timestamp";
  
  /** Field: system timestamp (value edit) = "sys.edit.timestamp" */
  public static final String FIELD_SYS_EDIT_TIMESTAMP = "sys.edit.timestamp";
  
  /** Field: user id (local geoportal user id) = "user.id" */
  public static final String FIELD_USER_ID = "user.id";
  
  /** Field: system IP address = "user.ipaddress" */
  public static final String FIELD_USER_IPADDRESS = "user.ipaddress";
  
  /** Field: user key (typically the distinguished name) = "user.key" */
  public static final String FIELD_USER_KEY = "user.key";
  
  /** Field: username = "user.name" */
  public static final String FIELD_USER_NAME = "user.name";
  
  /** Operation exception = "urn:esri:geoportal:operation:exception */
  public static final String OPERATION_EXCEPTION = "urn:esri:geoportal:operation:exception";
  
  /** Operation status = "urn:esri:geoportal:operation:status:failed" */
  public static final String OPERATION_STATUS_FAILED = "urn:esri:geoportal:operation:status:failed";

  /** Operation status = "urn:esri:geoportal:operation:status:ok" */
  public static final String OPERATION_STATUS_OK = "urn:esri:geoportal:operation:status:ok";

  /** Principal = "urn:esri:geoportal:principal:administrator" */
  public static final String PRINCIPAL_ADMINISTRATOR = "urn:esri:geoportal:principal:administrator";

  /** Principal (any user) = "urn:esri:geoportal:principal:any" */
  public static final String PRINCIPAL_ANY = "urn:esri:geoportal:principal:any";

  /** Principal = "urn:esri:geoportal:principal:moderator" */
  public static final String PRINCIPAL_MODERATOR = "urn:esri:geoportal:principal:moderator";
  
  /** Principal = "urn:esri:geoportal:principal:owner" */
  public static final String PRINCIPAL_OWNER = "urn:esri:geoportal:principal:owner";
  
  /** Principal = "urn:esri:geoportal:principal:resource:owner" */
  public static final String PRINCIPAL_RESOURCE_OWNER = "urn:esri:geoportal:principal:resource:owner";
  
  /** Subject prefix = "urn:esri:geoportal:resourceid" */
  public static final String SUBJECT_PREFIX_RESOURCEID = "urn:esri:geoportal:resourceid";
    
}
