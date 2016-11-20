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
package info.bioinfweb.jphyloio.demo.metadata;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.demo.metadata.NodeData.Taxonomy;
import info.bioinfweb.jphyloio.demo.tree.TreeReader;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.utils.JPhyloIOReadingUtils;

import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;



/**
 * The reader class of this demo application that consumes the stream of <i>JPhyloIO</i> events from an instance
 * of {@link JPhyloIOEventReader} and writes according data in the application model is this demo.
 * <p>
 * It is based on {@link TreeReader} that was implemented and explained in the tree demo project. To read metadata
 * it overwrites the methods {@link #readNodeContents(DefaultMutableTreeNode)} and 
 * {@link #readEdgeContents(DefaultMutableTreeNode)}, which handle the event subsequence modeling the contents of
 * a node or edge. (The inherited implementation just skips over these events and ignored them, since the tree
 * demo does not model metadata.)
 * 
 * @author Ben St&ouml;ver
 */
public class MetadataTreeReader extends info.bioinfweb.jphyloio.demo.tree.TreeReader implements IOConstants {
	/**
	 * Reads the contents of a {@link Taxonomy} metadata object from an <i>JPhyloIO</i> event stream.
	 */
	private void readTaxonomy(Taxonomy taxonomy) throws IOException {
		JPhyloIOEvent event = reader.next();
		while (reader.hasNextEvent() && !event.getType().getTopologyType().equals(EventTopologyType.END)) {
				// This loop shall stop, if a resource metadata end event is encountered. (Nested end events are already consumed 
				// in the loop, so checking the content type of the end event is unnecessary.)
			
			if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
				if (event.getType().getContentType().equals(EventContentType.LITERAL_META)) { 
					LiteralMetadataEvent literalEvent = event.asLiteralMetadataEvent();
					if (PREDICATE_HAS_GENUS.equals(literalEvent.getPredicate().getURI())) { 
						taxonomy.setGenus(JPhyloIOReadingUtils.readLiteralMetadataContentAsString(reader));
					}
					else if (PREDICATE_HAS_SPECIES.equals(literalEvent.getPredicate().getURI())) {
						taxonomy.setSpecies(JPhyloIOReadingUtils.readLiteralMetadataContentAsString(reader));
					}
					else {
						JPhyloIOReadingUtils.reachElementEnd(reader);
					  		// Skip all nested events and the end event if another literal metadata element (with an unsupported 
								// predicate) is nested.
					}
				}
				else {  // Skip possible other event subsequences.
					JPhyloIOReadingUtils.reachElementEnd(reader);
				}
			}
			event = reader.next();
		}
	}
	
	
	/**
	 * Processes the events nested between a node start and end event.
	 */
	@Override
	protected void readNodeContents(DefaultMutableTreeNode node) throws IOException {
		// Replace String user object (inherited from tree demo) by NodeData object:
		NodeData data = new NodeData(node.toString());
		node.setUserObject(data);

		// Read application specific metadata:
		JPhyloIOEvent event = reader.next();
		while (reader.hasNextEvent() && !event.getType().getTopologyType().equals(EventTopologyType.END)) {
				// This loop shall stop, if a node end event is encountered. (Nested end events are already consumed in the loop, so 
				// checking the content type of the end event is unnecessary.)
			
			if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
				switch (event.getType().getContentType()) { 
					case RESOURCE_META:  // Handle taxonomy resource metadata event that may be nested under a node.
						ResourceMetadataEvent resourceEvent = event.asResourceMetadataEvent();
						if (PREDICATE_HAS_TAXONOMY.equals(resourceEvent.getRel().getURI())) {
							readTaxonomy(data.getTaxonomy());
						}
						else {  // Skip all nested events and the end event if other (unsupported) resource metadata are nested.
							JPhyloIOReadingUtils.reachElementEnd(reader);
						}
						break;

					case LITERAL_META:  // This case is only relevant for Nexus and Newick, where to parent taxonomy resource meta-element can be modeled.
						//TODO Process
				  	JPhyloIOReadingUtils.reachElementEnd(reader);
						break;

					default:  // Here possible additional events on the top level are handled.
						JPhyloIOReadingUtils.reachElementEnd(reader);
						break;
				}
			}
			event = reader.next();
		}
	}

	
	/**
	 * Processes the events nested between an edge start and end event.
	 */
	@Override
	protected void readEdgeContents(DefaultMutableTreeNode targetNode) throws IOException {
		// targetNode already has a NodeData instance as a user object, since it has been loaded using the readNodeContents() method above.
		
		// Read application specific metadata:
		JPhyloIOEvent event = reader.next();
		while (reader.hasNextEvent() && !event.getType().getTopologyType().equals(EventTopologyType.END)) {
			if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
				if (event.getType().getContentType().equals(EventContentType.LITERAL_META)) { 
					LiteralMetadataEvent literalEvent = event.asLiteralMetadataEvent();
					if (PREDICATE_HAS_SUPPORT.equals(literalEvent.getPredicate().getURI())) {
						((NodeData)targetNode.getUserObject()).setSupport(
								JPhyloIOReadingUtils.readLiteralMetadataContentAsObject(reader, Number.class).doubleValue());
								// By specifying Nubmer.class instead of Double.class, this method would also work for support values declared 
								// as e.g. Float or Integer.
					}
					else {  // Skip all nested events and the end event if another literal metadata element is nested.
						JPhyloIOReadingUtils.reachElementEnd(reader);
					}
				}
				else {  // Skip possible other event subsequences.
					JPhyloIOReadingUtils.reachElementEnd(reader);
				}
			}
			event = reader.next();
		}
	}
}
