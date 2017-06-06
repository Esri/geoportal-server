using System;
using System.Collections.Generic;
using Geocortex.Essentials.Client;
using Geocortex.Essentials.Client.Ows;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    /// <summary>
    /// The MapServiceManager class is intended for managing any interaction with Map Services and Geocortex Essentials
    /// (i.e. Adding Map Services)
    /// </summary>
    public class MapServiceManager
    {
        private Site _site = null;
        private string _proxyURL = string.Empty;
        private List<string> _lstProxiedHosts = null;

        #region Constructors

        //Constructor
        public MapServiceManager(Site site)
        {
            _site = site;
        }

        //Constructor - overloaded for Proxying
        public MapServiceManager(Site site, string ProxyURL, List<string> ProxiedHosts)
        {
            _site = site;
            _proxyURL = ProxyURL;
            _lstProxiedHosts = ProxiedHosts;
        }

        #endregion

        #region Properties

        public Site Site
        {
            get { return _site; }
            set { _site = value; }
        }

        public string ProxyURL
        {
            get { return _proxyURL; }
            set { _proxyURL = value; }
        }

        public List<string> ProxiedHosts
        {
            get { return _lstProxiedHosts; }
            set { _lstProxiedHosts = value; }
        }

        #endregion

        #region Public Functions

        public const string errorMessageLayerNotFoundInService = "Could not find layer '{0}' from service '{1}' in the site.";
        public const string errorMessageLayerNameAndIdNotFoundInService = "Could not find layer '{0}-{1}' from service '{2}' in the site.";
        public const string errorMessageServiceNotFound = "Could not find service '{0}' in the site.";
        public const string errorMessageLayerNotFound = "Could not find layer '{0}' in the site.";

        /// <summary>
        /// This method will handle the Addition of any type of Map Service to a Geocortex Viewer
        /// </summary>
        /// <param name="layer"></param>
        /// <param name="index"></param>
        public void AddMapService(ESRI.ArcGIS.Client.Layer layer, int index = 0)
        {
            MapServiceFactory mapFact = new MapServiceFactory(_site, _proxyURL, _lstProxiedHosts);

            if (layer is WmsLayer)
                mapFact.AddWMSMapService(layer as WmsLayer, index);
            else if (layer is ESRI.ArcGIS.Client.ArcGISDynamicMapServiceLayer)
                mapFact.AddAGSDynamicMapService(layer as ESRI.ArcGIS.Client.ArcGISDynamicMapServiceLayer, index);
        }

        /// <summary>
        /// Retrieves layer object from site based upon Map Service Display Name and Layer Name
        /// </summary>
        /// <param name="MapServiceDisplayName"></param>
        /// <param name="LayerName"></param>
        /// <returns></returns>
        [ObsoleteAttribute("This function is obsolete.", false)]
        public Layer GetLayer(string MapServiceDisplayName, string LayerName)
        {
            if (_site == null) return null;
            if ((String.IsNullOrEmpty(MapServiceDisplayName)) || (String.IsNullOrEmpty(LayerName))) return null;

            Layer lyr = null;

            //Search for the NTS Grid 50K layer in all Map Services within the Site
            foreach (MapService ms in _site.EssentialsMap.MapServices)
            {
                if (ms.DisplayName.Trim() == MapServiceDisplayName.Trim())
                {
                    lyr = ms.Layers.FindLayerByName(LayerName.Trim());
                }
            }

            return lyr;
        }

        /// <summary>
        /// Retrieves layer object from site based upon Map Service Display Name and Layer Name
        /// </summary>
        /// <param name="site"></param>
        /// <param name="mapServiceDisplayName"></param>
        /// <param name="layerName"></param>
        /// <returns></returns>
        [ObsoleteAttribute("This function is obsolete. Use GetLayerFromSite(Site site, string mapServiceDisplayName, string layerId, string layerName) instead.", false)]
        public static Layer GetLayerFromSite(Site site, string mapServiceDisplayName, string layerName)
        {
            if (site == null || String.IsNullOrEmpty(mapServiceDisplayName) || String.IsNullOrEmpty(layerName)) return null;

            Layer lyr = null;

            //Search for the NTS Grid 50K layer in all Map Services within the Site
            foreach (MapService ms in site.EssentialsMap.MapServices)
            {
                if (ms.DisplayName.Trim() == mapServiceDisplayName.Trim())
                {
                    lyr = ms.Layers.FindLayerByName(layerName.Trim());
                }
            }

            return lyr;
        }

        /// <summary>
        /// Retrieves layer object from site based upon Map Service Display Name and Layer Id
        /// </summary>
        /// <param name="site"></param>
        /// <param name="mapServiceDisplayName"></param>
        /// <param name="layerId"></param>
        /// <returns></returns>
        public static Layer GetLayerFromSite(Site site, string mapServiceDisplayName, string layerId, string layerName)
        {
            if (site == null || String.IsNullOrEmpty(mapServiceDisplayName) || String.IsNullOrEmpty(layerId) || String.IsNullOrEmpty(layerName)) return null;

            Layer lyr = null;

            //Search for the NTS Grid 50K layer in all Map Services within the Site
            foreach (MapService ms in site.EssentialsMap.MapServices)
            {
                if (ms.DisplayName.Trim() == mapServiceDisplayName.Trim())
                {
                    lyr = ms.Layers.FindLayerByID(layerId);
                    if (string.Compare(lyr.Name, layerName, StringComparison.InvariantCulture) != 0)
                        lyr = null;
                    break;
                }
            }

            return lyr;
        }

        #endregion
    }
}
