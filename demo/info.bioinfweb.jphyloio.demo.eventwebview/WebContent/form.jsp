<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%--
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
<h3>File from web server</h3>
<form action="EventLister/Output/index.jsp" method="GET">
	<p class="no-column-block" style="
			display: -ms-grid;
			display: grid;
			grid-column-gap: 1rem;
			-ms-grid-columns: min-content 1fr min-content;
			grid-template-columns: min-content 1fr min-content;
			grid-row-gap: 0;
			grid-column-gap: 5px;">
		<label for="sourceURL">URL:</label>
		<input type="text" name="sourceURL" id="sourceURL" />
		<button type="submit" style="white-space: nowrap;">List events</button>
	</p>
</form>

<h3>Upload file</h3>
<form action="EventLister/Output/index.jsp" method="POST" enctype="multipart/form-data">
	<p class="no-column-block">
		<label for="sourceFile">File:</label>
		<input type="file" name="sourceFile" id="sourceFile" />
		<button type="submit" style="white-space: nowrap;">List events</button>
	</p>
</form>

<h3>Paste or type file contents</h3>
<form action="EventLister/Output/index.jsp" method="POST">
	<p class="no-column-block">
		<label for="sourceContent">Content:</label>
		<textarea name="sourceContent" id="sourceContent" wrap="soft" rows="25" style="width: 100%; white-space: pre; overflow-wrap: normal; overflow-x: scroll;"></textarea>
		<button type="submit" style="white-space: nowrap;">List events</button>
	</p>
</form>