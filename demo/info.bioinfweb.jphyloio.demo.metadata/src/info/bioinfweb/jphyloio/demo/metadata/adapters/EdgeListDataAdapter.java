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


import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.demo.metadata.IOConstants;
import info.bioinfweb.jphyloio.demo.metadata.NodeData;
import info.bioinfweb.jphyloio.demo.tree.NodeEdgeListDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.utils.JPhyloIOWritingUtils;

import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;



/**
 * This class is indirectly inherited from {@link NodeEdgeListDataAdapter} in the tree demo application and adds
 * functionality to write edge metadata (the support value).
 * 
 * @author Ben St&ouml;ver
 */
public class EdgeListDataAdapter extends NodeEdgeListDataAdapter<EdgeEvent> 
		implements IOConstants, W3CXSConstants, ReadWriteConstants {
	
	public EdgeListDataAdapter(List<TreeNode> nodes) {
		super(nodes, EDGE_ID_PREFIX);
	}

	
	@Override
	protected EdgeEvent createEvent(String id, int index, TreeNode node) {
		String sourceID = null;
		if (node.getParent() != null) {
			sourceID = NodeListDataAdapter.NODE_ID_PREFIX + getNodes().indexOf(node.getParent());  // For large trees, a node to index map could be used here instead for performance reasons.
		}
		return new EdgeEvent(id, null, sourceID, id.replace(EDGE_ID_PREFIX, NODE_ID_PREFIX), Double.NaN);
	}

	
	@Override
	protected void writeContentData(ReadWriteParameterMap parameters,	JPhyloIOEventReceiver receiver, String id, int index, 
			TreeNode node) throws IOException, IllegalArgumentException {
		
		// Write support value as simple literal metadata:
		double support = ((NodeData)((DefaultMutableTreeNode)node).getUserObject()).getSupport();
		if (!Double.isNaN(support)) {
	    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Support", null,
	    			PREDICATE_HAS_SUPPORT, DATA_TYPE_DOUBLE, support, null);
	    		// Simple literal metadata of this type will automatically be written to a property tag in PhyloXML.  //TODO Test
		}
	}
}
