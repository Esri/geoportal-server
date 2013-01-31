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
using System.Text;

namespace com.esri.gpt.csw
{
    /// <summary>
    /// Envelope defined as in geographical coordinate system. Used for CSW search criteria.
    /// </summary>
    public class Envelope
    {
        private double _minX, _minY, _maxX, _maxY;

        #region Properties
        /// <summary>
        /// Max longitude
        /// </summary>
        public double MinX
        {
            get { return _minX; }
            set 
            {
                //if (value < -180 || value > 180) { throw new ArgumentOutOfRangeException(); }
                //else
                //{
                _minX = value;
                //}
            }
        }

        /// <summary>
        /// Min latitude
        /// </summary>
        public double MinY
        {
            get { return _minY; }
            set
            {
                //if (value < -90 || value > 90) { throw new ArgumentOutOfRangeException(); }
                //else
                //{
                _minY = value;
                //}
            }
        }

        /// <summary>
        /// Max longitude
        /// </summary>
        public double MaxX
        {
            get { return _maxX; }
            set
            {
                //if (value < -180 || value > 180) { throw new ArgumentOutOfRangeException(); }
                //else
                //{
                _maxX = value;
                //}
            }
        }

        /// <summary>
        /// Max latitude
        /// </summary>
        public double MaxY
        {
            get { return _maxY; }
            set
            {
                //if (value < -90 || value > 90) { throw new ArgumentOutOfRangeException(); }
                //else
                //{
                _maxY = value;
                //}
            }
        }
        #endregion
        #region Constructor(s)
        /// <summary>
        /// Envelope constructor
        /// </summary>
        public Envelope()
        {
            _minX = _minY = _maxX = _maxY = 0.0;
        }

        /// <summary>
        /// Envelope constructor
        /// </summary>
        /// <param name="minX">Min longitude of the envelope</param>
        /// <param name="minY">Min latitude of the envelope</param>
        /// <param name="maxX">Max longitude of the envelope</param>
        /// <param name="maxY">Max latitude of the envelope</param>
        public Envelope(double minX, double minY, double maxX, double maxY)
        {
            MinX = minX;
            MinY = minY;
            MaxX = maxX;
            MaxY = maxY;
        }
        #endregion
    }
}
