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
package info.bioinfweb.jphyloio.dataadapters;


import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;



/**
 * Allows to access data from the application business model that make up an OTU list. 
 * 
 * @author Ben St&ouml;ver
 * @see DocumentDataAdapter
 * @see JPhyloIOEventWriter
 */
public interface OTUListDataAdapter extends ObjectListDataAdapter, AnnotatedDataAdapter {
	
	/**
	 * Returns the start event of an OTU definition determined by the specified OTU ID.
	 * <p>
	 * This method is meant for writers that just need access to a single OTU of which they already
	 * know the ID (because it was e.g. referenced by a sequence or tree node event), whereas 
	 * {@link #writeData(JPhyloIOEventReceiver, String)} is used to get the whole list of OTUs
	 * including their metadata.
	 * 
	 * @param otuID the ID of the requested OTU
	 * @return an instance of a labeled ID event that describes the specified OTU
	 * @throws IllegalArgumentException if no to OTU for the specified ID is present 
	 */
	public LabeledIDEvent getOTUStartEvent(String otuID) throws IllegalArgumentException;
}
