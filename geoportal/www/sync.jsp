<%@page import="com.esri.gpt.agp.ags.Ags2AgpCopy"%>
<%@page import="java.net.UnknownHostException"%>
<%@page import="java.net.InetAddress"%>
<%@page import="com.esri.gpt.agp.client.AgpCredentials"%>
<%@page import="com.esri.gpt.agp.client.AgpTokenCriteria"%>
<%@page import="com.esri.gpt.catalog.harvest.protocols.HostContextPair"%>
<%@page import="com.esri.gpt.agp.client.AgpConnection"%>
<%@page import="com.esri.gpt.agp.sync.AgpDestination"%>
<%@page import="com.esri.gpt.control.webharvest.client.arcgis.ArcGISInfo"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
      ArcGISInfo source = getSource();
      AgpDestination destination = getDestination();

      Ags2AgpCopy copy = new Ags2AgpCopy(source, destination);
      
      copy.copy();
%>

<%!
  private ArcGISInfo getSource() {
    /*
    return new ArcGISInfo(
            "http://www.landscape.blm.gov/ArcGIS/rest/services",
            "http://www.landscape.blm.gov/ArcGIS/services/?wsdl",
            "",
            "");
    */
    return new ArcGISInfo(
            "http://www.geocommunicator.gov/ArcGIS/rest/services",
            "http://www.geocommunicator.gov/ArcGIS/services/?wsdl",
            "",
            "");
    
  }
  
  private AgpDestination getDestination() {
    AgpConnection connection = new AgpConnection();
    
    AgpDestination destination = new AgpDestination();
    destination.setConnection(connection);
    
    HostContextPair hcp = HostContextPair.makeHostContextPair("184.72.61.201/arcgis");
    connection.setHost(hcp.getHost());
    connection.setWebContext(hcp.getContext());
    AgpTokenCriteria agpTokenCriteria = new AgpTokenCriteria();
    agpTokenCriteria.setCredentials(new AgpCredentials("piotrandzel", "piotrandzel"));
    agpTokenCriteria.setReferer(getReferrer());
    connection.setTokenCriteria(agpTokenCriteria);
    
    destination.setDestinationOwner("piotrandzel");
//    destination.setDestinationFolderID("367e3b156010478cb0fd219ee8a9b531");
    destination.setDestinationFolderID("943763cf142e4a69adc3dac8dbb5640f");
    
    return destination;
  }

  /**
   * Gets referrer.
   * @return referrer
   */
  private String getReferrer() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException ex) {
      return "";
    }
  }
%>