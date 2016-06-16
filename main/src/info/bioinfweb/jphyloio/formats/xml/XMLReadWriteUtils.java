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


import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class XMLReadWriteUtils {
	public static final String NAMESPACE_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
	
	public static final String XSI_DEFAULT_PRE = "xsi";
	public static final String XSD_DEFAULT_PRE = "xsd";
	public static final String RDF_DEFAULT_PRE = "rdf";
	
	
	public static String getXSIPrefix(XMLStreamWriter writer) throws XMLStreamException {
		String prefix = writer.getPrefix(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
		if (prefix == null) { //TODO should not happen, leave out this part?
			prefix = XSI_DEFAULT_PRE;
		}
		return prefix;
	}
	
	
	public static String getXSDPrefix(XMLStreamWriter writer) throws XMLStreamException {
		String prefix = writer.getPrefix(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		if (prefix == null) { //TODO should not happen, leave out this part?
			prefix = XSD_DEFAULT_PRE;
		}
		return prefix;
	}
	
	
	public static String getRDFPrefix(XMLStreamWriter writer) throws XMLStreamException {
		String prefix = writer.getPrefix(NAMESPACE_RDF);
		if (prefix == null) { //TODO should not happen, leave out this part?
			prefix = RDF_DEFAULT_PRE;
		}
		return prefix;
	}
}
