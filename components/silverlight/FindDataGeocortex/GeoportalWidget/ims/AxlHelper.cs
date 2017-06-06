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
using System.Text;

namespace ESRI.ArcGIS.IMS
{
    public class AxlHelper
    {
        
        public static string GetQueryAxl(double minx, double miny, double maxx, double maxy, ESRI.ArcGIS.IMS.Tasks.Query query)
        {            
            string sGeometry = "";
            if (query.Geometry != null)
            {
                StringBuilder sbGeo = new StringBuilder();
                sbGeo.Append("<SPATIALFILTER relation=\"area_intersection\">");
                sbGeo.Append("<ENVELOPE minx=\""+query.Geometry.Extent.XMin+"\" miny=\""+query.Geometry.Extent.YMin+"\" maxx=\""+query.Geometry.Extent.XMax+"\" maxy=\""+query.Geometry.Extent.YMax+"\"/>");
                sbGeo.Append("</SPATIALFILTER>");
                sGeometry = sbGeo.ToString();
            }


            StringBuilder ArcXml = new StringBuilder();
            ArcXml.Append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            ArcXml.Append("<ARCXML>");
            ArcXml.Append("<REQUEST>");
            ArcXml.Append("<GET_FEATURES outputmode=\"xml\" geometry=\"true\" compact=\"false\">");
            ArcXml.Append("<LAYER id=\"" + query.LayerID + "\" />");
            if (query != null && query.Where != null)
            {
                ArcXml.Append("<SPATIALQUERY where=\"" + query.Where + "\" >");
            }
            else
                ArcXml.Append("<SPATIALQUERY>"); //subfields=\"#ALL#\"");
            ArcXml.Append(sGeometry);

            ArcXml.Append("</SPATIALQUERY>");
            ArcXml.Append("</GET_FEATURES>");
            ArcXml.Append("</REQUEST>");
            ArcXml.Append("</ARCXML>");

            return ArcXml.ToString();
        }

        public static string GetInitialAxl()
        {
            StringBuilder str = new StringBuilder();
            str.Append("<ARCXML version=\"1.1\">");
            str.Append("<REQUEST>");
            str.Append("<GET_SERVICE_INFO extensions=\"false\" fields=\"false\" renderer=\"false\"/>");
            str.Append("</REQUEST>");
            str.Append("</ARCXML>");

          
            return str.ToString();
            
        }
        

        public static string GetRequestAxl(double minx, 
                                            double miny, 
                                             double maxx, 
                                            double maxy,                                             
                                            string backgroudcolor,
                                            string backgroundTranscolor,
                                            string imageFormat,
                                            int width,
                                            int height)
        {
           
            StringBuilder ArcXml = new StringBuilder();
            ArcXml.Append("<ARCXML version=\"1.1\">");
            ArcXml.Append("<REQUEST>");
            ArcXml.Append("<GET_IMAGE>");
            ArcXml.Append("<PROPERTIES>");
            ArcXml.Append("<BACKGROUND color=\"" + backgroudcolor + "\" transcolor=\"" + backgroundTranscolor + "\"/>");            
            ArcXml.Append("<ENVELOPE minx=\"" + minx + "\" miny=\"" + miny + "\" maxx=\"" + maxx + "\" maxy=\"" + maxy + "\" />");
          
            ArcXml.Append("<FEATURECOORDSYS id=\"4326\"/>");
            ArcXml.Append("<FILTERCOORDSYS id=\"4326\" />");
          
            ArcXml.Append("<IMAGESIZE width=\""+width+"\" height=\""+height+"\" />");
            ArcXml.Append("<OUTPUT type=\"" + imageFormat + "\"/>");
            ArcXml.Append("</PROPERTIES>");
            ArcXml.Append("</GET_IMAGE>");
            ArcXml.Append("</REQUEST>");
            ArcXml.Append("</ARCXML>");
                                   
            return ArcXml.ToString();
        }

        public static string BuildURL(string ServiceHost, string ServiceName, string ProxyUrl)
        {
            return BuildURL(ServiceHost, ServiceName, ProxyUrl, false);
        }
                
        /// <summary>
        /// http://webhelp.esri.com/arcims/9.2/general/mergedProjects/ArcXMLGuide/elements/using_get_features.htm
        /// </summary>
        /// <param name="ServiceHost"></param>
        /// <param name="ServiceName"></param>
        /// <param name="ProxyUrl"></param>
        /// <param name="bForQuery"></param>
        /// <returns></returns>
        public static string BuildURL(string ServiceHost, string ServiceName, string ProxyUrl, bool bForQuery)
        {
            string sUrl = "";

            if (ServiceHost != null && ServiceName != null)
            {
                if (ServiceHost.IndexOf("com.esri.esrimap.Esrimap") == -1)
                {
                    sUrl = ServiceHost + "/servlet/com.esri.esrimap.Esrimap?ServiceName=" + ServiceName;
                }
                else
                {
                    sUrl = ServiceHost + "?ServiceName=" + ServiceName;
                }

                if (ProxyUrl != null)
                {
                    sUrl = ProxyUrl + "?" + sUrl;
                }
            }

            if (bForQuery == true)
            {
                sUrl += "&CustomService=Query";
            }

            return sUrl;
        }


    }
}
