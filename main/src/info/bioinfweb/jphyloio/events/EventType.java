/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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
package info.bioinfweb.jphyloio.events;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;



/**
 * Enumerates the types of events that can be triggered by {@link JPhyloIOEventReader}.
 * 
 * @author Ben St&ouml;ver
 */
public enum EventType {
	DOCUMENT_START,
	DOCUMENT_END,
	META_INFORMATION,
	COMMENT,
	
	ALIGNMENT_START,
	ALIGNMENT_END,
	SEQUENCE_CHARACTERS,
	CHARACTER_SET,
	TOKEN_SET_DEFINITION,
	SINGLE_TOKEN_DEFINITION,
	
	GRAPH_START,
	GRAPH_END,
	TREE_START,
	TREE_END,
	NODE_START,
	NODE_END,
	EDGE_START,
	EDGE_END;
}
