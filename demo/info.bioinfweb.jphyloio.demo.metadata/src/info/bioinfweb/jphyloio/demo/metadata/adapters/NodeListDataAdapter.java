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
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.ReadWriteParameterNames;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.demo.metadata.IOConstants;
import info.bioinfweb.jphyloio.demo.metadata.NodeData;
import info.bioinfweb.jphyloio.demo.tree.NodeEdgeListDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;
import info.bioinfweb.jphyloio.utils.JPhyloIOWritingUtils;

import java.io.IOException;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;



/**
 * This class is indirectly inherited from {@link NodeEdgeListDataAdapter} in the tree demo application and adds
 * functionality to write node metadata (the taxonomy information).
 * 
 * @author Ben St&ouml;ver
 */
public class NodeListDataAdapter extends NodeEdgeListDataAdapter<NodeEvent> 
		implements IOConstants, W3CXSConstants, ReadWriteConstants, ReadWriteParameterNames {
	
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
		
		// Write taxonomy information:
		if ((data.getTaxonomy() != null) && !data.getTaxonomy().isEmpty()) {
			
			// In order to use the special phyloXML tags, according predicates need to be used:
			if (parameters.getObject(KEY_WRITER_INSTANCE, null, JPhyloIOEventWriter.class).getFormatID().equals(
					JPhyloIOFormatIDs.PHYLOXML_FORMAT_ID)) {
					// Each writer stores a reference to itself into it's parameter map, so it can be accessed by data adapters like this one.
				
		    receiver.add(new ResourceMetadataEvent(id + DEFAULT_META_ID_PREFIX + "Tax1", null, 
		    		new URIOrStringIdentifier(null, PhyloXMLConstants.PREDICATE_TAXONOMY), null, null));
			  
		    // Write NCBI taxonomy ID to JPhyloIO:
		    if ((data.getTaxonomy().getNCBIID() != null) && !data.getTaxonomy().getNCBIID().isEmpty()) {
			    receiver.add(new ResourceMetadataEvent(id + DEFAULT_META_ID_PREFIX + "Tax3", null, 
			    		new URIOrStringIdentifier(null, PhyloXMLConstants.PREDICATE_TAXONOMY_ID), null, null));
			    
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax4", null,
			        PhyloXMLConstants.PREDICATE_TAXONOMY_ID_ATTR_PROVIDER, DATA_TYPE_STRING, PHYLOXML_ID_PROVIDER_NCBI, null);
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax5", null,
			        PhyloXMLConstants.PREDICATE_TAXONOMY_ID_VALUE, DATA_TYPE_STRING, data.getTaxonomy().getNCBIID(), null);
			    
			    receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));  // Terminate the taxonomy ID resource metadata element.
		    }

		    // Write scientific name to JPhyloIO:
		    if ((data.getTaxonomy().getScientificName() != null) && !data.getTaxonomy().getScientificName().isEmpty()) {
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax2", null,
			        PhyloXMLConstants.PREDICATE_TAXONOMY_SCIENTIFIC_NAME, DATA_TYPE_STRING, data.getTaxonomy().getScientificName(), null);
		    }
		    
		    // It is important to write the ID before the scientific name, since PhyloXML forces this order in its schema. 
		    // JPhyloIO would throw an exception if the order would be invalid.
			    
		    receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));  // Terminate the taxonomy resource metadata element.
			}
			
			// Use "real" predicates for all other formats:
			else {
		    receiver.add(new ResourceMetadataEvent(id + DEFAULT_META_ID_PREFIX + "Tax1", null, 
		    		new URIOrStringIdentifier(null, PREDICATE_HAS_TAXONOMY), null, null));
		    
		    if ((data.getTaxonomy().getNCBIID() != null) && !data.getTaxonomy().getNCBIID().isEmpty()) {
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax2", null,
			        PREDICATE_HAS_NCBI_ID, DATA_TYPE_STRING, data.getTaxonomy().getNCBIID(), null);
		    }
		    
		    if ((data.getTaxonomy().getScientificName() != null) && !data.getTaxonomy().getScientificName().isEmpty()) {
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax3", null,
			        PREDICATE_HAS_SCIENTIFIC_NAME, DATA_TYPE_STRING, data.getTaxonomy().getScientificName(), null);
		    }
		    
		    receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));  // Terminate the taxonomy resource metadata element.
			}
		}
		
    // Write size measurements:
    if ((data.getSizeMeasurements() != null) && !data.getSizeMeasurements().isEmpty()) {
	    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Sizes", null, 
	    		PREDICATE_HAS_SIZE_MEASUREMENTS, DATA_TYPE_SIMPLE_VALUE_LIST, data.getSizeMeasurements());
    }
	}
}
