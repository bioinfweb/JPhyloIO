/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.phyloxml;


import javax.xml.XMLConstants;
import javax.xml.namespace.QName;



public interface PhyloXMLConstants {
	public static final String PHYLOXML_FORMAT_NAME = "PhyloXML";
	public static final String PHYLO_XML = "phyloXML";
	
	public static final String NAMESPACE_URI = "http://www.phyloxml.org";
	public static final String NAMESPACE_XS = XMLConstants.W3C_XML_SCHEMA_NS_URI;
	public static final String NAMESPACE_XSI = XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
	public static final String NAMESPACE_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns";

	public static String PHY_PRE = "phy";
	public static String XSI_PRE = "xsi";
	public static String XSD_PRE = "xsd";	
	public static String RDF_PRE = "rdf";
	
	public static final QName TAG_ROOT = new QName(NAMESPACE_URI, "phyloxml");
	public static final QName TAG_PHYLOGENY = new QName(NAMESPACE_URI, "phylogeny");
	public static final QName TAG_CLADE = new QName(NAMESPACE_URI, "clade");
	
	public static final QName TAG_TAXONOMY = new QName(NAMESPACE_URI, "taxonomy");
	public static final QName TAG_SEQUENCE = new QName(NAMESPACE_URI, "sequence");
	
	public static final QName TAG_NAME = new QName(NAMESPACE_URI, "name");
	public static final QName TAG_ID = new QName(NAMESPACE_URI, "id");
	public static final QName TAG_NODE_ID = new QName(NAMESPACE_URI, "node_id");
	public static final QName TAG_SCI_NAME = new QName(NAMESPACE_URI, "scientific_name");
	public static final QName TAG_COMMON_NAME = new QName(NAMESPACE_URI, "common_name");
	public static final QName TAG_BRANCH_LENGTH = new QName(NAMESPACE_URI, "branch_length");
	public static final QName TAG_CONFIDENCE = new QName(NAMESPACE_URI, "confidence");
	public static final QName TAG_BRANCH_WIDTH = new QName(NAMESPACE_URI, "width");
	public static final QName TAG_BRANCH_COLOR = new QName(NAMESPACE_URI, "color");
	public static final QName TAG_PROPERTY = new QName(NAMESPACE_URI, "property");
	
	public static final QName ATTR_BRANCH_LENGTH = new QName("branch_length");
	public static final QName ATTR_BRANCH_LENGTH_UNIT = new QName("branch_length_unit");
	public static final QName ATTR_ROOTED = new QName("rooted");
	public static final QName ATTR_TYPE = new QName("type");
	public static final QName ATTR_DATATYPE = new QName(NAMESPACE_XS, "datatype", XSD_PRE);
	public static final QName ATTR_APPLIES_TO = new QName("applies_to");
	
	public static final String PHYLOXML_PREDICATE_NAMESPACE = "http://bioinfweb.info/xmlns/JPhyloIO/PhyloXML/";
	public static final QName PREDICATE_TAXONOMY_RANK = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/Rank");
}
