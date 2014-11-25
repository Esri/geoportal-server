using System;
using System.Windows.Forms;  // MessageBox
using System.Collections.Generic;
using System.Text;
using Microsoft.Win32;  // Registry
using ESRI.ArcGIS;

namespace com.esri.gpt.publish
{
    static class Globals
    {
        // Define the formatting strings and base date/time
        // for use by date/time controls on the GUI panels
        public static string defaultDateFormat = "yyyy-MM-dd";
        public static string defaultTimeFormat = "HH:mm:ss";
        public static string DateTimeMinValue = "1900-01-01 00:00:00";

        // Locates the ArcGIS Desktop installation directory using
        // the \HKEY_LOCAL_MACHINE\SOFTWARE\ESRI\Desktop10.0 registry key
        public static string ArcGISInstallationFolder
        {
            get
            {
                string keyValue = "";
                string errMessage = "";
                string arcgisVersion = "10.3";
                bool succeeded = ESRI.ArcGIS.RuntimeManager.Bind(ProductCode.Desktop);
                if (succeeded)
                {
                    RuntimeInfo activeRunTimeInfo = RuntimeManager.ActiveRuntime;
                    arcgisVersion = "SOFTWARE\\ESRI\\Desktop" +  activeRunTimeInfo.Version;
                }

                RegistryKey esriKey = Registry.LocalMachine.OpenSubKey(arcgisVersion);
                
                // If the registry key can found, obtain the key's value
                if (esriKey == null)
                    errMessage = arcgisVersion + " registry key cannot be found.";
                else
                    keyValue = esriKey.GetValue("InstallDir").ToString();

                // If the registry key value is an existing directory, return the key's value
                if (System.IO.Directory.Exists(keyValue) == false)
                    errMessage = arcgisVersion + " registry key 'InstallDir' value is invalid.";
                else
                    return keyValue;

                // Message box only displayes if an error was encountered
                MessageBox.Show("Unable to determine ArcGIS installation directory." + "\n\n"
                    + "Error Message:" + "\n\t" + errMessage, "Error", MessageBoxButtons.OK);

                return "";
            }
        }

        public static string ArcGISInstallationFolderFromCommandLine
        {
            get
            {
                String cmdLine = System.Environment.CommandLine;
                int idx = cmdLine.IndexOf("Bin");
                if (idx == -1)
                {
                    idx = cmdLine.IndexOf("bin");
                }
                if (idx > -1)
                {
                    cmdLine = cmdLine.Substring(0, idx-1);
                    cmdLine = cmdLine.Replace("\"", "");
                }
                return cmdLine;
            }
        }

        // Locates the ArcGIS Desktop Metadata Config directory using
        // the \HKEY_CURRENT_USER\Software\ESRI\Desktop10.0\Metadata\Config registry key
        public static string ArcGISDesktopMetadataConfig
        {
            get
            {
                string keyValue = "";
                string errMessage = "";
                
                string arcgisVersion = "10.3";
                bool succeeded = ESRI.ArcGIS.RuntimeManager.Bind(ProductCode.Desktop);
                if (succeeded)
                {
                    RuntimeInfo activeRunTimeInfo = RuntimeManager.ActiveRuntime;
                    arcgisVersion = activeRunTimeInfo.Version;
                }

                RegistryKey esriKey = Registry.CurrentUser.OpenSubKey("Software\\ESRI\\Desktop" + arcgisVersion + "\\Metadata\\Config");
                
                // If the registry key can found, obtain the key's value
                if (esriKey == null)
                {
                    errMessage = StringMessages.MetadataConfigNotFound;
                }
                else
                {
                    if (esriKey.GetValue("CurrConfig") != null)
                    {
                        keyValue = esriKey.GetValue("CurrConfig").ToString();
                        return keyValue;
                    }
                    else
                    {
                        errMessage = StringMessages.MetadataConfigNotFound;
                    }
                }

                // Message box only displayes if an error was encountered
                MessageBox.Show(StringMessages.MetadataConfigDirectoryNotFound + "\n\n"
                    + "Error Message:" + "\n\t" + errMessage, "Error", MessageBoxButtons.OK);

                return "";
            }
        }
    }
}