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

import java.awt.Color;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;
import info.bioinfweb.jphyloio.objecttranslation.AbstractObjectTranslator;
import info.bioinfweb.jphyloio.objecttranslation.InvalidObjectSourceDataException;

public class RGBColorTranslator extends AbstractObjectTranslator<Color> {

	@Override
	public Class<Color> getObjectClass() {
		return Color.class;
	}

	@Override
	public boolean hasStringRepresentation() {
		return true;
	}

	@Override
	public String javaToRepresentation(Color object) throws UnsupportedOperationException, ClassCastException {
		return object.toString();
	}

	@Override
	public void writeXMLRepresentation(XMLStreamWriter writer, Color object) throws IOException, XMLStreamException {
		writer.writeStartElement("red"); //TODO use constant
		writer.writeCharacters(Integer.toString(object.getRed()));
		writer.writeEndElement();
		
		writer.writeStartElement("green"); //TODO use constant
		writer.writeCharacters(Integer.toString(object.getGreen()));
		writer.writeEndElement();
		
		writer.writeStartElement("blue"); //TODO use constant
		writer.writeCharacters(Integer.toString(object.getBlue()));
		writer.writeEndElement();		
	}

	@Override
	public Color representationToJava(String representation) throws InvalidObjectSourceDataException,
			UnsupportedOperationException {
		Color color = null;
		
		try {
			color = Color.decode(representation);
		}
		catch (NumberFormatException e) {
			throw new InvalidObjectSourceDataException("The string \"" + representation + "\" could not be parsed to a color object.");
		}
		
		return color;
	}

	@Override
	public Color readXMLRepresentation(XMLEventReader reader) throws IOException, XMLStreamException,
			InvalidObjectSourceDataException {
		Color color = null;
		String red = null;
		String green = null;
		String blue = null;
		
		Set<QName> encounteredTags = new HashSet<QName>();
		XMLEvent event = reader.peek();
		
		while (!(event.isEndElement() && !encounteredTags.contains(event.asEndElement().getName()))) {
			XMLEvent nextEvent = reader.nextEvent();
			if (nextEvent.isStartElement()) {
				QName elementName = nextEvent.asStartElement().getName();
				if (elementName.equals(PhyloXMLConstants.TAG_RED)) {
					encounteredTags.add(elementName);
					red = reader.getElementText();
				}
				else if (elementName.equals(PhyloXMLConstants.TAG_GREEN)) {
					encounteredTags.add(elementName);
					green = reader.getElementText();
				}
				else if (elementName.equals(PhyloXMLConstants.TAG_BLUE)) {
					encounteredTags.add(elementName);
					blue = reader.getElementText();
				}
			}
			
			event = reader.peek();
		}
		
		if ((red != null) && (green != null) && (blue != null)) {
			try {
				color = new Color(Integer.parseInt(red), Integer.parseInt(green), Integer.parseInt(blue));
			}
			catch (NumberFormatException e) {
				throw new InvalidObjectSourceDataException("The encountered XML could not be parsed to a color object.");
			}
		}
		
		return color;
	}
	
}
