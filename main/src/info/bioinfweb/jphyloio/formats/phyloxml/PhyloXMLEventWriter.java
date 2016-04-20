/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
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


import java.io.IOException;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.newick.DefaultNewickWriterNodeLabelProcessor;
import info.bioinfweb.jphyloio.formats.newick.NewickStringWriter;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventWriter;



public class PhyloXMLEventWriter extends AbstractXMLEventWriter implements PhyloXMLConstants {
	private XMLStreamWriter writer;
	private ReadWriteParameterMap parameters;
	private ApplicationLogger logger;

	private DocumentDataAdapter document;
	
	
	public PhyloXMLEventWriter() {
		super();
	}
	

	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.PHYLOXML_FORMAT_ID;
	}

	
	public XMLStreamWriter getWriter() {
		return writer;
	}
	

	public ReadWriteParameterMap getParameters() {
		return parameters;
	}
	

	@Override
	protected void doWriteDocument(DocumentDataAdapter document, XMLStreamWriter writer, ReadWriteParameterMap parameters)
			throws IOException, XMLStreamException {
		this.writer = writer; //TODO Move to superclass?
		this.parameters = parameters; //TODO Move to superclass (also used by NexusEventWriter)?
		this.logger = parameters.getLogger(); //TODO Move to superclass (also used by NexusEventWriter)?
		this.document = document;
		
		//TODO why is XML tag always written to the file?
		if (documentHasTree(document)) { //empty documents are not written
			getWriter().writeStartElement(TAG_ROOT.getLocalPart());		
			
			getWriter().writeDefaultNamespace(NAMESPACE_URI);
			getWriter().writeNamespace("xsi", NAMESPACE_URI_XSI);
	//		TODO write namespaces collected in document
			
			writePhylogenyTags();
			
			getWriter().writeEndElement();
		}
		else {
			logger.addWarning("Since no trees were contained in the document information, no file could be written.");
		}
	}
	
	
	protected boolean documentHasTree(DocumentDataAdapter document) {
		Iterator<TreeNetworkGroupDataAdapter> treeGroups = document.getTreeNetworkGroupIterator();
		while (treeGroups.hasNext()) {
			Iterator<TreeNetworkDataAdapter> trees = treeGroups.next().getTreeNetworkIterator();
			if (trees.hasNext()) {
				return true;
			}
		}		
		return false;
	}
	
	
	private void writePhylogenyTags() throws XMLStreamException {
		Iterator<TreeNetworkGroupDataAdapter> treeNetworkGroupIterator = document.getTreeNetworkGroupIterator();		
		while (treeNetworkGroupIterator.hasNext()) {
			TreeNetworkGroupDataAdapter treeNetworkGroup = treeNetworkGroupIterator.next();
			
			Iterator<TreeNetworkDataAdapter> treeNetworkIterator = treeNetworkGroup.getTreeNetworkIterator();
			while (treeNetworkIterator.hasNext()) {
				TreeNetworkDataAdapter tree = treeNetworkIterator.next();

				if (tree.isTree()) { //networks can not be written to PhyloXML
					writePhylogenyTag(tree);
				}
				else { //TODO can networks be written using the CladeRelation tag?
					logger.addWarning("A provided network definition with the ID \"" + tree.getStartEvent().getID() 
							+ "\" was ignored, because the PhyloXML format only supports trees.");
				}
			}	
		}		
	}
	
	
	private void writePhylogenyTag(TreeNetworkDataAdapter tree) throws XMLStreamException {
		LabeledIDEvent startEvent = tree.getStartEvent();
		Iterator<String> rootEdgeIterator = tree.getRootEdgeIDs();
		boolean rooted = rootEdgeIterator.hasNext();
		getWriter().writeStartElement(TAG_PHYLOGENY.getLocalPart());
		getWriter().writeAttribute(ATTR_ROOTED.getLocalPart(), Boolean.toString(rooted));
		getWriter().writeAttribute(ATTR_BRANCH_LENGTH_UNIT.getLocalPart(), "xs:double"); //TODO write value with correct namespace prefix
		
		writeSimpleTag(TAG_NAME.getLocalPart(), startEvent.getLabel());
		writeSimpleTag(TAG_ID.getLocalPart(), startEvent.getID());
		
		if (rooted) {
			String rootEdgeID = rootEdgeIterator.next();
			if (rootEdgeIterator.hasNext()) {
				logger.addWarning("A tree definition contains more than one root edge, which is not supported "
						+ "by the PhyloXML format. Only the first root edge will be considered.");
			}
			writeCladeTag(tree, rootEdgeID);
		}
		else {
			logger.addWarning("A specified tree does not specify any root edge. (Event unrooted trees need a "
					+ "root edge definition defining the edge to start writing tree to the PhyloXML format.) No "
					+ "tree was written.");
		}
		
		//TODO write meta data
		
		getWriter().writeEndElement();
	}
	
	
	private void writeCladeTag(TreeNetworkDataAdapter tree, String rootEdgeID) throws XMLStreamException {
		String nodeID = tree.getEdgeStartEvent(rootEdgeID).getTargetID();
		
		getWriter().writeStartElement(TAG_CLADE.getLocalPart());
		
		writeSimpleTag(TAG_NAME.getLocalPart(), tree.getNodeStartEvent(nodeID).getLabel());
		writeSimpleTag(TAG_BRANCH_LENGTH.getLocalPart(), Double.toString(tree.getEdgeStartEvent(rootEdgeID).getLength()));
		writeSimpleTag(TAG_NODE_ID.getLocalPart(), nodeID);
		
		//TODO should sequences be written here?
		
		Iterator<String> childEdgeIDIterator = tree.getEdgeIDsFromNode(nodeID);
		
		while (childEdgeIDIterator.hasNext()) {
			writeCladeTag(tree, childEdgeIDIterator.next());
		}
		
//	TODO Write meta data (edge or node content or both?)
		
		getWriter().writeEndElement();

	}
	
	
	private void writeSimpleTag(String tagName, String characters) throws XMLStreamException {
		if ((characters != null) && !characters.isEmpty()) {
			getWriter().writeStartElement(tagName);		
			getWriter().writeCharacters(characters);
			getWriter().writeEndElement();
		}
	}
}