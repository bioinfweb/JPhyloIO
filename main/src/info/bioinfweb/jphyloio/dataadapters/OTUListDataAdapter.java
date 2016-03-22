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
	 * Returns an event describing the OTU list modeled by this instance. This event allows
	 * to specify a label and an ID, which will be used by some writers.
	 * 
	 * @return an event describing the OTU list
	 */
	public LabeledIDEvent getListStartEvent();
}
