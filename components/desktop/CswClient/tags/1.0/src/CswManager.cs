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

namespace com.esri.gpt.csw
{
    /// <summary>
    /// Handles CSW Management
    /// </summary>
    /// <remarks>
    /// Handles profiles and CswCatalog list
    /// </remarks>
    public class CswManager
    {        
        private CswCatalogs catalogList = new CswCatalogs();
        private CswProfiles profileList = new CswProfiles();
        private string DEFAULT_CATALOGFILE;
        private string DEFAULT_PROFILEFILE;
        private string USER_CATALOGFILE;

        # region constructor definition
        /// <summary>
        /// Constructor
        /// </summary>
        public CswManager()
        {
            DEFAULT_CATALOGFILE = System.IO.Path.Combine(Utils.GetSpecialFolderPath(SpecialFolder.ConfigurationFiles), "CSWCatalogs.xml");
            DEFAULT_PROFILEFILE = System.IO.Path.Combine(Utils.GetSpecialFolderPath(SpecialFolder.ConfigurationFiles), "CSWProfiles.xml");
            USER_CATALOGFILE = System.IO.Path.Combine(Utils.GetSpecialFolderPath(SpecialFolder.TransformationFiles), "CSWCatalogs.xml");
        }
        #endregion
        #region "PublicMethods"
        /// <summary>
        /// Loads the profile details from configuration file. 
        /// </summary>
        /// <remarks>
        /// The profiles details are loaded in the collection.
        /// Duplicate or invalid profiles are ignored.
        /// </remarks>
        /// <returns>CswProfiles collection.</returns>
        public CswProfiles loadProfile()
        {
            profileList.loadProfilefromConfig(DEFAULT_PROFILEFILE);
            return profileList;

        }

        /// <summary>
        /// Loads the catalog details from configuration file. 
        /// </summary>
        /// <remarks>
        /// The catalog details are loaded in the collection.
        /// Duplicate or invalid catalog are ignored.
        /// Invalid catalog includes catalogs with profiles information
        /// not present in profiles collection.
        /// </remarks>
        /// <returns>CswCatalogs collection.</returns>
        public CswCatalogs loadCatalog()
        {

            if (!isUserCatalogExists())
            {
                catalogList.loadCatalogfromConfig(DEFAULT_CATALOGFILE, profileList);
            }
            else
            {
                catalogList.loadCatalogfromConfig(USER_CATALOGFILE, profileList);
            }

            return catalogList;

        }

        /// <summary>
        /// Add a new Catalog to the configuration file. 
        /// </summary>
        /// <remarks>
        /// The catalog details are added in the collection.
        /// The catalog details are also appended in the configuration file.
        /// </remarks>
        /// <param name="catalog">CswCatalog</param>
        public void addCatalog(CswCatalog catalog)
        {

            if (!isUserCatalogExists())
            {
                createUserCatalogFile();
            }

            catalogList.AddCatalogtoConfig(catalog, USER_CATALOGFILE);
        }

        /// <summary>
        /// Check if user Catalog file exists at "Documents And Settings\<user>\Application Data\ESRI\Portal\CswClients"
        /// </summary>
        /// <remarks>
        /// Returns true/false 
        /// </remarks>
        /// <param>None</param>
        public bool isUserCatalogExists()
        {
            try
            {
                if (null != USER_CATALOGFILE)
                {
                    if (System.IO.File.Exists(USER_CATALOGFILE))
                    {
                        return true;
                    }
                    else { return false; }
                }
            }
            catch (Exception e)
            {
                //  ShowErrorMessageBox(StringResources + "\r\n" + e.Message);
            }

            return false;
        }

        /// <summary>
        /// Creates user Catalog file at "Documents And Settings\<user>\Application Data\ESRI\Portal\CswClients"
        /// </summary>
        /// <remarks>
        /// Returns Nothing
        /// </remarks>
        /// <param>None</param>
        public void createUserCatalogFile()
        {
            if (null != USER_CATALOGFILE)
            {
                System.IO.FileStream file = System.IO.File.Create(USER_CATALOGFILE);
                file.Close();

            }
            else
            {
                //  throw exception(StringResources);
            }
        }

        /// <summary>
        /// Delete an exsisting Catalog from the configuration file. 
        /// </summary>
        /// <remarks>
        /// The catalog details are deleted from the collection.
        /// The catalog details are also deleted from the configuration file.
        /// </remarks>
        /// <param name="catalog">CswCatalog</param>
        public void deleteCatalog(CswCatalog catalog)
        {

            if (!isUserCatalogExists())
            {
                addCatalog(catalog);
                // catalogList.deleteCatalogfromConfig(catalog, DEFAULT_CATALOGFILE);
            }
            // else
            //   {
            catalogList.deleteCatalogfromConfig(catalog, USER_CATALOGFILE);
            //   }                        
        }

        /// <summary>
        /// Update the name of an exsisting Catalog. 
        /// </summary>
        /// <remarks>
        /// The catalog name is updated from the collection.
        /// The catalog name is also updated from the configuration file.
        /// </remarks>
        /// <param name="catalog">CswCatalog</param>
        /// <param name="name">The string display name</param>
        /// <param name="url">The string url</param>
        /// <param name="name">The profile</param>
        public void updateCatalog(CswCatalog catalog, String displayname, String url, CswProfile profile)
        {

            if (!isUserCatalogExists())
            {
                createUserCatalogFile();

            }

            catalogList.updateCatalogNameinConfig(catalog, displayname, url, profile, USER_CATALOGFILE);

        }
        #endregion
    }
}
