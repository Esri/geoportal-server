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
namespace com.esri.gpt.csw {
    /// <summary>
    ///  Collections class for catalogs.
    /// </summary>
    /// <remarks>
    /// The collection support both sequential and random access objects based on key.
    /// </remarks>
    /// 
   public class CswCatalogs :CswObjects
   {
       #region Properties
       /// <summary>
       /// CswCatalog parameter by key
       /// </summary>
       public new CswCatalog this[string key] {
            get { return (CswCatalog)base[key]; }
        }
       /// <summary>
       /// CswCatalog parameter by index
       /// </summary>
        public new CswCatalog this[int index] {
            get {
                return (CswCatalog)base[index];
            }
        }
       #endregion
       #region PrivateMethods
        /// <summary>
        /// Update the file with the collection details. 
        /// The catalog details are also added to the configuration to the file.
        /// </summary>
        /// <remarks>
        /// The catalog details are populated from  the collection.
        /// The catalog details are added to the configuration file.
        /// </remarks>
        /// <param name="param1">the catalog configuration filename</param>
       private void updatetoFile(string filename)
       {
           try
           {
               XmlDocument xmlDoc = new XmlDocument();
               XmlNode xmlnode = xmlDoc.CreateNode(XmlNodeType.XmlDeclaration, "", "");
               XmlElement root = xmlDoc.CreateElement("CSWCatalogs");
               xmlDoc.AppendChild(root);
               if (this.Count > 0)
               {
                   for (int i = 0; i < this.Count; i++)
                   {
                       CswCatalog newcatalog = this[i];
                       XmlElement childNode = xmlDoc.CreateElement("CSWCatalog");
                       XmlElement urlNode = xmlDoc.CreateElement("URL");
                       XmlElement nameNode = xmlDoc.CreateElement("Name");
                       XmlElement profileNode = xmlDoc.CreateElement("CSWProfile");
                       XmlElement lockNode = xmlDoc.CreateElement("Lock");
                       XmlElement credentialsNode = xmlDoc.CreateElement("Credentials");
                       XmlElement userNode = xmlDoc.CreateElement("Username");
                       XmlElement passwdNode = xmlDoc.CreateElement("Password");
                       XmlNode rootnode = xmlDoc.DocumentElement;
                       credentialsNode.AppendChild(userNode);
                       credentialsNode.AppendChild(passwdNode);
                       childNode.AppendChild(urlNode);
                       childNode.AppendChild(nameNode);
                       childNode.AppendChild(profileNode);
                       childNode.AppendChild(lockNode);
                       childNode.AppendChild(credentialsNode);
                       rootnode.AppendChild(childNode);
                       //setting values
                       urlNode.InnerText = XmlEscape(newcatalog.URL.Trim());
                       nameNode.InnerText = XmlEscape(newcatalog.Name.Trim());
                       profileNode.InnerText = newcatalog.Profile.ID;
                       lockNode.InnerText = newcatalog.Locking.ToString();
                   }
                   xmlDoc.Save(filename);
               }
               else
               {
                   System.IO.File.Delete(filename);
               }
           }
           catch (Exception ex)
           {
               throw ex;
           }

       }

       /// <summary>
       /// replace specia lxml character  
       /// </summary>
       /// <remarks>
       /// Encode special characters (such as &, ", <, >, ') to percent values.
       /// </remarks>
       /// <param name="data">Text to be encoded</param>
       /// <returns>Encoded text.</returns>
       private string XmlEscape(string data) {
           data = data.Replace("&", "&amp;");
           data = data.Replace("<", "&lt;");
           data = data.Replace(">", "&gt;");
           data = data.Replace("\"", "&quot;");
           data = data.Replace("'", "&apos;");
           return data;
       }

       /// <summary>
       /// replace specia lxml character  
       /// </summary>
       /// <remarks>
       /// Encode special characters (such as &, ", <, >, ') to percent values.
       /// </remarks>
       /// <param name="data">Text to be encoded</param>
       /// <returns>Encoded text.</returns>
       private string XmlDatatoString(string data) {
           data = data.Replace("&amp;","&");
           data = data.Replace("&lt;", "<");
           data = data.Replace("&gt;",">");
           data = data.Replace("&quot;","\"");
           data = data.Replace("&apos;", "'");
           return data;
       }
        #endregion
       #region "PublicMethods"

       /// <summary>
        /// Add a key value pair to catalog collection. 
        /// </summary>
        /// <remarks>
        /// Add to catalog collection.
        /// </remarks>
        /// <param name="param1">The key which is the url hashcode for the catalog</param>
        /// <param name="param2">the catalog object</param>

        public void AddCatalog(object key, CswCatalog catalog) {
            base.Add(key, catalog);
        }

        /// <summary>
        /// Add a new Catalog to the collection. 
        /// The catalog details are also added to the configuration to the file.
        /// </summary>
        /// <remarks>
        /// The catalog details are added in the collection.
        /// The catalog details are also appended in the configuration file.
        /// </remarks>
        /// <param name="param1">CswCatalog</param>
        /// <param name="param2">the catalog configuration file</param>
        public void AddCatalogtoConfig(CswCatalog catalog,string filename) {
            try {
                this.AddCatalog(catalog.ID, catalog);
                updatetoFile(filename); 
            }
            catch (Exception e) {
                throw e;
            }

        }

        /// <summary>
        /// Delete an exsisting Catalog from the configuration file. 
        /// </summary>
        /// <remarks>
        /// The catalog details are deleted from the collection.
        /// The catalog details are also deleted from the configuration file.
        /// </remarks>
        /// <param name="param1">CswCatalog</param>
        /// <param name="param2">the catalog configuration file</param>
        public void deleteCatalogfromConfig(CswCatalog catalog,string filename) {
            try {
                this.Remove(catalog.ID);
                updatetoFile(filename);
            }
            catch (Exception ex) {
                throw ex;
            }
        }


        /// <summary>
        /// Update the name of an exsisting Catalog. 
        /// The name is also updated in the configuration file.
        /// </summary>
        /// <remarks>
        /// The catalog name is updated from the collection.
        /// The catalog name is also updated from the configuration file.
        /// </remarks>
        /// <param name="param1">CswCatalog</param>
        /// <param name="param2">The string name</param>
        /// <param name="param2">The string updated url</param>
        ///  <param name="param2">The string name</param>
        /// <param name="param3">the catalog configuration file</param>
       public void updateCatalogNameinConfig(CswCatalog catalog, string displayname, string surl,CswProfile profile,string filename) {
           try {
               CswCatalog updatedcatalog = (CswCatalog)this[catalog.ID.ToString()];
               updatedcatalog.Name = displayname;
               updatedcatalog.URL =surl;
               updatedcatalog.Profile = profile;
               updatetoFile(filename);
           }
           catch (Exception ex) {
               throw ex;
           }
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
       /// <param name="param1">the catalog configuration file</param>
       /// <param name="param2">the profiles collection</param>
       public void loadCatalogfromConfig(string filename,CswProfiles profileList) {
           try {
               XmlDocument doc = new XmlDocument();
               doc.Load(filename);
               XmlNodeList xmlnodes = doc.GetElementsByTagName("CSWCatalog");

               foreach (XmlNode xmlnode in xmlnodes)
               {
                   String url = XmlDatatoString(xmlnode.SelectSingleNode("URL").InnerText);
                   String name = XmlDatatoString(xmlnode.SelectSingleNode("Name").InnerText);
                   String profileID = xmlnode.SelectSingleNode("CSWProfile").InnerText;
                   if (profileList.ContainsKey(profileID))
                   {
                       CswProfile profile = (CswProfile)profileList[profileID];
                       CswCatalog catalog = new CswCatalog(url, name, profile);
                       this.AddCatalog(catalog.ID, catalog);
                   }
               }
           }
           catch (Exception ex) {
               throw ex;
           }

       }


        #endregion
    }
}
