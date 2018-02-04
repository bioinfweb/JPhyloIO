/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2018  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.test.dataadapters;


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyAnnotatedDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreObjectListDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.collections4.map.ListOrderedMap;



public class TestOTUListDataAdapter extends EmptyAnnotatedDataAdapter<LabeledIDEvent> implements OTUListDataAdapter {
	private int indexOfList;
	private ListOrderedMap<String, LabeledIDEvent> otus = new ListOrderedMap<String, LabeledIDEvent>();
	private StoreObjectListDataAdapter<LinkedLabeledIDEvent> otuSets = new StoreObjectListDataAdapter<LinkedLabeledIDEvent>();
	
	
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
	public long getCount(ReadWriteParameterMap parameters) {
		return otus.size();
	}

	
	@Override
	public Iterator<String> getIDIterator(ReadWriteParameterMap parameters) {
		return otus.keySet().iterator();
	}

	
	@Override
	public void writeContentData(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, String id) throws IOException {}

	
	@Override
	public LabeledIDEvent getStartEvent(ReadWriteParameterMap parameters) {
		return new LabeledIDEvent(EventContentType.OTU_LIST, ReadWriteConstants.DEFAULT_OTU_LIST_ID_PREFIX + indexOfList, 
				"OTU list " + indexOfList);
	}

	
	@Override
	public LabeledIDEvent getObjectStartEvent(ReadWriteParameterMap parameters, String otuID) throws IllegalArgumentException {
		return otus.get(otuID);
	}


	@Override
	public StoreObjectListDataAdapter<LinkedLabeledIDEvent> getOTUSets(ReadWriteParameterMap parameters) {
		return otuSets;
	}
}