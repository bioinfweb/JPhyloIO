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
import javax.xml.namespace.QName;

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
		//TODO Adjust the following to PhyloXML:
		
    // Write taxonomy information:
		if ((data.getTaxonomy() != null) && !data.getTaxonomy().isEmpty()) {
			
			// In order to use the special phyloXML tags, according predicates need to be used:
			if (parameters.getObject(KEY_WRITER_INSTANCE, null, JPhyloIOEventWriter.class).getFormatID().equals(
					JPhyloIOFormatIDs.PHYLOXML_FORMAT_ID)) {
					// Each writer stores a reference to itself into it's parameter map, so it can be accessed by data adapters like this one.
				
				// Write taxonomy tag for genus:
		    if ((data.getTaxonomy().getGenus() != null) && !data.getTaxonomy().getGenus().isEmpty()) {
			    receiver.add(new ResourceMetadataEvent(id + DEFAULT_META_ID_PREFIX + "Tax1", null, 
			    		new URIOrStringIdentifier(null, PhyloXMLConstants.PREDICATE_TAXONOMY), null, null));
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax2", null,
			        PhyloXMLConstants.PREDICATE_TAXONOMY_SCIENTIFIC_NAME, DATA_TYPE_STRING, data.getTaxonomy().getGenus(), null);
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax3", null,
			        PhyloXMLConstants.PREDICATE_TAXONOMY_RANK, DATA_TYPE_STRING, "genus", null);
			    receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));
		    }
		    
		    // Write taxonomy tag for species:
		    if ((data.getTaxonomy().getSpecies() != null) && !data.getTaxonomy().getSpecies().isEmpty()) {
			    receiver.add(new ResourceMetadataEvent(id + DEFAULT_META_ID_PREFIX + "Tax4", null, 
			    		new URIOrStringIdentifier(null, PhyloXMLConstants.PREDICATE_TAXONOMY), null, null));
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax5", null,
			        PhyloXMLConstants.PREDICATE_TAXONOMY_SCIENTIFIC_NAME, DATA_TYPE_STRING, data.getTaxonomy().getSpecies(), null);
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax6", null,
			        PhyloXMLConstants.PREDICATE_TAXONOMY_RANK, DATA_TYPE_STRING, "species", null);
			    receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));
		    }
		    
			}
			
			// Use "real" predicates for all other formats:
			else {
		    receiver.add(new ResourceMetadataEvent(id + DEFAULT_META_ID_PREFIX + "Tax1", null, 
		    		new URIOrStringIdentifier(null, PREDICATE_HAS_TAXONOMY), null, null));
		    
		    if ((data.getTaxonomy().getGenus() != null) && !data.getTaxonomy().getGenus().isEmpty()) {
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax2", null,
			        PREDICATE_HAS_GENUS, DATA_TYPE_STRING, data.getTaxonomy().getGenus(), null);
		    }
		    
		    if ((data.getTaxonomy().getSpecies() != null) && !data.getTaxonomy().getSpecies().isEmpty()) {
			    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Tax3", null,
			        PREDICATE_HAS_SPECIES, DATA_TYPE_STRING, data.getTaxonomy().getSpecies(), null);
		    }
		    
		    receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));
			}
		}
		
    // Write size measurements:
    if ((data.getSizeMeasurements() != null) && !data.getSizeMeasurements().isEmpty()) {
	    JPhyloIOWritingUtils.writeSimpleLiteralMetadata(receiver, id + DEFAULT_META_ID_PREFIX + "Sizes", null, 
	    		PREDICATE_HAS_SIZE_MEASUREMENTS, DATA_TYPE_SIMPLE_VALUE_LIST, data.getSizeMeasurements());
    }
	}
}
