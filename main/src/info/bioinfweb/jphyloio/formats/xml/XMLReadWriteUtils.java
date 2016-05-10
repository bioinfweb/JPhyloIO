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
	
	
//	@SuppressWarnings("serial")
//	private static Map<QName,Class<?>> classForXsdType = new HashMap<QName, Class<?>>() {{
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "decimal", XSD_PRE), BigDecimal.class);  //TODO Constant prefixes will anyway be removed.
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "integer", XSD_PRE), BigInteger.class);
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "boolean", XSD_PRE), Boolean.class);
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "byte", XSD_PRE), Byte.class);
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "QName", XSD_PRE), QName.class);		
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "double", XSD_PRE), Double.class);
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "float", XSD_PRE), Float.class);
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "long", XSD_PRE), Long.class);
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "short", XSD_PRE), Short.class);		
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string",XSD_PRE), String.class);
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "char", XSD_PRE), Character.class);
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "dateTime", XSD_PRE), Date.class);
//		put(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "duration", XSD_PRE), Duration.class);		
//	}};
//	
//	
//	@SuppressWarnings("serial")
//	private static Map<Class<?>,QName> xsdTypeForClass = new HashMap<Class<?>,QName>() {{
//		for ( QName xsdType : classForXsdType.keySet() ) {
//			put(classForXsdType.get(xsdType), xsdType);
//		}	
//		put(Integer.class,new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "integer", XSD_PRE));
//		put(Date.class, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "dateTime", XSD_PRE));
//		put(Calendar.class, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "dateTime", XSD_PRE));
//		put(UUID.class, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string", XSD_PRE));
//		put(java.awt.Image.class, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "base64Binary", XSD_PRE));
//		put(Duration.class, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "duration", XSD_PRE));
//		put(java.lang.Character.class, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "char", XSD_PRE));
//		put(Source.class, new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "base64Binary", XSD_PRE));
//	}};
}
