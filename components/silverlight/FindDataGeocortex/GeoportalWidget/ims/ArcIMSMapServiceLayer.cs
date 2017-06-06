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
using System.Xml.Linq;
using System.IO;

using ESRI.ArcGIS.Client.Geometry;
using ESRI.ArcGIS.Client;


namespace ESRI.ArcGIS.IMS
{
    


    /// <summary>ArcGIS IMS map service layer.</summary>
    /// <remarks>
    /// This layer represents an IMS (dynamic)  map
    /// service.
    /// </remarks>    
    public class ArcIMSMapServiceLayer : DynamicMapServiceLayer
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="ArcIMSMapServiceLayer"/> class.
        /// </summary>
        public ArcIMSMapServiceLayer()
            : base()
        {
            
        }

        /// <summary>
        /// to handle errors when image is not retrieved
        /// </summary>
        public event EventHandler GetImageFailed;



        #region Public Properties

        public string ImageUrl
        {
            get;
            set;
        }

        /// <summary>
        /// The service host for the IMS service
        /// example http://myimsserver
        /// </summary>
        public string ServiceHost
        {
            get;
            set;
        }

        /// <summary>
        /// The service name on that server to display in the map.
        /// </summary>
        public string ServiceName
        {
            get;
            set;
        }

        /// <summary>
        /// Used when the IMS server does not have a crossdomain.xml file
        /// full path of the proxy
        /// </summary>
        public string ProxyUrl
        {
            get;
            set;
        }

        public string Username
        {
            get;
            set;
        }

        public string Password
        {
            get;
            set;
        }

        public string ErrorMsg
        {
            get;
            set;
        }
        #endregion


        private string _backgroundTranscolor = "255,255,255";
        private string _backgroundColor = "255,255,255";
        private string _imageFormat = "png8";
        private bool initializing;
        private int _width;
        private int _height;
        OnUrlComplete _onComplete;
        int coorsys;
        double XMin;
		double YMin;
	    double XMax;
        double YMax;

        private string sDataToPost = "";
        /// <summary>
        /// Initializes the resource.
        /// Override this method if your resource requires asyncronous requests to initialize,
        /// and call the base method when initialization is completed.
        /// </summary>
        public override void Initialize()
        {            
            if (initializing || IsInitialized) return;
            //base.Initialize(); //We do this when service info is initialized
            initializing = true;

            
            // Get the ArcXml request
            string sAXL = AxlHelper.GetInitialAxl();

            // Builds the url
            string sUrl = AxlHelper.BuildURL(ServiceHost, ServiceName, ProxyUrl);

            MakeRequest(sUrl, sAXL, null);             
            
        }
                
        #region Private Methods             
        
        

        private void SetCredentials()
        {
            if (Username.Length > 0 && Password.Length > 0)
            {
              
            }
        }
        #endregion

        /// <summary>
        /// Gets the URL.
        /// </summary>
        /// <param name="extent">The extent.</param>
        /// <param name="width">The width.</param>
        /// <param name="height">The height.</param>
        /// <param name="onComplete">The on complete.</param>
        public override void GetUrl(ESRI.ArcGIS.Client.Geometry.Envelope extent, int width, int height, DynamicMapServiceLayer.OnUrlComplete onComplete)
        {
            string sAXL = AxlHelper.GetRequestAxl(extent.XMin,
               extent.YMin, extent.XMax, extent.YMax, _backgroundColor, _backgroundTranscolor, _imageFormat,
               width, height);
            //ARCIMS CAN RETURN ERROR WHEN HEIGHT AND WIDTH DON'T MATCH THE LIMITED SETTINGS - HAVE SEEN WHERE THIS DOESN'T OCCUR WITH FLEX AND DOES IN SILVERLIGHT - OUR MAP SEEMS TO 
            //BE LARGER - ACCORDING TO DOCUMENTATION, THIS CAN ONLY BE FIXED ON THE ARCIMS SERVER BY CHANGING THE SETTINGS
                      
            _width = width;
            _height = height;
          
            // Builds the url
            string sUrl = AxlHelper.BuildURL(ServiceHost, ServiceName, ProxyUrl);
            MakeRequest(sUrl, sAXL, onComplete);             
        }

        /// <summary>
        /// The private method to make the request to the IMS server.
        /// </summary>
        /// <param name="url"></param>
        /// <param name="data"></param>
        /// <param name="onComplete"></param>
        private void MakeRequest(string url, string data, OnUrlComplete onComplete)
        {
            _onComplete = onComplete;

            sDataToPost = data;
            // Create a request object
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(new Uri(url, UriKind.Absolute));
            request.Method = "POST";
            // don't miss out this
            //request.ContentType = "application/x-www-form-urlencoded";
            request.ContentType = "application/xml";
            
            request.BeginGetRequestStream(new AsyncCallback(RequestReady), request);            
        }

        /// <summary>
        /// Sumbit the Post Data
        /// </summary>
        /// <param name="asyncResult"></param>
        void RequestReady(IAsyncResult asyncResult)
        {
            HttpWebRequest request = asyncResult.AsyncState as HttpWebRequest;
            Stream stream = request.EndGetRequestStream(asyncResult);

            // Hack for solving multi-threading problem
            // I think this is a bug
            this.Dispatcher.BeginInvoke(delegate()
            {
                // Send the post variables
                StreamWriter writer = new StreamWriter(stream);                
                writer.Write(sDataToPost);
                writer.Flush();
                writer.Close();
                sDataToPost = "";

                request.BeginGetResponse(new AsyncCallback(ResponseReady), request);
            });
        }

        /// <summary>
        /// Get the Result
        /// </summary>
        /// <param name="asyncResult"></param>
        void ResponseReady(IAsyncResult asyncResult)
        {
            try
            {
                HttpWebRequest request = asyncResult.AsyncState as HttpWebRequest;
                HttpWebResponse response = (HttpWebResponse)request.EndGetResponse(asyncResult);

                this.Dispatcher.BeginInvoke(delegate()
                {
                    Stream responseStream = response.GetResponseStream();
                    StreamReader reader = new StreamReader(responseStream);
                    // get the result text
                    string result = reader.ReadToEnd();

                    // Send to the parser!
                    ParseAXL(result, _onComplete);

                });
            }
            catch (Exception ex)
            {
               // _onComplete("Error, _width, _height, newEnv);
            }
           }

        /// <summary>
        /// The parser for the layers and the image.
        /// </summary>
        /// <param name="sResult"></param>
        /// <param name="onComplete"></param>
        private void ParseAXL(string sResult, OnUrlComplete onComplete)
        {
            XDocument xDoc = XDocument.Parse(sResult);

            if (xDoc.Element("ARCXML").Element("RESPONSE").Element("SERVICEINFO") != null)
            {
                //ARCIMS SERVICE INFO HAS BEEN RETURNED
                XMin = Convert.ToDouble(xDoc.Element("ARCXML").Element("RESPONSE").Element("SERVICEINFO").Element("PROPERTIES").Element("ENVELOPE").Attribute("minx").Value);
                XMax = Convert.ToDouble(xDoc.Element("ARCXML").Element("RESPONSE").Element("SERVICEINFO").Element("PROPERTIES").Element("ENVELOPE").Attribute("maxx").Value);
                YMin = Convert.ToDouble(xDoc.Element("ARCXML").Element("RESPONSE").Element("SERVICEINFO").Element("PROPERTIES").Element("ENVELOPE").Attribute("miny").Value);
                YMax = Convert.ToDouble(xDoc.Element("ARCXML").Element("RESPONSE").Element("SERVICEINFO").Element("PROPERTIES").Element("ENVELOPE").Attribute("maxy").Value);

                coorsys = Int32.Parse(xDoc.Element("ARCXML").Element("RESPONSE").Element("SERVICEINFO").Element("PROPERTIES").Element("FEATURECOORDSYS").Attribute("id").Value);
                
                // Get the ArcXml request
                this.SpatialReference = new SpatialReference(coorsys);
                
                FullExtent = new ESRI.ArcGIS.Client.Geometry.Envelope()
                {
                    XMin = XMin,
                    YMin = YMin,
                    XMax = XMax,
                    YMax = YMax,
                    SpatialReference = new SpatialReference(coorsys)
                };
            
                base.Initialize();
            }

            else if (xDoc.Element("ARCXML").Element("RESPONSE").Element("IMAGE") != null)
            {
                //AN IMAGE HAS BEEN RETURNED
                string imageUrl = xDoc.Element("ARCXML").Element("RESPONSE").Element("IMAGE").Element("OUTPUT").Attribute("url").Value;

                this.ImageUrl = imageUrl;
               
                ESRI.ArcGIS.Client.Geometry.Envelope newEnv = new Envelope();
                newEnv.XMin = Convert.ToDouble(xDoc.Element("ARCXML").Element("RESPONSE").Element("IMAGE").Element("ENVELOPE").Attribute("minx").Value);
                newEnv.YMin = Convert.ToDouble(xDoc.Element("ARCXML").Element("RESPONSE").Element("IMAGE").Element("ENVELOPE").Attribute("miny").Value);
                newEnv.YMax = Convert.ToDouble(xDoc.Element("ARCXML").Element("RESPONSE").Element("IMAGE").Element("ENVELOPE").Attribute("maxy").Value);
                newEnv.XMax = Convert.ToDouble(xDoc.Element("ARCXML").Element("RESPONSE").Element("IMAGE").Element("ENVELOPE").Attribute("maxx").Value);
                SpatialReference spRef = new SpatialReference(coorsys);
                newEnv.SpatialReference = spRef;
                _onComplete(this.ImageUrl, _width, _height, newEnv);
                

            }
            else if (xDoc.Element("ARCXML").Element("RESPONSE").Element("ERROR") != null)
            {
                //AN ERROR HAS BEEN RETURNED
                this.Cancel();

                this.ErrorMsg = xDoc.Element("ARCXML").Element("RESPONSE").Element("ERROR").Value;
                
                this.GetImageFailed(this, new EventArgs());
      
            }

            else
            {
                
                System.Diagnostics.Debug.WriteLine(string.Format("IMS Returned an unknown response" + xDoc.ToString()));
            }
        }        
    }
}
