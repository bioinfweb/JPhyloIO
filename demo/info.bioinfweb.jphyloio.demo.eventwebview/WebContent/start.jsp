<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%--
  JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 Copyright (C) 2015-2018  Ben Stöver, Sarah Wiechers
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
 --%><!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>JPhyloIO event lister</title>
		<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/style.css" />
	</head>
	<body>
		<h1><i>JPhyloIO</i> event lister</h1>
		<p>Source: <code>${source}</code></p>
		<p>JPhyloIO version: ${jPhyloIOVersion}</p>