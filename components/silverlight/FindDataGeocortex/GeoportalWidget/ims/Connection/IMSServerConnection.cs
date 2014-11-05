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
