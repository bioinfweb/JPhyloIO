/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;

import java.util.Iterator;



public interface TreeNetworkGroupDataAdapter extends AnnotatedDataAdapter<LinkedLabeledIDEvent> {	
	/**
	 * Returns an iterator providing access to all trees and networks contained in the 
	 * document to be written. 
	 * @param parameters TODO
	 * 
	 * @return the iterator (Maybe empty but not {@code null}.)
	 */
	public Iterator<TreeNetworkDataAdapter> getTreeNetworkIterator(ReadWriteParameterMap parameters);
	
	/**
	 * Returns a list of tree sets defined for the tree-network-group modeled by this instance.
	 * @param parameters TODO
	 * 
	 * @return a (possibly empty) list of tree sets
	 */
	public ObjectListDataAdapter<LinkedLabeledIDEvent> getTreeSets(ReadWriteParameterMap parameters);
}
