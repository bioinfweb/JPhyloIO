<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%--
 JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
 <http://bioinfweb.info/JPhyloIO>
 
 This file is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This file is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program. If not, see <http://www.gnu.org/licenses/>.
 --%>
<jsp:include page="/start.jsp" />
<h2>Exception when processing the file</h2>
<p>The following exception occurred when trying to read the specified file. This is mostly caused by an invalid input file. If you think it is caused by an
		actual bug in <i>JPhyloIO</i>, please file a <a href="http://bioinfweb.info/JPhyloIO/Bugs">bug report</a> with a description of the problem.</p>

<c:forEach items="${causes}" var="cause">
	<p>${cause.message}</p>
	<pre>${cause.stackTrace}</pre>
</c:forEach>

<jsp:include page="/end.jsp" />