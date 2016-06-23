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


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;



public class StoreOTUListDataAdapter extends StoreAnnotatedDataAdapter<LabeledIDEvent> implements OTUListDataAdapter {
	private LabeledIDEvent listStartEvent;
	private StoreObjectListDataAdapter<LabeledIDEvent> otus = new StoreObjectListDataAdapter<LabeledIDEvent>();
	private StoreObjectListDataAdapter<LinkedLabeledIDEvent> otuSets = new StoreObjectListDataAdapter<LinkedLabeledIDEvent>();


	public StoreOTUListDataAdapter(LabeledIDEvent listStartEvent, List<JPhyloIOEvent> annotations) {
		super(annotations);
		this.listStartEvent = listStartEvent;
		this.otus = new StoreObjectListDataAdapter<LabeledIDEvent>();
		this.otuSets =  new StoreObjectListDataAdapter<LinkedLabeledIDEvent>();
	}
	
	
	@Override
	public LabeledIDEvent getStartEvent(ReadWriteParameterMap parameters) {
		return listStartEvent;
	}


	public void setListStartEvent(LabeledIDEvent listStartEvent) {
		this.listStartEvent = listStartEvent;
	}


	public StoreObjectListDataAdapter<LabeledIDEvent> getOtus() {
		return otus;
	}


	@Override
	public LabeledIDEvent getObjectStartEvent(String id) throws IllegalArgumentException {
		return otus.getObjectStartEvent(id);
	}


	@Override
	public long getCount() {
		return otus.getCount();
	}


	@Override
	public Iterator<String> getIDIterator() {
		return otus.getIDIterator();
	}


	@Override
	public void writeContentData(JPhyloIOEventReceiver receiver, String id) throws IOException, IllegalArgumentException {
		otus.writeContentData(receiver, id);
	}


	@Override
	public StoreObjectListDataAdapter<LinkedLabeledIDEvent> getOTUSets(ReadWriteParameterMap parameters) {
		return otuSets;
	}
}
