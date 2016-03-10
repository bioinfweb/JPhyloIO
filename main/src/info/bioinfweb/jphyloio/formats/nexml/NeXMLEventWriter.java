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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;



public class NeXMLEventWriter extends AbstractEventWriter implements NeXMLConstants {
	private XMLStreamWriter writer;
	private ReadWriteParameterMap parameters;
	private ApplicationLogger logger;
	
	
	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.NEXML_FORMAT_ID;
	}
	
	
	private void writeLabeledIDAttributes(LabeledIDEvent event) throws XMLStreamException {
		writer.writeAttribute(ATTR_ID.getLocalPart(), event.getID());  //TODO Add ID to set to ensure all IDs are unique. (Probably a task that should use resources to be added to the super class.)
		if (event.hasLabel()) {
			writer.writeAttribute(ATTR_LABEL.getLocalPart(), event.getLabel());
		}
	}
	
	
	private void writeLinkedOTUOrOTUsAttributes(LinkedOTUOrOTUsEvent event, QName linkAttribute, boolean forceOTULink) throws XMLStreamException {
		writeLabeledIDAttributes(event);
		if (event.isOTUOrOTUsLinked()) {
			writer.writeAttribute(linkAttribute.getLocalPart(), event.getOTUOrOTUsID());
		}
		else if (forceOTULink) {
			//TODO Link UNDEFINED taxon, if an OTU shall be linked.
		}
		//TODO Linking OTUs is never optional, therefore one OTU (usually the one containing the UDEFINED taxon) should be linked.
	}
	
	
	private void writeOTUTag(LabeledIDEvent otuEvent) throws XMLStreamException {
		writer.writeEmptyElement(TAG_OTU.getLocalPart());
		writeLabeledIDAttributes(otuEvent);
	}
	
	
	private void writeOTUSTag(OTUListDataAdapter otuList) throws XMLStreamException {		
		writer.writeStartElement(TAG_OTUS.getLocalPart());
		writeLabeledIDAttributes(otuList.getListStartEvent());
		
		Iterator<String> otuIDIterator = otuList.getIDIterator();
		while (otuIDIterator.hasNext()) {
			writeOTUTag(otuList.getOTUStartEvent(otuIDIterator.next()));
		}
		
		writer.writeEndElement();
	}
	
	
	private void writeOTUSTags(DocumentDataAdapter document) throws Exception {
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
			throw new IOException("A NeXML file must have at least one OTU list"); //TODO give better exception
			//TODO The generated UNDEFINED taxon may be the only entry. In such cases, no OTU list from the document adapter would be required.
			//TODO In such cases an default OTU list and an UNDEFINED taxon should be created here and stored in property, to be used in writeLinkedOTUOrOTUsAttributes() later. 
			//     (That should not be done, if a completely empty document (e.g. containing nothing or only document metadata) shall be written.
			//     An UNDEFINED taxon will also have to be created, if an OTU list is present, if there is at least one sequence without a linked OTU.
		}
	}
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer, ReadWriteParameterMap parameters) throws Exception {
		this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
		this.parameters = parameters;
		logger = parameters.getLogger();
		
		//TODO Before starting to write, the whole document must be iterated once and screened for 
		//     - all metadata namespaces,
		//     - whether it is empty (whether a default OTU list is needed),
		//     - whether there are sequences without OTU links (whether an UNDEFINED OTU needs to be created).
		
		this.writer.writeStartDocument();
		this.writer.writeStartElement(TAG_ROOT.getLocalPart());
		XMLUtils.writeNamespaceAttr(this.writer, NAMESPACE_URI.toString());  //TODO Link xsd? 
		
		writeOTUSTags(document);
		
		this.writer.writeEndElement();
		this.writer.writeEndDocument();
	}
}
