
namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    public interface ILayerInfo
    {
        string Name
        {
            get;
            set;
        }

        string Title
        {
            get;
            set;
        }

        double MaxScale
        {
            get;
            set;
        }
    }

    public class LayerInfo : ILayerInfo
    {
        public string _name = string.Empty;
        public string _title = string.Empty;
        public double _maxscale = 0.0;

        public string Name
        {
            get
            {
                return _name;
            }
            set
            {
                _name = value;
            }
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

        public double MaxScale
        {
            get
            {
                return _maxscale;
            }
            set
            {
                _maxscale = value;
            }
        }
    }
}
