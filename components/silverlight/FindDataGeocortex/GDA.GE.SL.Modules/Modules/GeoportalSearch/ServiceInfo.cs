using System;
using System.Collections.Generic;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    public interface IServiceInfo
    {
        string Title
        {
            get;
            set;
        }

        string Version
        {
            get;
            set;
        }

        string Url
        {
            get;
            set;
        }

        List<ILayerInfo> LayersInfo
        {
            get;
            set;
        }
    }

    public class ServiceInfo : IServiceInfo, IDisposable
    {
        public string _title = string.Empty;
        public string _version = string.Empty;
        public string _url = string.Empty;
        List<ILayerInfo> _layersInfo = null;

        private bool disposed = false;

        //Implement IDisposable.
        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (!disposed)
            {
                if (disposing)
                {
                    // Free other state (managed objects).
                    _layersInfo.Clear();
                }
                // Free your own state (unmanaged objects).
                // Set large fields to null.
                disposed = true;
            }
        }

        // Use C# destructor syntax for finalization code.
        ~ServiceInfo()
        {
            // Simply call Dispose(false).
            Dispose(false);
        }

        public string Title
        {
            get
            {
                return _title;
            }
            set
            {
                _title = value;
            }
        }

        public string Version
        {
            get
            {
                return _version;
            }
            set
            {
                _version = value;
            }
        }

        public string Url
        {
            get
            {
                return _url;
            }
            set
            {
                _url = value;
            }
        }

        public List<ILayerInfo> LayersInfo
        {
            get
            {
                return _layersInfo;
            }
            set
            {
                _layersInfo = value;
            }
        }

    }
}
