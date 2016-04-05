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
package info.bioinfweb.jphyloio;



/**
 * Provides general constants for reading and writing phylogenetic file formats.
 * 
 * @author Ben St&ouml;ver
 */
public interface ReadWriteConstants {
	public static final String DEFAULT_MATCH_TOKEN = ".";
	public static final int DEFAULT_MAX_TOKENS_TO_READ = 2048;
	public static final int DEFAULT_MAX_COMMENT_LENGTH = 1024 * 1024;
	
	
	public static final String META_KEY_SEQUENCE_COUNT = "info.bioinfweb.jphyloio.sequenceCount";
	public static final String META_KEY_CHARACTER_COUNT = "info.bioinfweb.jphyloio.characterCount";
	public static final String META_KEY_DISPLAY_TREE_ROOTED = "info.bioinfweb.jphyloio.displayTreeRooted";
	//TODO Replace these keys by URLs? (Depends on the ontology modeling classes to be added.)
	
	public static final String DEFAULT_OTU_LIST_ID_PREFIX = "otus";
	public static final String DEFAULT_OTU_ID_PREFIX = "otu";
	public static final String DEFAULT_MATRIX_ID_PREFIX = "matrix";
	public static final String DEFAULT_SEQUENCE_ID_PREFIX = "seq";
	public static final String DEFAULT_CHAR_SET_ID_PREFIX = "charSet";
	public static final String DEFAULT_TOKEN_SET_ID_PREFIX = "tokenSet";
	public static final String DEFAULT_TOKEN_DEFINITION_ID_PREFIX = "tokenDefinition";
	public static final String DEFAULT_TREE_ID_PREFIX = "tree";
	public static final String DEFAULT_TREES_ID_PREFIX = "trees";
	public static final String DEFAULT_NODE_ID_PREFIX = "n";
	public static final String DEFAULT_EDGE_ID_PREFIX = "e";
}
