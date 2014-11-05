using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows;
using ESRI.ArcGIS.Client;
using ESRI.ArcGIS.Client.Geometry;
using Geocortex.Essentials.Client;
using Geocortex.EssentialsSilverlightViewer.Infrastructure.Diagnostics;
using Geocortex.EssentialsSilverlightViewer.Infrastructure.ServiceDiscovery.Definition;
using System.Collections.ObjectModel;
using Geocortex.Essentials.Client.Ows;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    public class MapServiceFactory
    {
        private Site _site = null;
        private string _proxyURL = string.Empty;
        private List<string> _lstProxiedHosts = null;
        private IServiceInfo _servInfo = null;

        //Constructor
        public MapServiceFactory(Site site)
        {
            _site = site;
        }

        //Constructor - overloaded for Proxying
        public MapServiceFactory(Site site, string ProxyURL, List<string> ProxiedHosts)
        {
            _site = site;
            _proxyURL = ProxyURL;
            _lstProxiedHosts = ProxiedHosts;
        }

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

        /// <summary>
        /// Performs the operation of Adding a WMS service to the Geocortex Viewer Framework (i.e. Map and Layer List)
        /// </summary>
        /// <param name="mapservice"></param>
        /// <param name="index"></param>
        public void AddWMSMapService(WmsLayer mapservice, int index = 0)
        {
            if (mapservice == null)
                Trace.TraceError("MapServiceFactory.AddWMSMapService: layer parameter is null");

            _servInfo = null;

            try
            {

                //Call the WMS layer's GetCapabilities since the Visible Scale Range is not accessible in initialized WmsLayer object
                WMSHelper.eventGetCapabilities += (s1, e1) =>
                {
                    _servInfo = e1.ServiceInfo;



                    //Handle Map Service Initialization Failure
                    mapservice.InitializationFailed += MapService_InitializeFailure;

                    //Initialize WmsLayer object
                    mapservice.Initialized += (s, e) =>
                    {

                        Envelope wmsExtent = WMSHelper.GetCorrectedExtent(mapservice);

                        List<WmsLayer.LayerInfo> completeLayerList = WMSHelper.FlattenWmsLayerList(mapservice).ToList();

                        mapservice.Layers = completeLayerList.Where(lyr => lyr.Name != null)
                                                                .Select(lyr => lyr.Name)
                                                                .ToArray();

                        _site.Map.Layers.Add(mapservice);

                        dynamic serviceElement = new DynamicElement();
                        serviceElement.DisplayName = serviceElement.Name = mapservice.Title ?? mapservice.ID;
                        serviceElement.ID = _site.EssentialsMap.MapServices.Count;
                        serviceElement.MapServiceType = MapServiceType.WMS;
                        serviceElement.ServiceUrl = mapservice.Url;
                        serviceElement.IncludeInLayerList = true;
                        serviceElement.IncludeInLegend = true;
                        serviceElement.ProviderInvariantName = "Geocortex.Gis.Services.Wms";

                        var parentLayers = new Dictionary<string, string>();

                        foreach (var lyr in completeLayerList)
                        {
                            dynamic layerElement = (Geocortex.Essentials.Client.Utilities.DynamicElement)serviceElement.Layers.Add();

                            layerElement.DisplayName = layerElement.Name = lyr.Title;
                            layerElement.ID = completeLayerList.IndexOf(lyr).ToString();
                            layerElement.FullExtent = wmsExtent;
                            layerElement.IncludeInLayerList = true;
                            layerElement.Visible = mapservice.GetLayerVisibility(completeLayerList.IndexOf(lyr)); //true;
                            layerElement.IncludeInLegend = true;
                            layerElement.WmsLayerName = completeLayerList.IndexOf(lyr).ToString();
                            //Look up the Service Info for the layer's Visible Scale
                            if ((_servInfo != null) && (_servInfo.LayersInfo != null))
                            {
                                foreach (ILayerInfo lyrInfo in _servInfo.LayersInfo)
                                {
                                    if (lyr.Name == lyrInfo.Name)
                                    {
                                        if (lyrInfo.MaxScale > 0.0)
                                            layerElement.MinScale = lyrInfo.MaxScale;
                                    }
                                }
                            }

                            var sublayers = new List<string>();

                            if (lyr.ChildLayers.Count > 0)
                            {
                                foreach (var childlayer in lyr.ChildLayers)
                                {
                                    string childlayerId = completeLayerList.IndexOf(childlayer).ToString();
                                    sublayers.Add(childlayerId);
                                    parentLayers.Add(childlayerId, layerElement.ID.ToString());
                                }
                            }

                            layerElement.SubLayerIDs = sublayers;

                            if (parentLayers.ContainsKey(layerElement.ID.ToString()))
                            {
                                layerElement.ParentLayerID = parentLayers[layerElement.ID.ToString()];
                            }
                        }

                        //Add actual Map Service to Essentials GUI Framework (Map, Layer List)
                        //AddMapServiceToEssentials(mapservice, serviceElement, index);
                        //Create an Essentials MapService from our ESRI WMS Layer
                        MapService geMapService = MapService.CreateFrom(mapservice, serviceElement);

                        geMapService.ServiceLayer.ShowLegend = true;

                        foreach (var lyr in geMapService.Layers)
                        {
                            if (lyr.SubLayers.Count > 0)
                            {
                                foreach (var sublayer in lyr.SubLayers)
                                {
                                    sublayer.IsExpanded = false;
                                }
                            }
                        }

                        // Add our new MapService to the Essentials Site
                        if (index >= 0)
                            _site.EssentialsMap.MapServices.Insert(0, geMapService);
                        else
                            _site.EssentialsMap.MapServices.Add(geMapService);

                    };
                    mapservice.Initialize();
                };
                WMSHelper.GetCapabilities(mapservice.Url);

            }
            catch (Exception ex)
            {
                Trace.TraceError("MapServiceFactory.AddWMSMapService(): " + ex.Message);
            }
            finally
            {
                _servInfo = null;
            }
        }

        /// <summary>
        /// Performs the operation of Adding an ArcGIS Server Map Service to the Geocortex Viewer Framework (i.e. Map and Layer List)
        /// </summary>
        /// <param name="mapservice"></param>
        /// <param name="index"></param>
        public void AddAGSDynamicMapService(ArcGISDynamicMapServiceLayer mapservice, int index = 0)
        {
            if (mapservice == null)
                Trace.TraceError("MapServiceFactory.AddAGSDynamicMapService: layer parameter is null");

            try
            {
                //Assign a Proxy URL if the Map Service URL is from a Proxied Host
                if (_lstProxiedHosts != null)
                {
                    foreach (string host in _lstProxiedHosts)
                    {
                        if (mapservice.Url.Contains(host))
                        {
                            mapservice.ProxyURL = _proxyURL;
                            break;
                        }
                    }
                }

                //Instantiate MapServiceRESTMgr object for retrieving Layers Information from ArcGIS Server REST API Endpoint
                MapServiceRESTMgr mgrRESTMapService = new MapServiceRESTMgr(mapservice.Url, _proxyURL, _lstProxiedHosts);

                //Retrieve Layers Information and set the MapService's VisibleLayers property 
                MapServiceRESTMgr.eventGetLayersInfo += (s1, e1) =>
                {
                    //Handle Map Service Initialization Failure
                    mapservice.InitializationFailed += MapService_InitializeFailure;

                    List<bool> lstVisible = new List<bool>();
                    List<string> lstLayer = new List<string>();


                    //Initialize the Map Service and add to the ESRI and Geocortex Viewer Framework (i.e. Map and Layer List)
                    mapservice.Initialized += (s, e) =>
                    {
                        _site.Map.Layers.Add(mapservice);

                        var parentLayers = new Dictionary<int, int>();

                        dynamic serviceElement = new DynamicElement();
                        serviceElement.DisplayName = serviceElement.Name = mapservice.MapName;
                        serviceElement.ID = _site.EssentialsMap.MapServices.Count;
                        serviceElement.MapServiceType = MapServiceType.Dynamic;
                        serviceElement.ServiceUrl = mapservice.Url;
                        serviceElement.IncludeInLegend = true;
                        serviceElement.ProviderInvariantName = "Geocortex.Gis.Services.ArcGisServer.Rest";

                        foreach (var lyr in mapservice.Layers)
                        {
                            dynamic layerElement = (Geocortex.Essentials.Client.Utilities.DynamicElement)serviceElement.Layers.Add();
                            layerElement.DisplayName = layerElement.Name = lyr.Name;
                            layerElement.ID = lyr.ID;
                            layerElement.FullExtent = mapservice.FullExtent;
                            layerElement.MaxScale = lyr.MaxScale;
                            layerElement.MinScale = (lyr.MinScale != 0) ? lyr.MinScale : double.PositiveInfinity;
                            layerElement.Visible = lyr.DefaultVisibility;
                            layerElement.IncludeInLegend = true;

                            lstVisible.Add(lyr.DefaultVisibility);
                            lstLayer.Add(lyr.ID.ToString());

                            var sublayers = new Collection<string>();

                            if (lyr.SubLayerIds != null)
                            {
                                foreach (int id in lyr.SubLayerIds)
                                {
                                    sublayers.Add(id.ToString());
                                    parentLayers.Add(id, lyr.ID);
                                }
                            }

                            layerElement.SubLayerIDs = sublayers;

                            if (parentLayers.ContainsKey(lyr.ID))
                            {
                                layerElement.ParentLayerID = parentLayers[lyr.ID];
                            }
                        }

                        //Add actual Map Service to Essentials GUI Framework (Map, Layer List)
                        //AddMapServiceToEssentials(mapservice, serviceElement, index);

                        // Create an Essentials MapService from our ESRI WMS Layer
                        MapService geMapService = MapService.CreateFrom(mapservice, serviceElement);

                        geMapService.ServiceLayer.ShowLegend = true;
                        geMapService.SetVisibilities(lstLayer, lstVisible);

                        foreach (var lyr in geMapService.Layers)
                        {
                            if (lyr.SubLayers.Count > 0)
                            {
                                foreach (var sublayer in lyr.SubLayers)
                                {
                                    sublayer.IsExpanded = false;
                                }
                            }
                        }


                        // Add our new MapService to the Essentials Site
                        if (index >= 0)
                            _site.EssentialsMap.MapServices.Insert(0, geMapService);
                        else
                            _site.EssentialsMap.MapServices.Add(geMapService);
                    };

                    mapservice.Initialize();
                };

                mgrRESTMapService.GetLayersInfo();
            }
            catch (Exception ex)
            {
                Trace.TraceError("MapServiceFactory.AddAGSDynamicMapService(): " + ex.Message);
            }
        }

        /// <summary>
        /// Adds the Map Service to the ESRI Map Control and the Essentials Controls (Map, Layer List, etc)
        /// </summary>
        /// <param name="layer"></param>
        /// <param name="serviceElement"></param>
        /// <param name="index"></param>
        private void AddMapServiceToEssentials(ESRI.ArcGIS.Client.Layer layer, DynamicElement serviceElement, int index = 0)
        {
            if (_site == null)
                Trace.TraceError("MapServiceFactory.AddMapService: Site has not been initialized");
            if (layer == null)
                Trace.TraceError("MapServiceFactory.AddMapService: layer parameter is null");
            if (serviceElement == null)
                Trace.TraceError("MapServiceFactory.AddMapService: ServiceElement has not been initialized");

            // Create an Essentials MapService from our ESRI WMS Layer
            MapService geMapService = MapService.CreateFrom(layer, serviceElement);

            geMapService.ServiceLayer.ShowLegend = true;

            _site.Map.Layers.Add(layer);

            // Add our new MapService to the Essentials Site
            if (index >= 0)
                _site.EssentialsMap.MapServices.Insert(0, geMapService);
            else
                _site.EssentialsMap.MapServices.Add(geMapService);

        }



        /// <summary>
        /// In the event that a Map Service Initialization Fails:
        /// - display a message to the user
        /// - remove the map service element from the Layer List
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="args"></param>
        private void MapService_InitializeFailure(object sender, EventArgs args)
        {
            ESRI.ArcGIS.Client.Layer lyr = (ESRI.ArcGIS.Client.Layer)sender;

            System.Exception exception = lyr.InitializationFailure;
            string message = exception.Message;
            string innerMsg = exception.InnerException.ToString();
            Trace.TraceError("Unable to add Map Service: " + message + Environment.NewLine + innerMsg);

            MessageBox.Show("Failed to add map service to the map. Please try again. If it still does not work contact technical support.", "Add Map Service", MessageBoxButton.OK);

            List<MapService> lstMapService = new List<MapService>();

            foreach (MapService ms in _site.EssentialsMap.MapServices)
            {
                if (ms.ServiceLayer.InitializationFailure != null)
                {
                    lstMapService.Add(ms);
                }
            }

            foreach (MapService ms in lstMapService)
            {
                _site.EssentialsMap.MapServices.Remove(ms);
            }
        }

    }
}
