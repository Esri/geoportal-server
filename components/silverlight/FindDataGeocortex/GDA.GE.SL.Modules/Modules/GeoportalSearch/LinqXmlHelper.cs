/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
using System;
using System.Collections.Generic;
using System.Xml.Linq;
using System.Linq;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    public class LinqXmlHelper
    {
        public static string GetWmsVersion(XElement xEle)
        {
            string versionVal = string.Empty;
            var v = from w in xEle.Attributes().Where(d => d.Name.LocalName == "version") select w;
            if (v.Count() > 0)
            {
                XAttribute att = v.First();
                versionVal = att.Value;
            }
            return versionVal;
        }

        public static XElement GetWmsSingleElement(XElement xEle, string name)
        {

            IEnumerable<XElement> a = from b in xEle.Elements() where b.Name.LocalName == name select b;
            if (a.Count() > 0)
            {
                return a.First();
            }
            else return null;
        }

        public static string GetSpatialReferenceTagName(string versionIn)
        {
            string srsName = string.Empty;
            int verNum = Convert.ToInt32(versionIn.Replace(".", ""));
            srsName = "SRS";
            if (verNum >= 130)
                srsName = "CRS";
            return srsName;
        }

        public static List<int> GetSupportedWKIDs(XElement xEle, string srsTag)
        {
            List<int> supportedSRs = new List<int>();
            IEnumerable<string> srList = from s in xEle.Elements().Where(r => r.Name.LocalName == srsTag) select s.Value;
            if (srList.Count() > 0)
            {
                foreach (string str in srList)
                {
                    if (str.Contains("EPSG:"))
                    {
                        supportedSRs.Add(Convert.ToInt32(str.Replace("EPSG:", "")));
                    }
                }

            }
            return supportedSRs;
        }

        public static List<ILayerInfo> GetLayerListInfo(XElement xEle)
        {
            if (xEle == null) return null;

            List<ILayerInfo> lstLayer = GetCapabilitiesLayerList(xEle).ToList();

            return lstLayer;
        }

        /// <summary>
        /// Retrieves a list of layers from the GetCapabilities response of an WMS service
        /// </summary>
        /// <param name="xEle"></param>
        /// <returns></returns>
        public static IEnumerable<ILayerInfo> GetCapabilitiesLayerList(XElement xEle)
        {
            XNamespace aw = xEle.GetDefaultNamespace();
            IEnumerable<XElement> de = from el in xEle.Descendants(aw + "Layer") select el;

            foreach (XElement el in de)
            {
                IEnumerable<XElement> deSub = from elSub in el.Descendants(aw + "Layer") select elSub;

                LayerInfo lyrInfo = new LayerInfo();

                if (el.Elements(aw + "Name").Count() > 0)
                    lyrInfo.Name = el.Elements(aw + "Name").First().Value;
                if (el.Elements(aw + "Title").Count() > 0)
                    lyrInfo.Title = el.Elements(aw + "Title").First().Value;
                if (el.Elements(aw + "MaxScaleDenominator").Count() > 0)
                {
                    string sMax = el.Elements(aw + "MaxScaleDenominator").First().Value;

                    try
                    {
                        lyrInfo.MaxScale = Convert.ToDouble(sMax);
                    }
                    catch
                    {
                        lyrInfo.MaxScale = 0.0;
                    }
                }

                yield return lyrInfo;
            }
        }

        public static IEnumerable<XAttribute> GetElementAttributes(XElement xEle, string name)
        {
            if ((xEle == null) || String.IsNullOrEmpty(name)) return null;

            XElement xEle1 = LinqXmlHelper.GetWmsSingleElement(xEle, name);
            if (xEle1 == null) return null;

            IEnumerable<XAttribute> attList =
                from at in xEle1.Attributes() select at;

            return attList;
        }

        public static string[] GetLayerList(XElement xEle)
        {
            List<string> lst = new List<string>();
            XNamespace aw = xEle.GetDefaultNamespace();
            IEnumerable<XElement> de = from el in xEle.Descendants(aw + "Layer") select el;


            foreach (XElement el in de)
            {
                if (el.Elements(aw + "Name").Count() > 0)
                {
                    lst.Add(el.Elements(aw + "Name").First().Value);
                    string sMax = el.Elements(aw + "MaxScaleDenominator").First().Value;
                }
            }

            if (lst.Count() > 0)
            {
                string[] lyrList = new string[lst.Count];


                for (int i = 0; i < lst.Count; i++)
                {
                    lyrList[i] = lst[i];
                }
                return lyrList;
            }
            return null;
        }
    }
}
