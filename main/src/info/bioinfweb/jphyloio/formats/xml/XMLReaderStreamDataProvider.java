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

import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.ReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;



public class XMLReaderStreamDataProvider<R extends AbstractXMLEventReader<? extends XMLReaderStreamDataProvider<R>>> extends ReaderStreamDataProvider<R> {	
	private String format;	
	private String parentName;
	private String elementName;
	private String incompleteToken = null;
	
	private Stack<NodeEdgeInfo> sourceNode = new Stack<NodeEdgeInfo>();
	private Stack<NodeEdgeInfo> edgeInfos = new Stack<NodeEdgeInfo>();
	
	private CharacterStateSetType characterSetType;	
	
	
	public XMLReaderStreamDataProvider(R eventReader) {
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
	
	
	public boolean hasIncompleteToken() {
		return incompleteToken != null;
	}


	public String getIncompleteToken() {
		return incompleteToken;
	}


	public void setIncompleteToken(String incompleteToken) {
		this.incompleteToken = incompleteToken;
	}


	public CharacterStateSetType getCharacterSetType() {
		return characterSetType;
	}


	public void setCharacterSetType(CharacterStateSetType characterSetType) {
		this.characterSetType = characterSetType;
	}
	

	public Stack<NodeEdgeInfo> getSourceNode() {
		return sourceNode;
	}


	public Stack<NodeEdgeInfo> getEdgeInfos() {
		return edgeInfos;
	}
}
