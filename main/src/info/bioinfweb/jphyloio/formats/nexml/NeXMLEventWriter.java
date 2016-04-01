/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St√∂ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.nexml;


import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.UndefinedOTUListDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers.NeXMLCollectCharSetDataReceiver;
import info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers.NeXMLCollectNamespaceReceiver;
import info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers.NeXMLCollectSequenceDataReceiver;
import info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers.NeXMLCollectTokenSetDefinitionDataReceiver;
import info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers.NeXMLMetaDataReceiver;
import info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers.NeXMLSequenceContentReceiver;
import info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers.NeXMlTokenSetEventReceiver;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventWriter;
import info.bioinfweb.jphyloio.tools.NodeEdgeIDLister;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLEventWriter extends AbstractXMLEventWriter implements NeXMLConstants {
	private XMLStreamWriter writer;
	private ReadWriteParameterMap parameters;
	private ApplicationLogger logger;
	
	private NeXMLWriterStreamDataProvider streamDataProvider;
	
	
	public NeXMLEventWriter() {
		super();
		this.streamDataProvider = new NeXMLWriterStreamDataProvider(this);
	}


	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.NEXML_FORMAT_ID;
	}
	
	
	public XMLStreamWriter getWriter() {
		return writer;
	}
	
	
	private void writeTreesTags(DocumentDataAdapter document) throws XMLStreamException, IOException {
		if (!streamDataProvider.getPhylogenyLinkedOtusIDs().isEmpty()) {
			for (String otusID : streamDataProvider.getPhylogenyLinkedOtusIDs()) {
				writeTreesTag(document, otusID);
			}
		}
		else {
			writeTreesTag(document, null);
		}		
	}
	
	
	private void writeTreesTag(DocumentDataAdapter document, String linkedOTUs) throws XMLStreamException, IOException {
		getWriter().writeStartElement(TAG_TREES.getLocalPart());
		streamDataProvider.writeLinkedLabeledIDAttributes(new LinkedLabeledIDEvent(EventContentType.TREE, 
				ReadWriteConstants.DEFAULT_TREE_ID_PREFIX, null, linkedOTUs), TAG_OTUS, true);
		Iterator<TreeNetworkDataAdapter> treesAndNetworksIterator = document.getTreeNetworkIterator(); //TODO direkter Zugriff auf B‰ume per ID w‰re hier praktischer
		while (treesAndNetworksIterator.hasNext()) {
			TreeNetworkDataAdapter treeOrNetwork = treesAndNetworksIterator.next();
			if ((linkedOTUs == null) || treeOrNetwork.getStartEvent().getLinkedID().equals(linkedOTUs)) {
				writeTreeOrNetworkTag(treeOrNetwork);
			}
		}
		getWriter().writeEndElement();
	}
	
	
	private void writeTreeOrNetworkTag(TreeNetworkDataAdapter treeOrNetwork) throws XMLStreamException, IOException {
		NeXMLMetaDataReceiver receiver = new NeXMLMetaDataReceiver(writer, parameters, streamDataProvider);
		
		if (treeOrNetwork.isTree()) {
			getWriter().writeStartElement(TAG_TREE.getLocalPart());
			streamDataProvider.writeLabeledIDAttributes(treeOrNetwork.getStartEvent());
			getWriter().writeAttribute(ATTR_XSI_TYPE.getLocalPart(), TYPE_FLOAT_TREE); //trees are always written as float trees
		}
		else {
			getWriter().writeStartElement(TAG_NETWORK.getLocalPart());
			streamDataProvider.writeLabeledIDAttributes(treeOrNetwork.getStartEvent());
			getWriter().writeAttribute(ATTR_XSI_TYPE.getLocalPart(), TYPE_FLOAT_NETWORK); //networks are always written as float networks
		}		
		
		if (treeOrNetwork.hasMetadata()) {
			treeOrNetwork.writeMetadata(receiver);
		}
		
		NodeEdgeIDLister lister = new NodeEdgeIDLister(treeOrNetwork);
		
		for (String nodeID : lister.getNodeIDs()) {
			getWriter().writeStartElement(TAG_NODE.getLocalPart());
			streamDataProvider.writeLinkedLabeledIDAttributes(treeOrNetwork.getNodeStartEvent(nodeID), TAG_OTU, false);
			treeOrNetwork.writeNodeContentData(receiver, nodeID);
			getWriter().writeEndElement();
		}
		
		for (String edgeID : lister.getEdgeIDs()) {
			writeEdgeOrRootedgeTag(treeOrNetwork, treeOrNetwork.getEdgeStartEvent(edgeID));
		}
		
		getWriter().writeEndElement();
	}
	
	
	private void writeEdgeOrRootedgeTag(TreeNetworkDataAdapter tree, EdgeEvent edge) throws XMLStreamException, IllegalArgumentException, IOException {
		NeXMLMetaDataReceiver receiver = new NeXMLMetaDataReceiver(writer, parameters, streamDataProvider);
		
		if (edge.isRoot()) {
			getWriter().writeStartElement(TAG_ROOTEDGE.getLocalPart()); //TODO check if tree.isConsideredRooted(), always write a rootedge if thats true?
		}
		else {
			getWriter().writeStartElement(TAG_EDGE.getLocalPart());
			getWriter().writeAttribute(ATTR_SOURCE.getLocalPart(), edge.getSourceID());
		}
		getWriter().writeAttribute(ATTR_TARGET.getLocalPart(), edge.getTargetID());
		streamDataProvider.writeLabeledIDAttributes(edge);
		
		if (edge.getLength() != Double.NaN) {
			getWriter().writeAttribute(ATTR_LENGTH.getLocalPart(), Double.toString(edge.getLength()));
		}
		
		tree.writeEdgeContentData(receiver, edge.getID());
		getWriter().writeEndElement();
	}
	
	
	private void checkTreesAndNetworks(DocumentDataAdapter document) {
		Iterator<TreeNetworkDataAdapter> treesAndNetworksIterator = document.getTreeNetworkIterator();
		while (treesAndNetworksIterator.hasNext()) {
			String linkedOTUs = treesAndNetworksIterator.next().getStartEvent().getLinkedID();
			if (linkedOTUs == null) {
				streamDataProvider.setWriteUndefinedOtuList(true);
			}
			else {
				streamDataProvider.getPhylogenyLinkedOtusIDs().add(linkedOTUs);
			}
		}
	}
	
	
	private void writeCharactersTags(DocumentDataAdapter document) throws IOException, XMLStreamException {
		Iterator<MatrixDataAdapter> matricesIterator = document.getMatrixIterator();
		if (matricesIterator.hasNext()) {
			writeCharactersTag(matricesIterator.next());
			if (matricesIterator.hasNext()) {				
				do {
					writeCharactersTag(matricesIterator.next());
				}	while (matricesIterator.hasNext());
			}
		}
	}
	
	
	private void writeCharactersTag(MatrixDataAdapter alignment) throws IOException, XMLStreamException {
		getWriter().writeStartElement(TAG_CHARACTERS.getLocalPart());
		streamDataProvider.writeLinkedLabeledIDAttributes(alignment.getStartEvent(), TAG_OTUS, true);
		String alignmentType = null;
		
		if (streamDataProvider.isWriteCellsTags()) {
			switch (streamDataProvider.getAlignmentType()) {
				case NUCLEOTIDE:
				case DNA:
					alignmentType = TYPE_DNA_CELLS;
					break;
				case RNA:
					alignmentType = TYPE_RNA_CELLS;
					break;
				case AMINO_ACID:
					alignmentType = TYPE_PROTEIN_CELLS;
					break;
				case CONTINUOUS:
					alignmentType = TYPE_CONTIN_CELLS;
					break;
				case DISCRETE:
				case UNKNOWN:
					alignmentType = TYPE_STANDARD_CELLS;
					break;
				default:
					break;
			}
		}
		else {
			switch (streamDataProvider.getAlignmentType()) {
				case DNA:
					alignmentType = TYPE_DNA_SEQS;
					break;
				case RNA:
					alignmentType = TYPE_RNA_SEQS;
					break;
				case AMINO_ACID:
					alignmentType = TYPE_PROTEIN_SEQS;
					break;
				case CONTINUOUS:
					alignmentType = TYPE_CONTIN_SEQ;
					break;
				case DISCRETE:
				case UNKNOWN:
					alignmentType = TYPE_STANDARD_SEQ;
					break;
				default:
					break;
			}			
		}
		
		getWriter().writeAttribute(ATTR_XSI_TYPE.getLocalPart(), alignmentType);
		
		writeFormatTag(alignment);
		
		getWriter().writeStartElement(TAG_MATRIX.getLocalPart()); //tag does not have any attributes
		
		Iterator<String> sequenceIDIterator = alignment.getSequenceIDIterator();
		while (sequenceIDIterator.hasNext()) {
			writeRowTag(alignment.getSequenceStartEvent(sequenceIDIterator.next()), alignment);
		}		
		
		getWriter().writeEndElement();
		getWriter().writeEndElement();
		
		streamDataProvider.setAlignmentType(null);
	}
	
	
	private void writeFormatTag(MatrixDataAdapter alignment) throws XMLStreamException, IllegalArgumentException, IOException {
		getWriter().writeStartElement(TAG_FORMAT.getLocalPart());
				
		writeTokenSetDefinitions(alignment.getTokenSets()); //only in case of discrete data		
		
		writeCharacterSets(alignment);
		
		getWriter().writeEndElement();
	}
	
	
	private void writeTokenSetDefinitions(ObjectListDataAdapter<TokenSetDefinitionEvent> tokenSetDefinitions) throws XMLStreamException, IllegalArgumentException, IOException {
		Iterator<String> tokenSetDefinitionIDs = tokenSetDefinitions.getIDIterator();
		NeXMlTokenSetEventReceiver receiver = new NeXMlTokenSetEventReceiver(writer, parameters, streamDataProvider);
		
		while (tokenSetDefinitionIDs.hasNext()) {
			String tokenSetID = tokenSetDefinitionIDs.next();
			TokenSetDefinitionEvent startEvent = tokenSetDefinitions.getObjectStartEvent(tokenSetID);
			CharacterStateSetType alignmentType = startEvent.getSetType();
			
			switch (alignmentType) {
				case CONTINUOUS:
					break;
				case NUCLEOTIDE:
				case DNA:
					//TODO write standardized states tag for DNA
					break;
				case RNA:
					//TODO write standardized states tag for DNA
					break;
				case AMINO_ACID:
					//TODO write standardized states tag for DNA
					break;
				default: //discrete data
					getWriter().writeStartElement(TAG_STATES.getLocalPart());
					streamDataProvider.writeLabeledIDAttributes(startEvent);
					tokenSetDefinitions.writeContentData(receiver, tokenSetID);
					getWriter().writeEndElement();
				//TODO how should Restriction data token sets be handled here?			
			}
		}
	}
	

	private void writeCharacterSets(MatrixDataAdapter alignment) throws IllegalArgumentException, XMLStreamException, IOException {
		Iterator<String> characterSetIDs = alignment.getCharacterSets().getIDIterator();
		NeXMLMetaDataReceiver receiver = new NeXMLMetaDataReceiver(writer, parameters, streamDataProvider);
		
		for (long i = 0; i < alignment.getColumnCount(); i++) {
			String charID = streamDataProvider.getCharIndexToIDMap().get(i);
			
			getWriter().writeStartElement(TAG_CHAR.getLocalPart());
			getWriter().writeAttribute(ATTR_ID.getLocalPart(), charID);
			getWriter().writeAttribute(ATTR_STATES.getLocalPart(), streamDataProvider.getCharIDToStatesMap().get(charID));
			//TODO write MetaData
			getWriter().writeEndElement();
		}
		
		while (characterSetIDs.hasNext()) {
			String charSetID = characterSetIDs.next();			
			
			getWriter().writeStartElement(TAG_SET.getLocalPart());
			
			StringBuffer value = new StringBuffer();			
			for (long columnIndex : streamDataProvider.getCharSets().get(charSetID)) {
				value.append(streamDataProvider.getCharIndexToIDMap().get(columnIndex));
				value.append(" ");
			}
			
			getWriter().writeAttribute(ATTR_CHAR.getLocalPart(), value.toString());
			
			streamDataProvider.writeLabeledIDAttributes(alignment.getCharacterSets().getObjectStartEvent(charSetID));			
			alignment.getCharacterSets().writeContentData(receiver, charSetID); //interval events were already handled in checkCharacterSets()
			getWriter().writeEndElement();
		}
	}
	
	
	private void writeRowTag(LinkedLabeledIDEvent sequenceEvent, MatrixDataAdapter alignment) throws IOException, XMLStreamException {
		NeXMLSequenceContentReceiver receiver = new NeXMLSequenceContentReceiver(writer, parameters, alignment.containsLongTokens(), streamDataProvider);
		
		getWriter().writeStartElement(TAG_ROW.getLocalPart());
		streamDataProvider.writeLinkedLabeledIDAttributes(sequenceEvent, TAG_OTU, true);
		
		if (streamDataProvider.isWriteCellsTags()) {			
			alignment.writeSequencePartContentData(receiver, sequenceEvent.getID(), 0, alignment.getSequenceLength(sequenceEvent.getID()));		
		}
		else {
			getWriter().writeStartElement(TAG_SEQ.getLocalPart());
			alignment.writeSequencePartContentData(receiver, sequenceEvent.getID(), 0, alignment.getSequenceLength(sequenceEvent.getID()));
			getWriter().writeEndElement();			
		}
		
		getWriter().writeEndElement();
	}
	
	
	private void checkCharactersTags(DocumentDataAdapter document) throws IllegalArgumentException, IOException {
		Iterator<MatrixDataAdapter> matricesIterator = document.getMatrixIterator();
		
		if (matricesIterator.hasNext()) {
			checkMatrix(matricesIterator.next());
			if (matricesIterator.hasNext()) {				
				do {
					checkMatrix(matricesIterator.next());
				}	while (matricesIterator.hasNext());
			}
		}
	}
	
	
	private void checkMatrix(MatrixDataAdapter alignment) throws IllegalArgumentException, IOException {
		NeXMLCollectSequenceDataReceiver receiver = new NeXMLCollectSequenceDataReceiver(writer, parameters, streamDataProvider);
		
		if (alignment.getStartEvent().getLinkedID() == null) {
			streamDataProvider.setWriteUndefinedOtuList(true);
		}
		
		checkTokenSets(alignment.getTokenSets());
		checkCharacterSets(alignment.getCharacterSets(), alignment.getColumnCount());
		
		Iterator<String> sequenceIDs = alignment.getSequenceIDIterator();
		while (sequenceIDs.hasNext()) {
			String sequenceID = sequenceIDs.next();
			LinkedLabeledIDEvent sequenceStartEvent = alignment.getSequenceStartEvent(sequenceID);
			String linkedOtuID = sequenceStartEvent.getLinkedID();
			
			if ((linkedOtuID == null) || linkedOtuID.isEmpty()) {
				streamDataProvider.setWriteUndefinedOTU(true);
			}
			
			alignment.writeSequencePartContentData(receiver, sequenceID, 0, alignment.getSequenceLength(sequenceStartEvent.getID()));
		}
	}
	
	
	private void checkTokenSets(ObjectListDataAdapter<TokenSetDefinitionEvent> tokenSets) throws IllegalArgumentException, IOException {
		NeXMLCollectTokenSetDefinitionDataReceiver receiver = new NeXMLCollectTokenSetDefinitionDataReceiver(writer, parameters, streamDataProvider);
		Iterator<String> tokenSetDefinitionIDs = tokenSets.getIDIterator();
		
		while (tokenSetDefinitionIDs.hasNext()) {
			String tokenSetID = tokenSetDefinitionIDs.next();
			CharacterStateSetType alignmentType = tokenSets.getObjectStartEvent(tokenSetID).getSetType();
			
			CharacterStateSetType previousType = streamDataProvider.getAlignmentType();
			if ((previousType == null) || previousType.equals(CharacterStateSetType.UNKNOWN)) {
				streamDataProvider.setAlignmentType(alignmentType);
			}
			else {
				if (!previousType.equals(alignmentType)) {
					throw new JPhyloIOWriterException("Different data types were encountered but only character data of one type (e.g DNA or amino acid) can be written to a NeXML characters tag.");
				}
			}
			
			if (tokenSets.getObjectStartEvent(tokenSetID).getCharacterSetID() == null) {
				if (streamDataProvider.getCharSetToTokenSetMap().get("general") == null) {
					streamDataProvider.getCharSetToTokenSetMap().put("general", tokenSetID);
				}
				else {
					throw new JPhyloIOWriterException("More than one token set withot a reference to a chraracter set was encountered.");
				}
			}
			else {
				streamDataProvider.getCharSetToTokenSetMap().put(tokenSets.getObjectStartEvent(tokenSetID).getCharacterSetID(), tokenSetID);
			}
			
			tokenSets.writeContentData(receiver, tokenSetID);
		}
	}
	
	
	private void checkCharacterSets(ObjectListDataAdapter<LabeledIDEvent> charSets, long columnCount) throws IllegalArgumentException, IOException {
		Iterator<String> charSetIDs = charSets.getIDIterator();
		
		while (charSetIDs.hasNext()) {
			String charSetID = charSetIDs.next();
			NeXMLCollectCharSetDataReceiver receiver = new NeXMLCollectCharSetDataReceiver(writer, parameters, streamDataProvider, charSetID);			
			String referencedTokenSetID = streamDataProvider.getCharSetToTokenSetMap().get(charSetID);
			
			if (referencedTokenSetID == null) {
				referencedTokenSetID = streamDataProvider.getCharSetToTokenSetMap().get("general"); //TODO what to do if it is still null?
			}			
			
			streamDataProvider.getCharSets().put(charSetID, new HashSet<Long>());
			charSets.writeContentData(receiver, charSetID);
			
			for (long i = 0; i < columnCount; i++) {
				String charID = TAG_CHAR.getLocalPart() + i;
				streamDataProvider.getCharIndexToIDMap().put(i, charID);				
				streamDataProvider.getCharIDToStatesMap().put(charID, referencedTokenSetID);
			}
			
			streamDataProvider.getCharSets().put(charSetID, new HashSet<Long>());
			charSets.writeContentData(receiver, charSetID);
		}
	}
	
	
	private void writeOTUSTags(DocumentDataAdapter document) throws IOException, XMLStreamException {
		if (!streamDataProvider.isEmptyDocument()) {
			if (streamDataProvider.hasOTUList()) {
				Iterator<OTUListDataAdapter> otusIterator = document.getOTUListIterator();
				if (otusIterator.hasNext()) {
					OTUListDataAdapter otuList = otusIterator.next();
					writeOTUSTag(otuList);
					if (otusIterator.hasNext()) {				
						do {
							writeOTUSTag(otusIterator.next());
						}	while (otusIterator.hasNext());
					}
				}
			}
			else {
					streamDataProvider.setWriteUndefinedOtuList(true);
			}
			
			if (streamDataProvider.isWriteUndefinedOtuList()) {
				UndefinedOTUListDataAdapter undefinedOTUs = new UndefinedOTUListDataAdapter();
				writeOTUSTag(undefinedOTUs);
			}
		}
	}
	
	
	private void writeOTUSTag(OTUListDataAdapter otuList) throws IOException, XMLStreamException {
		NeXMLMetaDataReceiver receiver = new NeXMLMetaDataReceiver(writer, parameters, streamDataProvider);	
		
		streamDataProvider.getOtusIDs().add(otuList.getListStartEvent().getID());
		
		getWriter().writeStartElement(TAG_OTUS.getLocalPart());
		streamDataProvider.writeLabeledIDAttributes(otuList.getListStartEvent());		
		
		if (otuList.hasMetadata()) {
			otuList.writeMetadata(receiver);
		}
		
		Iterator<String> otuIDIterator = otuList.getIDIterator();
		Set<String> otuIDs = new HashSet<String>();
		while (otuIDIterator.hasNext()) {
			String otuID = otuIDIterator.next();
			streamDataProvider.getOtuIDs().add(otuID);
			otuIDs.add(otuID);
			
			getWriter().writeStartElement(TAG_OTU.getLocalPart()); //TODO possibly check if there is meta data to follow and write empty element if not
			streamDataProvider.writeLabeledIDAttributes(otuList.getObjectStartEvent(otuID));
			otuList.writeContentData(receiver, otuID);
			getWriter().writeEndElement();
		}
		
		if (streamDataProvider.isWriteUndefinedOTU() && (!otuIDs.contains(UndefinedOTUListDataAdapter.UNDEFINED_OTU_ID))) {
			streamDataProvider.getOtuIDs().add(UndefinedOTUListDataAdapter.UNDEFINED_OTU_ID);
			
			getWriter().writeEmptyElement(TAG_OTU.getLocalPart());
			streamDataProvider.writeLabeledIDAttributes(new LabeledIDEvent(EventContentType.OTU, UndefinedOTUListDataAdapter.UNDEFINED_OTU_ID, "undefined taxon"));
		}
		
		getWriter().writeEndElement();
	}
	
	
	private void checkOTUSTags(DocumentDataAdapter document) throws IOException {
		if (document.getOTUListCount() == 0) {
			streamDataProvider.setHasOTUList(false);
		}
		
		Iterator<OTUListDataAdapter> otusIterator = document.getOTUListIterator();
		if (otusIterator.hasNext()) {
			checkOTUSTag(otusIterator.next());
			if (otusIterator.hasNext()) {				
				do {
					checkOTUSTag(otusIterator.next());
				}	while (otusIterator.hasNext());
			}
		}
	}
	
	
	private void checkOTUSTag(OTUListDataAdapter otuList) throws IOException {
		if (otuList.hasMetadata()) {
			NeXMLCollectNamespaceReceiver receiver = new NeXMLCollectNamespaceReceiver(writer, parameters, streamDataProvider);
			otuList.writeMetadata(receiver);
		}
	}
	
	
	@Override
	protected void doWriteDocument(DocumentDataAdapter document, XMLStreamWriter writer, ReadWriteParameterMap parameters)
			throws IOException, XMLStreamException {
		this.writer = writer;  //TODO Move to superclass?
		this.parameters = parameters;  //TODO Move to superclass (also used by NexusEventWriter)?
		logger = parameters.getLogger();	  //TODO Move to superclass (also used by NexusEventWriter)?	

		checkDocument(document);
		
		NeXMLMetaDataReceiver receiver = new NeXMLMetaDataReceiver(writer, parameters, streamDataProvider);
		
		getWriter().writeStartElement(TAG_ROOT.getLocalPart());
		//TODO get and write document ID
		XMLUtils.writeNamespaceAttr(getWriter(), NAMESPACE_URI.toString());  //TODO Link xsd? 
		
		if (document.hasMetadata()) {
			document.writeMetadata(receiver);
		}
		
		writeOTUSTags(document);
		writeCharactersTags(document);
		writeTreesTags(document);
		
		getWriter().writeEndElement();
	}
	
	
	private void checkDocument(DocumentDataAdapter document) throws IOException { //check if document is empty (or contains only meta data)
		if (document.hasMetadata()) {
			NeXMLCollectNamespaceReceiver receiver = new NeXMLCollectNamespaceReceiver(writer, parameters, streamDataProvider);
			document.writeMetadata(receiver);
		}
		
		if (!document.getOTUListIterator().hasNext() && !document.getMatrixIterator().hasNext() && !document.getTreeNetworkIterator().hasNext()) {
			streamDataProvider.setEmptyDocument(true);
		}
		else {
			checkOTUSTags(document);
			checkCharactersTags(document);
			checkTreesAndNetworks(document);
		}
	}
}
