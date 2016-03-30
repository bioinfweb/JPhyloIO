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


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;

import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.Iterator;



/**
 * Adapter interface used to provide any type of character matrix data to instances of 
 * {@link JPhyloIOEventWriter}, including token and character set definitions related to this 
 * matrix.
 * <p>
 * This interface also allows to provide unaligned data (e.g. for the UNALIGNED block in Nexus).
 * In such cases implementations need to return an according value in {@link #getColumnCount()}.
 * 
 * @author Ben St&ouml;ver
 */
public interface MatrixDataAdapter extends AnnotatedDataAdapter, LinkedOTUsDataAdapter {
  //TODO If token and character sets are modeled in here, does this cause problems when writing to a single SETS block in Nexus?
	
	/**
	 * Returns the number of sequences contained in this matrix.
	 * 
	 * @return the number of sequences that will be returned by {@link #getSequenceIDIterator()}
	 */
	public long getSequenceCount();
	
	/**
	 * Returns the number of columns the modeled matrix has, if it contains aligned data or -1 if it 
	 * contains unaligned data and each sequence may have a different length.
	 * <p>
	 * For some writers the return value of this method may also determine which type of sequence
	 * data is written. (A Nexus writer would e.g. use an UNALIGNED instead of a CHARACTERS block,
	 * if -1 is returned.)
	 * 
	 * @return the number of columns in the matrix or -1 if each sequence may have a different length
	 */
	public long getColumnCount();
	
	/**
	 * Returns whether tokens longer than one character are contained in the matrix modeled by this 
	 * instance. Some writers will use this information to determine, whether whitespace needs to be
	 * inserted to separate tokens.
	 * 
	 * @return {@code true} if tokens longer than one character may occur, or {@code false} if all tokens
	 *         are exactly one character long
	 */
	public boolean containsLongTokens();
	
	/**
	 * Returns a list of character sets defined for the matrix modeled by this instance.
	 * 
	 * @return a (possibly empty) list of character sets
	 */
	public ObjectListDataAdapter<LabeledIDEvent> getCharacterSets();
	
	/**
	 * Returns a list of token sets defined for the matrix modeled by this instance.
	 * <p>
	 * Note that character sets referenced by token sets provided here, are expected be contained
	 * in the return value of {@link #getCharacterSets()}.
	 * 
	 * @return a (possibly empty) list of token sets
	 */
	public ObjectListDataAdapter<TokenSetDefinitionEvent> getTokenSets();
	
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
	public Iterator<String> getSequenceIDIterator();
	
	/**
	 * Returns an event describing the sequence with the specified ID.
	 * 
	 * @param sequenceID the ID of the sequence to be described
	 * @return an linked OTU event describing the specified sequence
	 */
	public LinkedOTUOrOTUsEvent getSequenceStartEvent(String sequenceID);
	
	/**
	 * Returns the length for the specified sequence. If {@link #getColumnCount()} returns does not return -1,
	 * this method should return the same value as {@link #getColumnCount()} for each sequence. Otherwise it 
	 * may return different values for each sequence.
	 *  
	 * @param sequenceID the ID of the sequence which defined the length
	 * @return the length of the according sequence
	 * @throws IllegalArgumentException if an unknown sequence ID was specified
	 */
	public long getSequenceLength(String sequenceID) throws IllegalArgumentException;
	
	/**
	 * Implementing classes must write a sequence of events here, that describe the sequence tokens present in
	 * the specified column range. A valid event sequence corresponds to the grammar node 
	 * {@code SequencePartContent} in the documentation of {@link JPhyloIOEventReader}.
	 * <p>
	 * Note that (according to the grammar definition) metadata related to the sequence as a whole can also be
	 * provided to the {@code receiver} in this method. In most cases it makes sense to provide the according
	 * metaevents at the beginning of this sequences, i.e. if this method is called with {@code startColumn} = 0
	 * the first events written should be the metaevents for the whole sequence. (Note that the grammar in principle 
	 * allows such metaevents also between sequence token events, but not all formats (not all writers) support
	 * metadata at such a position.) The same applies to comment events. (See the documentation if the single 
	 * writers for further details on supported data.)
	 * <p>
	 * Note that column indices in <i>JPhyloIO</i> start with 0.   
	 * 
	 * @param receiver the receiver to write the events to
	 * @param sequenceID the ID of the sequence from which a part shall be written
	 * @param startColumn the first column of the sequence part to be written (inclusive)
	 * @param endColumn the last column of the sequence part to be written (exclusive)
	 * @throws IOException if a I/O error occurs while writing the data
	 * @throws IllegalArgumentException if an unknown sequence ID was specified
	 */
	public void writeSequencePartContentData(JPhyloIOEventReceiver receiver, String sequenceID, long startColumn, 
			long endColumn) throws IOException, IllegalArgumentException;
}
