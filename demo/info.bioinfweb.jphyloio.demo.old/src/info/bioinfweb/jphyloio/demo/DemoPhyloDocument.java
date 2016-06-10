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
package info.bioinfweb.jphyloio.demo;


import java.util.Collection;
import java.util.Iterator;

import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.model.CharacterData;
import info.bioinfweb.jphyloio.model.ElementCollection;
import info.bioinfweb.jphyloio.model.GraphData;
import info.bioinfweb.jphyloio.model.PhyloDocument;



public class DemoPhyloDocument implements PhyloDocument {
	private ElementCollection<CharacterData> characterData;
	private Collection<JPhyloIOEvent> metaCommentEvents;
	
	
	public DemoPhyloDocument(ElementCollection<CharacterData> characterData, Collection<JPhyloIOEvent> metaCommentEvents) {
		super();
		this.characterData = characterData;
		this.metaCommentEvents = metaCommentEvents;
	}


	@Override
	public ElementCollection<CharacterData> getCharacterDataCollection() {
		return characterData;
	}

	
	@Override
	public ElementCollection<GraphData> getGraphDataCollection() { //TODO return actual graph data
		return null;
	}
	

	@Override
	public long getMetaCommentEventCount() {
		return metaCommentEvents.size();
	}	
	

	@Override
	public Iterator<JPhyloIOEvent> getMetaCommentEventIterator() {
		return metaCommentEvents.iterator();
	}	
}
