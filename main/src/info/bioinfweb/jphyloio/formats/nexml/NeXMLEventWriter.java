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
package info.bioinfweb.jphyloio.formats.nexml;


import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers.NeXMLOTUListContentReceiver;
import info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers.NeXMLSequenceContentReceiver;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventWriter;



public class NeXMLEventWriter extends AbstractXMLEventWriter implements NeXMLConstants {
	private XMLStreamWriter writer;
	private ReadWriteParameterMap parameters;
	private ApplicationLogger logger;
	
	private DocumentInformation documentInformation = new DocumentInformation();
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


	private void writeRowTag(LinkedOTUOrOTUsEvent sequenceEvent, MatrixDataAdapter alignment) throws IOException, XMLStreamException {
		NeXMLSequenceContentReceiver receiver = new NeXMLSequenceContentReceiver(writer, parameters, alignment.containsLongTokens());
		
		writer.writeStartElement(TAG_ROW.getLocalPart());
		streamDataProvider.writeLinkedOTUOrOTUsAttributes(sequenceEvent, TAG_OTU, true);
		
		alignment.writeSequencePartContentData(receiver, sequenceEvent.getID(), 0, alignment.getSequenceLength(sequenceEvent.getID()));
		
		writer.writeEndElement();
	}
	
	
	private void writeCharactersTag(MatrixDataAdapter alignment) throws IOException, XMLStreamException {
		writer.writeStartElement(TAG_CHARACTERS.getLocalPart());
		streamDataProvider.writeLinkedOTUOrOTUsAttributes(alignment.getStartEvent(), TAG_OTUS, true);
		
		//TODO write format tag
		
		writer.writeStartElement(TAG_MATRIX.getLocalPart()); //tag does not have attributes
		
		Iterator<String> sequenceIDIterator = alignment.getSequenceIDIterator();
		while (sequenceIDIterator.hasNext()) {
			writeRowTag(alignment.getSequenceStartEvent(sequenceIDIterator.next()), alignment);
		}		
		
		writer.writeEndElement();
		writer.writeEndElement();
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
	
	
	private void writeOTUSTag(OTUListDataAdapter otuList) throws IOException, XMLStreamException {		
		writer.writeStartElement(TAG_OTUS.getLocalPart());
		streamDataProvider.writeLabeledIDAttributes(otuList.getListStartEvent());
		
		NeXMLOTUListContentReceiver receiver = new NeXMLOTUListContentReceiver(writer, parameters, streamDataProvider);		
		
		Iterator<String> otuIDIterator = otuList.getIDIterator();
		while (otuIDIterator.hasNext()) {
			otuList.writeData(receiver, otuIDIterator.next());
		}
		
		if (otuList.hasMetadata()) {
			otuList.writeMetadata(receiver);
		}
		
		writer.writeEndElement();
	}
	
	
	private void checkOTUSTag(OTUListDataAdapter otuList) {		
//		otuList.writeData(receiver, id); //TODO check OTUS with according receiver		
		
		if (otuList.hasMetadata()) {
//			otuList.writeMetadata(receiver); //TODO check meta data 
		}
	}
	
	
	private void writeOTUSTags(DocumentDataAdapter document) throws IOException, XMLStreamException {
		Iterator<OTUListDataAdapter> otusIterator = document.getOTUListIterator();
		if (otusIterator.hasNext()) {
			writeOTUSTag(otusIterator.next());
			if (otusIterator.hasNext()) {				
				do {
					writeOTUSTag(otusIterator.next());
				}	while (otusIterator.hasNext());
			}
		}
		else {
			throw new JPhyloIOWriterException("A NeXML file must have at least one OTU list");
			//TODO The generated UNDEFINED taxon may be the only entry. In such cases, no OTU list from the document adapter would be required.
			//TODO In such cases a default OTU list and an UNDEFINED taxon should be created here and stored in property, to be used in writeLinkedOTUOrOTUsAttributes() later. 
			//     (That should not be done, if a completely empty document (e.g. containing nothing or only document metadata) shall be written.
			//     An UNDEFINED taxon will also have to be created, if an OTU list is present, if there is at least one sequence without a linked OTU.
		}
	}
	
	
	private void checkOTUSTags(DocumentDataAdapter document) {
		if (document.getOTUListCount() > 0) {
			documentInformation.setHasOTUList(true);
		}
		else {
			documentInformation.setHasOTUList(false);
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
	
	
	private void checkDocument(DocumentDataAdapter document) { //check if document is empty (or contains only meta data)
		if (document.hasMetadata()) {
//			document.writeMetadata(receiver); //TODO check meta data for namespaces with according receiver
		}
		
		if (!document.getOTUListIterator().hasNext() && !document.getMatrixIterator().hasNext() && !document.getTreeNetworkIterator().hasNext()) {
			documentInformation.setEmpty(true);
		}
		else {
			documentInformation.setEmpty(false);
			checkOTUSTags(document);
			//TODO check rest of document
		}
	}
	
	
	@Override
	protected void doWriteDocument(DocumentDataAdapter document, XMLStreamWriter writer, ReadWriteParameterMap parameters)
			throws IOException, XMLStreamException {

		this.writer = writer;  //TODO Move to superclass?
		this.parameters = parameters;  //TODO Move to superclass (also used by NexusEventWriter)?
		logger = parameters.getLogger();	  //TODO Move to superclass (also used by NexusEventWriter)?	
		
		//TODO Before starting to write, the whole document must be iterated once and screened for 
		//     - all metadata namespaces,
		//     - whether it is empty (whether a default OTU list is needed),
		//     - whether there are sequences without OTU links (whether an UNDEFINED OTU needs to be created).
//		checkDocument(document);
		
		this.writer.writeStartElement(TAG_ROOT.getLocalPart());
		XMLUtils.writeNamespaceAttr(this.writer, NAMESPACE_URI.toString());  //TODO Link xsd? 
		
		if (document.hasMetadata()) {
//			document.writeMetadata(receiver); //TODO write according meta data 
		}
		
		writeOTUSTags(document);
		writeCharactersTags(document);
		
		this.writer.writeEndElement();
	}
}
