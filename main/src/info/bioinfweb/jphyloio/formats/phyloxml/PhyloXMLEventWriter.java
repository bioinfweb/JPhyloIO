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
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLCollectMetadataDataReceiver;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLIgnoreMetadataReceiver;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLMetaDataReceiver;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLPropertyMetadataReceiver;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLSpecificPredicatesDataReceiver;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventWriter;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.utils.TreeTopologyExtractor;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;



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
		PhyloXMLIgnoreMetadataReceiver receiver = new PhyloXMLIgnoreMetadataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.OTHER, 
				false);
		
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
		
		if (getStreamDataProvider().isDocumentHasCustomXML() || getStreamDataProvider().isDocumentHasPhylogeny()) {
			if (getStreamDataProvider().isDocumentHasPhylogeny()) {
				writePhylogenyTags();
			}			
			
			getDocument().writeMetadata(getParameters(), receiver);
			//TODO also write document metadata, that can be represented as XML here as customXML
			if (receiver.hasMetadata()) {
				getParameters().getLogger().addWarning("Encountered document meta data was not written, because this is not supported by the PhyloXML format.");
			}
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
		
		getStreamDataProvider().setDocumentHasCustomXML(receiver.hasCustomXML());
		
		Iterator<TreeNetworkGroupDataAdapter> treeNetworkGroupIterator = getDocument().getTreeNetworkGroupIterator(getParameters());		
		while (treeNetworkGroupIterator.hasNext()) {
			TreeNetworkGroupDataAdapter treeNetworkGroup = treeNetworkGroupIterator.next();			
			treeNetworkGroup.writeMetadata(getParameters(), receiver);
			
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
		PhyloXMLIgnoreMetadataReceiver receiver = new PhyloXMLIgnoreMetadataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.OTHER, 
				true);
		
		Iterator<TreeNetworkGroupDataAdapter> treeNetworkGroupIterator = getDocument().getTreeNetworkGroupIterator(getParameters());		
		while (treeNetworkGroupIterator.hasNext()) {
			TreeNetworkGroupDataAdapter treeNetworkGroup = treeNetworkGroupIterator.next();
			
			Iterator<TreeNetworkDataAdapter> treeNetworkIterator = treeNetworkGroup.getTreeNetworkIterator(getParameters());
			while (treeNetworkIterator.hasNext()) {
				TreeNetworkDataAdapter tree = treeNetworkIterator.next();
				
				writePhylogenyTag(tree);  // Networks are written using the clade_relation element
			}
			
			if (receiver.hasMetadata()) {
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
		
		getXMLWriter().writeEndElement();
	}
	
	
	private void writeCladeTag(TreeNetworkDataAdapter tree, TreeTopologyExtractor topologyExtractor, String rootNodeID) throws XMLStreamException, IOException {	
		PhyloXMLMetaDataReceiver nodeReceiver = new PhyloXMLSpecificPredicatesDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.NODE, IDENTIFIER_CLADE);
		PhyloXMLMetaDataReceiver edgeReceiver = new PhyloXMLSpecificPredicatesDataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.PARENT_BRANCH, IDENTIFIER_CLADE);
		
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
		tree.getNodes(getParameters()).writeContentData(getParameters(), nodeReceiver, rootNodeID);
		tree.getEdges(getParameters()).writeContentData(getParameters(), edgeReceiver, afferentEdge.getID());			
		
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
		nodeReceiver = new PhyloXMLIgnoreMetadataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.NODE, false);
		edgeReceiver = new PhyloXMLIgnoreMetadataReceiver(getStreamDataProvider(), getParameters(), PropertyOwner.PARENT_BRANCH, false);
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