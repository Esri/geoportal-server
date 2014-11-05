using System;
using System.Windows;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    public static class Utilities
    {
        /// <summary>
        /// Gets the Base URL of the current application
        /// </summary>
        /// <returns></returns>
        public static string GetHostURL()
        {
            var uriAbsolute = new Uri(Application.Current.Host.Source.AbsoluteUri);
            string host = uriAbsolute.ToString().Substring(0, uriAbsolute.ToString().IndexOf(Constants.cClientBin));
            if (!host.EndsWith("/"))
                host += "/";

            return host;
        }

        /// <summary>
        /// Constructs a REST Endpoint URL via a specified Proxy
        /// </summary>
        /// <param name="ProxyURL"></param>
        /// <param name="MapServiceURL"></param>
        /// <returns></returns>
        public static string GetProxiedMapServicerEndPoint(string ProxyURL, string MapServiceURL)
        {
            if (String.IsNullOrEmpty(MapServiceURL)) return string.Empty;

            string sMapServiceRESTUrl = string.Empty;

            //Format Map Service REST URL for proxy with absolute path
            if (ProxyURL.StartsWith(Constants.cHTTPS))
            {
                sMapServiceRESTUrl = ProxyURL + "?" + MapServiceURL;

            }
            else //Format Map Service REST URL for proxy with relative path
            {
                sMapServiceRESTUrl = Utilities.GetHostURL() + GetRelativeURN(ProxyURL) + "?" + MapServiceURL;
            }

            if (!sMapServiceRESTUrl.EndsWith("/"))
                sMapServiceRESTUrl += "/";

            return sMapServiceRESTUrl;
        }

        /// <summary>
        /// Retrieves the Resource Name by stripping out the Parent Directory
        /// </summary>
        /// <param name="uri"></param>
        /// <returns></returns>
        private static string GetRelativeURN(string uri)
        {
            if (String.IsNullOrEmpty(uri)) return string.Empty;

            return uri.Substring(uri.IndexOf(Constants.cParentDirectory) + Constants.cParentDirectory.Length);
        }
    }
}
