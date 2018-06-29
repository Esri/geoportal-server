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
using System.Collections.Generic;
using System.Text;
using System.Xml;
using System.Xml.Xsl;
using System.Xml.XPath;
using ESRI.ArcGIS.Geometry;
using System.Globalization;

namespace com.esri.gpt.wmc
{
    /// <summary>
    /// Represents WMCDocument object
    /// </summary>
    class WMCDocument
    {
        private List<WMCLayer> layers = new List<WMCLayer>();
        private bool hasExtent = false;
        private IEnvelope extent = null;
        private string sSRS = null;
        private string sErr = "";

        /// <summary>
        /// List of WMC layers
        /// </summary>
        public List<WMCLayer> Layers
        {
            set
            {
                layers = value;
            }
            get
            {
                return layers;
            }
        }
        /// <summary>
        /// Envelope
        /// </summary>
        public IEnvelope Extent
        {
            set
            {
                extent = value;
            }
            get
            {
                return extent;
            }
        }
        /// <summary>
        /// Check for extent
        /// </summary>
        public bool HasExtent
        {
            set
            {
                hasExtent = value;
            }
            get
            {
                return hasExtent;
            }
        }
        /// <summary>
        /// stores SRS
        /// </summary>
        public string SRS
        {
            set
            {
                sSRS = value;
            }
            get
            {
                return sSRS;
            }
        }

        public string Err
        {
            set
            {
                sErr = value;
            }
            get
            {
                return sErr;
            }
        }


        /// <summary>
        /// Returns validity of document
        /// </summary>
        /// <returns></returns>
        public bool IsValidWMCDoc()
        {
            return sErr.Length == 0 ? true : false;
        }


        private string getAttribute(XmlAttributeCollection theAttributes, string strName)
        {
            XmlNode anAttribute = theAttributes.GetNamedItem(strName);
            if (anAttribute != null)
                return anAttribute.Value;
            else
                return "";
        }
        private string getAttributeFromNode(XmlNode pNode, string sAttrName)
        {
            XmlNode nd = pNode.Attributes.GetNamedItem(sAttrName);
            if (nd != null)
                return nd.Value;
            else
                return "";
        }

        static string ReadFromFile(string filename)
        {
            System.IO.StreamReader SR;
            StringBuilder S = new StringBuilder();
            SR = System.IO.File.OpenText(filename);
            String fileCnt = null;
            fileCnt = SR.ReadLine();  
            while (fileCnt != null)
            {
                S.Append(fileCnt);
                fileCnt = SR.ReadLine();                
            }
            SR.Close();
            return S.ToString();
        }
        /// <summary>
        /// Loads a wmc file with file name provided
        /// </summary>
        /// <param name="strFileName">the file name</param>
        public void LoadFromFile(string strFileName)
        {
            XmlAttributeCollection theAttributes = null;
            XmlNode theLayer = null;
            XmlNode theNameNode = null;
            XmlNode theTitleNode = null;
            XmlNode theServerNode = null;
            XmlNode theOnlineResourceNode = null;

            XmlDocument theWMC = new XmlDocument();
            try
            {
                string xml = ReadFromFile(strFileName);
                theWMC.LoadXml(xml);
            }
            catch (Exception e)
            {
                OpenWMC.logger.writeLog(e.StackTrace);
            }
            XmlNamespaceManager xmlnsManager = new XmlNamespaceManager(theWMC.NameTable);

            xmlnsManager.AddNamespace("context", "http://www.opengis.net/context");
            xmlnsManager.AddNamespace("sld", "http://www.opengis.net/sld");
            xmlnsManager.AddNamespace("xlink", "http://www.w3.org/1999/xlink");
            xmlnsManager.AddNamespace("xs", "http://www.w3.org/2001/XMLSchema");

            XmlNodeList theLayers = theWMC.SelectNodes("/context:ViewContext/context:LayerList/context:Layer",xmlnsManager);
            if (theLayers.Count == 0)
            {
                xmlnsManager = new XmlNamespaceManager(theWMC.NameTable);
                xmlnsManager.AddNamespace("sld", "http://www.opengis.net/sld");
                xmlnsManager.AddNamespace("xlink", "http://www.w3.org/1999/xlink");
                xmlnsManager.AddNamespace("xs", "http://www.w3.org/2001/XMLSchema");
                xmlnsManager.AddNamespace("context", "http://www.opengeospatial.net/context");

                theLayers = theWMC.SelectNodes("/context:ViewContext/context:LayerList/context:Layer",xmlnsManager);

                if (theLayers.Count == 0)
                {
                    sErr = StringResources.InvalidWMCDocument +
                                  StringResources.NoLayers;
                    return;
                }

            }

            for (int i = theLayers.Count -1; i >= 0 ; i--)
            {
                theLayer = theLayers.Item(i);
                WMCLayer myLayer = new WMCLayer();
                XmlNodeList children = theLayer.ChildNodes;
                foreach (XmlNode child in children)
                {
                    if (child.Name.ToLower() == "name")
                        theNameNode = child;
                    else if (child.Name.ToLower() == "title")
                        theTitleNode = child;
                    else if (child.Name.ToLower() == "server")
                    {
                        theServerNode = child;
                        XmlNodeList nds = theServerNode.ChildNodes;
                        foreach (XmlNode c in nds)
                        {
                            if (c.Name.ToLower() == "onlineresource")
                                theOnlineResourceNode = c;
                        }
                        if (theTitleNode == null)
                        {
                            myLayer.Title = getAttribute(theServerNode.Attributes, "title");
                        }
                    }
                }
                

                //1 - get Server info
                theAttributes = theServerNode.Attributes;
                myLayer.Server.Service = getAttribute(theAttributes, "service");
                myLayer.Server.Version = getAttribute(theAttributes, "version");
                myLayer.Server.Title = getAttribute(theAttributes, "title");

                //2 - get online info
                myLayer.Server.OnlineResource.Href = getAttributeFromNode(theOnlineResourceNode, "xlink:href");
                myLayer.Server.OnlineResource.ResourceType = getAttributeFromNode(theOnlineResourceNode, "xlink:type");
                myLayer.Server.OnlineResource.Role = getAttributeFromNode(theOnlineResourceNode, "xlink:role");
                myLayer.Server.OnlineResource.ArcRole = getAttributeFromNode(theOnlineResourceNode, "xlink:arcrole");
                myLayer.Server.OnlineResource.Title = getAttributeFromNode(theOnlineResourceNode, "xlink:title");
                myLayer.Server.OnlineResource.Show = getAttributeFromNode(theOnlineResourceNode, "xlink:show");
                myLayer.Server.OnlineResource.Actuate = getAttributeFromNode(theOnlineResourceNode, "xlink:actuate");

                //3 - get layer name, title and info     
                if (myLayer.Server.Service == "OGC:WMS")
                {
                    myLayer.Name = theNameNode.InnerText;
                    myLayer.SecretName = theNameNode.InnerText;
                }
                else if (myLayer.Server.Service == "ESRI:ARCIMS")
                {
                    if (theNameNode.InnerText.Contains(":"))
                    {
                        myLayer.Name = theNameNode.InnerText.Substring(0, theNameNode.InnerText.IndexOf(":") - 1);
                        myLayer.SecretName = theNameNode.InnerText.Substring(theNameNode.InnerText.IndexOf(":") + 1);
                    }
                    else
                    {
                        myLayer.Name = theNameNode.InnerText;
                        myLayer.SecretName = theNameNode.InnerText;
                    }
                }
                else if (myLayer.Server.Service == "ESRI:ARCIMS:HTTP")
                {
                    myLayer.Name = myLayer.Server.Title;
                    myLayer.SecretName = theTitleNode.InnerText;
                }
                else if (myLayer.Server.Service == "ESRI:AGS:MAP:SOAP")
                {
                    if (theNameNode.InnerText.Contains(":"))
                    {
                        myLayer.Name = theNameNode.InnerText.Substring(0, theNameNode.InnerText.IndexOf(":") - 1);
                        myLayer.SecretName = theNameNode.InnerText.Substring(theNameNode.InnerText.IndexOf(":") + 1);
                    }
                    else
                    {
                        myLayer.Name = theNameNode.InnerText;
                        myLayer.SecretName = theNameNode.InnerText;
                    }
                }
                else
                {
                    myLayer.Name = theNameNode.InnerText;
                }

                if (theTitleNode != null)
                {
                    myLayer.Title = theTitleNode.InnerText;
                }
                theAttributes = theLayer.Attributes;
                myLayer.IsHidden =theAttributes.GetNamedItem("hidden").Value;
                myLayer.IsQueryable = theAttributes.GetNamedItem("queryable").Value;

                layers.Add(myLayer);

            }

            if (theWMC.SelectNodes("/context:ViewContext/context:General/context:BoundingBox",xmlnsManager).Count == 0)
            {
                HasExtent = false;
            }
            HasExtent = true;

            XmlNode theGeneral = theWMC.SelectNodes("/context:ViewContext/context:General/context:BoundingBox",xmlnsManager).Item(0);
            theAttributes = theGeneral.Attributes;

            Double xmax, xmin, ymax, ymin;

            CultureInfo culture = new CultureInfo("us");
            xmax = Double.Parse(getAttribute(theAttributes, "maxx"), culture);
            xmin = Double.Parse(getAttribute(theAttributes, "minx"), culture);
            ymax = Double.Parse(getAttribute(theAttributes, "maxy"), culture);
            ymin = Double.Parse(getAttribute(theAttributes, "miny"), culture);

            extent = new EnvelopeClass();
            extent.XMax = xmax;
            extent.XMin = xmin;
            extent.YMax = ymax;
            extent.YMin = ymin;

            sSRS = getAttribute(theAttributes, "SRS");
        }
    }

}

