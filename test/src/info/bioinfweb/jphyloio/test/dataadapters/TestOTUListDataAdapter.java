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
package info.bioinfweb.jphyloio.test.dataadapters;


import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.collections4.map.ListOrderedMap;

import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyAnnotatedDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public class TestOTUListDataAdapter extends EmptyAnnotatedDataAdapter implements OTUListDataAdapter {
	private int indexOfList;
	private ListOrderedMap<String, LabeledIDEvent> otus = new ListOrderedMap<String, LabeledIDEvent>();
	
	
	public TestOTUListDataAdapter(int indexOfList, LabeledIDEvent... otus) {
		super();
		this.indexOfList = indexOfList;
		for (int i = 0; i < otus.length; i++) {
			this.otus.put(otus[i].getID(), otus[i]);
		}
	}
	

	public TestOTUListDataAdapter(int indexOfList, String... otuIDs) {
		super();
		this.indexOfList = indexOfList;
		for (int i = 0; i < otuIDs.length; i++) {
			this.otus.put(otuIDs[i], new LabeledIDEvent(EventContentType.OTU, otuIDs[i], "OTU " + otuIDs[i]));
		}
	}
	

	public ListOrderedMap<String, LabeledIDEvent> getOtus() {
		return otus;
	}


	@Override
	public String getID() {
		return ReadWriteConstants.DEFAULT_OTU_LIST_ID_PREFIX + indexOfList;
	}


	@Override
	public long getCount() {
		return otus.size();
	}

	
	@Override
	public Iterator<String> getIDIterator() {
		return otus.keySet().iterator();
	}

	
	@Override
	public void writeData(JPhyloIOEventReceiver receiver, String id) throws IllegalArgumentException, IOException {
		receiver.add(otus.get(id));
		receiver.add(new ConcreteJPhyloIOEvent(EventContentType.OTU, EventTopologyType.END));
	}

	
	@Override
	public LabeledIDEvent getListStartEvent() {
		return new LabeledIDEvent(EventContentType.OTU_LIST, "otus" + indexOfList, "OTU list " + indexOfList);
	}

	
	@Override
	public LabeledIDEvent getOTUStartEvent(String otuID) throws IllegalArgumentException {
		return otus.get(otuID);
	}
}
