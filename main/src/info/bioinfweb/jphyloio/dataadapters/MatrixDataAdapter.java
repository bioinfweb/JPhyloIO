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


import java.util.ConcurrentModificationException;
import java.util.Iterator;



public interface MatrixDataAdapter extends AnnotatedDataAdapter {
	//TODO Allow exceptions from write methods?
  //TODO If token and character sets are modeled in here, does this cause problems when writing to a single SETS block in Nexus?
	
	public boolean isAligned();
	
	public long getSequenceCount();
	
	public long getColumnCount();
	
	public ObjectListDataAdapter getCharacterSets();
	
	public ObjectListDataAdapter getTokenSets();
	
	/**
	 * Returns an iterator returning the IDs of all sequences in the represented matrix.
	 * <p>
	 * Note that the returned iterator will be in ongoing use while other methods (e.g. 
	 * {@link #writeSequencePartContent(JPhyloIOEventReceiver, String, long, long)}) are called.
	 * Therefore implementing classes should make sure, that the source of the iterator is not
	 * modified while a <i>JPhyloIO</i> writer is in use. (The iterator should not throw a
	 * {@link ConcurrentModificationException} before writing the target document is finished.)
	 * 
	 * @return an iterator returning the edge IDs (Must return at least one element.)
	 */
	public Iterator<String> getSequenceIDs();
	
	public void writeSequencePartContentData(JPhyloIOEventReceiver writer, String sequenceID, long startColumn, long endColumn);
}
