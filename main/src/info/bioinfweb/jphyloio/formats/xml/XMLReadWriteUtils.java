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


import java.util.Iterator;

import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLConstants;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EntityDeclaration;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class XMLReadWriteUtils {	
	public static final String XSI_DEFAULT_PRE = "xsi";
	public static final String XSD_DEFAULT_PRE = "xsd";	
	public static final String RDF_DEFAULT_PRE = "rdf";
	
	public static final String NAMESPACE_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
	
//	public static final QName ATTRIBUTE_RDF_PROPERTY = new QName(NAMESPACE_RDF, "property");
//	public static final QName ATTRIBUTE_RDF_DATATYPE = new QName(NAMESPACE_RDF, "datatype");
	//TODO Is an "about"-attribute needed if these attributes are used? Do PhyloXML with about attributes still validate?
	
  public static final String DEFAULT_NAMESPACE_PREFIX = "p";
	
	
	public static String getXSIPrefix(XMLStreamWriter writer) throws XMLStreamException {
		String prefix = writer.getPrefix(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		if (prefix == null || prefix.isEmpty()) {
			prefix = XSI_DEFAULT_PRE;
		}
		return prefix;
	}
	
	
	public static String getXSDPrefix(XMLStreamWriter writer) throws XMLStreamException {
		String prefix = writer.getPrefix(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		if (prefix == null || prefix.isEmpty()) {
			prefix = XSD_DEFAULT_PRE;
		}
		return prefix;
	}
	
	
//	public static String getRDFPrefix(XMLStreamWriter writer) throws XMLStreamException {
//		String prefix = writer.getPrefix(NAMESPACE_RDF);
//		if (prefix == null || prefix.isEmpty()) {
//			prefix = RDF_DEFAULT_PRE;
//		}
//		return prefix;
//	}
	
	
	public static String getNamespacePrefix(XMLStreamWriter writer, String givenPrefix, String namespaceURI) throws XMLStreamException {
		if (givenPrefix == null || givenPrefix.isEmpty()) {
			givenPrefix = writer.getPrefix(namespaceURI);
			
			if (givenPrefix == null || givenPrefix.isEmpty()) {
				if (namespaceURI.equals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI)) {
					givenPrefix = XSI_DEFAULT_PRE;
				}
				else if (namespaceURI.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
					givenPrefix = XSD_DEFAULT_PRE;
				}
				else if (namespaceURI.equals(NAMESPACE_RDF)) {
					givenPrefix = RDF_DEFAULT_PRE;
				}
				else if (namespaceURI.equals(NeXMLConstants.NEXML_NAMESPACE)) {
					givenPrefix = NeXMLConstants.NEXML_DEFAULT_NAMESPACE_PREFIX;
				}
				else if (namespaceURI.equals(PhyloXMLConstants.PHYLOXML_NAMESPACE)) {
					givenPrefix = PhyloXMLConstants.PHYLOXML_DEFAULT_PRE;
				}
				else if (namespaceURI.equals(ReadWriteConstants.JPHYLOIO_PREDICATE_NAMESPACE)) {
					givenPrefix = ReadWriteConstants.JPHYLOIO_PREDICATE_PREFIX;
				}
				else if (namespaceURI.equals(ReadWriteConstants.JPHYLOIO_ATTRIBUTES_NAMESPACE)) {
					givenPrefix = ReadWriteConstants.JPHYLOIO_ATTRIBUTES_PREFIX;
				}
				else {
					givenPrefix = DEFAULT_NAMESPACE_PREFIX;
				}
			}			
		}
		return givenPrefix;
	}
	
	
	public static void writeCustomXML(XMLStreamWriter writer, ReadWriteParameterMap parameters, XMLEvent event) throws XMLStreamException {
		switch (event.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				StartElement element = event.asStartElement();
				writer.writeStartElement(element.getName().getNamespaceURI(), element.getName().getLocalPart());  // Writer obtains the correct prefix from its namespace context
				
				// Write attributes
				@SuppressWarnings("unchecked")
				Iterator<Attribute> attributes = element.getAttributes();
				while (attributes.hasNext()) {
					Attribute attribute = attributes.next();
					writer.writeAttribute(attribute.getName().getNamespaceURI(), attribute.getName().getLocalPart(), attribute.getValue());
				}
				
				//TODO write ns
				break;
			case XMLStreamConstants.END_ELEMENT:
				writer.writeEndElement();
				break;
			case XMLStreamConstants.CHARACTERS:
			case XMLStreamConstants.SPACE:
				writer.writeCharacters(event.asCharacters().getData());
				break;
			case XMLStreamConstants.CDATA:
				writer.writeCData(event.asCharacters().getData()); //TODO multiple events with continued content should be buffered and written to a single CDATA element
				break;
			case XMLStreamConstants.ATTRIBUTE:
				Attribute contentAttribute = ((Attribute)event);
				writer.writeAttribute(contentAttribute.getName().getPrefix(), contentAttribute.getName().getNamespaceURI(), 
						contentAttribute.getName().getLocalPart(), contentAttribute.getValue());
				break;
			case XMLStreamConstants.NAMESPACE:
				Namespace contentNamespace = ((Namespace)event);
				writer.writeNamespace(contentNamespace.getPrefix(), contentNamespace.getNamespaceURI());
				break;
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
				ProcessingInstruction contentProcessingInstruction = ((ProcessingInstruction)event);
				writer.writeProcessingInstruction(contentProcessingInstruction.getTarget(), contentProcessingInstruction.getData());
				break;
			case XMLStreamConstants.COMMENT:
				writer.writeComment(((Comment)event).getText());
				break;
			case XMLStreamConstants.DTD:
				StringBuffer message = new StringBuffer();
				message.append("A document type declaration (DTD) with the content \"");
				
				if (((DTD)event).getDocumentTypeDeclaration().length() > 128) {
					message.append(((DTD)event).getDocumentTypeDeclaration().substring(0, 128));
					message.append(" [...]");
				}
				else {
					message.append(((DTD)event).getDocumentTypeDeclaration());
				}
				
				message.append("\" was found but can not be written at this position of the document.");
				parameters.getLogger().addWarning(message.toString());
				break;
			case XMLStreamConstants.NOTATION_DECLARATION:
				parameters.getLogger().addWarning("A notation declaration with the name \"" + ((NotationDeclaration)event).getName() + "\" was found but"
						+ "can not be written at this position of the document.");
				break;
			case XMLStreamConstants.ENTITY_DECLARATION:
				parameters.getLogger().addWarning("An entity declaration with the name \"" + ((EntityDeclaration)event).getName() + "\" was found but"
						+ "can not be written at this position of the document.");
				break;
			case XMLStreamConstants.ENTITY_REFERENCE:
				writer.writeEntityRef(((EntityReference)event).getName());
				break;
			default: // START_DOCUMENT and END_DOCUMENT can be ignored
				break;
		}
	}
}
