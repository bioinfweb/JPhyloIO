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
package info.bioinfweb.jphyloio.model.implementations;


import java.util.Iterator;

import info.bioinfweb.jphyloio.model.ElementCollection;



/**
 * Implementation of an empty {@link ElementCollection}. Instances of this class can be passed to
 * <i>JPhyloIO</i> writers, if no data of the type in this collection shall be written to the 
 * document.
 * 
 * @author Ben St&ouml;ver
 *
 * @param <E> the type of element in this collection
 * @see AbstractElementCollection
 */
public class EmptyElementCollection<E> extends AbstractElementCollection<E> {
	@Override
	public long size() {
		return 0;
	}

	
	@Override
	public Iterator<E> iterator() {
		throw new IndexOutOfBoundsException("There are no elements in this collection.");
	}
}
