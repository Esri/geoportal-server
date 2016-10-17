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

namespace com.esri.gpt.csw
{
    /**
 * The Class DcList.  List that has key value pairs.  The keys
 * can be duplicate and will produce a list on get(key)
 */
    class DcList : LinkedList<DcList.Value>
    {
        #region Private Variables

        /** The Constant DELIMETER_LIST. */
        private String[] DELIMETER_LIST = { "\u2715" };

        /** The constant DELIMETER_VALUES. */
        private String[] DELIMETER_VALUES = { "\u2714" };

        /** The Constant EMPTY_SCHEME. */
        private String EMPTY_SCHEME = "************";

        private static String CONTENTTYPE_FGDC = "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType";
        private static String CONTENTTYPE_ISO = "http://www.isotc211.org/2005/gmd/MD_Metadata/hierarchyLevelName";
        private static String SERVER = "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server";
        private static String SERVICE = "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Service";
        private static String SERVICE_TYPE = "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ServiceType";
        private static String ONLINK_FGDC = "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Onlink";
        private static String ONLINK_ISO = "http://www.isotc211.org/2005/gmd/MD_BrowseGraphic/filename";
        private static String THUMBNAIL_FGDC = "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Thumbnail";
        private static String THUMBNAIL_ISO = "http://www.isotc211.org/2005/gmd/MD_BrowseGraphic/filename";
        private static String METADATA_DOCUMENT = "urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Document";

        #endregion
        #region Constructors
        /**
         * Instantiates a new dc list.
         */
        public DcList() { }
        /**
         * Instantiates a new dc list.
         * 
         * @param dcList the dc list
         */
        public DcList(String dcList)
        {
            this.add(dcList);
        }
        #endregion
        #region Enum(s)
        public static String getScheme(Scheme scheme)
        {

            string name = "";

            switch (scheme)
            {
                case Scheme.METADATA_DOCUMENT:
                    name = DcList.METADATA_DOCUMENT;
                    break;
                case Scheme.SERVER:
                    name = DcList.SERVER;
                    break;
            }

            return name;

        }
        public enum Scheme
        {

            /** The FGDC content type scheme */
            CONTENTTYPE_FGDC,

            /** The ISO contenttype scheme. */
            CONTENTTYPE_ISO,

            /** The server scheme. */
            SERVER,

            /** The service scheme. */
            SERVICE,

            /** The service type scheme. */
            SERVICE_TYPE,

            /** The fgdc onlink scheme. */
            ONLINK_FGDC,

            /** The ISO onlink scheme. */
            ONLINK_ISO,

            /** The FGDC thumbnail scheme **/
            THUMBNAIL_FGDC,


            /** The iso thumbnail scheme **/
            THUMBNAIL_ISO,


            /** The Metadata document scheme. */
            METADATA_DOCUMENT

        }
        #endregion
        #region "StaticMethods"
        /**
         *If string is null, returns empty string, else returns trimmed
         *string.
         * 
         * @param string the string to be checked
         * 
         * @return the string (trimmed, never null)
         */
        public static string chkStr(String str)
        {
            if (str == null)
            {
                return "";
            }
            return str.Trim();
        }
        #endregion
        #region PublicMethods
        /// <summary>
        /// Adds the dcString.
        /// </summary>
        /// <param name="dcString">dcstring</param>
        public void add(String dcString)
        {

            dcString = chkStr(dcString);

            if (dcString == "")
            {
                return;
            }

            String[] schemeValues = dcString.Split(this.DELIMETER_LIST, 100, StringSplitOptions.RemoveEmptyEntries);
            String[] arrKeyValue;

            for (int i = 0; i < schemeValues.Length; i++)
            {

                String schemeValue = chkStr(schemeValues[i]);

                if (schemeValue == "")
                {
                    continue;
                }

                arrKeyValue = schemeValue.Split(this.DELIMETER_VALUES, 100, StringSplitOptions.RemoveEmptyEntries);

                if (arrKeyValue.Length == 1)
                {
                    this.AddLast(new Value(arrKeyValue[0], EMPTY_SCHEME));
                    continue;
                }
                this.AddLast(new Value(arrKeyValue[0], arrKeyValue[1]));

            }
        }
      
        /// <summary>
        ///  Gets the param scheme the scheme (if null or empty, EMPTY_SCHEME will be used)
        /// </summary>
        /// <param name="scheme">the scheme</param>
        /// <returns>the list</returns>

        public LinkedList<String> get(String scheme)
        {
            scheme = chkStr(scheme);
            if (scheme == "")
            {
                scheme = EMPTY_SCHEME;
            }
            LinkedList<String> valueList = new LinkedList<String>();
            LinkedList<DcList.Value>.Enumerator iter = this.GetEnumerator();

            while (iter.MoveNext())
            {
                // Value value = iter.MoveNext();
                if (iter.Equals(null))
                {
                    continue;
                }
                if (iter.Current.getScheme().ToLower().Equals(scheme.ToLower()))
                {
                    valueList.AddLast(iter.Current.getValue());
                }
            }
            return valueList;

        }
        #endregion
        #region Inner class
        /// <summary>
        /// The Value class
        /// </summary>
        public class Value
        {
            #region instance variables
            /** The scheme. */
            String scheme;

            /** The value. */
            String value;
            #endregion
            #region Constructor(s)
            // constructor 
            /**
             * Instantiates a new value.
             * 
             * @param scheme the scheme
             * @param value the value
             */
            public Value(String value, String scheme)
            {
                this.setScheme(scheme);
                this.setValue(value);
            }
            #endregion
            #region Properties
            /**
             * Gets the scheme.
             * 
             * @return the scheme (trimmed, never null)
             */
            public String getScheme()
            {
                return chkStr(scheme);
            }

            /**
             * Sets the scheme.
             * 
             * @param scheme the new scheme
             */
            public void setScheme(String scheme)
            {
                this.scheme = scheme;
            }

            /**
             * Gets the value.
             * 
             * @return the value (trimmed, never null)
             */
            public String getValue()
            {
                return chkStr(value);

            }

            /**
             * Sets the value.
             * 
             * @param value the new value
             */
            public void setValue(String value)
            {
                this.value = value;
            }
            #endregion

        }
        #endregion
    }
}
