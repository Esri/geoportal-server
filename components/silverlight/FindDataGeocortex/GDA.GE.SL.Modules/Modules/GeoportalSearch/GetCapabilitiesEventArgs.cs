using System;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    public class GetCapabilitiesEvent { }

    public class GetCapabilitiesEventArgs : EventArgs
    {
        public IServiceInfo ServiceInfo { get; set; }
        public string Error { get; set; }
    }
}
