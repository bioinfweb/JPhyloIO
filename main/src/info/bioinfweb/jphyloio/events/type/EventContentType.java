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
	DOCUMENT,
	META_INFORMATION,
	UNKNOWN_COMMAND,
	COMMENT,
	
	OTU_LIST,
	OTU,
	
	ALIGNMENT,
	SEQUENCE,
	SEQUENCE_TOKENS,
	SINGLE_SEQUENCE_TOKEN,
	
	/** Indicates the start or end of a sequence of {@link #CHARACTER_SET_INTERVAL} events. */
	CHARACTER_SET,
	CHARACTER_SET_INTERVAL,
	TOKEN_SET_DEFINITION,
	SINGLE_TOKEN_DEFINITION,
	
	NETWORK,
	TREE,
	NODE,
	EDGE;
}
