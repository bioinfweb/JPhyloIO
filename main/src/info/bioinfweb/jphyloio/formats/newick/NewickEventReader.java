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
package info.bioinfweb.jphyloio.formats.newick;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.text.AbstractTextEventReader;
import info.bioinfweb.jphyloio.formats.text.TextStreamDataProvider;



/**
 * Reads tree files in Newick format. Newick files are considered as text files, that contain trees as Newick strings
 * which are separated by {@code ';'}. Any whitespace, as well as comments contained in {@code '['} and {@code ']'} 
 * is allowed between all tokens.
 * <p>
 * Additionally this reader is able to parse hot comments associated with nodes or edges as described in the 
 * documentation of {@link NewickStringReader}.
 * 
 * @author Ben St&ouml;ver
 */
public class NewickEventReader extends AbstractTextEventReader<TextStreamDataProvider<NewickEventReader>> 
		implements NewickConstants {  //TODO Add support for NHX as defined here? https://sites.google.com/site/cmzmasek/home/software/forester/nhx
	
	private static enum State {
		START,
		IN_DOCUMENT,
		END;
	}
	
	
	private State state = State.START;
	private NewickStringReader newickStringReader;
	
	
	private void init() {
		newickStringReader = new NewickStringReader(getStreamDataProvider(), null, null, new DefaultNewickReaderNodeLabelProcessor());
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Newick data to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public NewickEventReader(BufferedReader reader, ReadWriteParameterMap parameters) throws IOException {
		super(reader, parameters, parameters.getMatchToken());
		init();
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the Newick file to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public NewickEventReader(File file, ReadWriteParameterMap parameters) throws IOException {
		super(file, parameters, parameters.getMatchToken());
		init();
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the Newick data to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public NewickEventReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException {
		super(stream, parameters, parameters.getMatchToken());
		init();
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Newick data to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public NewickEventReader(Reader reader, ReadWriteParameterMap parameters) throws IOException {
		super(reader, parameters, parameters.getMatchToken());
		init();
	}
	
	
	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.NEWICK_FORMAT_ID;
	}
	
	
	@Override
	protected void readNextEvent() throws Exception {
		switch (state) {
			case START:
				state = State.IN_DOCUMENT;
				getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
				break;
			case IN_DOCUMENT:
				if (!newickStringReader.addNextEvents()) {
					state = State.END;
					getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
				}
				break;
			case END:
				break;
			default:
				throw new InternalError("Unsupported state " + state + ".");
		}
	}
}
