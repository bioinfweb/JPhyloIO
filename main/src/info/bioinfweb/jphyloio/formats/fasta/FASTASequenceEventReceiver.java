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
package info.bioinfweb.jphyloio.formats.fasta;


import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;

import info.bioinfweb.commons.SystemUtils;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



class FASTASequenceEventReceiver implements JPhyloIOEventReceiver {
	private int charsPerLineWritten = 0;
	private Writer writer;
	private MatrixDataAdapter matrixDataAdapter;
	private long lineLength;
	
	
	public FASTASequenceEventReceiver(Writer writer, MatrixDataAdapter matrixDataAdapter, long lineLength) {
		super();
		this.writer = writer;
		this.matrixDataAdapter = matrixDataAdapter;
		this.lineLength = lineLength;
	}


	public int getCharsPerLineWritten() {
		return charsPerLineWritten;
	}


	protected void writeNewLine(Writer writer) throws IOException {
		writer.write(SystemUtils.LINE_SEPARATOR);
		charsPerLineWritten = 0;
	}
	
	
	private void writeToken(String token) throws IOException {
		if (matrixDataAdapter.containsLongTokens()) {
			token += " "; 
		}
		else if (token.length() > 1) {
			throw new IllegalArgumentException("The specified string representation of one or more of token(s) is longer "
					+ "than one character, although this reader is set to not allow longer tokens.");
		}
		if (charsPerLineWritten + token.length() > lineLength) {
			writeNewLine(writer);
		}
		writer.write(token);
		charsPerLineWritten += token.length();
	}
	
	
	private void writeTokens(Collection<String> tokens) throws IOException {
		Iterator<String> tokenIterator = tokens.iterator();
		while (tokenIterator.hasNext()) {
			writeToken(tokenIterator.next());
		}
	}
	
	
	@Override
	public boolean add(JPhyloIOEvent event) throws IllegalArgumentException, IOException {
		switch (event.getType().getContentType()) {
			case SEQUENCE_TOKENS:
				writeTokens(event.asSequenceTokensEvent().getCharacterValues());
				break;
			case SINGLE_SEQUENCE_TOKEN:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					writeToken(event.asSingleSequenceTokenEvent().getToken());
				}  // End events can be ignored.
				break;
			case META_INFORMATION:
			case META_XML_CONTENT:
				//TODO Log that these event have been ignored.
				break;
			default:
				throw new IllegalArgumentException("Events of the type " + event.getType().getContentType() + 
						" are not allowed in a sequence content subsequence.");
		}
		return true;
	}
}
