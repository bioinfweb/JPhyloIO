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
package info.bioinfweb.jphyloio.events.type;


import info.bioinfweb.jphyloio.events.JPhyloIOEvent;



/**
 * Enumerates the content types of an {@link JPhyloIOEvent}. Elements of each content type can either
 * occur in one version in combination with {@link EventTopologyType#SOLE} or in two versions in
 * combination with {@link EventTopologyType#START} or {@link EventTopologyType#END}.
 * 
 * @author Ben St&ouml;ver
 */
public enum EventContentType {
	// General events:
	
	/** Indicates the start or the end of the read document. */
	DOCUMENT,
	
	META_INFORMATION,
	
	/** 
	 * Events of this type are used by some readers to provide the application with contents
	 * of unknown commits in a format.
	 */
	UNKNOWN_COMMAND,
	
	/** Indicates a comment found in the underlying data source. */
	COMMENT,
	
	
	// OTU events:
	
	/** Indicates the start or the end of a list of OTU/taxon definitions. */
	OTU_LIST,
	
	/** Indicates the start or the end of an OTU/taxon definition. */ 
	OTU,
	
	
	// Matrix events:
	
	/** Indicates the start or the end of the contents of a matrix. */
	ALIGNMENT,
	
	/** Indicates the start or the end of the contents of a sequence in a matrix. */
	SEQUENCE,
	
	/** Indicates a number of sequence tokens. Events of this type never have nested elements. */
	SEQUENCE_TOKENS,
	
	/** Indicates a number a single sequence token. Events of this type may have nested elements. */
	SINGLE_SEQUENCE_TOKEN,
	
	
	// Set events:
	
	/** Indicates the start or end of a sequence of {@link #CHARACTER_SET_INTERVAL} events. */
	CHARACTER_SET,
	
	/** Indicates a single interval of a character set. */
	CHARACTER_SET_INTERVAL,
	
	/** Indicates the start or end of a token set definition. **/
	TOKEN_SET_DEFINITION,
	
	/** 
	 * Indicates the definition of a single sequence token symbol. Such events are nested 
	 * in {@link #TOKEN_SET_DEFINITION}.
	 */
	SINGLE_TOKEN_DEFINITION,
	
	
	// Tree and network events:
	
	/** Indicates the start of the end of the contents of a phylogenetic network. */
	NETWORK,

	/** Indicates the start of the end of the contents of a phylogenetic tree. */
	TREE,
	
	/** Indicates a node in a phylogenetic tree or network. */
	NODE,
	
	/** Indicates an edge in a phylogenetic tree or network. */
	EDGE;
}
