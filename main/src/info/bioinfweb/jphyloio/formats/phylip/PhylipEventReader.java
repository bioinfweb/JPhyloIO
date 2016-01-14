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
package info.bioinfweb.jphyloio.formats.phylip;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



/**
 * Event based reader for Phylip alignment files.
 * <p>
 * The format is expected to be valid under the definition available here: 
 * <a href="http://evolution.genetics.washington.edu/phylip/doc/main.html#inputfiles">http://evolution.genetics.washington.edu/phylip/doc/main.html#inputfiles</a>.
 * The extended Phylip format is supported according to this definition:
 * <a href="http://www.phylo.org/index.php/help/relaxed_phylip">http://www.phylo.org/index.php/help/relaxed_phylip</a>.
 * 
 * @author Ben St&ouml;ver
 * @see SequentialPhylipEventReader
 */
public class PhylipEventReader extends AbstractPhylipEventReader {
	private boolean allowInterleavedParsing = true;
	private List<String> sequenceNames = new ArrayList<String>();
	private int currentSequenceIndex = 0;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Phylip data to be read
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param allowInterleavedParsing defines whether interleaved Phylip files shall be supported by this parser instance
	 *        (In order to support this feature the reader needs to keep a list of all sequence names. To parse files with 
	 *        a very large number of sequences which are not interleaved, this feature can be switched off to save memory.
	 *        If this switch is set to {@code true} non-interleaved files can also still be parsed.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public PhylipEventReader(BufferedReader reader, boolean translateMatchToken, boolean allowInterleavedParsing, 
			boolean relaxedPhylip) throws IOException {
		
		super(reader, translateMatchToken, relaxedPhylip);
		this.allowInterleavedParsing = allowInterleavedParsing;
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the Phylip file to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param allowInterleavedParsing defines whether interleaved Phylip files shall be supported by this parser instance
	 *        (In order to support this feature the reader needs to keep a list of all sequence names. To parse files with 
	 *        a very large number of sequences which are not interleaved, this feature can be switched off to save memory.
	 *        If this switch is set to {@code true} non-interleaved files can also still be parsed.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public PhylipEventReader(File file, boolean translateMatchToken, boolean allowInterleavedParsing, boolean relaxedPhylip) 
			throws IOException {
		
		super(file, translateMatchToken, relaxedPhylip);
		this.allowInterleavedParsing = allowInterleavedParsing;
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the Phylip data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param allowInterleavedParsing defines whether interleaved Phylip files shall be supported by this parser instance
	 *        (In order to support this feature the reader needs to keep a list of all sequence names. To parse files with 
	 *        a very large number of sequences which are not interleaved, this feature can be switched off to save memory.
	 *        If this switch is set to {@code true} non-interleaved files can also still be parsed.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public PhylipEventReader(InputStream stream, boolean translateMatchToken, boolean allowInterleavedParsing, 
			boolean relaxedPhylip) throws IOException {
		
		super(stream, translateMatchToken, relaxedPhylip);
		this.allowInterleavedParsing = allowInterleavedParsing;
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Phylip data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param allowInterleavedParsing defines whether interleaved Phylip files shall be supported by this parser instance
	 *        (In order to support this feature the reader needs to keep a list of all sequence names. To parse files with 
	 *        a very large number of sequences which are not interleaved, this feature can be switched off to save memory.
	 *        If this switch is set to {@code true} non-interleaved files can also still be parsed.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public PhylipEventReader(Reader reader, boolean translateMatchToken, boolean allowInterleavedParsing, 
			boolean relaxedPhylip) throws IOException {
		
		super(reader, translateMatchToken, relaxedPhylip);
		this.allowInterleavedParsing = allowInterleavedParsing;
	}
	
	
	private void increaseSequenceIndex() {
		currentSequenceIndex++;
		if (currentSequenceIndex >= getSequenceCount()) {
			currentSequenceIndex -= getSequenceCount();
		}
	}
	
	
	@Override
	protected void readNextEvent() throws Exception {
		if (isBeforeFirstAccess()) {
			getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
		}
		else {
			switch (getPreviousEvent().getType().getContentType()) {
				case DOCUMENT:
					if (getPreviousEvent().getType().getTopologyType().equals(EventTopologyType.START)) {
						readMatrixDimensions();
						
						getUpcomingEvents().add(createAlignmentStartEvent(getSequenceCount(), getCharacterCount()));
					}  // Calling method will throw a NoSuchElementException for the else case. //TODO Check if this is still true after refactoring in r164.
					break;
					
				case ALIGNMENT:
					if (getPreviousEvent().getType().getTopologyType().equals(EventTopologyType.START)) {
						if (getSequenceCount() == 0) {  // Empty alignment:
							getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
							break;
						}  // fall through
					}
					else {
						getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
						break;
					}
				case SEQUENCE_TOKENS:
					if (lineConsumed) {  // Keep current name if current line was not completely consumed yet.
						while (getReader().isNewLineNext()) {  // Ignore empty lines between interleaved blocks.
							getReader().consumeNewLine();  // Note: Parsing this way does not allow to have empty lines for some sequences in interleaved format, if they ended earlier than others. (Sequences with different lengths are anyway not allowed by the format definition. E.g. unaligned could still be read from non-interleaved files.
						}
						
						if (!allowInterleavedParsing || sequenceNames.size() < getSequenceCount()) {  // Read name from first (interleaved) block:
							currentSequenceName = readSequenceName();
							sequenceNames.add(currentSequenceName);
						}
						else {
							if (allowInterleavedParsing) {  // Reuse saved name for interleaved lines:
								currentSequenceName = sequenceNames.get(currentSequenceIndex);
							}
							else {  // Not saved names available
								throw new IllegalStateException("Interleaved Phylip format found, although interleaved parsing was not allowed.");
							}
						}
						increaseSequenceIndex();
						
						if (getReader().peek() == -1) {  // End of file was reached
							// if (currentSequenceIndex < sequenceCount) {}  //TODO Should an exception be thrown here, if the specified number of sequences has not been found yet? => Probably not, because parsing files with a wrong number of specified sequences would still make sense, unless this is not a accidental stream break.
							getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
							break;
						}
					}
					JPhyloIOEvent event = readCharacters(currentSequenceName);
					if (event != null) {
						getUpcomingEvents().add(event);
					}
					else {
						readNextEvent();  // Make sure to add an event to the list.
					}
					break;
					
				default:  // includes META_INFORMATION
					throw new InternalError("Impossible case");
			}
		}
	}
}
