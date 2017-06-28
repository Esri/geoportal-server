using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using ESRI.ArcGIS.Framework;
using ESRI.ArcGIS.esriSystem;

namespace com.esri.gpt.csw
{
    public class CswClientButton : ESRI.ArcGIS.Desktop.AddIns.Button
    {
        private static IDockableWindow _DockWindow;

        public CswClientButton()
        {
            UID dockWinID = new UIDClass();
            dockWinID.Value = @"ESRI_CswSearch_CswClientDockableWindow";
            _DockWindow = ArcMap.DockableWindowManager.GetDockableWindow(dockWinID);
        }

        protected override void OnClick()
        {
            //
            //  TODO: Sample code showing how to access button host
            //
            ArcMap.Application.CurrentTool = null;
            if (_DockWindow == null)
                return;

            _DockWindow.Show(!_DockWindow.IsVisible());
        }
        protected override void OnUpdate()
        {
            Enabled = ArcMap.Application != null;
        }
    }

}
