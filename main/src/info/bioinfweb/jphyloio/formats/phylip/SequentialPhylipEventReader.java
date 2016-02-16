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


import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.text.TextStreamDataProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;



/**
 * Event based reader for sequential Phylip alignment files.
 * <p>
 * For parsing non-sequential Phylip files use {@link PhylipEventReader} instaead.
 * <p>
 * The format is expected to be valid under the definition available here: 
 * <a href="http://evolution.genetics.washington.edu/phylip/doc/main.html#inputfiles">http://evolution.genetics.washington.edu/phylip/doc/main.html#inputfiles</a>.
 * The extended Phylip format is supported according to this definition:
 * <a href="http://www.phylo.org/index.php/help/relaxed_phylip">http://www.phylo.org/index.php/help/relaxed_phylip</a>.
 * 
 * @author Ben St&ouml;ver
 * @see PhylipEventReader
 */
public class SequentialPhylipEventReader extends AbstractPhylipEventReader<TextStreamDataProvider<SequentialPhylipEventReader>> {
	private int charactersRead = Integer.MAX_VALUE;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Phylip data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public SequentialPhylipEventReader(BufferedReader reader, boolean translateMatchToken, boolean relaxedPhylip) throws IOException {
		super(reader, translateMatchToken, relaxedPhylip);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the Phylip file to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public SequentialPhylipEventReader(File file, boolean translateMatchToken, boolean relaxedPhylip) throws IOException {
		super(file, translateMatchToken, relaxedPhylip);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the Phylip data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public SequentialPhylipEventReader(InputStream stream, boolean translateMatchToken, boolean relaxedPhylip) throws IOException {
		super(stream, translateMatchToken, relaxedPhylip);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Phylip data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public SequentialPhylipEventReader(Reader reader, boolean translateMatchToken, boolean relaxedPhylip) throws IOException {
		super(reader, translateMatchToken, relaxedPhylip);
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
						getUpcomingEvents().add(new LinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, 
								DEFAULT_MATRIX_ID_PREFIX + getIDManager().createNewID(), null, null));
						readMatrixDimensions();
					}  // Calling method will throw a NoSuchElementException for the else case. //TODO Check if this is still true after refactoring in r164.
					break;
					
				case ALIGNMENT:  // Only for the END case. START cannot happen, because it is directly followed by metaevents.
					getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
					break;
				case META_INFORMATION:
					if (getSequenceCount() == 0) {  // Empty alignment:
						getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
						break;
					}  // fall through
				case SEQUENCE_TOKENS:
					if (lineConsumed) {  // Keep current name if current line was not completely consumed yet.
						while (getReader().isNewLineNext()) {  // Ignore empty lines.
							getReader().consumeNewLine();
						}
						
						if (charactersRead >= getCharacterCount()) {  // Read next name:
							currentSequenceName = readSequenceName();
							charactersRead = 0;
							
							if (!getPreviousEvent().getType().getContentType().equals(EventContentType.META_INFORMATION)) {
								getUpcomingEvents().add(new PartEndEvent(EventContentType.SEQUENCE, 
										getSequenceTokensEventManager().getCurrentPosition() >= getCharacterCount()));
							}
							if (getReader().peek() != -1) {  // Do not start a new sequence, if the end of the alignment was reached.
								getUpcomingEvents().add(new LinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, 
										DEFAULT_SEQUENCE_ID_PREFIX + getIDManager().createNewID(), currentSequenceName, null));  // Saving IDs to names in not necessary, since sequential Phylip cannot be interleaved.
							}
						}
						
						if (getReader().peek() == -1) {  // End of file was reached
							// if (currentSequenceIndex < sequenceCount) {}  //TODO Should an exception be thrown here, if the specified number of sequences has not been found yet? => Probably not, because parsing files with a wrong number of specified sequences would still make sense, unless this is not a accidental stream break.
							getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
							break;
						}
					}
					
					// Read characters:
					JPhyloIOEvent event = readCharacters(currentSequenceName);
					if (event == null) {
						readNextEvent();  // Make sure to add an event to the list.
					}
					else {
						getUpcomingEvents().add(event);
						if (EventContentType.SEQUENCE_TOKENS.equals(event.getType().getContentType())) {
							charactersRead += ((SequenceTokensEvent)event).getCharacterValues().size();
						}
					}
					break;

				default:  // includes META_INFORMATION
					throw new InternalError("Impossible case");
			}
		}
	}	
}
