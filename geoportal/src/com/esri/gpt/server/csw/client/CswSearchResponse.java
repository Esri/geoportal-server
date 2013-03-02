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
package com.esri.gpt.server.csw.client;

/**
 * CswSearchResponse class.
 * 
 * CswSearchResponse class is used to store the response for CSW search
 * request.
 */
public class CswSearchResponse 
{

	private CswRecords _records = new CswRecords();
	private String _responseXML = "";
    private String _requestStr = "";

	/**
	 * Constructor
	 */
	public CswSearchResponse()
	{
	}

	/**
	 * set CSW Records
	 */
	public void setRecords(CswRecords records)
	{
		_records = records; 
	}
	
	/**
	 * CSW Records returned
	 */
	public CswRecords getRecords()
	{
		return _records;
	}

	
	/**
	 * set responseXML string. reponseXML string is raw response from a service
	 */
	public void setResponseXML(String responseXML)
	{
		_responseXML = responseXML;
	}
	
	
	/**
	 * get responseXML string.
	 */
	public String getResponseXML()
	{
		return _responseXML;
	}

    public String get_requestStr()
    {
        return _requestStr;
    }

    public void set_requestStr(String str)
    {
        _requestStr = str;
    }

}