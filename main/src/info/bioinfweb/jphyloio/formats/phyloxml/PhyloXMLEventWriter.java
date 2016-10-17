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


import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.ReadWriteParameterNames;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLCollectMetadataDataReceiver;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLMetaDataReceiver;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLOnlyCustomXMLDataReceiver;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLPropertyMetadataReceiver;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLSpecificPredicatesDataReceiver;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventWriter;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.utils.TreeTopologyExtractor;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;



/**
 * Event writer for the <a href="http://phyloxml.org/">PhyloXML</a> format.
 * <p>
 * This writer supports writing phylogenetic trees and rooted networks. Phylogenetic networks are represented using 
 * the {@code clade_rel} tag in <i>PhyloXML</i>. To be able to write a hierarchical tree structure, the topology is 
 * reconstructed in the tool class {@link TreeTopologyExtractor} from the node and edge lists provided by 
 * {@link TreeNetworkDataAdapter}. This writer does not support writing phylogenetic networks with multiple roots 
 * due to the way topologies are reconstructed from the sequential lists of nodes and edges in <i>JPhyloIO</i>.
 * <p>
 * Meta-events with specific, internally used predicates are translated to the according <i>PhyloXML</i> tags.
 * Since the <i>PhyloXML</i> schema defines a fixed order of tags, only meta-events with certain predicates
 * are allowed in the content of different data elements and they also need to be in a specific order. Otherwise
 * an {@link InconsistentAdapterDataException} will be thrown.
 * In the following the allowed predicates nested under a {@link JPhyloIOEvent} with a certain content type are listed. Which 
 * predicates are allowed to be present in the content of these meta-events results from the information in the
 * <a href="http://www.phyloxml.org/documentation/version_1.10/phyloxml.xsd.html">PhyloXML schema</a> and the 
 * accordingly named predicates in the {ink {@link PhyloXMLConstants}. Some predicates representing tags with more content
 * than just free text (e.g. attributes or nested tags) may only appear in {@link ResourceMetadataEvent}s used to group 
 * literal meta-events representing these contents. Tags with text only content are represented by 
 * {@link LiteralMetadataEvent}s. This information can also be obtained from the <i>PhyloXML</i> schema.
 * <p>
 * Predicates allowed nested under events with {@link EventContentType#TREE} or {@link EventContentType#NETWORK}:
 * {@link PhyloXMLConstants#PREDICATE_PHYLOGENY_ATTR_REROOTABLE}, 
 * {@link PhyloXMLConstants#PREDICATE_PHYLOGENY_ATTR_BRANCH_LENGTH_UNIT}, 
 * {@link PhyloXMLConstants#PREDICATE_PHYLOGENY_ATTR_TYPE}, 
 * {@link PhyloXMLConstants#PREDICATE_PHYLOGENY_DESCRIPTION}, 
 * {@link PhyloXMLConstants#PREDICATE_PHYLOGENY_DATE}, 
 * {@link PhyloXMLConstants#PREDICATE_CONFIDENCE},
 * {@link PhyloXMLConstants#PREDICATE_PROPERTY}
 * <p>
 * Predicates allowed nested under events with {@link EventContentType#EDGE} or {@link EventContentType#ROOT_EDGE}: 
 * {@link PhyloXMLConstants#PREDICATE_CONFIDENCE}, 
 * {@link PhyloXMLConstants#PREDICATE_WIDTH}, 
 * {@link PhyloXMLConstants#PREDICATE_COLOR}
 * <p>
 * Predicates allowed nested under events with {@link EventContentType#NODE}: 
 * {@link PhyloXMLConstants#PREDICATE_NODE_ID}, 
 * {@link PhyloXMLConstants#PREDICATE_TAXONOMY}, 
 * {@link PhyloXMLConstants#PREDICATE_SEQUENCE}, 
 * {@link PhyloXMLConstants#PREDICATE_EVENTS}, 
 * {@link PhyloXMLConstants#PREDICATE_BINARY_CHARACTERS}, 
 * {@link PhyloXMLConstants#PREDICATE_DISTRIBUTION},	
 * {@link PhyloXMLConstants#PREDICATE_DATE}, 
 * {@link PhyloXMLConstants#PREDICATE_REFERENCE}, 
 * {@link PhyloXMLConstants#PREDICATE_PROPERTY}
 * <p>
 * Custom XML can be written nested under {@code clade} and {@code phylogeny} tags if it does not consist of character 
 * data that is not nested under any tags or tags that are already defined in <i>PhyloXML</i>. Metadata with literal 
 * values that belongs to a tree, network, node or edge can be written to {@code property} tags nested under
 * {@code phylogeny} or {@code clade}. Since these can not be nested in each other, the user can define a strategy
 * to deal with nested meta-events with a parameter of the type {@link PhyloXMLMetadataTreatment}. This allows
 * to e.g. write all meta-event values sequentially or ignore any nested metadata.
 * 
 * <h3><a id="parameters"></a>Recognized parameters</h3> 
 * <ul>
 *   <li>{@link ReadWriteParameterNames#KEY_WRITER_INSTANCE}</li>
 *   <li>{@link ReadWriteParameterNames#KEY_LOGGER}</li>
 *   <li>{@link ReadWriteParameterNames#KEY_OBJECT_TRANSLATOR_FACTORY}</li>
 *   <li>{@link ReadWriteParameterNames#KEY_APPLICATION_NAME}</li>
 *   <li>{@link ReadWriteParameterNames#KEY_APPLICATION_VERSION}</li>
 *   <li>{@link ReadWriteParameterNames#KEY_APPLICATION_URL}</li>
 *   <li>{@link ReadWriteParameterNames#KEY_PHYLOXML_METADATA_TREATMENT}</li>
 * </ul>
 * 
 * @author Sarah Wiechers
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class PhyloXMLEventWriter extends AbstractXMLEventWriter<PhyloXMLWriterStreamDataProvider> implements PhyloXMLConstants, PhyloXMLPrivateConstants {
	
	
	public PhyloXMLEventWriter() {
		super();
	}
	

	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.PHYLOXML_FORMAT_ID;
	}
	
	
	@Override
	protected PhyloXMLWriterStreamDataProvider createStreamDataProvider() {
		return new PhyloXMLWriterStreamDataProvider(this);
	}

	
	@Override
	protected void doWriteDocument() throws IOException, XMLStreamException {
		PhyloXMLOnlyCustomXMLDataReceiver receiver = new PhyloXMLOnlyCustomXMLDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.OTHER);
		
		getStreamDataProvider().setNamespacePrefix(XMLReadWriteUtils.XSD_DEFAULT_PRE, XMLConstants.W3C_XML_SCHEMA_NS_URI);  // Ensures that the prefix for this NS is always 'xsd'
		
		checkDocument();
		getStreamDataProvider().setNamespacePrefix(XMLReadWriteUtils.getXSIPrefix(getXMLWriter()), XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
//		getStreamDataProvider().setNamespacePrefix(XMLReadWriteUtils.getRDFPrefix(getXMLWriter()), XMLReadWriteUtils.NAMESPACE_RDF);
		
		getXMLWriter().writeStartElement(TAG_ROOT.getLocalPart());
		
		// Write namespace declarations
		getXMLWriter().writeDefaultNamespace(PHYLOXML_NAMESPACE);
		for (String prefix : getStreamDataProvider().getNamespacePrefixes()) {
			getXMLWriter().writeNamespace(prefix, getXMLWriter().getNamespaceContext().getNamespaceURI(prefix));
		}
		
		getXMLWriter().writeComment(" " + getFileStartInfo(getParameters()) + " ");
		
		if (getStreamDataProvider().isDocumentHasMetadata() || getStreamDataProvider().isDocumentHasPhylogeny()) {			
			writePhylogenyTags();			
			getDocument().writeMetadata(getParameters(), receiver);
		}
		else {
			getXMLWriter().writeStartElement(TAG_PHYLOGENY.getLocalPart());
			getXMLWriter().writeEndElement();
			
			getParameters().getLogger().addWarning("The document did not contain any data that could be written to the file.");
		}
		
		getXMLWriter().writeEndElement();		
	}
	
	
	private void checkDocument() throws IOException {
		PhyloXMLCollectMetadataDataReceiver receiver = new PhyloXMLCollectMetadataDataReceiver(getStreamDataProvider(), getParameters());
		
		getDocument().writeMetadata(getParameters(), receiver);		
		getStreamDataProvider().setDocumentHasMetadata(receiver.hasMetadata());
		
		Iterator<TreeNetworkGroupDataAdapter> treeNetworkGroupIterator = getDocument().getTreeNetworkGroupIterator(getParameters());		
		while (treeNetworkGroupIterator.hasNext()) {
			TreeNetworkGroupDataAdapter treeNetworkGroup = treeNetworkGroupIterator.next();
			
			receiver.resetHasMetadata();
			treeNetworkGroup.writeMetadata(getParameters(), receiver);
			getStreamDataProvider().setTreeGroupHasMetadata(receiver.hasMetadata());
			
			Iterator<TreeNetworkDataAdapter> treeNetworkIterator = treeNetworkGroup.getTreeNetworkIterator(getParameters());
			while (treeNetworkIterator.hasNext()) {
				TreeNetworkDataAdapter tree = treeNetworkIterator.next();					
				tree.writeMetadata(getParameters(), receiver);
				
				getStreamDataProvider().setDocumentHasPhylogeny(true);
				
				Iterator<String> edgeIDIterator = tree.getEdges(getParameters()).getIDIterator(getParameters());
				while (edgeIDIterator.hasNext()) {
					tree.getEdges(getParameters()).writeContentData(getParameters(), receiver, edgeIDIterator.next());
				}
				
				Iterator<String> nodeIDIterator = tree.getNodes(getParameters()).getIDIterator(getParameters());
				while (nodeIDIterator.hasNext()) {
					tree.getNodes(getParameters()).writeContentData(getParameters(), receiver, nodeIDIterator.next());
				}
			}
		}
	}
	
	
	private void writePhylogenyTags() throws XMLStreamException, IOException {		
		Iterator<TreeNetworkGroupDataAdapter> treeNetworkGroupIterator = getDocument().getTreeNetworkGroupIterator(getParameters());		
		while (treeNetworkGroupIterator.hasNext()) {
			TreeNetworkGroupDataAdapter treeNetworkGroup = treeNetworkGroupIterator.next();
			
			Iterator<TreeNetworkDataAdapter> treeNetworkIterator = treeNetworkGroup.getTreeNetworkIterator(getParameters());
			while (treeNetworkIterator.hasNext()) {
				TreeNetworkDataAdapter tree = treeNetworkIterator.next();
				
				writePhylogenyTag(tree);  // Networks are written using the clade_relation element
			}
			
			if (getStreamDataProvider().hasTreeGroupMetadata()) {
				getLogger().addWarning("No metadata for the tree or network group with the ID \"" + treeNetworkGroup.getStartEvent(getParameters()).getID() + 
						"\" was written, because the PhyloXML format does not support this.");
			}
		}
	}
	
	
	private void writePhylogenyTag(TreeNetworkDataAdapter tree) throws XMLStreamException, IOException {
		PhyloXMLMetaDataReceiver receiver = new PhyloXMLSpecificPredicatesDataReceiver(getStreamDataProvider(), getParameters(), 
				PropertyOwner.PHYLOGENY, IDENTIFIER_PHYLOGENY);
		LabeledIDEvent startEvent = tree.getStartEvent(getParameters());
		TreeTopologyExtractor topologyExtractor = new TreeTopologyExtractor(tree, getParameters());
		
		String rootNodeID = topologyExtractor.getPaintStartID();		
		boolean rooted = tree.getNodes(getParameters()).getObjectStartEvent(getParameters(), rootNodeID).isRootNode();
		
		getXMLWriter().writeStartElement(TAG_PHYLOGENY.getLocalPart());
		getXMLWriter().writeAttribute(ATTR_ROOTED.getLocalPart(), Boolean.toString(rooted));
		getXMLWriter().writeAttribute(ATTR_BRANCH_LENGTH_UNIT.getLocalPart(), 
				XMLReadWriteUtils.getXSDPrefix(getXMLWriter()) + XMLUtils.QNAME_SEPARATOR + "double");
		
		writeSimpleTag(TAG_NAME.getLocalPart(), startEvent.getLabel());
		
		// Write ID element
		String phylogenyID = getStreamDataProvider().getPhylogenyID();		
		if (phylogenyID == null) {
			phylogenyID = startEvent.getID();
		}
		
		getXMLWriter().writeStartElement(TAG_ID.getLocalPart());
		
		if (getStreamDataProvider().getPhylogenyIDProvider() != null) {
			getXMLWriter().writeAttribute(ATTR_ID_PROVIDER.getLocalPart(), getStreamDataProvider().getPhylogenyIDProvider());
		}
		
		getXMLWriter().writeCharacters(phylogenyID);
		getXMLWriter().writeEndElement();		
		
		// Write metadata with PhyloXML-specific predicates
		tree.writeMetadata(getParameters(), receiver);
		
		writeCladeTag(tree, topologyExtractor, rootNodeID);  // It is ensured by the TreeTopologyExtractor that the root node ID is not null		
		
		for (String networkEdgeID : topologyExtractor.getNetworkEdgeIDs()) {
			EdgeEvent networkEdgeEvent = tree.getEdges(getParameters()).getObjectStartEvent(getParameters(), networkEdgeID);
			getXMLWriter().writeStartElement(TAG_CLADE_RELATION.getLocalPart());
			getXMLWriter().writeAttribute(ATTR_ID_REF_0.getLocalPart(), networkEdgeEvent.getSourceID());
			getXMLWriter().writeAttribute(ATTR_ID_REF_1.getLocalPart(), networkEdgeEvent.getTargetID());
			getXMLWriter().writeAttribute(ATTR_DISTANCE.getLocalPart(), Double.toString(networkEdgeEvent.getLength()));
			getXMLWriter().writeAttribute(ATTR_TYPE.getLocalPart(), TYPE_NETWORK_EDGE);
		}
		
		// Write property tags from PhyloXML-specific predicates
		receiver = new PhyloXMLPropertyMetadataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.PHYLOGENY);
		tree.writeMetadata(getParameters(), receiver);
		
		// Write general meta data
		receiver = new PhyloXMLMetaDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.PHYLOGENY);	
		tree.writeMetadata(getParameters(), receiver);
		
		// Write custom XML
		receiver = new PhyloXMLOnlyCustomXMLDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.PHYLOGENY);	
		tree.writeMetadata(getParameters(), receiver);
		
		getXMLWriter().writeEndElement();
	}
	
	
	private void writeCladeTag(TreeNetworkDataAdapter tree, TreeTopologyExtractor topologyExtractor, String rootNodeID) throws XMLStreamException, IOException {	
		PhyloXMLMetaDataReceiver nodeReceiver = new PhyloXMLSpecificPredicatesDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.NODE, IDENTIFIER_NODE);
		PhyloXMLMetaDataReceiver edgeReceiver = new PhyloXMLSpecificPredicatesDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.PARENT_BRANCH, IDENTIFIER_EDGE);
		
		NodeEvent rootNode = tree.getNodes(getParameters()).getObjectStartEvent(getParameters(), rootNodeID);
		EdgeEvent afferentEdge = tree.getEdges(getParameters()).getObjectStartEvent(getParameters(), 
				topologyExtractor.getIDToNodeInfoMap().get(rootNodeID).getAfferentBranchID());
		
		getXMLWriter().writeStartElement(TAG_CLADE.getLocalPart());
		
		getXMLWriter().writeAttribute(ATTR_ID_SOURCE.getLocalPart(), rootNodeID);
		
		if (!Double.isNaN(afferentEdge.getLength())) {
			getXMLWriter().writeAttribute(ATTR_BRANCH_LENGTH.getLocalPart(), Double.toString(afferentEdge.getLength()));
		}
		
		writeSimpleTag(TAG_NAME.getLocalPart(), rootNode.getLabel());
		
		// Write PhyloXML-specific metadata
		tree.getEdges(getParameters()).writeContentData(getParameters(), edgeReceiver, afferentEdge.getID());
		tree.getNodes(getParameters()).writeContentData(getParameters(), nodeReceiver, rootNodeID);	
		
		// Write general metadata
		nodeReceiver = new PhyloXMLMetaDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.NODE);
		edgeReceiver = new PhyloXMLMetaDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.PARENT_BRANCH);		
		tree.getNodes(getParameters()).writeContentData(getParameters(), nodeReceiver, rootNodeID);
		tree.getEdges(getParameters()).writeContentData(getParameters(), edgeReceiver, afferentEdge.getID());
		
		// Write subtree
		for (String childID : topologyExtractor.getIDToNodeInfoMap().get(rootNodeID).getChildNodeIDs()) {		
			writeCladeTag(tree, topologyExtractor, childID);
		}
	
		// Write custom XML
		nodeReceiver = new PhyloXMLOnlyCustomXMLDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.NODE);
		edgeReceiver = new PhyloXMLOnlyCustomXMLDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.PARENT_BRANCH);
		tree.getNodes(getParameters()).writeContentData(getParameters(), nodeReceiver, rootNodeID);
		tree.getEdges(getParameters()).writeContentData(getParameters(), edgeReceiver, afferentEdge.getID());
		
		getXMLWriter().writeEndElement();
	}
	
	
	private void writeSimpleTag(String tagName, String characters) throws XMLStreamException {
		if ((characters != null) && !characters.isEmpty()) {
			getXMLWriter().writeStartElement(tagName);
			getXMLWriter().writeCharacters(characters);
			getXMLWriter().writeEndElement();
		}
	}
}