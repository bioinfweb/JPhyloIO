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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.phyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.phyloio.events.PhyloIOEvent;
import info.bioinfweb.phyloio.events.SequenceCharactersEvent;



/**
 * Implements shared functionality of Phylip event readers.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractPhylipEventReader extends AbstractBufferedReaderBasedEventReader {
	public static final int DEFAULT_NAME_LENGTH = 10;
	public static final String PREMATURE_NAME_END_CHARACTER = "\t";
	
	
	private boolean relaxedPhylip = false;
	private int sequenceCount = -1;
	private int characterCount = -1;
	protected String currentSequenceName = null;
	protected boolean lineConsumed = true;


	public AbstractPhylipEventReader(PeekReader reader,	boolean relaxedPhylip) {
		super(reader);
		this.relaxedPhylip = relaxedPhylip;
	}


	public AbstractPhylipEventReader(Reader reader,	boolean relaxedPhylip) throws IOException {
		super(reader);
		this.relaxedPhylip = relaxedPhylip;
	}


	public AbstractPhylipEventReader(InputStream stream, boolean relaxedPhylip) throws IOException {
		super(stream);
		this.relaxedPhylip = relaxedPhylip;
	}


	public AbstractPhylipEventReader(File file, boolean relaxedPhylip)
			throws IOException {
		super(file);
		this.relaxedPhylip = relaxedPhylip;
	}


	public boolean isRelaxedPhylip() {
		return relaxedPhylip;
	}
	
	
	protected int getSequenceCount() {
		return sequenceCount;
	}


	protected int getCharacterCount() {
		return characterCount;
	}


	protected List<String> createTokenList(CharSequence sequence) {
		List<String> result = new ArrayList<String>(sequence.length());
		for (int i = 0; i < sequence.length(); i++) {
			if (!Character.isWhitespace(sequence.charAt(i))) {  // Phylip allows white spaces in between sequences
				result.add(Character.toString(sequence.charAt(i)));
			}
		}
		return result;
	}
	
	
	protected void readMatrixDimensions() throws IOException {
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
	
	
	protected String readSequenceName() throws IOException {
		String result;
		if (isRelaxedPhylip()) {  // Allow longer names terminated by one or more white spaces
			result = getReader().readRegExp(".+\\s+", true).getSequence().toString().trim();
		}
		else {  // Allow names with exactly 10 characters or shorter and terminated with a tab
			result = getReader().readUntil(DEFAULT_NAME_LENGTH, PREMATURE_NAME_END_CHARACTER).getSequence().toString().trim();
		}
		return result;
	}
	
	
	protected PhyloIOEvent readCharacters() throws Exception {
		PeekReader.ReadResult readResult = getReader().readLine(getMaxTokensToRead());
		lineConsumed = readResult.isCompletelyRead();
		List<String> characters = createTokenList(readResult.getSequence());
		if (characters.isEmpty()) {  // The rest of the line was consisting only of spaces
			return readNextEvent();  // Continue parsing to create the next event
		}
		else {
			return new SequenceCharactersEvent(currentSequenceName, characters);
		}
	}
}
