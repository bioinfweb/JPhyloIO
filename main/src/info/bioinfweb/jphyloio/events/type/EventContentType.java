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


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SingleSequenceTokenEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.UnknownCommandEvent;



/**
 * Enumerates the content types of a {@link JPhyloIOEvent}. Elements of each content type can either
 * occur in one version in combination with {@link EventTopologyType#SOLE} or in two versions in
 * combination with {@link EventTopologyType#START} or {@link EventTopologyType#END}.
 * <p>
 * The documentation of the single types includes information on which event classes are used with
 * each type and whether it is separated into a start and end event or not. In addition, the
 * documentation of {@link JPhyloIOEventReader} contains a full grammar of JPhyloIO event streams.
 * 
 * @author Ben St&ouml;ver
 * @see JPhyloIOEventReader
 * @see EventTopologyType
 * @see EventType
 */
public enum EventContentType {
	// General events:
	
	/** 
	 * Indicates the start or the end of the read document.
	 * <p>
	 * Both start and end events of this type are instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	DOCUMENT,
	
	/**
	 * Indicates meta information attached to the object modeled by the preceding start event.
	 * <p>
	 * Start events of this type are instances of {@link MetaInformationEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	META_INFORMATION,
	
	/** 
	 * Events of this type are used by some readers to provide the application with contents
	 * of unknown commands in a format.
	 * <p>
	 * Events of this type are instances of {@link UnknownCommandEvent} and always have the
	 * topology type {@link EventTopologyType#SOLE}. 
	 */  //TODO Check if this is still true, before the first release.
	UNKNOWN_COMMAND,
	
	/** 
	 * Indicates a comment found in the underlying data source.
	 * <p>
	 * Events of this type are instances of {@link CommentEvent} and always have the
	 * topology type {@link EventTopologyType#SOLE} and therefore no nested events.
	 */
	COMMENT,
	
	
	// OTU events:
	
	/** 
	 * Indicates the start or the end of a list of OTU/taxon definitions. 
	 * <p>
	 * Start events of this type are instances of {@link LabeledIDEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	OTU_LIST,
	
	/** 
	 * Indicates the start or the end of an OTU/taxon definition.
	 * <p>
	 * Start events of this type are instances of {@link LabeledIDEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */ 
	OTU,
	
	
	// Matrix events:
	
	/** 
	 * Indicates the start or the end of the contents of a matrix. 
	 * <p>
	 * Start events of this type are instances of {@link LabeledIDEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	ALIGNMENT,
	
	/** 
	 * Indicates the start or the end of the contents of a sequence in a matrix. 
	 * <p>
	 * Start events of this type are instances of {@link LinkedOTUEvent}, end events are
	 * instances of {@link PartEndEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	SEQUENCE,
	
	/** 
	 * Indicates a number of sequence tokens. Events of this type never have nested elements. 
	 * <p>
	 * Events of this type are instances of {@link SequenceTokensEvent} and always have the
	 * topology type {@link EventTopologyType#SOLE}. 
	 */
	SEQUENCE_TOKENS,
	
	/** 
	 * Indicates a number a single sequence token. Events of this type may have nested elements. 
	 * <p>
	 * Start events of this type are instances of {@link SingleSequenceTokenEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	SINGLE_SEQUENCE_TOKEN,
	
	
	// Set events:
	
	/** 
	 * Indicates the start or end of a sequence of {@link #CHARACTER_SET_INTERVAL} events. 
	 * <p>
	 * Start events of this type are instances of {@link LabeledIDEvent}, end events are
	 * instances of {@link PartEndEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	CHARACTER_SET,
	
	/** 
	 * Indicates a single interval of a character set. Such events are nested 
	 * in {@link #CHARACTER_SET}.
	 * <p>
	 * Events of this type are instances of {@link CharacterSetIntervalEvent} and always have the
	 * topology type {@link EventTopologyType#SOLE}. 
	 */
	CHARACTER_SET_INTERVAL,
	
	/** 
	 * Indicates the start or end of a token set definition.
	 * <p>
	 * Start events of this type are instances of {@link LabeledIDEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 **/
	TOKEN_SET_DEFINITION,
	
	/** 
	 * Indicates the definition of a single sequence token symbol. Such events are nested 
	 * in {@link #TOKEN_SET_DEFINITION}.
	 * <p>
	 * Start events of this type are instances of {@link SingleTokenDefinitionEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	SINGLE_TOKEN_DEFINITION,
	
	
	// Tree and network events:
	
	/** 
	 * Indicates the start of the end of the contents of a phylogenetic network. 
	 * <p>
	 * Start events of this type are instances of {@link LabeledIDEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	NETWORK,

	/** 
	 * Indicates the start of the end of the contents of a phylogenetic tree. 
	 * <p>
	 * Start events of this type are instances of {@link LabeledIDEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}.
	 * <p>
	 * In some formats metaevents indicating whether the tree shall be displayed rooted or unrooted
	 * may follow the start event of this type. 
	 */
	TREE,
	
	/** 
	 * Indicates a node in a phylogenetic tree or network.
	 * <p> 
	 * Start events of this type are instances of {@link LinkedOTUEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	NODE,
	
	/** 
	 * Indicates an edge in a phylogenetic tree or network. 
	 * <p> 
	 * Start events of this type are instances of {@link EdgeEvent}, end events are
	 * instances of {@link ConcreteJPhyloIOEvent}.
	 * <p>
	 * This content type will never be combined with {@link EventTopologyType#SOLE}. 
	 */
	EDGE;
}
