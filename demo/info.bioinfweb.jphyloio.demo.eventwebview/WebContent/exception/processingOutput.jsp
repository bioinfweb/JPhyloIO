<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<jsp:include page="/start.jsp" />
<h2>Exception when processing the file</h2>
<p>The following exception occurred when trying to read the specified file. This is mostly caused by an invalid input file. If you think it is caused by an
		actual bug in <i>JPhyloIO</i>, please file a <a href="http://bioinfweb.info/JPhyloIO/Bugs">bug report</a> with a description of the problem.</p>

<c:forEach items="${causes}" var="cause">
	<p>${cause.message}</p>
	<pre>${cause.stackTrace}</pre>
</c:forEach>

<jsp:include page="/end.jsp" />