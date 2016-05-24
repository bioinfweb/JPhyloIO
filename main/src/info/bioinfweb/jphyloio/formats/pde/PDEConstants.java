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
package info.bioinfweb.jphyloio.formats.pde;


import javax.xml.namespace.QName;



public interface PDEConstants {
	public static final String PDE_FORMAT_NAME = "PDE (PhyDE)";
	public static final String PDE = "PDE";	

	public static final QName TAG_ROOT = new QName("phyde");
	public static final QName TAG_DESCRIPTION = new QName("description");
	public static final QName TAG_ALIGNMENT = new QName("alignment");
	
	public static final QName TAG_HEADER = new QName("header");
	public static final QName TAG_META_TYPE_DEFINITIONS = new QName("entries");
	
	public static final QName TAG_SEQUENCE_INFORMATION = new QName("seq");
	public static final QName TAG_SEQUENCE_NAME = new QName("name");
	public static final QName TAG_SEQUENCE_COMMENT = new QName("cmt");
	public static final QName TAG_SEQUENCE_META_INFORMATION = new QName("e");
	public static final QName TAG_SEQ_TAXONSETS = new QName("txsets");
	public static final QName TAG_SWITCHES = new QName("switches");
	public static final QName TAG_PHEROGRAM = new QName("pher");
	public static final QName TAG_ACC = new QName("acc"); //TODO what does this stand for?
	
	public static final QName TAG_MATRIX = new QName("matrix");
	public static final QName TAG_BLOCK = new QName("block");
	
	public static final QName TAG_CHARSETS = new QName("CharSets");
	public static final QName TAG_CHARSET = new QName("charset");
	public static final QName TAG_GENCHARSET = new QName("gencharset");
	
	public static final QName TAG_LABELS = new QName("Labels");
	public static final QName TAG_LABEL = new QName("label");
	
	public static final QName TAG_TAXONSETS = new QName("TaxonSets");
	
	public static final QName ATTR_DATATYPE = new QName("datatype");
	public static final QName ATTR_ALIGNMENT_LENGTH = new QName("width");
	public static final QName ATTR_SEQUENCE_COUNT = new QName("height");
	public static final QName ATTR_GENCODE = new QName("gencode");
	public static final QName ATTR_OFFSET = new QName("offset");
	
	public static final QName ATTR_SEQUENCE_INDEX = new QName("idx");
	public static final QName ATTR_ID = new QName("id");
	public static final QName ATTR_VERSION = new QName("version");	
	public static final QName ATTR_NAME = new QName("name");
	public static final QName ATTR_VISIBILITY = new QName("vis");
	public static final QName ATTR_LEVEL = new QName("level");
	
	public static final QName ATTR_X = new QName("x");
	public static final QName ATTR_Y = new QName("y");
	public static final QName ATTR_W = new QName("w");	
	public static final QName ATTR_H = new QName("h");
	// Attribute missing here: cs1
	
	public static final String META_TYPE_STRING = "STRING";
	public static final String META_TYPE_NUMBER = "NUMBER";
	public static final String META_TYPE_FILE = "FILE";
	
	public static final String DNA_TYPE = "dna";
	public static final String AMINO_TYPE = "amino";
	
	public static final String SEQUENCE_END = "/FF";
	public static final String UNKNOWN_CHARS = "/FE";
	
	public static final int META_ID_SEQUENCE_LABEL = 1;
	public static final int META_ID_LINKED_FILE = 2;
	public static final int META_ID_ACCESS_NUMBER = 3;
	public static final int META_ID_COMMENT = 4;
	public static final int FIRST_CUSTOM_META_ID = 32;	
}
