<%@ page contentType="text/html;charset=gb2312" %>
<%@ page errorPage="errpage.jsp" %>
<%@ page import="java.util.*" %>
<%@ page import="javax.mail.*" %>
<%@ page import="javax.mail.internet.*" %>
<%@ page import="javax.activation.*" %>
<%@ page import="com.sun.sceri.model.Mail" %> 
<html>
<head>
<title>Send JDIC feedback to owner@jdic.dev.java.net</title>
</head>
<body>

<%
String name = request.getParameter("name");
String email = request.getParameter("email");
String url = request.getParameter("url");
String feedback = request.getParameter("feedback");

// Validate the name and feedback fields.
if ((name == null) || (name == "")){
%>
<jsp:forward page="inputerr.jsp"/>
<%}
if ((feedback == null) || (feedback == "")){
%>
<jsp:forward page="inputerr.jsp"/>
<%}
// Construct the message body by concating the input fields.
String crlfString = "\r\n";
String tabString = "\t";
String messageBody = "";

messageBody = messageBody.concat("This is a feedback on JDIC, sent from https://jdic.dev.java.net/feedback/index.html");
messageBody = messageBody.concat(crlfString);
messageBody = messageBody.concat(crlfString);
        
messageBody = messageBody.concat("Submitter's Name:");
messageBody = messageBody.concat(crlfString);
messageBody = messageBody.concat(tabString);
messageBody = messageBody.concat(name);
messageBody = messageBody.concat(crlfString);
messageBody = messageBody.concat(crlfString);
        
messageBody = messageBody.concat("Submitter's Email:");
messageBody = messageBody.concat(crlfString);
messageBody = messageBody.concat(tabString);
messageBody = messageBody.concat(email);
messageBody = messageBody.concat(crlfString);
messageBody = messageBody.concat(crlfString);

messageBody = messageBody.concat("URL:");
messageBody = messageBody.concat(crlfString);
messageBody = messageBody.concat(tabString);
messageBody = messageBody.concat(url);
messageBody = messageBody.concat(crlfString);
messageBody = messageBody.concat(crlfString);

messageBody = messageBody.concat("Feedback:");
messageBody = messageBody.concat(crlfString);
messageBody = messageBody.concat(tabString);
messageBody = messageBody.concat(feedback);
messageBody = messageBody.concat(crlfString);
messageBody = messageBody.concat(crlfString);

Properties prop = new Properties();

prop.setProperty("contentType", "text/plain");
prop.setProperty("to", name);
prop.setProperty("from", email);

prop.setProperty("subject", "[NEW] JDIC Feedback");

prop.setProperty("body", messageBody);

prop.setProperty("protocol", "smtp");
prop.setProperty("host", "dev.java.net");                     

try{
    Mail m = new Mail(prop);
    m.send();
}catch(Exception e){
    out.println("Sending mail to owner@jdic.dev.java.net failed.");
    e.printStackTrace();
}%> 

<jsp:forward page="sendok.jsp"/>
</table>
</body>
</html>