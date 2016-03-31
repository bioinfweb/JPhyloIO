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
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;

import java.io.IOException;
import java.util.List;



public class StoreOTUListDataAdapter extends StoreObjectListDataAdapter<LabeledIDEvent> implements OTUListDataAdapter {
	private LabeledIDEvent listStartEvent;
	private StoreAnnotatedDataAdapter annotationAdapter;


	public StoreOTUListDataAdapter(LabeledIDEvent listStartEvent, List<JPhyloIOEvent> annotations) {
		super();
		this.listStartEvent = listStartEvent;
		annotationAdapter = new StoreAnnotatedDataAdapter(annotations);
	}
	
	
	@Override
	public LabeledIDEvent getListStartEvent() {
		return listStartEvent;
	}


	public void setListStartEvent(LabeledIDEvent listStartEvent) {
		this.listStartEvent = listStartEvent;
	}


	public void writeMetadata(JPhyloIOEventReceiver receiver) throws IOException {
		annotationAdapter.writeMetadata(receiver);
	}


	public boolean hasMetadata() {
		return annotationAdapter.hasMetadata();
	}


	public List<JPhyloIOEvent> getAnnotations() {
		return annotationAdapter.getAnnotations();
	}
}
