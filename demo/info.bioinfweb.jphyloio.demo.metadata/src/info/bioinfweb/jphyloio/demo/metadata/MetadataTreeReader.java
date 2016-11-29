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
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;
import info.bioinfweb.jphyloio.objecttranslation.InvalidObjectSourceDataException;
import info.bioinfweb.jphyloio.objecttranslation.implementations.ListTranslator;
import info.bioinfweb.jphyloio.utils.JPhyloIOReadingUtils;

import java.io.IOException;
import java.util.List;

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
	private void readStandardTaxonomy(Taxonomy taxonomy) throws IOException {
		JPhyloIOEvent event = reader.next();
		while (reader.hasNextEvent() && !event.getType().getTopologyType().equals(EventTopologyType.END)) {
				// This loop shall stop, if a resource metadata end event is encountered. (Nested end events are already consumed 
				// in the loop, so checking the content type of the end event is unnecessary.)
			
			if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
				if (event.getType().getContentType().equals(EventContentType.LITERAL_META)) { 
					LiteralMetadataEvent literalEvent = event.asLiteralMetadataEvent();
					if (PREDICATE_HAS_SCIENTIFIC_NAME.equals(literalEvent.getPredicate().getURI())) { 
						taxonomy.setScientificName(JPhyloIOReadingUtils.readLiteralMetadataContentAsString(reader));
					}
					else if (PREDICATE_HAS_NCBI_ID.equals(literalEvent.getPredicate().getURI())) {
						taxonomy.setNCBIID(JPhyloIOReadingUtils.readLiteralMetadataContentAsString(reader));
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
	 * Reads the contents of a {@link Taxonomy} metadata object from an <i>JPhyloIO</i> event stream modeling a <i>PhyloXML</i>
	 * document. Since <i>PhyloXML</i> offers specialized <i>XML</i> tags for taxonomy information, events with different 
	 * predicates are produces from such a document.
	 * <p>
	 * Note that in contrast to {@link #readStandardTaxonomy(Taxonomy)} this method is called once for the genus and once for 
	 * the species, due to the structure of <i>PhyloXML</i>.
	 */
	private void readPhyloXMLTaxonomy(Taxonomy taxonomy) throws IOException {
		JPhyloIOEvent event = reader.next();
		while (reader.hasNextEvent() && !event.getType().getTopologyType().equals(EventTopologyType.END)) {
			if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
				
				// Read scientific name:
				if (event.getType().getContentType().equals(EventContentType.LITERAL_META) 
						&& PhyloXMLConstants.PREDICATE_TAXONOMY_SCIENTIFIC_NAME.equals(event.asLiteralMetadataEvent().getPredicate().getURI())) {
					
					taxonomy.setScientificName(JPhyloIOReadingUtils.readLiteralMetadataContentAsString(reader));
				}
				
				// Read NCBI taxonomy ID:
				else if (event.getType().getContentType().equals(EventContentType.RESOURCE_META) 
						&& PhyloXMLConstants.PREDICATE_TAXONOMY_ID.equals(event.asResourceMetadataEvent().getRel().getURI())) {
					
					readPhyloXMLTaxonomyID(taxonomy);
				}
				
				// Skip possible other events:
				else {
					JPhyloIOReadingUtils.reachElementEnd(reader);
				}
			}
			event = reader.next();
		}
	}
	
	
	private void readPhyloXMLTaxonomyID(Taxonomy taxonomy) throws IOException {
		String provider = null;
		String id = null;
		
		JPhyloIOEvent event = reader.next();
		while (reader.hasNextEvent() && !event.getType().getTopologyType().equals(EventTopologyType.END)) {
			if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
				if (event.getType().getContentType().equals(EventContentType.LITERAL_META)) { 
					LiteralMetadataEvent literalEvent = event.asLiteralMetadataEvent();
					if (PhyloXMLConstants.PREDICATE_TAXONOMY_ID_ATTR_PROVIDER.equals(literalEvent.getPredicate().getURI())) {
						provider = JPhyloIOReadingUtils.readLiteralMetadataContentAsString(reader);
					}
					else if (PhyloXMLConstants.PREDICATE_TAXONOMY_ID_VALUE.equals(literalEvent.getPredicate().getURI())) {
						id = JPhyloIOReadingUtils.readLiteralMetadataContentAsString(reader);
					}
					else {
						JPhyloIOReadingUtils.reachElementEnd(reader);
					}
				}
				else {  // Skip possible other event subsequences.
					JPhyloIOReadingUtils.reachElementEnd(reader);
				}
			}
			event = reader.next();
		}
		
		if (PHYLOXML_ID_PROVIDER_NCBI.equals(provider.toLowerCase())) {  // Set the ID only if the provider is really NCBI, since other IDs might also be specified.
			taxonomy.setNCBIID(id);
		}
	}
	
	
	/**
	 * Processes the events nested between a node start and end event.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
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
					case RESOURCE_META:  
						ResourceMetadataEvent resourceEvent = event.asResourceMetadataEvent();
						
						// Handle taxonomy resource metadata event that may be nested under a node:
						// (Such nested metadata can only be read from NeXML. Other formats like Nexus do not model such data.)
						if (PREDICATE_HAS_TAXONOMY.equals(resourceEvent.getRel().getURI())) {
							readStandardTaxonomy(data.getTaxonomy());
						}
						else if (PhyloXMLConstants.PREDICATE_TAXONOMY.equals(resourceEvent.getRel().getURI())) {
							readPhyloXMLTaxonomy(data.getTaxonomy());
						}
						else {  // Skip all nested events and their end event if other (unsupported) resource metadata are nested.
							JPhyloIOReadingUtils.reachElementEnd(reader);
						}
						break;

					case LITERAL_META:
						LiteralMetadataEvent literalEvent = event.asLiteralMetadataEvent();
						
						// Load the list of possibly attached size measurements.
						if (PREDICATE_HAS_SIZE_MEASUREMENTS.equals(literalEvent.getPredicate().getURI()) ||
								PREDICATE_HAS_SIZE_MEASUREMENTS.getLocalPart().equals(literalEvent.getPredicate().getStringRepresentation())) {
								// The first part of the condition is sufficient for formats that store predicates, e.g. NeXML.
								// The second part compares the CURIE's local part and the string representation in addition, to be able to load
								// the data from formats that use string keys instead of RDF-predicates, e.g. Nexus.
							
							
							Object list = JPhyloIOReadingUtils.readLiteralMetadataContentAsObject(reader, Object.class);
							if (list instanceof List) {  // This case is used when reading valid documents of all formats but PhyloXML.
								data.setSizeMeasurements((List<Double>)list);  // If the document is invalid, the list would not necessarily contain only double values. This would have to checked in a real-world application to avoid exceptions.
							}
							else if (list instanceof String) {  // This block is used when reading valid PhyloXML documents.
								// Since PhyloXML does not allow to specify externally defined datatypes in it's property tags, the list needs to
								// be represented as a string there. Therefore JPhyloIO cannot know about the correct object translator and returns
								// the value as a string, that still needs to be parsed.
								
								try {
									data.setSizeMeasurements((List)ListTranslator.parseList((String)list));
								} 
								catch (UnsupportedOperationException | InvalidObjectSourceDataException e) {}
							}
						}
						else {  // Skip all nested events and their end event if other (unsupported) literal metadata are nested.
							JPhyloIOReadingUtils.reachElementEnd(reader);
						}
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
					
					// Load possible support value:
					if (PREDICATE_HAS_SUPPORT.equals(literalEvent.getPredicate().getURI()) ||
							PREDICATE_HAS_SUPPORT.getLocalPart().equals(literalEvent.getPredicate().getStringRepresentation())) {
						
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
