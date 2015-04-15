/*
 * PhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
 * <http://bioinfweb.info/PhyloIO>
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
package info.bioinfweb.phyloio.formats.phylip;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.phyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.phyloio.events.ConcretePhyloIOEvent;
import info.bioinfweb.phyloio.events.EventType;
import info.bioinfweb.phyloio.events.PhyloIOEvent;
import info.bioinfweb.phyloio.events.SequenceCharactersEvent;



/**
 * Event based reader for Phylip alignment files.
 * <p>
 * The format is expected to be valid under the definition available here: 
 * <a href="http://evolution.genetics.washington.edu/phylip/doc/main.html#inputfiles">http://evolution.genetics.washington.edu/phylip/doc/main.html#inputfiles</a>.
 * The extended Phylip format is supported according to this definition:
 * <a href="http://www.phylo.org/index.php/help/relaxed_phylip">http://www.phylo.org/index.php/help/relaxed_phylip</a>.
 * 
 * @author Ben St&ouml;ver
 */
public class PhylipEventReader extends AbstractBufferedReaderBasedEventReader {
	public static final int DEFAULT_NAME_LENGTH = 10;
	public static final String NAME_END_CHARACTER = "\t";
	
	
	private boolean allowInterleavedParsing = true;
	private boolean relaxedPhylip = false;
	private int sequenceCount = -1;
	private int characterCount = -1;
	private String currentSequenceName = null;
	private boolean lineConsumed = true;
	private List<String> sequenceNames = new ArrayList<String>();
	private int currentSequenceIndex = 0;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Phylip data to be read 
	 * @param allowInterleavedParsing defines whether interleaved Phylip files shall be supported by this parser instance
	 *        (In order to support this feature the reader needs to keep a list of all sequence names. To parse files with 
	 *        a very large number of sequences which are not interleaved, this feature can be switched off to save memory.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 */
	public PhylipEventReader(BufferedReader reader, boolean allowInterleavedParsing, boolean relaxedPhylip) throws IOException {
		super(reader);
		this.allowInterleavedParsing = allowInterleavedParsing;
		this.relaxedPhylip = relaxedPhylip;
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the Phylip file to be read 
	 * @param allowInterleavedParsing defines whether interleaved Phylip files shall be supported by this parser instance
	 *        (In order to support this feature the reader needs to keep a list of all sequence names. To parse files with 
	 *        a very large number of sequences which are not interleaved, this feature can be switched off to save memory.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 */
	public PhylipEventReader(File file, boolean allowInterleavedParsing, boolean relaxedPhylip) throws IOException {
		super(file);
		this.allowInterleavedParsing = allowInterleavedParsing;
		this.relaxedPhylip = relaxedPhylip;
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the Phylip data to be read 
	 * @param allowInterleavedParsing defines whether interleaved Phylip files shall be supported by this parser instance
	 *        (In order to support this feature the reader needs to keep a list of all sequence names. To parse files with 
	 *        a very large number of sequences which are not interleaved, this feature can be switched off to save memory.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 */
	public PhylipEventReader(InputStream stream, boolean allowInterleavedParsing, boolean relaxedPhylip) throws IOException {
		super(stream);
		this.allowInterleavedParsing = allowInterleavedParsing;
		this.relaxedPhylip = relaxedPhylip;
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Phylip data to be read 
	 * @param allowInterleavedParsing defines whether interleaved Phylip files shall be supported by this parser instance
	 *        (In order to support this feature the reader needs to keep a list of all sequence names. To parse files with 
	 *        a very large number of sequences which are not interleaved, this feature can be switched off to save memory.)
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 */
	public PhylipEventReader(Reader reader, boolean allowInterleavedParsing, boolean relaxedPhylip) throws IOException {
		super(reader);
		this.allowInterleavedParsing = allowInterleavedParsing;
		this.relaxedPhylip = relaxedPhylip;
	}
	
	
	private void readMatrixDimensions() throws IOException {
		PeekReader.ReadResult firstLine = getReader().readLine();
		if (!firstLine.isCompletelyRead()) {
			throw new IOException("First line of Phylip file is too long. It does not seem to be a valid Phylip file.");
		}
		else {
			String[] parts = firstLine.getSequence().toString().trim().split("\\s+");
			if (parts.length == 2) {
				try {
					sequenceCount = Integer.parseInt(parts[0]);
				}
				catch (NumberFormatException e) {
					throw new IOException("Invalid integer constant \"" + parts[0] + "\" found for the sequence count in line 1.");
				}

				try {
					characterCount = Integer.parseInt(parts[1]);
				}
				catch (NumberFormatException e) {
					throw new IOException("Invalid integer constant \"" + parts[1] + "\" found for the character count in line 1.");
				}
			}
			else {
				throw new IOException("The first line of a Phylip file needs to contain exactly two integer values spcifying the "
						+ "sequence and character count. " + parts.length + " value(s) was/were found instead.");
			}
		}
	}
	
	
	private void increaseSequenceIndex() {
		currentSequenceIndex++;
		if (currentSequenceIndex >= sequenceCount) {
			currentSequenceIndex -= sequenceCount;
		}
	}
	
	
	private List<String> createTokenList(CharSequence sequence) {
		List<String> result = new ArrayList<String>(sequence.length());
		for (int i = 0; i < sequence.length(); i++) {
			if (!Character.isWhitespace(sequence.charAt(i))) {  // Phylip allows white spaces in between sequences
				result.add(Character.toString(sequence.charAt(i)));
			}
		}
		return result;
	}
	
	
	@Override
	protected PhyloIOEvent readNextEvent() throws Exception {
		if (isBeforeFirstAccess()) {
			return new ConcretePhyloIOEvent(EventType.DOCUMENT_START);
		}
		else {
			PhyloIOEvent alignmentEndEvent;
			
			switch (getPreviousEvent().getEventType()) {
				case DOCUMENT_START:
					readMatrixDimensions();
					return createAlignmentStartEvent(sequenceCount, characterCount);
					
				case ALIGNMENT_START:
					if (sequenceCount == 0) {  // Empty alignment:
						return new ConcretePhyloIOEvent(EventType.ALIGNMENT_END);
					}
				case SEQUENCE_CHARACTERS:
					if (lineConsumed) {  // Keep current name if current line was not completely consumed yet.
						while (getReader().isNewLineNext()) {  // Ignore empty lines between interleaved blocks.
							getReader().consumeNewLine();  // Note: Parsing this way does not allow to have empty lines for some sequences in interleaved format, if they ended earlier than others. (Sequences with different lengths are anyway not allowed by the format definition. E.g. unaligned could still be read from non-interleaved files.
						}
						
						if (!allowInterleavedParsing || sequenceNames.size() < sequenceCount) {  // Read Name from first (interleaved) block:
							if (relaxedPhylip) {
								currentSequenceName = getReader().readRegExp(".+\\s+", true).getSequence().toString().trim();
							}
							else {
								currentSequenceName = getReader().readUntil(DEFAULT_NAME_LENGTH, NAME_END_CHARACTER).getSequence().toString().trim();
							}
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
							return new ConcretePhyloIOEvent(EventType.ALIGNMENT_END);
						}
					}
					
					// Read characters:
					PeekReader.ReadResult readResult = getReader().readLine(getMaxTokensToRead());
					lineConsumed = readResult.isCompletelyRead();
					List<String> characters = createTokenList(readResult.getSequence());
					if (characters.isEmpty()) {  // The rest of the line was consisting only of spaces
						return readNextEvent();  // Continue parsing to create the next event
					}
					else {
						return new SequenceCharactersEvent(currentSequenceName, characters);
					}
					
				case ALIGNMENT_END:
					return new ConcretePhyloIOEvent(EventType.DOCUMENT_END);

				case DOCUMENT_END:
					return null;  // Calling method will throw a NoSuchElementException.

				default:  // includes META_INFORMATION
					throw new InternalError("Impossible case");
			}
		}
	}
}
