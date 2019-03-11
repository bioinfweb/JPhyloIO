<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
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

<div class="event-box">
	<h3><code>${event.type.contentType}.${event.type.topologyType}</code>
			(<a href="http://bioinfweb.info/Code/sventon/repos/JPhyloIO/show/trunk/main/info.bioinfweb.jphyloio.core/src/${fn:replace(event['class'].name, '.', '/')}.java?revision=HEAD"
			>${event['class'].name}</a>)</h3>
			
	<c:if test="${fn:length(properties) > 0}">
		<h4>Properties:</h4>
		<ul>
			<c:forEach items="${properties}" var="property">
				<li>${property.name}: ${property.value}</li>
			</c:forEach>
		</ul>
	</c:if>
</div>