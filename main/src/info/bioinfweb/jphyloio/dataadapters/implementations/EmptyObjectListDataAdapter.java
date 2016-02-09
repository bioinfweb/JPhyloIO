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


import java.util.Collections;
import java.util.Iterator;

import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;



/**
 * Empty implementation of {@link EmptyObjectListDataAdapter}. Can be used by applications
 * that do not model any of the according objects.
 * 
 * @author Ben St&ouml;ver
 */
public class EmptyObjectListDataAdapter implements ObjectListDataAdapter {
	public static final EmptyObjectListDataAdapter SHARED_EMPTY_OBJECT_LIST_ADAPTER = new EmptyObjectListDataAdapter();
	
	
	/**
	 * This default implementation always returns 0.
	 * 
	 * @return always 0
	 * @see info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter#getCount()
	 */
	@Override
	public long getCount() {
		return 0;
	}

	
	/**
	 * This default implementation always returns an empty iterator.
	 * 
	 * @return always an empty iterator
	 * @see info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter#getIDIterator()
	 */
	@Override
	public Iterator<String> getIDIterator() {
		return Collections.emptyIterator();
	}

	
	/**
	 * Always throws an {@link IllegalArgumentException}, since this default implementation does not contain
	 * any objects.
	 * 
	 * @see info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter#writeData(info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver, java.lang.String)
	 */
	@Override
	public void writeData(JPhyloIOEventReceiver receiver, String id) throws IllegalArgumentException {
		throw new IllegalArgumentException("No object with the ID \"" + id + "\" is offered by this adapter.");
	}
}