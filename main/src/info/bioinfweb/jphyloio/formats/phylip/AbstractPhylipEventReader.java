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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.text.AbstractTextEventReader;
import info.bioinfweb.jphyloio.formats.text.TextStreamDataProvider;



/**
 * Implements shared functionality of Phylip event readers.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractPhylipEventReader<P extends TextStreamDataProvider<? extends AbstractPhylipEventReader<P>>>
		extends AbstractTextEventReader<P> implements PhylipConstants {
	
	private long sequenceCount = -1;
	private long characterCount = -1;
	protected String currentSequenceName = null;


	public AbstractPhylipEventReader(PeekReader reader,	ReadWriteParameterMap parameters) {
		super(reader, parameters, parameters.getMatchToken());
	}


	public AbstractPhylipEventReader(Reader reader,	ReadWriteParameterMap parameters) throws IOException {
		super(reader, parameters, parameters.getMatchToken());
	}


	public AbstractPhylipEventReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException {
		super(stream, parameters, parameters.getMatchToken());
	}


	public AbstractPhylipEventReader(File file, ReadWriteParameterMap parameters) throws IOException {
		super(file, parameters, parameters.getMatchToken());
	}


	public boolean isRelaxedPhylip() {
		return getParameters().getBoolean(ReadWriteParameterMap.KEY_RELAXED_PHYLIP, false);
	}
	
	
	protected long getSequenceCount() {
		return sequenceCount;
	}


	protected long getCharacterCount() {
		return characterCount;
	}


	protected void readMatrixDimensions() throws IOException {
		PeekReader.ReadResult firstLine = getReader().readLine();
		if (!firstLine.isCompletelyRead()) {
			throw new JPhyloIOReaderException("First line of Phylip file is too long. It does not seem to be a valid Phylip file.", 
					getReader());
		}
		else {
			String[] parts = firstLine.getSequence().toString().trim().split("\\s+");
			if (parts.length == 2) {
				try {
					sequenceCount = Long.parseLong(parts[0]);
				}
				catch (NumberFormatException e) {
					throw new JPhyloIOReaderException("Invalid integer value \"" + parts[0] + "\" found for the Phylip sequence count.", 
							getReader(), e);
				}
				getCurrentEventCollection().add(new MetaInformationEvent(META_KEY_SEQUENCE_COUNT, null, parts[0], sequenceCount));
				getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));

				try {
					characterCount = Long.parseLong(parts[1]);
				}
				catch (NumberFormatException e) {
					throw new JPhyloIOReaderException("Invalid integer value \"" + parts[1] + "\" found for the Phylip character count.", 
							getReader(), e);
				}
				getCurrentEventCollection().add(new MetaInformationEvent(META_KEY_CHARACTER_COUNT, null, parts[1], characterCount));
				getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
			}
			else {
				throw new JPhyloIOReaderException("The first line of a Phylip file needs to contain exactly two integer values "
						+ "spcifying the sequence and character count. " + parts.length + " value(s) was/were found instead.", getReader());
			}
		}
	}
	
	
	protected String readSequenceName() throws IOException {
		String result;
		if (isRelaxedPhylip()) {  // Allow longer names terminated by one or more white spaces
			result = getReader().readRegExp(RELAXED_PHYLIP_NAME_PATTERN, true).getSequence().toString().trim();
		}
		else {  // Allow names with exactly 10 characters or shorter and terminated with a tab
			result = getReader().readUntil(DEFAULT_NAME_LENGTH, PREMATURE_NAME_END_CHARACTER).getSequence().toString().trim();
		}
		return result;
	}
}
