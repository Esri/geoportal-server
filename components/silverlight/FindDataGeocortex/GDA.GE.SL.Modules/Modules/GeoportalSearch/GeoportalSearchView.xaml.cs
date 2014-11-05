using System;
using System.Windows;
using System.Windows.Controls;
using System.ComponentModel.Composition;
using Microsoft.Practices.Prism.Events;
using Geocortex.EssentialsSilverlightViewer.Infrastructure.Commands;
using Geocortex.EssentialsSilverlightViewer.Infrastructure.UIComponents;
using Geocortex.EssentialsSilverlightViewer.Infrastructure.Events;
using Microsoft.Practices.Prism.Regions;
using Geocortex.Essentials.Client;
using Geocortex.EssentialsSilverlightViewer.Infrastructure.Diagnostics;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    [Export]
    public partial class GeoportalSearchView : UserControl, IPartImportsSatisfiedNotification, IMultiViewContent
    {
        // Importing an IEventAggregator is the primary starting point for publishing and subscribing to events
        [Import]
        public IEventAggregator EventAggregator { get; set; }

        // Used for invoking commands by string name
        [Import]
        public CommandRegistry CommandRegistry { get; set; }

        /// <summary>
        /// Gets or sets the RegionManager.  This is imported by MEF.
        /// </summary>
        /// <value>The region manager adds regions dynamically.</value>
        [Import]
        public IRegionManager RegionManager { get; set; }

        [Import]
        public Site Site { get; set; }

        private const string cViewName = "GeoportalSearchView";

        public GeoportalSearchView()
        {
            InitializeComponent();
            IsSelectable = true;
            Title = "Geoportal Search";
            LargeIconUri = "/Resources/Images/GeoportalSearch.png";
            SmallIconUri = LargeIconUri;
        }
        
        #region IPartImportsSatisfiedNotification Members

        void IPartImportsSatisfiedNotification.OnImportsSatisfied()
        {
            EventAggregator.GetEvent<SiteInitializedEvent>().Subscribe(HandleSiteInitializeEvent);
            EventAggregator.GetEvent<ShowPortalSearchEvent>().Subscribe(HandleShowEvent);
            EventAggregator.GetEvent<ReAddGraphicsEvent>().Subscribe(ReAddGraphicLayersEvent);
        }

        /// <summary>
        /// The View must be hidden when initialized. If it is the last module in the 
        /// Viewer configuration, then it will appear as active when the site loads
        /// </summary>
        /// <param name="args"></param>
        public void HandleSiteInitializeEvent(SiteInitializedEventArgs args)
        {
            ViewCommands.ActivateView.Execute(cViewName);
            ViewCommands.HideView.Execute(cViewName);
        }

        public void HandleShowEvent(ShowPortalSearchEventArgs args)
        {
            ViewCommands.ActivateView.Execute(cViewName);
            ShellCommands.BringToFront.Execute(this);
            ShellCommands.OpenDataFrame.Execute(this);
        }

        #endregion

        #region IMultiViewContent Members

        public bool Busy { get; set; }

        public bool IsSelectable { get; set; }

        public string LargeIconUri { get; set; }

        public int Priority { get; set; }

        public string SmallIconUri { get; set; }

        public System.Collections.ObjectModel.ObservableCollection<SpeedButton> SpeedButtons  { get; set; }

        public string Tooltip { get; set; }
        
        public string Title { get; set; }

        public event System.ComponentModel.PropertyChangedEventHandler PropertyChanged
        {
            add { }
            remove { }
        }

        #endregion

        /// <summary>
        /// This is meant to initialize the SearchWidget control with a reference to the Map object
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void SearchWidget_Loaded(object sender, RoutedEventArgs e)
        {
            SearchWidget.Initialize(Site.Map);
        }

        /// <summary>
        /// Handles the clicking of a hyperlink in the info callout window
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void SearchWidget_AddToMapEvent(object sender, GeoportalWidget.AddServiceEventArgs e)
        {
            if (e != null)
            {
                try
                {
                    //ArcGIS Server Map Services
                    if (e.ServiceType == GeoportalWidget.Common.eServiceType.ags)
                    {
                        AddAGSLayerEventArgs argAddAGS = new AddAGSLayerEventArgs();
                        argAddAGS.URL = e.ServiceURL;

                        EventAggregator.GetEvent<AddAGSLayerEvent>().Publish(argAddAGS);
                    }
                    //WMS Map Services
                    else if (e.ServiceType == GeoportalWidget.Common.eServiceType.wms)
                    {
                        AddWMSEventArgs argAddWMS = new AddWMSEventArgs();
                        argAddWMS.URL = e.ServiceURL;

                        EventAggregator.GetEvent<AddWMSEvent>().Publish(argAddWMS);
                    }
                    else if (e.ServiceType == GeoportalWidget.Common.eServiceType.image)
                    {
                        AddImageLayerEventArgs argAddImage = new AddImageLayerEventArgs();
                        argAddImage.URL = e.ServiceURL;

                        EventAggregator.GetEvent<AddImageLayerEvent>().Publish(argAddImage);
                    }
                }
                catch (Exception ex)
                {
                    Trace.TraceError("SearchWidget_AddToMapEvent: " + ex.Message);
                }

                LayerListCommands.SwitchToLayerView.Execute(this);
            }
        }

        /// <summary>
        /// Removes then adds back the Graphic Layers to the map (Extents and Layer Information Callout)
        /// -- this ensures that they are not hidden behind dynamically added layers
        /// </summary>
        /// <param name="args"></param>
        public void ReAddGraphicLayersEvent(ReAddGraphicsEventArgs args)
        {
            SearchWidget.ReAddGraphicLayers();
        }
    }
}
