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


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;



public class UndefinedOTUListDataAdapter implements OTUListDataAdapter {
	public static final String UNDEFINED_OTU_ID = "undefinedTaxon";
	public static final String UNDEFINED_OTUS_ID = "undefinedTaxa";
	
	
	private LabeledIDEvent undefinedOTU = new LabeledIDEvent(EventContentType.OTU, UNDEFINED_OTU_ID, "undefined taxon");
	
	
	public UndefinedOTUListDataAdapter() {
		super();
	}


	@Override
	public LabeledIDEvent getObjectStartEvent(String id) throws IllegalArgumentException {
		if (id.equals(UNDEFINED_OTU_ID)) {
			return undefinedOTU;
		}
		else {
			throw new IllegalArgumentException("No OTU with the ID \"" + id + "\" is offered by this adapter.");
		}
	}
	

	@Override
	public long getCount() {
		return 1;
	}

	
	@Override
	public Iterator<String> getIDIterator() {
		return Arrays.asList(new String[]{UNDEFINED_OTU_ID}).iterator();
	}
	
	
	public String getUndefinedOtuID() {
		return undefinedOTU.getID();
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
		return new LabeledIDEvent(EventContentType.OTU_LIST, UNDEFINED_OTUS_ID, "undefined taxa");
	}	
}