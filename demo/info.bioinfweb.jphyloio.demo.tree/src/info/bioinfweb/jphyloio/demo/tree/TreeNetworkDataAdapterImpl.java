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
package info.bioinfweb.jphyloio.demo.tree;


import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyAnnotatedDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyObjectListDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public class TreeNetworkDataAdapterImpl extends EmptyAnnotatedDataAdapter<LabeledIDEvent> implements TreeNetworkDataAdapter {
	public static final String TREE_ID = "tree";
	public static final String NODE_ID_PREFIX = "node";
	public static final String EDGE_ID_PREFIX = "edge";
	
	
	private ObjectListDataAdapter<NodeEvent> nodeList;
	private ObjectListDataAdapter<EdgeEvent> edgeList;
	
	
	public TreeNetworkDataAdapterImpl(DefaultTreeModel model) {
		super();
		
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		addSubtreeToList((TreeNode)model.getRoot(), nodes);
		
		nodeList = new NodeEdgeDataAdapter<NodeEvent>(nodes, NODE_ID_PREFIX) {
			@Override
			protected NodeEvent createEvent(String id, int index, TreeNode node) {
				return new NodeEvent(id, node.toString(), null, node.getParent() == null);
			}
		};
		
		edgeList = new NodeEdgeDataAdapter<EdgeEvent>(nodes, EDGE_ID_PREFIX) {
			@Override
			protected EdgeEvent createEvent(String id, int index, TreeNode node) {
				String sourceID = null;
				if (node.getParent() != null) {
					sourceID = NODE_ID_PREFIX + (index - node.getParent().getIndex(node) - 1);  // The number of the parent node is one less than the first sibling of this node.
				}
				return new EdgeEvent(id, null, sourceID, id.replace(EDGE_ID_PREFIX, NODE_ID_PREFIX), Double.NaN);
			}
		};
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
		// Since this application support only one alignment at a time, a static ID may be used.
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

	
	@SuppressWarnings("unchecked")
	@Override
	public ObjectListDataAdapter<LinkedLabeledIDEvent> getNodeEdgeSets(ReadWriteParameterMap parameters) {
		return EmptyObjectListDataAdapter.SHARED_EMPTY_OBJECT_LIST_ADAPTER;
	}
}
