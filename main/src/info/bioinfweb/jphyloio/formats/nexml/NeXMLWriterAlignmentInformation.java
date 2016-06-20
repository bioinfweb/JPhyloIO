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
package info.bioinfweb.jphyloio.formats.nexml;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;



public class NeXMLWriterAlignmentInformation implements NeXMLConstants {
	private boolean writeCellsTags;
	private long alignmentLength; // Length of the longest sequence found in the alignment adapter
	private CharacterStateSetType alignmentType;
	
	private Map<String, NeXMLWriterTokenSetInformation> idToTokenSetInfoMap = new HashMap<String, NeXMLWriterTokenSetInformation>();
	
	private Map<String, String> charSetToTokenSetMap = new HashMap<String, String>();
	private Map<String, SortedSet<Long>> charSets = new HashMap<String, SortedSet<Long>>();
	Map<Long, String> columnIndexToIDMap = new HashMap<Long, String>();
	private Map<Long, String> columnIndexToStatesMap = new HashMap<Long, String>();
	
	private Set<String> definedTokens = new HashSet<String>();
	private Set<String> occuringTokens = new HashSet<String>();
	
	
	public boolean isWriteCellsTags() {
		return writeCellsTags;
	}


	public void setWriteCellsTags(boolean writeCellsTags) {
		this.writeCellsTags = writeCellsTags;
	}


	public long getAlignmentLength() {
		return alignmentLength;
	}


	public void setAlignmentLength(long alignmentLength) {
		this.alignmentLength = alignmentLength;
	}


	public CharacterStateSetType getAlignmentType() {
		return alignmentType;
	}


	public void setAlignmentType(CharacterStateSetType alignmentType) throws JPhyloIOWriterException {
		this.alignmentType = alignmentType;	
	}


	public boolean hasTokenDefinitionSet() {
		return !(getIDToTokenSetInfoMap().isEmpty() || (getIDToTokenSetInfoMap().size() == 1 && getIDToTokenSetInfoMap().containsKey(DEFAULT_TOKEN_DEFINITION_SET_ID)));
	}


	public Map<String, NeXMLWriterTokenSetInformation> getIDToTokenSetInfoMap() {
		return idToTokenSetInfoMap;
	}


	public Map<String, String> getCharSetToTokenSetMap() {
		return charSetToTokenSetMap;
	}
	

	public Map<String, SortedSet<Long>> getCharSets() {
		return charSets;
	}


	public Map<Long, String> getColumnIndexToIDMap() {
		return columnIndexToIDMap;
	}


	public Map<Long, String> getColumnIndexToStatesMap() {
		return columnIndexToStatesMap;
	}
	

	public Set<String> getDefinedTokens() {
		return definedTokens;
	}


	public Set<String> getOccuringTokens() {
		return occuringTokens;
	}
}
