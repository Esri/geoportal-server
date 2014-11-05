using System.Runtime.Serialization;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    [DataContract]
    public class LayersInfo
    {
        [DataMember(Name = "layers")]
        public RESTLayerInfo[] Layers { get; set; }
    }

    [DataContract]
    public class RESTLayerInfo
    {
        [DataMember(Name = "id")]
        public int Id { get; set; }
        [DataMember(Name = "name")]
        public string Name { get; set; }
        [DataMember(Name = "defaultVisibility")]
        public bool DefaultVisibility { get; set; }
    }
}
