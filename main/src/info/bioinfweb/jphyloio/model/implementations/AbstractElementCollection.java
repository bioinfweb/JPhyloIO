/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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
package info.bioinfweb.jphyloio.model.implementations;


import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.model.ElementCollection;



/**
 * Basic implementation of an {@link ElementCollection} that does not contain any comments or metadata.
 * 
 * @author Ben St&ouml;ver
 *
 * @param <E> the type of element in this collection
 */
public abstract class AbstractElementCollection<E> implements ElementCollection<E> {
	@Override
	public long getMetaCommentEventCount(long elementIndex) {
		return 0;
	}

	
	@Override
	public JPhyloIOEvent getMetaCommentEvent(long elementIndex, long metaDataIndex) {
		throw new IndexOutOfBoundsException(
				"There are not meta or comment events present for the element with the index " + elementIndex + ".");
	}
}
