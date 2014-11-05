using Geocortex.EssentialsSilverlightViewer.Infrastructure.Commands;

namespace GDA.GE.SL.Modules.Modules.GeoportalSearch
{
    public static class GeoportalCommands
    {
        static GeoportalCommands()
        {
            // instantiate and set the values of our commands below
            PortalSearchCommand = new CompositeDelegateCommand();
            AddLayer = new CompositeDelegateCommand();
            AddWMSLayer = new CompositeDelegateCommand();
        }

        [NamedCommand("PortalSearchCommand", null)]
        public static CompositeDelegateCommand PortalSearchCommand { get; private set; }

        [NamedCommand("AddLayer", null)]
        public static CompositeDelegateCommand AddLayer { get; private set; }

        [NamedCommand("AddWMSLayer", null)]
        public static CompositeDelegateCommand AddWMSLayer { get; private set; }
    }
}
