package com.esri.gpt.wfs
{
  import mx.collections.ArrayCollection;
  import mx.collections.ArrayList;

  public class WFSCapabilities
  {
    
    private var m_wfsFeatureTypes:ArrayList = new ArrayList();
    private var m_wfsDefaultSRS:String;
    
    public function WFSCapabilities(capabilities:String)
    {
      var xml:XML = new XML(capabilities);
      this.readFeatureTypes(xml);
    }
    
    private function readFeatureTypes(xml:XML):void {
      var wfsNS :Namespace = new Namespace( "http://www.opengis.net/wfs" );
      
      for each( var featureXML :XML in xml..wfsNS::FeatureType )
      {
        var title :String = featureXML.wfsNS::Title;
        if( m_wfsDefaultSRS == null )
        {
          m_wfsDefaultSRS = featureXML.wfsNS::DefaultSRS;
        }
        m_wfsFeatureTypes.addItem( title )
      }
    
    
    }

  }
}