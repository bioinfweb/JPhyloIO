/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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
package info.bioinfweb.jphyloio.formats.fasta;


import info.bioinfweb.commons.SystemUtils;
import info.bioinfweb.jphyloio.JPhyloIOModelWriter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.model.AbstractModelWriter;
import info.bioinfweb.jphyloio.model.CharacterData;
import info.bioinfweb.jphyloio.model.ElementCollection;
import info.bioinfweb.jphyloio.model.ModelWriterParameterMap;
import info.bioinfweb.jphyloio.model.PhyloDocument;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;



public class FastaModelWriter extends AbstractModelWriter implements JPhyloIOModelWriter, FASTAConstants {
	private static final int DEFAULT_LINE_LENGTH = 80;
	
	
	private int lineLength = DEFAULT_LINE_LENGTH;
	private int charsPerLineWritten = 0;
	

	public FastaModelWriter() {
		super();
	}


	private void writeNewLine(Writer writer) throws IOException {
		writer.write(SystemUtils.LINE_SEPARATOR);
		charsPerLineWritten = 0;
	}
	
	
	public void writeSequenceName(String sequenceName, Writer writer) throws IOException {
		if (charsPerLineWritten > 0) {
			writeNewLine(writer);
		}
		System.out.println(sequenceName);
		writer.write(NAME_START_CHAR + sequenceName + SystemUtils.LINE_SEPARATOR);
	}
	
	
	private void writeTokens(ElementCollection<String> tokens, ModelWriterParameterMap parameters, Writer writer) throws IOException, IllegalArgumentException {
		Iterator<String> tokenIterator = tokens.iterator();
		while (tokenIterator.hasNext()) {
			String token = tokenIterator.next();
			if (parameters.getBoolean(ModelWriterParameterMap.KEY_ALLOW_LONG_TOKENS, false)) {
				token += " "; 
			}
			else if (token.length() > 1) {
				throw new IllegalArgumentException("The specified string representation of one or more of token(s) is longer "
						+ "than one character, although this reader is set to not allow longer tokens.");
			}
			if (charsPerLineWritten + token.length() > lineLength) {
				writeNewLine(writer);
			}
			System.out.println(token);
			writer.write(token);
			charsPerLineWritten += token.length();
		}
	}
	
	
	private void writeComments(JPhyloIOEvent event, Writer writer) throws IOException, IllegalArgumentException {
		if (charsPerLineWritten > 0) {
			writeNewLine(writer);
		}
		switch (event.getEventType()) {
			case META_INFORMATION:
				break;
			case COMMENT:
				writer.write(COMMENT_START_CHAR);				
				writer.write(event.asCommentEvent().getContent());				
				break;
			default:
				throw new IllegalArgumentException();  // Unsupported event.
		}
	}
	
	
	@Override
	public void writeDocument(PhyloDocument document, Writer writer, ModelWriterParameterMap parameters) throws IOException, IllegalArgumentException {
		if (document.getCharacterDataCollection().iterator().hasNext()) {
			CharacterData sequences = document.getCharacterDataCollection().iterator().next();
			ElementCollection<String> sequenceNames = sequences.getSequenceNames();
			Iterator<String> sequenceNameIterator = sequenceNames.iterator();
			while (sequenceNameIterator.hasNext()) {
				String sequenceName = sequenceNameIterator.next();
				writeSequenceName(sequenceName, writer);
				long commentCount = sequenceNames.getMetaCommentEventCount(0);
				if (commentCount > 0) {
					Iterator<JPhyloIOEvent> metaCommentEventIterator = sequenceNames.metaCommentEventsIterator(0);
					while (metaCommentEventIterator.hasNext()) {
						writeComments(metaCommentEventIterator.next(), writer);
					}
				}
				writeTokens(sequences.getTokens(sequenceName), parameters, writer);
			}
		}
	}
}
