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
package com.esri.gpt.server.csw.provider.components;

/**
 * Defines some commonly used CSW constants.
 */
public class CswConstants {
  
  /** class variables ========================================================= */
  
  /** ="brief", return the brief record response */
  public static String ElementSetType_Brief = "brief";
  
  /** ="summary", return the summary record response */
  public static String ElementSetType_Summary = "summary";
  
  /** ="full", return the full record response */
  public static String ElementSetType_Full = "full";
  
  /** ="DescribeRecord" operation name*/
  public static String Operation_DescribeRecord = "DescribeRecord";
  
  /** ="GetCapabilities" operation name*/
  public static String Operation_GetCapabilities = "GetCapabilities";
  
  /** ="GetDomain" operation name*/
  public static String Operation_GetDomain = "GetDomain";
  
  /** ="GetRecordById" operation name*/
  public static String Operation_GetRecordById = "GetRecordById";
  
  /** ="GetRecords" operation name*/
  public static String Operation_GetRecords = "GetRecords";
  
  /** ="Transaction" operation name*/
  public static String Operation_Transaction = "Transaction";
  
  /** ="application/xml" HTTP MIME type */
  public static String OutputFormat_ApplicationXml = "application/xml";
  
  /** ="text/xml" HTTP MIME type */
  public static String OutputFormat_TextXml = "text/xml";
  
  /** ="http://www.opengis.net/cat/csw/2.0.2", the CSW core record schema */
  public static String OutputSchema_CswRecord = "http://www.opengis.net/cat/csw/2.0.2";
  
  /** ="original", the original XML schema for the document */
  public static String OutputSchema_Original = "original";
  
  
  /** ="constraintCql", applies to: GetRecords */
  public static String Parameter_ConstraintCql = "constraintCql";
  
  /** ="constraintVersion", applies to: GetRecords */
  public static String Parameter_ConstraintVersion = "constraintVersion";
  
  /** ="elementName", applies to: GetRecords */
  public static String Parameter_ElementName = "elementName";
  
  /** ="elementSetType", applies to: GetRecordById, GetRecords e.g. brief,summary,full */
  public static String Parameter_ElementSetType = "elementSetType";
  
  /** ="elementSetTypeNames", applies to: GetRecordById, GetRecords */
  public static String Parameter_ElementSetTypeNames = "elementSetTypeNames";
  
  /** ="id", applies to: GetRecordById */
  public static String Parameter_Id = "id";
  
  /** ="operationName", 
   * e.g. GetCapabilities, DescribeRecord, GetRecordById, GetRecords, GetDomain, Transaction 
   */
  public static String Parameter_OperationName = "operationName";
  
  /** ="outputFormat", applies to: all operations */
  public static String Parameter_OutputFormat = "outputFormat";
  
  /** ="outputSchema", applies to: GetRecordById, GetRecords */
  public static String Parameter_OutputSchema = "outputSchema";
  
  /** ="queryTypeNames", applies to: GetRecords */
  public static String Parameter_QueryTypeNames = "queryTypeNames";
  
  /** ="resultType", applies to: GetRecordById, GetRecords e.g. hits,results,validate */
  public static String Parameter_ResultType = "resultType";
  
  /** ="schemaLanguage", applies to: DescribeRecord */
  public static String Parameter_SchemaLanguage = "schemaLanguage";
  
  /** ="sections", applies to: GetCapabilities */
  public static String Parameter_Sections = "sections";
  
  /** ="service", applies to: all operations */
  public static String Parameter_Service = "service";
  
  /** ="typeName", applies to: DescribeRecord */
  public static String Parameter_TypeName = "typeName";
  
  /** ="version", applies to: all operations */
  public static String Parameter_Version = "version";
  
  
  
  
  
  
  
  
  
    
  /** ="hits", return a hit count only */
  public static String ResultType_Hits = "hits";
  
  /** "=results", return the result */
  public static String ResultType_Results = "results";
  
  /** ="validate", validate and return an acknowledgement */
  public static String ResultType_Validate = "validate";
  
  /** ="XMLSCHEMA" */
  public static String SchemaLanguage_XMLSCHEMA = "XMLSCHEMA";
  
  /** ="http://www.w3.org/XML/Schema" */
  public static String SchemaLanguage_XMLW3C = "http://www.w3.org/XML/Schema";
  // http://www.w3.org/2001/XMLSchema
  
  /** ="Delete" transaction type */
  public static String TransactionType_Delete = "Delete";
  
  /** ="Insert" transaction type */
  public static String TransactionType_Insert = "Insert";
  
  /** ="Update" transaction type */
  public static String TransactionType_Update = "Update";
  
  /** ="csw:Record" */
  public static String TypeName_CswRecord = "csw:Record";

}
