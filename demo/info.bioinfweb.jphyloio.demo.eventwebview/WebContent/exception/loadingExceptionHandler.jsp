<%@ page language="java" contentType="text/html; charset=UTF-8" isErrorPage="true" pageEncoding="UTF-8"%>
<jsp:include page="/start.jsp" />
<p>The following error occurred when trying to load the file: &quot;${requestScope['javax.servlet.error.exception'].message}&quot;</p>
<jsp:include page="/end.jsp" />