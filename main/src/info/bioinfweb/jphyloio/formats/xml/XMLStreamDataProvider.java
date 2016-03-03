/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.formats.xml;


import java.util.Stack;

import javax.xml.stream.XMLEventReader;

import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.jphyloio.StreamDataProvider;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;



public class XMLStreamDataProvider<R extends AbstractXMLEventReader<? extends XMLStreamDataProvider<R>>>
		extends StreamDataProvider<R> {	
	private String format;
	
	private String parentName;
	private String elementName;
	
	private CharacterStateType characterSetType;
	
	
	private Stack<NodeEdgeInfo> sourceNode;
	private NodeEdgeInfo currentNodeEdgeInfo;
	
	
	
	public XMLStreamDataProvider(R eventReader) {
		super(eventReader);
	}
	
	
	public XMLEventReader getXMLReader() {
		return getEventReader().getXMLReader();
	}


	public String getFormat() {
		return format;
	}


	public void setFormat(String format) {
		this.format = format;
	}


	public String getParentName() {
		return parentName;
	}


	public void setParentName(String parentName) {
		this.parentName = parentName;
	}


	public String getElementName() {
		return elementName;
	}


	public void setElementName(String elementName) {
		this.elementName = elementName;
	}


	public CharacterStateType getCharacterSetType() {
		return characterSetType;
	}


	public void setCharacterSetType(CharacterStateType characterSetType) {
		this.characterSetType = characterSetType;
	}
	

	public Stack<NodeEdgeInfo> getSourceNode() {
		return sourceNode;
	}


	public void setSourceNode(Stack<NodeEdgeInfo> sourceNode) {
		this.sourceNode = sourceNode;
	}


	public NodeEdgeInfo getCurrentNodeEdgeInfo() {
		return currentNodeEdgeInfo;
	}


	public void setCurrentNodeEdgeInfo(NodeEdgeInfo currentNodeInfo) {
		this.currentNodeEdgeInfo = currentNodeInfo;
	}
}
