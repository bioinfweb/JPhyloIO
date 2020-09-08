/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.events.replacing;


import java.util.HashMap;
import java.util.Map;

import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedIDEvent;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;



public class UniqueIDEventReplacer implements EventReplacer {
	Map<String, String> idReplacements = new HashMap<String, String>();

	
	private String modifyID(String id) {
		if (idReplacements.containsKey(id)) {
			int suffix = 1;
			while (idReplacements.containsKey(id + suffix)) {
				suffix++;
			}
			String newID = id + suffix;  // Note that the returned ID might still be identical with an upcoming ID which would then also be edited.
			idReplacements.put(id, newID);
			return newID;
		}
		else {
			idReplacements.put(id, id);
			return id;
		}
	}
	
	
	private String mapID(String oldID) {
		if (oldID == null) {
			return null;
		}
		else {
			if (idReplacements.containsKey(oldID)) {
				return idReplacements.get(oldID);
			}
			else {
				throw new InconsistentAdapterDataException("An event reference another event with the ID \"" + oldID + "\" which has not been encountered before.");  //TODO The exception is not ideal since this method is not necessarily called in the context of data adapters.
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public <E extends JPhyloIOEvent> E replaceEvent(E event) {
		if (event instanceof LabeledIDEvent) {
			LabeledIDEvent labeledIDEvent = event.asLabeledIDEvent();
			String newID = modifyID(labeledIDEvent.getID());
			
			if (event instanceof LinkedIDEvent) {
				LinkedIDEvent linkedIDEvent = event.asLinkedIDEvent();
				return (E)linkedIDEvent.cloneWithNewIDs(newID, mapID(linkedIDEvent.getLinkedID()));
			}
			else if (event instanceof EdgeEvent) {
				EdgeEvent edgeEvent = event.asEdgeEvent();
				return (E)edgeEvent.cloneWithNewIDs(newID, mapID(edgeEvent.getSourceID()), mapID(edgeEvent.getTargetID()));
			}
			else {  // LabeledIDEvent and all other inherited classes
				return (E)labeledIDEvent.cloneWithNewID(newID);
			}
		}
		else {
			return event;
		}
	}
}
