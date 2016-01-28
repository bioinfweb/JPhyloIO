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
package info.bioinfweb.jphyloio.formats.nexml;


import java.util.Collection;

import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;



public class NeXMLTokenSetInformation {
	private String id = null;
	private String label = null;
	private CharacterStateType setType;
	private String characterSetID = null;
	
	private Collection<JPhyloIOEvent> singleTokens;
	private Collection<JPhyloIOEvent> charSetIntervals;
	
	
	public NeXMLTokenSetInformation(String id, String label, CharacterStateType setType) { //TODO make sure ID and set type are not null
		super();		
		this.id = id; 
		this.label = label;
		this.setType = setType;
	}


	public String getId() {
		return id;
	}
	
	
	public void setId(String id) {
		this.id = id;
	}
	
	
	public String getLabel() {
		return label;
	}
	
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	
	public CharacterStateType getSetType() {
		return setType;
	}
	
	
	public void setSetType(CharacterStateType setType) {
		this.setType = setType;
	}
	
	
	public String getCharacterSetID() {
		return characterSetID;
	}
	
	
	public void setCharacterSetID(String characterSetID) {
		this.characterSetID = characterSetID;
	}


	public Collection<JPhyloIOEvent> getSingleTokens() {
		return singleTokens;
	}


	public void setSingleTokens(Collection<JPhyloIOEvent> singleTokens) {
		this.singleTokens = singleTokens;
	}


	public Collection<JPhyloIOEvent> getCharSetIntervals() {
		return charSetIntervals;
	}


	public void setCharSetIntervals(Collection<JPhyloIOEvent> charSetIntervals) {
		this.charSetIntervals = charSetIntervals;
	}
}
