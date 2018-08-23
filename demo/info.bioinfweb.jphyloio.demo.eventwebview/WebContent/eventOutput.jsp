<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="event-box">
	<ul>
		<li><code><a href="http://bioinfweb.info/Code/sventon/repos/JPhyloIO/show/trunk/main/info.bioinfweb.jphyloio.core/src/${fn:replace(event['class'].name, '.', '/')}.java?revision=HEAD">${event['class'].name}</a></code></li>
		<li><code>${event.type.contentType}</code></li>
		<li><code>${event.type.topologyType}</code></li>
	</ul>
</div>