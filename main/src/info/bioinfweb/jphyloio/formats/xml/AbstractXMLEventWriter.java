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
package info.bioinfweb.jphyloio.formats.xml;


import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;



public abstract class AbstractXMLEventWriter extends AbstractEventWriter {
	protected abstract void doWriteDocument(DocumentDataAdapter document, XMLStreamWriter writer, ReadWriteParameterMap parameters) 
			throws IOException, XMLStreamException;
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer, ReadWriteParameterMap parameters) throws IOException {
		try {
			XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
			xmlWriter.writeStartDocument();
			doWriteDocument(document, xmlWriter, parameters);
			xmlWriter.writeEndDocument();
		}
		catch (XMLStreamException e) {
			throw new JPhyloIOWriterException("An XML stream exception occured in the underlying XMLStreamWriter.", e);
		}
	}
}
