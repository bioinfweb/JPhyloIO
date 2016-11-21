/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.demo.metadata.adapters;


import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.NoSetsTreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.demo.metadata.IOConstants;
import info.bioinfweb.jphyloio.demo.tree.TreeNetworkDataAdapterImpl;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * The implementation of this class is similar to {@link TreeNetworkDataAdapterImpl} in the tree demo. Unlike that class,
 * this implementation uses {@link NodeListDataAdapter} and {@link EdgeListDataAdapter} internally to write metadata.
 * 
 * @author Ben St&ouml;ver
 */
public class MetadataTreeNetworkDataAdapterImpl extends NoSetsTreeNetworkDataAdapter 
		implements TreeNetworkDataAdapter, IOConstants {
	
	private ObjectListDataAdapter<NodeEvent> nodeList;
	private ObjectListDataAdapter<EdgeEvent> edgeList;
	
	
	public MetadataTreeNetworkDataAdapterImpl(DefaultTreeModel model) {
		super();
		
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		addSubtreeToList((TreeNode)model.getRoot(), nodes);
		
		nodeList = new NodeListDataAdapter(nodes);
		edgeList = new EdgeListDataAdapter(nodes);
	}
	
	
	private void addSubtreeToList(TreeNode root, List<TreeNode> list) {
		list.add(root);
		for (int i = 0; i < root.getChildCount(); i++) {
			addSubtreeToList(root.getChildAt(i), list);
		}
	}
	
	
	@Override
	public LabeledIDEvent getStartEvent(ReadWriteParameterMap parameters) {
		return new LabeledIDEvent(EventContentType.TREE, TREE_ID, null);  
		// Since this application supports only one tree at a time, a static ID may be used.
	}
	

	@Override
	public boolean isTree(ReadWriteParameterMap parameters) {
		return true;
	}

	
	@Override
	public ObjectListDataAdapter<NodeEvent> getNodes(ReadWriteParameterMap parameters) {
		return nodeList;
	}
	

	@Override
	public ObjectListDataAdapter<EdgeEvent> getEdges(ReadWriteParameterMap parameters) {
		return edgeList;
	}
}
