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


import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.demo.metadata.IOConstants;
import info.bioinfweb.jphyloio.demo.metadata.NodeData;
import info.bioinfweb.jphyloio.demo.tree.NodeEdgeListDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.utils.JPhyloIOWritingUtils;



/**
 * This class is indirectly inherited from {@link NodeEdgeListDataAdapter} in the tree demo application and adds
 * functionality to write node metadata (the taxonomy information).
 * 
 * @author Ben St&ouml;ver
 */
public class NodeListDataAdapter extends NodeEdgeListDataAdapter<NodeEvent> 
		implements IOConstants, W3CXSConstants, ReadWriteConstants {
	
	public NodeListDataAdapter(List<TreeNode> nodes) {
		super(nodes, NODE_ID_PREFIX);
	}

	
	@Override
	protected NodeEvent createEvent(String id, int index, TreeNode node) {
		return new NodeEvent(id, node.toString(), null, node.getParent() == null);  // Note that this implementation will always specify a node label, even if none is present, the label will be stored as "" in formats that support it.
	}

	
	@Override
	protected void writeContentData(ReadWriteParameterMap parameters,	JPhyloIOEventReceiver receiver, String id, int index, 
			TreeNode node) throws IOException, IllegalArgumentException {

		NodeData data = (NodeData)((DefaultMutableTreeNode)node).getUserObject();
		//TODO Adjust the following to PhyloXML:
		
    // Write taxonomy information:
		if ((data.getTaxonomy() != null) && !data.getTaxonomy().isEmpty()) {
	    receiver.add(new ResourceMetadataEvent(id + DEFAULT_META_ID_PREFIX + "Tax", null, new URIOrStringIdentifier(null, PREDICATE_HAS_TAXONOMY), null, null));
	    
	    if ((data.getTaxonomy().getGenus() != null) && !data.getTaxonomy().getGenus().isEmpty()) {
		    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Genus", null,
		        PREDICATE_HAS_GENUS, DATA_TYPE_STRING, data.getTaxonomy().getGenus(), null);
	    }
	    
	    if ((data.getTaxonomy().getSpecies() != null) && !data.getTaxonomy().getSpecies().isEmpty()) {
		    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Species", null,
		        PREDICATE_HAS_SPECIES, DATA_TYPE_STRING, data.getTaxonomy().getSpecies(), null);
	    }
	    
	    // Write size measurements:
//	    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Sizes", null, 
//	    		PREDICATE_HAS_SIZE_MEASUREMENTS, originalType, objectValue);
	    //TODO Decide if list data type (to be used with XML formats) should be defined here or in JPhyloIO. 
	    
	    receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));
		}
	}
}
