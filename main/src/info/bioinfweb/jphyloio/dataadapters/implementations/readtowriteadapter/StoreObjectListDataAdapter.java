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
package info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter;


import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.map.ListOrderedMap;



/**
 * @author s_wiec03
 *
 * @param <E>
 */
public class StoreObjectListDataAdapter<E extends JPhyloIOEvent> implements ObjectListDataAdapter<E> {	
	private ListOrderedMap<String, StoreObjectData<E>> objectMap = new ListOrderedMap<String, StoreObjectData<E>>();
	

	public ListOrderedMap<String, StoreObjectData<E>> getObjectMap() {
		return objectMap;
	}


	@Override
	public E getObjectStartEvent(String id)	throws IllegalArgumentException {
		return objectMap.get(id).getObjectStartEvent();
	}
	
	
	public void setObjectStartEvent(String id, E event)	throws IllegalArgumentException {
		objectMap.get(id).setObjectStartEvent(event);
	}
	
	
	public List<JPhyloIOEvent> getObjectContent(String id) {
		return objectMap.get(id).getObjectContent();
	}

	
	@Override
	public long getCount() {
		return objectMap.size();
	}

	
	@Override
	public Iterator<String> getIDIterator() {
		return objectMap.keyList().iterator();
	}

	
	@Override
	public void writeContentData(JPhyloIOEventReceiver receiver, String id)
			throws IOException, IllegalArgumentException {
		for (JPhyloIOEvent event : objectMap.get(id).getObjectContent()) {
			receiver.add(event);
		}		
	}	
}
