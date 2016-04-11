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


import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;



/**
 * All data adapters possibly linking another data element (e.g. an OTU list or a matrix) should implement this interface.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public interface LinkedDataAdapter {
	/**
	 * Returns the start event of this data element (usually a matrix, tree or network). The returned
	 * event can be used to determine the label and ID of the modeled data element and an optionally linked
	 * data element. 
	 * 
	 * @return the start event of this data element
	 * @see OTUListDataAdapter#getListStartEvent()
	 */
	public LinkedLabeledIDEvent getStartEvent();
}
