using System;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    public class GetLayersInfoEvent { }

    public class GetLayersInfoEventArgs : EventArgs
    {
        public LayersInfo LayersInfo { get; set; }
        public string Error { get; set; }
    }
}
