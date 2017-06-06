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
using System.Xml.Linq;
using System.Linq;
using System.Collections.Generic;

namespace ESRI.ArcGIS.IMS
{
    public static class GeographyConverter
    {
        public static ESRI.ArcGIS.Client.Geometry.Polygon FromIMSToPolygon(XElement item)
        {
            ESRI.ArcGIS.Client.Geometry.Polygon poly = new ESRI.ArcGIS.Client.Geometry.Polygon();

            var Rings = from ring in item.Element("POLYGON").Descendants("RING")
                        select ring;

            foreach (var ring in Rings)
            {
                List<ImsPoints> Points = (from point in ring.Descendants("POINT")
                             select new ImsPoints
                             {
                                 X = point.Attribute("x").Value,
                                 Y = point.Attribute("y").Value,
                             }).ToList();

                ESRI.ArcGIS.Client.Geometry.PointCollection pointcollection = GetPoints(Points);

                poly.Rings.Add(pointcollection);
            }


            return poly;
        }


        public static ESRI.ArcGIS.Client.Geometry.Polyline FromIMSToPolyline(XElement item)
        {
            ESRI.ArcGIS.Client.Geometry.Polyline poly = new ESRI.ArcGIS.Client.Geometry.Polyline();

            var Paths = from path in item.Element("POLYLINE").Descendants("PATH")
                        select path;

            foreach (var path in Paths)
            {
                List<ImsPoints> Points = (from point in path.Descendants("POINT")
                                          select new ImsPoints
                                          {
                                              X = point.Attribute("x").Value,
                                              Y = point.Attribute("y").Value,
                                          }).ToList();

                ESRI.ArcGIS.Client.Geometry.PointCollection pointcollection = GetPoints(Points);

                poly.Paths.Add(pointcollection);
            }

            return poly;
        }

        private static ESRI.ArcGIS.Client.Geometry.PointCollection GetPoints(List<ImsPoints> points)
        {
            ESRI.ArcGIS.Client.Geometry.PointCollection pointcollection = new ESRI.ArcGIS.Client.Geometry.PointCollection();
            foreach (var po in points)
            {
                ESRI.ArcGIS.Client.Geometry.MapPoint mapPoint = new
                    ESRI.ArcGIS.Client.Geometry.MapPoint(Convert.ToDouble(po.X, System.Globalization.CultureInfo.CurrentCulture),
                    Convert.ToDouble(po.Y, System.Globalization.CultureInfo.CurrentCulture));

                pointcollection.Add(mapPoint);
            }

            return pointcollection;
        }
    }

    public class ImsPoints
    {
        public string X
        {
            get;
            set;
        }

        public string Y
        {
            get;
            set;
        }
    }
}
