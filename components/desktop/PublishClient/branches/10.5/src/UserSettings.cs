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
using System.Xml;

namespace com.esri.gpt.publish
{
    class UserSettings
    {
        #region Private Variable (s)
        private PublicationParams parameters;
        private String userSettingFile;
        #endregion
        #region Constructor
        public UserSettings(String userSettingFile)
        {
            this.userSettingFile = userSettingFile;
        }
        #endregion
        #region Accessor Methods
        public PublicationParams Params
        {
            get
            {
                return parameters;
            }
            set
            {
                parameters = value;
            }
        }        
        /// <summary>
        /// Creates user file at "Documents And Settings\<user>\Application Data\"
        /// </summary>
        /// <remarks>
        /// Returns Nothing
        /// </remarks>
        /// <param>None</param>
        public void createUserSettingsFile()
        {
            if (null != userSettingFile)
            {
                try
                {
                    System.IO.FileStream file = System.IO.File.Create(userSettingFile);
                    file.Close();
                }
                catch (Exception e)
                {
                    throw e;
                }
            }

        }
        /// <summary>
        /// Updates user file at "Documents And Settings\<user>\Application Data\"
        /// </summary>
        /// <remarks>
        /// Returns Nothing
        /// </remarks>
        /// <param>None</param>
        public void updateUserSettingsFile()
        {
            if (null != userSettingFile)
            {
                XmlDocument xmlDoc = new XmlDocument();

                XmlNode xmlnode = xmlDoc.CreateNode(XmlNodeType.XmlDeclaration, "", "");
                XmlElement root = xmlDoc.CreateElement(StringMessages.UserSettings);
                xmlDoc.AppendChild(root);

                XmlElement serverNode = xmlDoc.CreateElement(StringMessages.Setting);
                serverNode.SetAttribute(StringMessages.Server, parameters.ServerUrl);


                XmlElement serviceNode = xmlDoc.CreateElement(StringMessages.Setting);
                serviceNode.SetAttribute(StringMessages.Service, parameters.Service);

                XmlElement usernameNode = xmlDoc.CreateElement(StringMessages.Setting);
                usernameNode.SetAttribute(StringMessages.Username, parameters.UserName);

                root.AppendChild(serverNode);
                root.AppendChild(serviceNode);
                root.AppendChild(usernameNode);

                xmlDoc.Save(userSettingFile);

            }

        }
        /// <summary>
        /// Check if user file exists at "Documents And Settings\<user>\Application Data\"
        /// </summary>
        /// <remarks>
        /// Returns true/false 
        /// </remarks>
        /// <param>None</param>
        public bool isUserSettingsFileExists()
        {
            try
            {
                if (null != userSettingFile)
                {
                    if (System.IO.File.Exists(userSettingFile))
                    {
                        return true;
                    }
                    else { return false; }
                }
            }
            catch (Exception e)
            {
                throw e;
            }

            return false;
        }
        #endregion
    }
}
