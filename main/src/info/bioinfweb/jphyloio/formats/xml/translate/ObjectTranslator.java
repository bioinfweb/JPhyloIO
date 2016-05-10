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
package info.bioinfweb.jphyloio.formats.xml.translate;


import java.io.IOException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import info.bioinfweb.jphyloio.events.meta.UriOrStringIdentifier;



/**
 * Classes implementing this interface are able to convert between Java Objects and their text or XML representation.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 *
 * @param <O> the type of Java object this translator instance is able to handle
 */
public interface ObjectTranslator<O> {
	public UriOrStringIdentifier getDataType();
	
	/**
	 * Returns the Java class or interface the objects handled by this instance have.
	 * 
	 * @return the class of the handled objects
	 */
	public Class<O> getObjectClass();
	//TODO Should subclasses always be allowed here?
	//TODO Should it be possible to support more than one class?
	
	/**
	 * Determines whether the objects handled by this instance have a simple string representation or need a more complex
	 * XML representation.
	 * <p>
	 * Instances that return {@code false here} will throw an {@link UnsupportedOperationException}, if 
	 * {@link #javaToStringRepresentation(Object)} is called.
	 * 
	 * @return {@code true} if handled objects have a simple string representation or {@code false} if XML is necessary to
	 *         represent the handled objects
	 */
	public boolean hasStringRepresentation();
	
	/**
	 * Converts the specified Java object to its string representation.
	 * 
	 * @param object the object to be translated
	 * @return the string representation of the object
	 * @throws UnsupportedOperationException if objects handled by this instance can only be represented as XML
	 * @throws ClassCastException if the specified object is not an instance of the supported class or does not implement the supported
	 *         interface
	 * @see #hasStringRepresentation()
	 */
	public String javaToStringRepresentation(O object) throws UnsupportedOperationException, ClassCastException;
	
	/**
	 * Writes the XML representation of the specified object into the specified XML writer.
	 * <p>
	 * If {@link #hasStringRepresentation()} of this instance returns {@code true}, the XML representation will only contain characters
	 * and no tags.
	 * 
	 * @param writer the writer to be used to write the XML representation
	 * @param object the object to be converted
	 * @throws IOException if an I/O error occurs while trying to write to the specified writer
	 * @throws XMLStreamException if an XML stream exception occurs while trying to write to the specified writer
	 */
	public void writeXMLRepresentation(XMLStreamWriter writer, O object) throws IOException, XMLStreamException;
	
	/**
	 * Converts the specified string representation to a new instance of the according Java object.
	 * <p>
	 * If {@link #getClass()} returns an interface for this instance, the concrete class of the returned object may 
	 * depend on the representation. 
	 * 
	 * @param representation the string representation of the object to be created
	 * @return the new object
	 * @throws UnsupportedOperationException if objects handled by this instance can only be represented as XML
	 * @throws InvalidObjectSourceDataException if the specified string representation cannot be parsed to a supported object
	 */
	public O representationToJava(String representation) throws InvalidObjectSourceDataException, UnsupportedOperationException;

	/**
	 * Tries to create a new instance of the handled object type from the data provided by the specified XML reader.
	 * <p>
	 * This method will start reading from the current position of the reader and read only as far as necessary to collect all
	 * data for the new object. Therefore the reader should be positioned before the start tag, that represents the object and
	 * will read until the according end tag was consumed. If the supported objects have a simple string representation, the 
	 * reader should be positioned in front of the according characters.
	 * <p>
	 * If this 
	 * 
	 * @param reader the XML reader providing the data to create a new object
	 * @return the new object
	 * @throws IOException if an I/O error occurs while trying to read from the specified reader
	 * @throws XMLStreamException if an XML stream exception occurs while trying to read from the specified reader
	 * @throws InvalidObjectSourceDataException if an unexpected XML event was encountered or an XML event has unexpected contents
	 */
	public O readXMLRepresentation(XMLEventReader reader) throws IOException, XMLStreamException, InvalidObjectSourceDataException;  //TODO Is inversion of control necessary here instead (e.g. to consume the XML events elsewhere too)?
}