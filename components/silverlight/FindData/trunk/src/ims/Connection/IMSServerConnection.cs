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
using System;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Documents;
using System.Windows.Ink;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Animation;
using System.Windows.Shapes;

namespace ESRI.ArcGIS.IMS.Connection
{
    /// <summary>
    /// Abstract class for common functionality of a connection to an ArcIMS server.
    /// </summary>
    /// <remarks>
    /// 	<para>
    ///         An IMS server connection is used to communicate with an ArcIMS server without
    ///         going through a
    ///         <a href="ms-help://ESRI.EDNv9.3/ArcGIS.ADF.Web.UI.WebControls/ESRI.ArcGIS.ADF.Web.UI.WebControls~ESRI.ArcGIS.ADF.Web.UI.WebControls.Map.html">
    ///         Map</a> control or
    ///         other ESRI Web ADF controls. Use one of the derived classes to actually create
    ///         a connection:
    ///     </para>
    /// 	<list type="bullet">
    /// 		<item>
    /// 			<see cref="ESRI.ArcGIS.ADF.Connection.IMS.TCPConnection">TCPConnection</see>
    ///             - for connecting over a local network using a TCP/IP connection.
    ///         </item>
    /// 		<item>
    /// 			<see cref="ESRI.ArcGIS.ADF.Connection.IMS.HTTPConnection">HTTPConnection</see>
    ///             - for connecting over the Internet using an HTTP connection.
    ///         </item>
    /// 	</list>
    /// </remarks>
    
    public abstract class IMSServerConnection : IDisposable
    {
        #region member variables

        private string serviceName;

        #endregion

        #region public properties

        /// <summary>
        /// Service name to which requests are sent.
        /// </summary>
        public string ServiceName
        {
            get { return serviceName; }
            set { serviceName = value; }
        }

        #endregion

        #region public methods

        /// <summary>
        /// Sends a request to the specified service.
        /// </summary>
        /// <param name="request">A valid ArcXML request.</param>
        /// <returns>ArcXML response from the ArcIMS Application Server.</returns>
        public abstract string Send(string request);

        /// <summary>
        /// Sends a request to the specified service using a custom service.
        /// </summary>
        /// <returns>ArcXML response from the ArcIMS Application Server.</returns>
        /// <param name="request">A valid ArcXML request.</param>
        /// <param name="customservice">
        /// The custom service (Query, Geocode, Extract) associated with the specified
        /// service name.
        /// </param>
        public abstract string Send(string request, string customservice);

        /// <summary>
        /// Convenience method to retrieve an ArrayList of services associated with an ArcIMS site.
        /// </summary>
        /// <returns><see cref="System.Collections.ArrayList"/> of service names.</returns>
        //public virtual ArrayList ClientServicesArray()
        //{
        //    XmlDocument doc = GetClientServicesXml();
        //    XmlHelper xh = new XmlHelper();
        //    xh.LoadXML(doc.OuterXml, LoadType.FromString);
        //    XmlNodeList nlist = xh.GetChildNodesFromRoot("SERVICE");
        //    ArrayList arr = new ArrayList(nlist.Count);
        //    foreach (XmlNode node in nlist)
        //    {
        //        string val = xh.GetAttributeValue(node, "name");  //for HTTP, lowercase name
        //        if (val == null)
        //            val = xh.GetAttributeValue(node, "NAME"); //for TCP, upper case name
        //        if (val != null)
        //            arr.Add(val);
        //    }
        //    return arr;

        //}


        /// <summary>
        /// Convenience method to retrieve an ArrayList of services associated with an ArcIMS site.
        /// </summary>
        /// <returns><see cref="System.Collections.ArrayList"/> of services.</returns>
        /// <param name="filterServerType">Specify the Services based on this ServerType[] value (e.g. ImageServer)</param>
        /// <param name="ignoreCase">Set true to ignore the case of filterType string, false otherwise (default: True)</param>
        //public virtual ArrayList ClientServicesArray(ServerType[] filterServerType, bool ignoreCase)
        //{
        //    XmlDocument doc = GetClientServicesXml();
        //    XmlHelper xh = new XmlHelper();
        //    xh.LoadXML(doc.OuterXml, LoadType.FromString);
        //    XmlNodeList nlist = xh.GetChildNodesFromRoot("SERVICE");
        //    ArrayList arr = new ArrayList(nlist.Count);
        //    string val = null;
        //    string tmpServer = null;
        //    string tmpVersion = null;
        //    bool isFilterType = true;
        //    bool isVersion = true;
        //    int cnt = filterServerType.Length;
        //    string type = "";
        //    string version = "";
        //    for (int i = 0; i < cnt; i++)
        //    {
        //        type = filterServerType[i].Type;
        //        version = filterServerType[i].Version;

        //        foreach (XmlNode node in nlist)
        //        {
        //            tmpServer = xh.GetAttributeValue(node, "type"); // for HTTP, lowercase
        //            if (tmpServer == null)
        //                tmpServer = xh.GetAttributeValue(node, "TYPE"); // for TCP upper case

        //            isFilterType = (ignoreCase ? (tmpServer.ToLower() == type.ToLower()) : (tmpServer == type));

        //            if (version != "")
        //            {
        //                tmpVersion = xh.GetAttributeValue(node, "version"); // for HTTP, lowercase
        //                if (tmpVersion == null)
        //                    tmpVersion = xh.GetAttributeValue(node, "version"); // for TCP upper case

        //                if (tmpVersion != null)
        //                    isVersion = (ignoreCase ? (tmpVersion.ToLower() == version.ToLower()) : (tmpVersion == version));

        //            }

        //            if (isFilterType && isVersion)
        //            {
        //                val = xh.GetAttributeValue(node, "name");  //for HTTP, lowercase name
        //                if (val == null)
        //                    val = xh.GetAttributeValue(node, "NAME"); //for TCP, upper case name
        //                if (val != null)
        //                {
        //                    if (!arr.Contains(val))
        //                        arr.Add(val);
        //                }
        //            }

        //            // reset defaults
        //            isVersion = true;
        //            isFilterType = true;
        //        }
        //    }
        //    return arr;

        //}

        #endregion

        #region protected methods

        /// <summary>
        /// Retrieves the catalog of services from the server.
        /// </summary>
        /// <returns>XML document with services.</returns>
        //protected virtual XmlDocument GetClientServicesXml()
        //{
        //    string xml = null;
        //    string oldService = ServiceName;
        //    ServiceName = "catalog";
        //    xml = Send(AxlRequests.GetClientServices());
        //    ServiceName = oldService;
        //    XmlDocument doc = new XmlDocument();
        //    doc.LoadXml(xml);
        //    return doc;
        //}

        #endregion

        #region IServerConnection Interface

        /// <summary>
        /// Check whether the server connection is alive.
        /// </summary>
        /// <returns></returns>
        public abstract bool IsAlive();

        #endregion

        #region IDisposable Interface
        /// <summary>
        /// Empty method.
        /// </summary>
        public virtual void Dispose()
        { }
        #endregion
    }

    /// <summary>Type used to identify ArcIMS Server Types.</summary>
    public class ServerType
    {

        private string _server = "";
        private string _version = "";

        /// <summary>Default constructor.</summary>
        public ServerType() { }

        /// <summary>Constructor taking the service type and version.</summary>
        /// <param name="type">The type of server: ImageServer, FeatureServer, or MetaDataServer.</param>
        /// <param name="version">ArcMap to specify ArcMap Server, empty for ArcXML-based image services.</param>
        public ServerType(string type, string version)
        {
            _server = type;
            _version = version;
        }

        /// <summary>
        ///     Type of ArcIMS server: 
        ///     <example>ImageServer, FeatureServer, MetaDataServer.</example>
        /// </summary>
        public string Type
        {
            get { return _server; }
            set { _server = value; }
        }
        /// <summary>Identify a specific version of the ServerType (i.e., ArcMap or empty).</summary>
        public string Version
        {
            get { return _version; }
            set { _version = value; }
        }
    }
}
