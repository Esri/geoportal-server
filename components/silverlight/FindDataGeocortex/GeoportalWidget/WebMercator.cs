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
using ESRI.ArcGIS.Client.Geometry;

namespace GeoportalWidget
{
    public class WebMercator
    {
        private static double DEGREES_PER_RADIANS = 57.295779513082320;
        private static double RADIANS_PER_DEGREES = 0.017453292519943;
        private static double PI_OVER_2 = Math.PI / 2.0;

        private static double RADIUS = 6378137; // Using Equatorial radius: http://en.wikipedia.org/wiki/Earth_radius

        public static int WebMercatorWKID = 102100;
        /// <summary>
        /// From WGS84 to WebMercator
        /// </summary>
        /// <param name="latitude"></param>
        /// <returns></returns>
        public static double ToWebMercatorY(double latitude)
        {
            double rad = latitude * RADIANS_PER_DEGREES;
            double sin = Math.Sin(rad);
            double y = RADIUS / 2.0 * Math.Log((1.0 + sin) / (1.0 - sin));
            return y;
        }

        /// <summary>
        ///  From WGS84 to WebMercator
        /// </summary>
        /// <param name="longitude"></param>
        /// <returns></returns>
        public static double ToWebMercatorX(double longitude)
        {
            double x = longitude * RADIANS_PER_DEGREES * RADIUS;
            return x;
        }

        /// <summary>
        /// From WGS84 to WebMercator
        /// </summary>
        /// <param name="originalEnvelope"></param>
        /// <returns></returns>
        public static Envelope ToWebMercatorEnvelope(Envelope originalEnvelope)
        {
            Envelope webMercatorEnv = new Envelope()
            {
                XMax = ToWebMercatorX(originalEnvelope.XMax),
                XMin = ToWebMercatorX(originalEnvelope.XMin),
                YMax = ToWebMercatorY(originalEnvelope.YMax),
                YMin = ToWebMercatorY(originalEnvelope.YMin),
                SpatialReference = new SpatialReference(102100)
            };

            return webMercatorEnv;
        }

        /// <summary>
        /// From WebMercacor 102100 to WGS84
        /// </summary>
        /// <param name="x"></param>
        /// <returns></returns>
        public static double FromWebMercatorX(double x)
        {
            double rad = x / RADIUS;
            double deg = rad * DEGREES_PER_RADIANS;
            double rot = Math.Floor((deg + 180) / 360);
            double lon = deg - (rot * 360);
            return lon;
        }

        /// <summary>
        /// From WebMercacor 102100 to WGS84
        /// </summary>
        /// <param name="y"></param>
        /// <returns></returns>
        public static double FromWebMercatorY(double y)
        {
            double rad = PI_OVER_2 - (2.0 * Math.Atan(Math.Exp(-1.0 * y / RADIUS)));
            double lat = rad * DEGREES_PER_RADIANS;
            return lat;
        }

        public static Envelope ToWGS84Envelope(Envelope originalEnvelope)
        {
            Envelope env = new Envelope()
            {
                XMax = FromWebMercatorX(originalEnvelope.XMax),
                XMin = FromWebMercatorX(originalEnvelope.XMin),
                YMax = FromWebMercatorY(originalEnvelope.YMax),
                YMin = FromWebMercatorY(originalEnvelope.YMin),
                SpatialReference = new SpatialReference(4326)
            };

            return env;
        }
    }
}
