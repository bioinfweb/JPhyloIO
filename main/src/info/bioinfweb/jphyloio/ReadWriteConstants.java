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


import javax.xml.namespace.QName;



/**
 * Provides general constants for reading and writing phylogenetic file formats.
 * <p>
 * The {@code DEFAULT_XXX_PREFIX} constants are used by some reader implementations to generate event IDs.
 * Their value may change any time without further notice. Such a change would not be considered as an API
 * change and would not be reflected in a major version number increase. Application developers should never
 * rely on expecting specific prefixes for event IDs.
 * 
 * @author Ben St&ouml;ver
 * @author Sarah Wiechers
 * @since 0.0.0
 */
public interface ReadWriteConstants {
	public static final String DEFAULT_MATCH_TOKEN = ".";
	public static final int DEFAULT_MAX_TOKENS_TO_READ = 2048;
	public static final int DEFAULT_MAX_COMMENT_LENGTH = 1024 * 1024;
	
	public static final String PREDICATE_PART_SEPERATOR = ".";
	public static final String PREDICATE_NAMESPACE_FOLDER = "Predicates";
	
	public static final String JPHYLOIO_NAMESPACE_PREFIX = "http://bioinfweb.info/xmlns/JPhyloIO/";
	
	public static final String JPHYLOIO_PREDICATE_NAMESPACE = JPHYLOIO_NAMESPACE_PREFIX + "General/" + PREDICATE_NAMESPACE_FOLDER + "/";
	
	public static final QName PREDICATE_HAS_LITERAL_METADATA = new QName(JPHYLOIO_PREDICATE_NAMESPACE, "hasLiteralMetadata");
	public static final QName PREDICATE_HAS_RESOURCE_METADATA = new QName(JPHYLOIO_PREDICATE_NAMESPACE, "hasResourceMetadata");
	
	public static final QName PREDICATE_DISPLAY_TREE_ROOTED = new QName(JPHYLOIO_PREDICATE_NAMESPACE, "displayTreeRooted");
	public static final QName PREDICATE_SEQUENCE_COUNT = new QName(JPHYLOIO_PREDICATE_NAMESPACE, "sequenceCount");
	public static final QName PREDICATE_CHARACTER_COUNT = new QName(JPHYLOIO_PREDICATE_NAMESPACE, "characterCount");
	
	public static final QName PREDICATE_EDGE_SOURCE_NODE = new QName(JPHYLOIO_PREDICATE_NAMESPACE, "edgeSourceNode");
	public static final QName PREDICATE_EDGE_TARGET_NODE = new QName(JPHYLOIO_PREDICATE_NAMESPACE, "edgeTargetNode");
	public static final QName PREDICATE_EDGE_LENGTH = new QName(JPHYLOIO_PREDICATE_NAMESPACE, "edgeLength");
	public static final QName PREDICATE_IS_CROSSLINK = new QName(JPHYLOIO_PREDICATE_NAMESPACE, "isCrosslink");
	
	public static final String DEFAULT_META_ID_PREFIX = "meta";
	public static final String DEFAULT_OTU_LIST_ID_PREFIX = "otus";
	public static final String DEFAULT_OTU_ID_PREFIX = "otu";
	public static final String DEFAULT_MATRIX_ID_PREFIX = "matrix";
	public static final String DEFAULT_SEQUENCE_ID_PREFIX = "seq";
	public static final String DEFAULT_CHAR_ID_PREFIX = "char";
	public static final String DEFAULT_CHAR_SET_ID_PREFIX = "charSet";
	public static final String DEFAULT_TOKEN_SET_ID_PREFIX = "tokenSet";
	public static final String DEFAULT_TOKEN_DEFINITION_ID_PREFIX = "tokenDefinition";
	public static final String DEFAULT_TREE_ID_PREFIX = "tree";
	public static final String DEFAULT_NETWORK_ID_PREFIX = "network";
	public static final String DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX = "treesOrNetworks";
	public static final String DEFAULT_NODE_ID_PREFIX = "n";
	public static final String DEFAULT_EDGE_ID_PREFIX = "e";
}
