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


import java.util.Queue;
import java.util.Stack;

import javax.xml.stream.events.StartElement;

import info.bioinfweb.jphyloio.StreamDataProvider;
import info.bioinfweb.jphyloio.formats.NodeInfo;



public class XMLStreamDataProvider<R extends AbstractXMLEventReader<? extends XMLStreamDataProvider<R>>>
		extends StreamDataProvider<R> {
	private String parentName;
	private String elementName;
	
	private String treeID;
	private String treeLabel;
	private boolean rooted;
	
	private Stack<Queue<NodeInfo>> passedSubnodes;
	private StartElement metaWithAttributes;
	private NodeInfo currentNodeInfo;
	private String currentParentNodeID;
	
	
	public XMLStreamDataProvider(R eventReader) {
		super(eventReader);
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


	public String getTreeID() {
		return treeID;
	}


	public void setTreeID(String treeID) {
		this.treeID = treeID;
	}


	public String getTreeLabel() {
		return treeLabel;
	}


	public void setTreeLabel(String treeLabel) {
		this.treeLabel = treeLabel;
	}


	public boolean isRooted() {
		return rooted;
	}


	public void setRooted(boolean rooted) {
		this.rooted = rooted;
	}


	public Stack<Queue<NodeInfo>> getPassedSubnodes() {
		return passedSubnodes;
	}


	public void setPassedSubnodes(Stack<Queue<NodeInfo>> passedSubnodes) {
		this.passedSubnodes = passedSubnodes;
	}


	public StartElement getMetaWithAttributes() {
		return metaWithAttributes;
	}


	public void setMetaWithAttributes(StartElement metaWithAttributes) {
		this.metaWithAttributes = metaWithAttributes;
	}


	public NodeInfo getCurrentNodeInfo() {
		return currentNodeInfo;
	}


	public void setCurrentNodeInfo(NodeInfo currentNodeInfo) {
		this.currentNodeInfo = currentNodeInfo;
	}


	public String getCurrentParentNodeID() {
		return currentParentNodeID;
	}


	public void setCurrentParentNodeID(String currentParentNodeID) {
		this.currentParentNodeID = currentParentNodeID;
	}	
}
