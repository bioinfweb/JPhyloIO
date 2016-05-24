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
package info.bioinfweb.jphyloio.objecttranslation.implementations;


import java.io.IOException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.objecttranslation.InvalidObjectSourceDataException;
import info.bioinfweb.jphyloio.objecttranslation.ObjectTranslator;



public abstract class SimpleValueTranslator<O> implements ObjectTranslator<O> {
	public static final int MAX_STRING_REPRESENTATION_LENGTH = 16 * 1024;
	
	
	@Override
	public boolean hasStringRepresentation() {
		return true;
	}
	

	/**
	 * 
	 * 
	 * @see info.bioinfweb.jphyloio.objecttranslation.ObjectTranslator#javaToStringRepresentation(java.lang.Object)
	 */
	@Override
	public String javaToStringRepresentation(O object) throws UnsupportedOperationException, ClassCastException {
		return object.toString();
	}
	

	@Override
	public void writeXMLRepresentation(XMLStreamWriter writer, O object) throws IOException, XMLStreamException {
		writer.writeCharacters(javaToStringRepresentation(object));
	}

	
	@Override
	public O readXMLRepresentation(XMLEventReader reader) throws IOException,	XMLStreamException, InvalidObjectSourceDataException {
		StringBuilder text = new StringBuilder();
		while (reader.peek().isCharacters()) {
			if (text.length() > MAX_STRING_REPRESENTATION_LENGTH) {  // Avoid loading very large amounts of (invalid) data.
				throw new JPhyloIOReaderException("The text to parse a simple value from is longer than " + MAX_STRING_REPRESENTATION_LENGTH + 
						" characters. Reading is aborted.", reader.peek().getLocation());
			}
			text.append(reader.nextEvent().asCharacters().getData());
		}
		return representationToJava(text.toString());
	}	
}
