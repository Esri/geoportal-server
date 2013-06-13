<%execute(request,response,out);%>
<%!
  protected void execute(HttpServletRequest request, HttpServletResponse response, JspWriter out) throws Exception  {
    try {
      AgpConnection connection = new AgpConnection();
      connection.setHost(request.getParameter("h"));
      connection.setTokenCriteria(new AgpTokenCriteria());
      connection.getTokenCriteria().setCredentials(new AgpCredentials(
              request.getParameter("u"), request.getParameter("p")));
      connection.getTokenCriteria().setReferer(getReferrer());
      AgpDestination destination = new AgpDestination();
      destination.setConnection(connection);

      destination.getConnection().generateToken();
      AgpFetchUsersRequest agpRequest = new AgpFetchUsersRequest();
      AgpFetchUsersRequest.AgpUser user = agpRequest.getSelfUser(destination.getConnection());
      String callback = request.getParameter("callback");
      if (callback!=null) {
        out.write(callback+"(");
      } else {
        out.write("");
      }
      out.write("{\"username\":\""+user.getUsername()+"\",\"fullName\":\""+user.getFullName()+"\", \"access\": \"" +user.getAccess()+ "\", \"role\": \"" +user.getRole()+ "\", \"orgId\": \"" +user.getOrgId()+ "\"}");
      if (callback!=null) {
        out.write(")");
      } else {
        out.write("");
      }
    } catch (Exception ex) {
      
    }
  }
  protected String getReferrer() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException ex) {
      return "";
    }
  }
%>
<%@page import="java.net.UnknownHostException"%>
<%@page import="java.net.InetAddress"%>
<%@page import="com.esri.gpt.agp.client.AgpConnection"%>
<%@page import="com.esri.gpt.agp.sync.AgpDestination"%>
<%@page import="java.util.List"%>
<%@page import="com.esri.gpt.agp.client.AgpFetchUsersRequest"%>
<%@page import="com.esri.gpt.agp.client.AgpCredentials"%>
<%@page import="com.esri.gpt.agp.client.AgpTokenCriteria"%>
<%@page contentType="text/plain" pageEncoding="UTF-8"%>
