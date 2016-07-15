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


import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.phyloxml.receivers.PhyloXMLMetaDataReceiver;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventWriter;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.utils.TreeTopologyExtractor;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;



public class PhyloXMLEventWriter extends AbstractXMLEventWriter implements PhyloXMLConstants {
	private PhyloXMLWriterStreamDataProvider streamDataProvider; //TODO move this property to superclass
	
	
	public PhyloXMLEventWriter() {
		super();
	}
	

	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.PHYLOXML_FORMAT_ID;
	}

	
	@Override
	protected void doWriteDocument() throws IOException, XMLStreamException {
		this.streamDataProvider = new PhyloXMLWriterStreamDataProvider(this);
		
		PhyloXMLMetaDataReceiver receiver = new PhyloXMLMetaDataReceiver(getXMLWriter(), getParameters(), streamDataProvider, PropertyOwner.OTHER);
		getXMLWriter().writeStartElement(TAG_ROOT.getLocalPart());		
		
		getXMLWriter().writeDefaultNamespace(PHYLOXML_NAMESPACE);
		getXMLWriter().writeNamespace(XMLReadWriteUtils.XSI_DEFAULT_PRE, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
//		TODO write namespaces collected in document

		writePhylogenyTags();
		
		getDocument().writeMetadata(getParameters(), receiver); //TODO only write custom XML here, warning if other metadata is encountered
		
		getXMLWriter().writeEndElement();
	}
	
	
	private void writePhylogenyTags() throws XMLStreamException, IOException {
		Iterator<TreeNetworkGroupDataAdapter> treeNetworkGroupIterator = getDocument().getTreeNetworkGroupIterator(getParameters());		
		while (treeNetworkGroupIterator.hasNext()) {
			TreeNetworkGroupDataAdapter treeNetworkGroup = treeNetworkGroupIterator.next();
			
			Iterator<TreeNetworkDataAdapter> treeNetworkIterator = treeNetworkGroup.getTreeNetworkIterator(getParameters());
			while (treeNetworkIterator.hasNext()) {
				TreeNetworkDataAdapter tree = treeNetworkIterator.next();

				if (tree.isTree(getParameters())) {
					writePhylogenyTag(tree);
				}
				else { //TODO can networks be written using the CladeRelation tag?
					getLogger().addWarning("A provided network definition with the ID \"" + tree.getStartEvent(getParameters()).getID() 
							+ "\" was ignored, because the PhyloXML format only supports trees.");
				}
			}
			
			//TODO Use receiver to log ignored metadata
//			if (treeNetworkGroup.getMetadataAdapter() != null) {
//				getLogger().addWarning("No metadata for the tree or network group with the ID \"" + treeNetworkGroup.getStartEvent().getID() + 
//						"\" was written, because the PhyloXML format does not support this.");
//			}
		}
	}
	
	
	private void writePhylogenyTag(TreeNetworkDataAdapter tree) throws XMLStreamException, IOException {
		PhyloXMLMetaDataReceiver receiver = new PhyloXMLMetaDataReceiver(getXMLWriter(), getParameters(), streamDataProvider, PropertyOwner.PHYLOGENY);		
		LabeledIDEvent startEvent = tree.getStartEvent(getParameters());
		TreeTopologyExtractor topologyExtractor = new TreeTopologyExtractor(tree, getParameters());
		
		String rootNodeID = topologyExtractor.getPaintStartID();		
		boolean rooted = tree.getNodes(getParameters()).getObjectStartEvent(getParameters(), rootNodeID).isRootNode();
		
		getXMLWriter().writeStartElement(TAG_PHYLOGENY.getLocalPart());
		getXMLWriter().writeAttribute(ATTR_ROOTED.getLocalPart(), Boolean.toString(rooted));
		getXMLWriter().writeAttribute(ATTR_BRANCH_LENGTH_UNIT.getLocalPart(), "xs:double"); //TODO write value with correct namespace prefix
		
		writeSimpleTag(TAG_NAME.getLocalPart(), startEvent.getLabel());
		writeSimpleTag(TAG_ID.getLocalPart(), startEvent.getID());
		
		if (rootNodeID != null) { // TODO should already be checked in the utils class
			writeCladeTag(tree, topologyExtractor, rootNodeID);
		}
		else {
			getLogger().addWarning("A specified tree does not specify any root edge. (Event unrooted trees need a "
					+ "root edge definition defining the edge to start writing tree to the PhyloXML format.) No "
					+ "tree was written.");
		}

		tree.writeMetadata(getParameters(), receiver);
		
		getXMLWriter().writeEndElement();
	}
	
	
	private void writeCladeTag(TreeNetworkDataAdapter tree, TreeTopologyExtractor topologyExtractor, String rootNodeID) throws XMLStreamException, IOException {
		PhyloXMLMetaDataReceiver nodeReceiver = new PhyloXMLMetaDataReceiver(getXMLWriter(), getParameters(), streamDataProvider, PropertyOwner.CLADE);
		PhyloXMLMetaDataReceiver edgeReceiver = new PhyloXMLMetaDataReceiver(getXMLWriter(), getParameters(), streamDataProvider, PropertyOwner.PARENT_BRANCH);
		
		NodeEvent rootNode = tree.getNodes(getParameters()).getObjectStartEvent(getParameters(), rootNodeID);
		EdgeEvent afferentEdge = tree.getEdges(getParameters()).getObjectStartEvent(getParameters(), 
				topologyExtractor.getIDToNodeInfoMap().get(rootNodeID).getAfferentBranchID());
		
		getXMLWriter().writeStartElement(TAG_CLADE.getLocalPart());
		
		writeSimpleTag(TAG_NAME.getLocalPart(), rootNode.getLabel());
		writeSimpleTag(TAG_BRANCH_LENGTH.getLocalPart(), Double.toString(afferentEdge.getLength()));
		writeSimpleTag(TAG_NODE_ID.getLocalPart(), rootNodeID);
		
		tree.getNodes(getParameters()).writeContentData(getParameters(), nodeReceiver, rootNodeID);
		tree.getEdges(getParameters()).writeContentData(getParameters(), edgeReceiver, afferentEdge.getID());
		
		for (String childID : topologyExtractor.getIDToNodeInfoMap().get(rootNodeID).getChildNodeIDs()) {		
			writeCladeTag(tree, topologyExtractor, childID);
		}
		
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