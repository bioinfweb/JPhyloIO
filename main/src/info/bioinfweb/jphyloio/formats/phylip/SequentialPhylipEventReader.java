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


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.text.TextReaderStreamDataProvider;

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
 * <h3><a name="parameters"></a>Recognized parameters</h3> 
 * <ul>
 *   <li>{@link ReadWriteParameterMap#KEY_REPLACE_MATCH_TOKENS}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_RELAXED_PHYLIP}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_LOGGER}</li>
 * </ul>
 * 
 * @author Ben St&ouml;ver
 * @see PhylipEventReader
 */
public class SequentialPhylipEventReader extends AbstractPhylipEventReader<TextReaderStreamDataProvider<SequentialPhylipEventReader>> {
	private int charactersRead = Integer.MAX_VALUE;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Phylip data to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public SequentialPhylipEventReader(BufferedReader reader, ReadWriteParameterMap parameters) throws IOException {
		super(reader, parameters);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the Phylip file to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public SequentialPhylipEventReader(File file, ReadWriteParameterMap parameters) throws IOException {
		super(file, parameters);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the Phylip data to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public SequentialPhylipEventReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException {
		super(stream, parameters);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Phylip data to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public SequentialPhylipEventReader(Reader reader, ReadWriteParameterMap parameters) throws IOException {
		super(reader, parameters);
	}
	
	
	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.SEQUENTIAL_PHYLIP_FORMAT_ID;
	}


	@Override
	protected void readNextEvent() throws IOException {
		if (isBeforeFirstAccess()) {
			getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
		}
		else {
			switch (getPreviousEvent().getType().getContentType()) {
				case DOCUMENT:
					if (getPreviousEvent().getType().getTopologyType().equals(EventTopologyType.START)) {
						getCurrentEventCollection().add(new LinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, 
								DEFAULT_MATRIX_ID_PREFIX + getIDManager().createNewID(), null, null));
						readMatrixDimensions();
					}  // Calling method will throw a NoSuchElementException for the else case. //TODO Check if this is still true after refactoring in r164.
					break;
					
				case ALIGNMENT:  // Only for the END case. START cannot happen, because it is directly followed by metaevents.
					getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
					break;
				case META_INFORMATION:
					if (getSequenceCount() == 0) {  // Empty alignment:
						getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
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
								getCurrentEventCollection().add(new PartEndEvent(EventContentType.SEQUENCE, 
										getSequenceTokensEventManager().getCurrentPosition() >= getCharacterCount()));
							}
							if (getReader().peek() != -1) {  // Do not start a new sequence, if the end of the alignment was reached.
								getCurrentEventCollection().add(new LinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, 
										DEFAULT_SEQUENCE_ID_PREFIX + getIDManager().createNewID(), currentSequenceName, null));  // Saving IDs to names in not necessary, since sequential Phylip cannot be interleaved.
							}
						}
						
						if (getReader().peek() == -1) {  // End of file was reached
							// if (currentSequenceIndex < sequenceCount) {}  //TODO Should an exception be thrown here, if the specified number of sequences has not been found yet? => Probably not, because parsing files with a wrong number of specified sequences would still make sense, unless this is not a accidental stream break.
							getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
							break;
						}
					}
					
					// Read characters:
					JPhyloIOEvent event = readCharacters(currentSequenceName);
					if (event == null) {
						readNextEvent();  // Make sure to add an event to the list.
					}
					else {
						getCurrentEventCollection().add(event);
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
