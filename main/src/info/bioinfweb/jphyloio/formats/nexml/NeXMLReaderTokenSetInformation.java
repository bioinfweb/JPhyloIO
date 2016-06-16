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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;



public class NeXMLReaderTokenSetInformation {
	private String id = null;
	private String label = null;
	private CharacterStateSetType setType;
	
	private Collection<JPhyloIOEvent> nestedEvents = new ArrayList<JPhyloIOEvent>();
	
	private Map<Integer, String> symbolTranslationMap = new HashMap<Integer, String>(); //is only used for standard data
	
	
	public NeXMLReaderTokenSetInformation(String id, String label, CharacterStateSetType setType) {
		super();
		if (id == null) {
			throw new NullPointerException("The ID of this event must not be null.");
		}
		else if (setType == null) {
			throw new NullPointerException("The set type of this event must not be null.");
		}
		else {
			this.id = id; 
			this.label = label;
			this.setType = setType;
		}
	}


	public String getID() {
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
	
	
	public CharacterStateSetType getSetType() {
		return setType;
	}
	
	
	public void setSetType(CharacterStateSetType setType) {
		this.setType = setType;
	}


	protected Collection<JPhyloIOEvent> getNestedEvents() {
		return nestedEvents;
	}


	public Map<Integer, String> getSymbolTranslationMap() {
		return symbolTranslationMap;
	}
}
