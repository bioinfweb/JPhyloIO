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
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventWriter;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;



public class PhyloXMLEventWriter extends AbstractXMLEventWriter implements PhyloXMLConstants {
	public PhyloXMLEventWriter() {
		super();
	}
	

	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.PHYLOXML_FORMAT_ID;
	}

	
	@Override
	protected void doWriteDocument() throws IOException, XMLStreamException {
		getXMLWriter().writeStartElement(TAG_ROOT.getLocalPart());		
		
		getXMLWriter().writeDefaultNamespace(NAMESPACE_URI);
		getXMLWriter().writeNamespace("xsi", NAMESPACE_URI_XSI);
//		TODO write namespaces collected in document

		writePhylogenyTags();
		
		//TODO write document metadata
		
		getXMLWriter().writeEndElement();
	}
	
	
	private void writePhylogenyTags() throws XMLStreamException {
		Iterator<TreeNetworkGroupDataAdapter> treeNetworkGroupIterator = getDocument().getTreeNetworkGroupIterator();		
		while (treeNetworkGroupIterator.hasNext()) {
			TreeNetworkGroupDataAdapter treeNetworkGroup = treeNetworkGroupIterator.next();
			
			Iterator<TreeNetworkDataAdapter> treeNetworkIterator = treeNetworkGroup.getTreeNetworkIterator();
			while (treeNetworkIterator.hasNext()) {
				TreeNetworkDataAdapter tree = treeNetworkIterator.next();

				if (tree.isTree()) {
					writePhylogenyTag(tree);
				}
				else { //TODO can networks be written using the CladeRelation tag?
					getLogger().addWarning("A provided network definition with the ID \"" + tree.getStartEvent().getID() 
							+ "\" was ignored, because the PhyloXML format only supports trees.");
				}
			}	
		}
	}
	
	
	private void writePhylogenyTag(TreeNetworkDataAdapter tree) throws XMLStreamException {
		LabeledIDEvent startEvent = tree.getStartEvent();
		Iterator<String> rootEdgeIterator = tree.getRootEdgeIDs();
		boolean rooted = rootEdgeIterator.hasNext();
		getXMLWriter().writeStartElement(TAG_PHYLOGENY.getLocalPart());
		getXMLWriter().writeAttribute(ATTR_ROOTED.getLocalPart(), Boolean.toString(rooted));
		getXMLWriter().writeAttribute(ATTR_BRANCH_LENGTH_UNIT.getLocalPart(), "xs:double"); //TODO write value with correct namespace prefix
		
		writeSimpleTag(TAG_NAME.getLocalPart(), startEvent.getLabel());
		writeSimpleTag(TAG_ID.getLocalPart(), startEvent.getID());
		
		if (rooted) {
			String rootEdgeID = rootEdgeIterator.next();
			if (rootEdgeIterator.hasNext()) {
				getLogger().addWarning("A tree definition contains more than one root edge, which is not supported "
						+ "by the PhyloXML format. Only the first root edge will be considered.");
			}
			writeCladeTag(tree, rootEdgeID);
		}
		else {
			getLogger().addWarning("A specified tree does not specify any root edge. (Event unrooted trees need a "
					+ "root edge definition defining the edge to start writing tree to the PhyloXML format.) No "
					+ "tree was written.");
		}
		
		//TODO write meta data
		
		getXMLWriter().writeEndElement();
	}
	
	
	private void writeCladeTag(TreeNetworkDataAdapter tree, String rootEdgeID) throws XMLStreamException {
		String nodeID = tree.getEdgeStartEvent(rootEdgeID).getTargetID();
		
		getXMLWriter().writeStartElement(TAG_CLADE.getLocalPart());
		
		writeSimpleTag(TAG_NAME.getLocalPart(), tree.getNodeStartEvent(nodeID).getLabel());
		writeSimpleTag(TAG_BRANCH_LENGTH.getLocalPart(), Double.toString(tree.getEdgeStartEvent(rootEdgeID).getLength()));
		writeSimpleTag(TAG_NODE_ID.getLocalPart(), nodeID);
		
		//TODO should sequences be written here?
		
		Iterator<String> childEdgeIDIterator = tree.getEdgeIDsFromNode(nodeID);
		
		while (childEdgeIDIterator.hasNext()) {
			writeCladeTag(tree, childEdgeIDIterator.next());
		}
		
//	TODO Write meta data (edge or node content or both?)
		
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