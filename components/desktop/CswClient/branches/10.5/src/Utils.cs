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
using Microsoft.Win32;
using com.esri.gpt.logger;
namespace com.esri.gpt.csw
{
    /// <summary>
    /// Enumeration of special folders
    /// </summary>
    public enum SpecialFolder
    {
        Application,
        ConfigurationFiles,
        Data,
        ExecutingAssembly,
        Help,
        Temp,
        TransformationFiles,
    }
    /// <summary>
    /// Ulitily class with convinience methods
    /// </summary>
    public class Utils
    {
        public static AppLogger logger =  new AppLogger("CswClient");

        #region "File/Folder Path"
        /// <summary>
        /// Get folder path for a system environment special folder
        /// </summary>
        /// <param name="folderName">enumeration of the system special folder</param>
        /// <returns>full path to the special folder</returns>
        public static string GetSpecialFolderPath(System.Environment.SpecialFolder folder)
        {
            return System.Environment.GetFolderPath(folder);
        }
       

        /// <summary>
        /// Get folder path for CSW dll related folder, such as the data folder, transformation file folder, etc.
        /// </summary>
        /// <param name="folderName">enumeration of the CSW special folder</param>
        /// <returns>full path to the special folder</returns>
        public static string GetSpecialFolderPath(com.esri.gpt.csw.SpecialFolder folder)
        {
            string folderPath = "";
            string configFileDir = "";
           
            switch (folder)
            {
                case SpecialFolder.ConfigurationFiles:

                    try
                    {
                        folderPath = logger.DataFolder;
                    }
                    catch (Exception e)
                    {
                        // throw e;
                    }
                    break;               
                case SpecialFolder.TransformationFiles:
                    // "Documents And Settings\<user>\Application Data\ESRI\PortalFindServices"
                    folderPath = GetSpecialFolderPath(Environment.SpecialFolder.ApplicationData);
                    folderPath = System.IO.Path.Combine(folderPath, "ESRI");
                    break;
                case SpecialFolder.Data:
                    folderPath = System.IO.Path.Combine(ExecutingAssemblyPath(), "Data");
                    break;
                case SpecialFolder.Help:
                    folderPath = System.IO.Path.Combine(ExecutingAssemblyPath(), "Help");
                    break;
                case SpecialFolder.ExecutingAssembly:
                    folderPath = ExecutingAssemblyPath();
                    break;
                case SpecialFolder.Application:
                    folderPath = System.Windows.Forms.Application.ExecutablePath;
                    break;
                case SpecialFolder.Temp:
                    // Returns the path of the current system's temporary folder
                    folderPath = System.IO.Path.GetTempPath();
                    break;
            }

            return folderPath;
        }
        /// <summary>
        /// Gets executing assembly path
        /// </summary>
        /// <returns></returns>
        private static string ExecutingAssemblyPath()
        {
            return System.IO.Path.GetDirectoryName(System.Reflection.Assembly.GetExecutingAssembly().Location);
        }
        #endregion
        #region "URL"
        /// <summary>
        /// Ensure trailing question mark or ampersand in the url
        /// </summary>
        /// <param name="url">the url string</param>
        /// <returns>the url</returns>
        public static string EnsureTrailingQuestionOrAmpersandInURL(string url)
        {
            if (url == null) return null;

            // clean up
            string newUrl = url.Trim();
            if (newUrl.Length == 0) return newUrl;
            if (newUrl.EndsWith("/")) { newUrl = newUrl.Substring(0, newUrl.Length - 1); }

            // check if has "?" in the url
            if (!newUrl.Contains("?"))
            {
                newUrl += "?";
            }
            else
            {
                switch (newUrl.Substring(newUrl.Length - 1, 1))
                {
                    case "?":
                    case "&":
                        break;

                    default:
                        newUrl += "&";
                        break;
                }
            }

            return newUrl;
        }
        #endregion
        #region PublicStaticMethods
        /// <summary>
        /// Get next visible tabstop control. Used for tab key handling.
        /// </summary>
        /// <param name="form">the form that contains those controls</param>
        /// <returns>a control; or null if not found</returns>
        public static System.Windows.Forms.Control GetNextTabStopControl(System.Windows.Forms.Form form)
        {
            System.Windows.Forms.Control ctrl = form.GetNextControl(form.ActiveControl, true);
            bool found = false, exhausted = false, marked = false;
            while (!found && !exhausted)
            {
                if (ctrl != null)
                {
                    if (ctrl.TabStop && ctrl.CanFocus) { found = true; break; }  // found a control
                    else { ctrl = form.GetNextControl(ctrl, true); }
                }
                else
                {
                    if (marked) { exhausted = true; break; }// exhausted all the controls
                    else
                    {
                        ctrl = form.GetNextControl(null, true);
                        marked = true;
                    }
                }
            }

            return ctrl;
        }

        /// <summary>
        /// Get next visible tabstop control. Used for tab key handling.
        /// </summary>
        /// <param name="form">the form that contains those controls</param>
        /// <returns>a control; or null if not found</returns>
        public static System.Windows.Forms.Control GetNextTabStopControl(System.Windows.Forms.UserControl form)
        {
            System.Windows.Forms.Control ctrl = form.GetNextControl(form.ActiveControl, true);
            bool found = false, exhausted = false, marked = false;
            while (!found && !exhausted)
            {
                if (ctrl != null)
                {
                    if (ctrl.TabStop && ctrl.CanFocus) { found = true; break; }  // found a control
                    else { ctrl = form.GetNextControl(ctrl, true); }
                }
                else
                {
                    if (marked) { exhausted = true; break; }// exhausted all the controls
                    else
                    {
                        ctrl = form.GetNextControl(null, true);
                        marked = true;
                    }
                }
            }

            return ctrl;
        }
        #endregion
    }
}
