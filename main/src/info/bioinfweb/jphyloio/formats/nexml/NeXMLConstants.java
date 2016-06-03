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


import info.bioinfweb.jphyloio.ReadWriteConstants;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;



public interface NeXMLConstants {
	public static final String NEXML_FORMAT_NAME = "NeXML";
	public static final String NEXML_VERSION = "0.9";
	public static final String NEXML_DEFAULT_PRE = "nex";
	public static final String NEXML_NAMESPACE = "http://www.nexml.org/2009";

	public static final String DEFAULT_TOKEN_DEFINITION_SET_ID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + "DEFAULT";

	public static final QName TAG_ROOT = new QName(NEXML_NAMESPACE, "nexml");
	public static final QName TAG_META = new QName(NEXML_NAMESPACE, "meta");
	public static final QName TAG_SET = new QName(NEXML_NAMESPACE, "set");

	public static final QName TAG_OTUS = new QName(NEXML_NAMESPACE, "otus");
	public static final QName TAG_OTU = new QName(NEXML_NAMESPACE, "otu");

	public static final QName TAG_CHARACTERS = new QName(NEXML_NAMESPACE, "characters");
	public static final QName TAG_MATRIX = new QName(NEXML_NAMESPACE, "matrix");
	public static final QName TAG_FORMAT = new QName(NEXML_NAMESPACE, "format");
	public static final QName TAG_STATES = new QName(NEXML_NAMESPACE, "states");
	public static final QName TAG_STATE = new QName(NEXML_NAMESPACE, "state");
	public static final QName TAG_POLYMORPHIC = new QName(NEXML_NAMESPACE, "polymorphic_state_set");
	public static final QName TAG_UNCERTAIN = new QName(NEXML_NAMESPACE, "uncertain_state_set");
	public static final QName TAG_MEMBER = new QName(NEXML_NAMESPACE, "member");
	public static final QName TAG_CHAR = new QName(NEXML_NAMESPACE, "char");
	public static final QName TAG_ROW = new QName(NEXML_NAMESPACE, "row");
	public static final QName TAG_CELL = new QName(NEXML_NAMESPACE, "cell");
	public static final QName TAG_SEQ = new QName(NEXML_NAMESPACE, "seq");

	public static final QName TAG_TREES = new QName(NEXML_NAMESPACE, "trees");
	public static final QName TAG_TREE = new QName(NEXML_NAMESPACE, "tree");
	public static final QName TAG_NETWORK = new QName(NEXML_NAMESPACE, "network");
	public static final QName TAG_NODE = new QName(NEXML_NAMESPACE, "node");
	public static final QName TAG_EDGE = new QName(NEXML_NAMESPACE, "edge");
	public static final QName TAG_ROOTEDGE = new QName(NEXML_NAMESPACE, "rootedge");

	public static final QName ATTR_VERSION = new QName("version");
	public static final QName ATTR_GENERATOR = new QName("generator");

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

	public static final QName ATTR_XSI_TYPE = new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type");
	public static final QName ATTR_DATATYPE = new QName("datatype");
	public static final QName ATTR_HREF = new QName("href");
	public static final QName ATTR_REL = new QName("rel");
	public static final QName ATTR_CONTENT = new QName("content");
	public static final QName ATTR_PROPERTY = new QName("property");

	public static final String TYPE_DNA_SEQS = "DnaSeqs";
	public static final String TYPE_DNA_CELLS = "DnaCells";
	public static final String TYPE_RNA_SEQS = "RnaSeqs";
	public static final String TYPE_RNA_CELLS= "RnaCells";
	public static final String TYPE_PROTEIN_SEQS = "ProteinSeqs";
	public static final String TYPE_PROTEIN_CELLS = "ProteinCells";
	public static final String TYPE_RESTRICTION_SEQS = "RestrictionSeqs";
	public static final String TYPE_RESTRICTION_CELLS = "RestrictionCells";
	public static final String TYPE_CONTIN_SEQ = "ContinuousSeqs";
	public static final String TYPE_CONTIN_CELLS = "ContinuousCells";
	public static final String TYPE_STANDARD_SEQ = "StandardSeqs";
	public static final String TYPE_STANDARD_CELLS = "StandardCells";

	public static final String TYPE_FLOAT_TREE = "FloatTree";
	public static final String TYPE_INT_TREE = "IntTree";
	public static final String TYPE_FLOAT_NETWORK = "FloatNetwork";
	public static final String TYPE_INT_NETWORK = "IntNetwork";

	public static final String TYPE_LITERAL_META = "LiteralMeta";
	public static final String TYPE_RESOURCE_META = "ResourceMeta";

    public static final String UNDEFINED_OTUS_ID = "undefinedOTUs";
    public static final String UNDEFINED_OTUS_LABEL = "UNDEFINED OTU list generated by JPhyloIO";
    public static final String UNDEFINED_OTU_ID = "undefinedOTU";
    public static final String UNDEFINED_OTU_LABEL = "UNDEFINED OTU generated by JPhyloIO";
}
