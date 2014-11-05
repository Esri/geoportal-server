using System;
using System.Net;
using System.Runtime.Serialization.Json;
using System.IO;
using System.Collections.Generic;
using System.Text;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    public delegate void GetLayersInfoEventHandler(object sender, GetLayersInfoEventArgs e);

    public class MapServiceRESTMgr
    {
        private string _mapServiceUrl = string.Empty;
        private string _proxyUrl = string.Empty;
        private List<string> _lstProxiedHosts = null;
        private string _mapServiceRESTUrl = string.Empty;

        public static event GetLayersInfoEventHandler eventGetLayersInfo;

        public MapServiceRESTMgr(string MapServiceUrl)
        {
            _mapServiceUrl = MapServiceUrl;
            _mapServiceRESTUrl = _mapServiceUrl;
        }

        public MapServiceRESTMgr(string MapServiceUrl, string ProxyUrl, List<string> ProxiedHosts)
        {
            _mapServiceUrl = MapServiceUrl;
            _proxyUrl = ProxyUrl;
            _lstProxiedHosts = ProxiedHosts;
            _mapServiceRESTUrl = _mapServiceUrl;

            //Verify if the map service is requires proxying for REST requests
            if (!String.IsNullOrEmpty(_proxyUrl) && (_lstProxiedHosts != null))
            {
                foreach (string host in _lstProxiedHosts)
                {
                    if (_mapServiceUrl.Contains(host))
                    {
                        string sHostURL = string.Empty;

                        _mapServiceRESTUrl = Utilities.GetProxiedMapServicerEndPoint(_proxyUrl, _mapServiceUrl);

                        break;
                    }
                }
            }
        }

        public void GetLayersInfo()
        {
            if (String.IsNullOrEmpty(_mapServiceRESTUrl)) return;

            //Append "Layers" request in JSON format
            string sEndPointLayers = _mapServiceRESTUrl + Constants.cLayers + Constants.cJSONFormat;

            WebClient client = new WebClient();
            client.DownloadStringCompleted += (s, e) =>
            {
                using (MemoryStream ms = new MemoryStream(Encoding.Unicode.GetBytes(e.Result)))
                {

                    DataContractJsonSerializer serializer = new DataContractJsonSerializer(typeof(LayersInfo));

                    LayersInfo lyrsInfo = (LayersInfo)serializer.ReadObject(ms);

                    GetLayersInfoEventArgs arg = new GetLayersInfoEventArgs();

                    arg.LayersInfo = lyrsInfo;
                    arg.Error = "";
                    eventGetLayersInfo(null, arg);
                }
            };

            client.DownloadStringAsync(new Uri(sEndPointLayers));
        }

    }
}
