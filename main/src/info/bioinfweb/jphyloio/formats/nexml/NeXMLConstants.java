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


import javax.xml.namespace.QName;



public interface NeXMLConstants {
	public static final String NEXML_FORMAT_NAME = "NeXML";

	public static final String NAMESPACE_URI = "http://www.nexml.org/2009";
	public static final String NAMESPACE_URI_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	
	
//	public static String XSI_URI = javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
//	public static String XMLNS_URI = javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
	public static String XS_URI = javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
//	public static String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns";
//
//	public static String XMLNS_PRE = javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
	public static String XSI_PRE = "xsi";
	public static String XSD_PRE = "xsd";
	public static String NEX_PRE = "nex";
//	public static String RDF_PRE = "rdf";
//	
//	public static String XSI_TYPE = XSI_PRE + ":type";
	
	
	public static final QName TAG_ROOT = new QName(NAMESPACE_URI, "nexml");
	public static final QName TAG_META = new QName(NAMESPACE_URI, "meta");
	public static final QName TAG_SET = new QName(NAMESPACE_URI, "set");
	
	public static final QName TAG_OTUS = new QName(NAMESPACE_URI, "otus");
	public static final QName TAG_OTU = new QName(NAMESPACE_URI, "otu");
	
	public static final QName TAG_CHARACTERS = new QName(NAMESPACE_URI, "characters");
	public static final QName TAG_MATRIX = new QName(NAMESPACE_URI, "matrix");	
	public static final QName TAG_FORMAT = new QName(NAMESPACE_URI, "format");	
	public static final QName TAG_STATES = new QName(NAMESPACE_URI, "states");
	public static final QName TAG_STATE = new QName(NAMESPACE_URI, "state");
	public static final QName TAG_POLYMORPHIC = new QName(NAMESPACE_URI, "polymorphic_state_set");
	public static final QName TAG_UNCERTAIN = new QName(NAMESPACE_URI, "uncertain_state_set");
	public static final QName TAG_MEMBER = new QName(NAMESPACE_URI, "member");
	public static final QName TAG_CHAR = new QName(NAMESPACE_URI, "char");
	public static final QName TAG_ROW = new QName(NAMESPACE_URI, "row");
	public static final QName TAG_CELL = new QName(NAMESPACE_URI, "cell");
	public static final QName TAG_SEQ = new QName(NAMESPACE_URI, "seq");
	
	public static final QName TAG_TREES = new QName(NAMESPACE_URI, "trees");
	public static final QName TAG_TREE = new QName(NAMESPACE_URI, "tree");
	public static final QName TAG_NETWORK = new QName(NAMESPACE_URI, "network");
	public static final QName TAG_NODE = new QName(NAMESPACE_URI, "node");
	public static final QName TAG_EDGE = new QName(NAMESPACE_URI, "edge");
	public static final QName TAG_ROOTEDGE = new QName(NAMESPACE_URI, "rootedge");
	
	public static final QName ATTR_ID = new QName("id");
	public static final QName ATTR_LABEL = new QName("label");
	public static final QName ATTR_ABOUT = new QName("about");	
	public static final QName ATTR_OTU = new QName("otu");
	public static final QName ATTR_OTUS = new QName("otus");
	public static final QName ATTR_SYMBOL = new QName("symbol");
	public static final QName ATTR_STATE = new QName("state");
	public static final QName ATTR_STATES = new QName("states");
	public static final QName ATTR_CHAR = new QName("char");
	
	public static final QName ATTR_SOURCE = new QName("source");
	public static final QName ATTR_TARGET = new QName("target");
	public static final QName ATTR_LENGTH = new QName("length");
	
	public static final QName ATTR_XSI_TYPE = new QName(XSI_PRE + ":type");	
	public static final QName ATTR_DATATYPE = new QName("datatype");
	public static final QName ATTR_HREF = new QName("href");
	public static final QName ATTR_REL = new QName("rel");
	public static final QName ATTR_CONTENT = new QName("content");
	public static final QName ATTR_PROPERTY = new QName("property");	
	
	public static final String TYPE_DNA_SEQS = NEX_PRE + ":DnaSeqs";
	public static final String TYPE_DNA_CELLS = NEX_PRE + ":DnaCells";
	public static final String TYPE_RNA_SEQS = NEX_PRE + ":RnaSeqs";
	public static final String TYPE_RNA_CELLS= NEX_PRE + ":RnaCells";
	public static final String TYPE_PROTEIN_SEQS = NEX_PRE + ":ProteinSeqs";
	public static final String TYPE_PROTEIN_CELLS= NEX_PRE + ":ProteinCells";
	public static final String TYPE_RESTRICTION_SEQS = NEX_PRE + ":RestrictionSeqs";
	public static final String TYPE_RESTRICTION_CELLS = NEX_PRE + ":RestrictionCells";
	public static final String TYPE_CONTIN_SEQ = NEX_PRE + ":ContinuousSeqs";
	public static final String TYPE_CONTIN_CELLS = NEX_PRE + ":ContinuousCells";
	public static final String TYPE_STANDARD_SEQ = NEX_PRE + ":StandardSeqs";
	public static final String TYPE_STANDARD_CELLS = NEX_PRE + ":StandardCells";
	
	public static final String TYPE_FLOAT_TREE = NEX_PRE + ":FloatTree";
	public static final String TYPE_INT_TREE = NEX_PRE + ":IntTree";
	
	public static final String TYPE_STRING = XSD_PRE + ":string";
	
	public static final String TYPE_LITERAL_META = NEX_PRE + ":LiteralMeta";
	public static final String TYPE_RESOURCE_META = NEX_PRE + ":ResourceMeta";
}
