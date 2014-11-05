using System;
using System.Collections.Generic;
using System.ComponentModel.Composition;
using System.Xml.Linq;
using ESRI.ArcGIS.Client;
using Geocortex.Essentials.Client;
using Geocortex.EssentialsSilverlightViewer.Infrastructure.Configuration;
using Geocortex.EssentialsSilverlightViewer.Infrastructure.Diagnostics;
using Geocortex.EssentialsSilverlightViewer.Infrastructure.Modularity;
using Microsoft.Practices.Prism.Events;
using Microsoft.Practices.Prism.MefExtensions.Modularity;
using Microsoft.Practices.Prism.Commands;
using Geocortex.EssentialsSilverlightViewer.Infrastructure.ServiceDiscovery.Definition;
using Geocortex.Essentials.Client.Ows;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    [ModuleExport(typeof(GeoportalSearchModule))]
    public class GeoportalSearchModule : ViewerModule, IPartImportsSatisfiedNotification
    {
        [Import]
        public IEventAggregator EventAggregator { get; set; }

        [Import]
        public Site Site { get; set; }

        private MapServiceManager _mapserviceMgr = null;
        private string _serviceHost = string.Empty;
        private string _proxy = string.Empty;

        protected override void Initialize(ModuleConfiguration moduleConfiguration)
        {
            base.Initialize(moduleConfiguration);

            //Configuration
            XElement elConfig = XDocument.Parse(moduleConfiguration.XmlConfiguration).Element(Common.ModuleConstants.cConfiguration);

            var ServiceHost = elConfig.Element(Common.ModuleConstants.cServiceHost).Attribute(Common.ModuleConstants.cValue).Value;
            _serviceHost = ServiceHost;

            var Proxy = elConfig.Element(Common.ModuleConstants.cServiceHost).Attribute(Common.ModuleConstants.cProxy).Value;
            _proxy = Proxy;

            //Initialize MapServiceManager object with configured properties
            List<string> lstProxiedHosts = new List<string>();
            lstProxiedHosts.Add(_serviceHost);
            _mapserviceMgr = new MapServiceManager(Site, _proxy, lstProxiedHosts);

            // register our command implementations
            GeoportalCommands.PortalSearchCommand.RegisterCommand(new DelegateCommand(ExecutePortalSearchCommand, CanPortalSearchCommand));
        }

        public void OnImportsSatisfied()
        {
            EventAggregator.GetEvent<AddAGSLayerEvent>().Subscribe(HandleAddAGSEvent);
            EventAggregator.GetEvent<AddWMSEvent>().Subscribe(HandleAddWMSEvent);
            EventAggregator.GetEvent<AddImageLayerEvent>().Subscribe(HandleAddImageEvent);
        }

        /// <summary>
        /// Raised the event to show the Portal Search View form
        /// </summary>
        private void ExecutePortalSearchCommand()
        {
            ShowPortalSearchEventArgs args = new ShowPortalSearchEventArgs()
            {
                ShowPortalSearchEventArgsProperty = ""
            };
            EventAggregator.GetEvent<ShowPortalSearchEvent>().Publish(args);
        }

        private bool CanPortalSearchCommand()
        {
            return true;
        }

        /// <summary>
        /// Handles the initial event for Adding a WMS layer to the map
        /// </summary>
        /// <param name="args"></param>
        public void HandleAddWMSEvent(AddWMSEventArgs args)
        {
            if (args.URL == null || args.URL == "")
            {
                Trace.TraceError("GeoportalSearchModule.HandleAddWMSEvent(): no URL provided");
                return;
            }

            WmsLayer mapservice = new WmsLayer()
            {
                Url = args.URL,
                SkipGetCapabilities = false
            };

            Trace.TraceInfo("Adding WMS Map Service: " + args.URL);
            _mapserviceMgr.AddMapService(mapservice, 0);

            // Re-add the Graphic Layers so that they are on top of newly added layers
            ReAddGraphicsEventArgs argsGraphicLyr = new ReAddGraphicsEventArgs();
            EventAggregator.GetEvent<ReAddGraphicsEvent>().Publish(argsGraphicLyr);
        }
        
        /// <summary>
        /// Handles the event for Adding a ArcGIS Server layer to the map
        /// </summary>
        /// <param name="args"></param>
        public void HandleAddAGSEvent(AddAGSLayerEventArgs args)
        {
            if (args.URL == null || args.URL == "")
            {
                Trace.TraceError("GeoportalSearchModule.HandleAddAGSEvent(): no URL provided");
                return;
            }

            try
            {
                var mapservice = new ArcGISDynamicMapServiceLayer();
                mapservice.Url = args.URL;

                Trace.TraceInfo("Adding AGS Map Service: " + args.URL);
                _mapserviceMgr.AddMapService(mapservice, 0);

                // Re-add the Graphic Layers so that they are on top of newly added layers
                ReAddGraphicsEventArgs argsGraphicLyr = new ReAddGraphicsEventArgs();
                EventAggregator.GetEvent<ReAddGraphicsEvent>().Publish(argsGraphicLyr);
            }
            catch (Exception ex)
            {
                Trace.TraceError("GeoportalSearchModule.HandleAddAGSEvent(): " + ex.Message);
            }
        }

        /// <summary>
        /// Handles the initial event for Adding a Image Service layer to the map
        /// </summary>
        /// <param name="args"></param>
        public void HandleAddImageEvent(AddImageLayerEventArgs args)
        {
            if (args.URL == null || args.URL == "")
                return;

            var imageservice = new ArcGISImageServiceLayer();
            imageservice.Url = args.URL;
            imageservice.ImageFormat = ArcGISImageServiceLayer.ImageServiceImageFormat.PNG8;
            imageservice.NoData = 0;

            imageservice.Initialized += (s, e) =>
            {
                Site.Map.Layers.Add(imageservice);

                dynamic serviceElement = new DynamicElement();
                serviceElement.DisplayName = serviceElement.Name = (s as ArcGISImageServiceLayer).Name;
                serviceElement.ID = Site.EssentialsMap.MapServices.Count;
                serviceElement.MapServiceType = MapServiceType.Dynamic;
                serviceElement.FullExtent = (s as ArcGISImageServiceLayer).FullExtent;

                Site.EssentialsMap.MapServices.Add(MapService.CreateFrom((s as ArcGISImageServiceLayer), serviceElement));
            };

            imageservice.Initialize();
        }

    }
}
