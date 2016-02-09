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


import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;



/**
 * Abstract implementation of {@link MatrixDataAdapter}, which returns empty object list adapters
 * for {@link #getTokenSets()} and {@link #getCharacterSets()}. Additionally an empty implementation
 * of {@link #writeMetadata(info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver)} is inherited.
 * <p>
 * Application adapters that do not provide any token or character sets can be inherited from this class.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class NoSetsMatrixDataAdapter extends EmptyAnnotatedDataAdapter implements MatrixDataAdapter {
	/**
	 * Default implementation that always returns an empty object list adapter 
	 * 
	 * @return a shared instance of {@link EmptyObjectListDataAdapter}
	 * @see info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter#getTokenSets()
	 */
	@Override
	public ObjectListDataAdapter getTokenSets() {
		return EmptyObjectListDataAdapter.SHARED_EMPTY_OBJECT_LIST_ADAPTER;
	}
	
	
	/**
	 * Default implementation that always returns an empty object list adapter 
	 * 
	 * @return a shared instance of {@link EmptyObjectListDataAdapter}
	 * @see info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter#getCharacterSets()
	 */
	@Override
	public ObjectListDataAdapter getCharacterSets() {
		return EmptyObjectListDataAdapter.SHARED_EMPTY_OBJECT_LIST_ADAPTER;
	}
}