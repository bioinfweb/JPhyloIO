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
		writer.writeAttribute(ATTR_ID.getLocalPart(), event.getID());
		writer.writeAttribute(ATTR_LABEL.getLocalPart(), event.getLabel());
	}
	
	
	private void writeLinkedOTUOrOTUsAttributes(LinkedOTUOrOTUsEvent event, boolean singleOTULinked) throws XMLStreamException {
		writeLabeledIDAttributes(event);
		if (singleOTULinked) {
			writer.writeAttribute(ATTR_OTU.getLocalPart(), event.getOTUOrOTUsID());
		}
		else {
			writer.writeAttribute(ATTR_OTUS.getLocalPart(), event.getOTUOrOTUsID());
		}
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
		}
	}
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer, ReadWriteParameterMap parameters) throws Exception {
		this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
		this.parameters = parameters;
		logger = parameters.getLogger();
		
		this.writer.writeStartDocument();
		this.writer.writeStartElement(TAG_ROOT.getLocalPart());
		XMLUtils.writeNamespaceAttr(this.writer, NAMESPACE_URI.toString());  //TODO Link xsd? 
		
		writeOTUSTags(document);
		
		this.writer.writeEndElement();
		this.writer.writeEndDocument();
	}
}
