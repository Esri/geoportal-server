using System;
using Microsoft.Practices.Prism.Events;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    public class ShowPortalSearchEvent : CompositePresentationEvent<ShowPortalSearchEventArgs>
    {
    }

    public class ShowPortalSearchEventArgs : EventArgs
    {
        public string ShowPortalSearchEventArgsProperty { get; set; }
    }

    public class AddAGSLayerEvent : CompositePresentationEvent<AddAGSLayerEventArgs>
    {
    }

    public class AddAGSLayerEventArgs : EventArgs
    {
        public string URL { get; set; }
    }

    public class AddWMSEvent : CompositePresentationEvent<AddWMSEventArgs>
    {
    }

    public class AddWMSEventArgs : EventArgs
    {
        public string URL { get; set; }
    }

    public class AddImageLayerEvent : CompositePresentationEvent<AddImageLayerEventArgs>
    {
    }

    public class AddImageLayerEventArgs : EventArgs
    {
        public string URL { get; set; }
    }

    public class ReAddGraphicsEvent : CompositePresentationEvent<ReAddGraphicsEventArgs>
    {
    }

    public class ReAddGraphicsEventArgs : EventArgs
    {
    }
}

