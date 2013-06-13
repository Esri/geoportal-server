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
/**
 * Indexable elements associated with the time period of the content.
 * 
 * <br/>
 * <br/><b>Query Expression Examples</b><br/>
 * <br/>timeperiod:[2008Z TO 2012Z] - from the first millisecond of 2008 to the last millisecond of 2012
 * <br/>timeperiod:[2008-06 TO 2012-07]
 * <br/>timeperiod:[2008 TO *]
 * <br/>timeperiod:[* TO 2012]
 * <br/>timeperiod:[2008 TO 2012-07-01T01:00:00-08:00]
 * <br/>timeperiod:{2000 TO 2012}
 * <br/>
 * <br/>Datetime strings are ISO 8601. If a time zone is not supplied, the JVM default
 * for the web server is used (this applies to documents that are indexed as well).  
 * <br/>
 * <br/>Operations: [] - intersects , {} - within
 * <br/>A document intersects when any of its intervals intersect the query interval.
 * <br/>A document is within when the document's minimum lower boundary and maximum 
 * upper boundary are within the query interval. 
 * <br/>
 * <br/>Two additional property names can be used to explicitly choose an operation:
 * <br/>timeperiod.intersects 
 * <br/>timeperiod.within
 * <br/>
 * <br/>CSW example:
 * <br/>&lt;ogc:PropertyIsBetween&gt;
 * <br/>&nbsp;&nbsp;&lt;ogc:PropertyName&gt;timeperiod.within&lt;/ogc:PropertyName&gt;
 * <br/>&nbsp;&nbsp;&lt;ogc:LowerBoundary&gt;2000&lt;/ogc:LowerBoundary&gt;
 * <br/>&nbsp;&nbsp;&lt;ogc:UpperBoundary&gt;2012&lt;/ogc:UpperBoundary&gt;
 * <br/>&lt;/ogc:PropertyIsBetween&gt;
 * <br/>
 * <br/><b>Indexed Fields</b><br/>
 * <br/>A document can have many time intervals. Each time interval is stored within 
 * a pair of boundary fields. A boundary (Lucene NumericField - Long) represents 
 * milliseconds since the epoch (January 1, 1970, 00:00:00 UTC). 
 * Upper boundaries represent the final millisecond of the interval. For the time 
 * interval 2011, the first millisecond of 2011 is stored as the lower boundary, 
 * the final millisecond of 2011 is stored as the upper boundary.<br/>
 * <br/>timeperiod.l.0    timeperiod.u.0 - lower/upper boundaries for the document
 * <br/>timeperiod.l.1    timeperiod.u.1 - lower/upper boundaries for interval [n]
 * <br/>timeperiod.l.2    timeperiod.u.2
 * <br/>timeperiod.l.3    timeperiod.u.3
 * <br/>timeperiod.l.[n]  timeperiod.u.[n] 
 * <br/>
 * <br/>Several additional fields are also indexed:<br/>
 * <br/>timeperiod.imeta - metadata per time interval
 * <br/>timeperiod.meta - summary metadata
 * <br/>timeperiod.num - the number of intervals for the document (NumericField)
 * <br/>
 * <br/>timeperiod.meta can contain the following values:
 * <br/>isDeterminate is1Determinate isIndeterminate is1Indeterminate isUnknown 
 * <br/>hasDeterminate hasUnknown hasNow hasLowerNow hasUpperNow wasInvalid wasEmpty
 * <br/>
 * <br/>Some additional queries:
 * <br/>timeperiod.num:[2 TO *] - all documents that have 2 or more intervals
 * <br/>timeperiod.meta:isDeterminate - all documents that have fully determinate intervals
 * <br/>
 * <br/><b>Document Analysis During Indexing</b><br/>
 * <br/>During the process of indexing a document, values associated with the 
 * property <em>timeperiod.analyze</em> will be analyzed to determine the time
 * period intervals for the document. The values for analysis are expected to 
 * follow a specific format.
 * <br/>
 * <br/>For FGDC documents:
 * <br/>tp.position.<b><em>date</em></b>.fgdctime.<b><em>time</em></b>
 * <br/>tp.begin.<b><em>date</em></b>.fgdctime.<b><em>time</em></b>.end.<b><em>date</em></b>.fgdctime.<b><em>time</em></b>
 * <br/>
 * <br/>Example (see fgdc-indexables.xml):
 * <br/>&lt;property xpath=&quot;/metadata/idinfo/timeperd/timeinfo/mdattim/sngdate&quot;&gt;
 * <br/>&nbsp;&nbsp;&lt;property meaning=&quot;timeperiod.analyze&quot; xpathType=&quot;STRING&quot;
 * <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;xpath=&quot;concat('tp.position.',caldate,'.fgdctime.',time)&quot;/&gt;
 * <br/>&lt;/property&gt;
 * <br/>
 * <br/>For ISO documents:
 * <br/>tp.position.<b><em>datetime</em></b>.indeterminate.<b><em>value</em></b>
 * <br/>tp.begin.<b><em>datetime</em></b>.indeterminate.<b><em>value</em></b>.end.<b><em>datetime</em></b>.indeterminate.<b><em>value</em></b>
 * <br/>
 * <br/>Example (see apiso-indexables.xml):
 * <br/>&lt;property xpath=&quot;//gml:TimePeriod&quot;&gt;
 * <br/>&nbsp;&nbsp;&lt;property meaning=&quot;timeperiod.analyze&quot; xpathType=&quot;STRING&quot;
 * <br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;xpath=&quot;concat('tp.begin.',gml:beginPosition,'.indeterminate.',gml:beginPosition/@indeterminatePosition,'.end.',gml:endPosition,'.indeterminate.',gml:endPosition/@indeterminatePosition)&quot;/&gt;
 * <br/>&lt;/property&gt;
 * <br/>
 * <br/><b>Indeterminates</b><br/>
 * <br/>Recognized indeterminates: <em>unknown now present after before</em>
 * <br/>
 * <br/><em>now</em> and <em>present</em> are equivalent<br/>
 * <br/>Documents containing an <em>unknown</em> indeterminate will not be matched
 * by the <em>within</em> operation.
 * <br/>
 * <br/>Documents containing multiple intervals plus a <em>now</em> or <em>present</em> 
 * indeterminate will only be matched by the <em>within</em> operation if the intervals 
 * are sequential and the indeterminate has been declared for the end of the highest range.
 * <br/>e.g. 2008-08-01..2009-08-31 , 2009-09-01..2010-04-15 , 2010-04-16..now
 * <br/>
 * <br/>Documents containing an <em>after</em> or <em>before</em> indeterminate
 * will not have their time periods indexed by default.
 * <br/>
 * <br/><b>Configuration Parameters</b><br/>
 * <br/>The following parameters can be configured within gpt.xml
 * <br/><em>allowAfterAndBefore</em> - if true then accept dates 
 * associated with these indeterminates (default=false)
 * <br/><em>allowOpenEndedRange</em> - if true then treat an open
 * ended range (empty or 'unknown') as a single date (default=true)
 * <br/><em>timeperiod.maxIntervalsPerDocument</em> - the maximum 
 * number of intervals to index per document, documents that exceed 
 * the maximum will not have their time periods indexed (default=50)
 * <br/> 
 * <br/>&lt;parameter key=&quot;timeperiod.allowAfterAndBefore&quot; value=&quot;false&quot;/&gt;  
 * <br/>&lt;parameter key=&quot;timeperiod.allowOpenEndedRange&quot; value=&quot;true&quot;/&gt; 
 * <br/>&lt;parameter key=&quot;timeperiod.maxIntervalsPerDocument&quot; value=&quot;50&quot;/&gt; 
 */
package com.esri.gpt.catalog.schema.indexable.tp;
