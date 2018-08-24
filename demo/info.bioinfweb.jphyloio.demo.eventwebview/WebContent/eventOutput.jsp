<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="event-box">
	<p><code>${event.type.contentType}.${event.type.topologyType}</code>
			(<a href="http://bioinfweb.info/Code/sventon/repos/JPhyloIO/show/trunk/main/info.bioinfweb.jphyloio.core/src/${fn:replace(event['class'].name, '.', '/')}.java?revision=HEAD"
			>${event['class'].name}</a>)</p>
			
	<c:if test="${fn:length(properties) > 0}">
		<h4>Properties:</h4>
		<ul>
			<c:forEach items="${properties}" var="property">
				<li>${property.name}: <code>${property.value}</code></li>
			</c:forEach>
		</ul>
	</c:if>
</div>