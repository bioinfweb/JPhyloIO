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
package info.bioinfweb.jphyloio.dataadapters.implementations;


import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public class EmptyOTUListDataAdapter implements OTUListDataAdapter {
	private int otuListIndex;
	
	
	public EmptyOTUListDataAdapter(int otuListIndex) {
		super();
		this.otuListIndex = otuListIndex;
	}


	@Override
	public LabeledIDEvent getObjectStartEvent(String id) throws IllegalArgumentException {
		throw new IllegalArgumentException("No OTU with the ID \"" + id + "\" is offered by this adapter.");
	}
	

	@Override
	public long getCount() {
		return 0;
	}

	
	@Override
	public Iterator<String> getIDIterator() {
		return Collections.emptyIterator();
	}

	
	@Override
	public void writeContentData(JPhyloIOEventReceiver receiver, String id) throws IOException, IllegalArgumentException {}

	
	@Override
	public void writeMetadata(JPhyloIOEventReceiver receiver) throws IOException {}

	
	@Override
	public boolean hasMetadata() {
		return false;
	}

	
	@Override
	public LabeledIDEvent getListStartEvent() {
		return new LabeledIDEvent(EventContentType.OTU_LIST, ReadWriteConstants.DEFAULT_OTU_LIST_ID_PREFIX + otuListIndex, "Undefined OTUs");
	}	
}
