/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.phyloxml;


import java.util.Stack;

import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.xml.XMLStreamDataProvider;



public class PhyloXMLStreamDataProvider extends XMLStreamDataProvider<PhyloXMLEventReader>  {
	private NodeEdgeInfo treeInfo;
	private Stack<NodeEdgeInfo> edges;
	

	public PhyloXMLStreamDataProvider(PhyloXMLEventReader eventReader) {
		super(eventReader);
	}


	public NodeEdgeInfo getTreeInfo() {
		return treeInfo;
	}


	public void setTreeInfo(NodeEdgeInfo treeInfo) {
		this.treeInfo = treeInfo;
	}


	public Stack<NodeEdgeInfo> getEdges() {
		return edges;
	}


	public void setEdges(Stack<NodeEdgeInfo> edges) {
		this.edges = edges;
	}
}
