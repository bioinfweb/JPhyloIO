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
package info.bioinfweb.jphyloio.formats.newick;



public interface NewickConstants {
	public static final char SUBTREE_START = '(';
	public static final char SUBTREE_END = ')';
	public static final char NAME_DELIMITER = '\'';
	public static final char ALTERNATIVE_NAME_DELIMITER = '"';
	public static final char LENGTH_SEPERATOR = ':'; 
	public static final char ELEMENT_SEPERATOR = ','; 
	public static final char TERMINAL_SYMBOL = ';';
	public static final char COMMENT_START = '[';
	public static final char COMMENT_END = ']';
	public static final char FREE_NAME_BLANK = '_';
	
	public static final String ROOTED_HOT_COMMENT = "&r";
	public static final String UNROOTED_HOT_COMMENT = "&u";
	
	
	// Hot comment constants:
	
	public static final char HOT_COMMENT_START_SYMBOL = '&';
	public static final char ALLOCATION_SEPARATOR_SYMBOL = ',';
	public static final char ALLOCATION_SYMBOL = '=';
	public static final char FIELD_START_SYMBOL = '{';
  public static final char FIELD_END_SYMBOL = '}';
	public static final char FIELD_VALUE_SEPARATOR_SYMBOL = ',';
	
	public static final char INDEX_START_SYMBOL = '[';
	public static final char INDEX_END_SYMBOL = ']';

	public static final char NHX_VALUE_SEPARATOR_SYMBOL = ':';
	public static final String NHX_START = "&&NHX" + NHX_VALUE_SEPARATOR_SYMBOL;
	public static final String NHX_KEY_PREFIX = "NHX:";
	
//	public static final String UNNAMED_EDGE_DATA_NAME = "unnamedEdgeHotComment";  //TODO Specify URL or similar ID here?
//	public static final String UNNAMED_NODE_DATA_NAME = "unnamedNodeHotComment";  //TODO Specify URL or similar ID here?
}
