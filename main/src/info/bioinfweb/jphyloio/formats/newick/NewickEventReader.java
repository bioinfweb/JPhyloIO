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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.text.AbstractTextEventReader;
import info.bioinfweb.jphyloio.formats.text.TextStreamDataProvider;



/**
 * Reads tree files in Newick format. Newick files are considered as text files, that contain trees as Newick strings
 * which are separated by {@code ';'}. Any whitespace, as well as comments contained in {@code '['} and {@code ']'} 
 * is allowed between all tokens.
 * <p>
 * Additionally this reader is able to parse hot comments associated with nodes or edges as metadata as they are used 
 * in the output of <a href="http://beast.bio.ed.ac.uk/treeannotator">TreeAnnotator</a> or 
 * <a href="http://mrbayes.sourceforge.net/">MrBayes</a>. (See 
 * <a href="https://code.google.com/archive/p/beast-mcmc/wikis/NexusMetacommentFormat.wiki">here</a> for a definition.)
 * The following format of hot comments is recognized by this reader:
 * <pre>
 * [&numericValue1=1.05, numericValue2 = 2.76e-5, stringValue1="12", stringValue2=ABC, arrayValue={18, "AB C"}]
 * </pre>
 * Each hot comment needs to start with an {@code '&'} and can contain one or more key/value pairs separated by 
 * {@code ','}. Each value can either be a numeric value, a string value or an array value. Arrays are indicated
 * by braces and array elements are separated by {@code ','}, as shown in the example above. Array elements maybe
 * any numeric or string value in any combination. Whitespace between tokens of a hot comment is allowed but not
 * necessary.
 * <p>
 * Hot comments following a node name or a subtree are considered a metadata attached to a node and hot comments 
 * following a branch length definition are considered to be attached to an edge (branch). Subsequent hot comments
 * are combined, with the exception that a branch length definition is omitted. In such a case, the first hot 
 * comment is considered to attached to the node and all subsequent hot comments are considered to be attached to
 * the edge.
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
		newickStringReader = new NewickStringReader(getStreamDataProvider(), null, null, new DefaultNewickNodeLabelProcessor());
	}
	
	
	public NewickEventReader(File file) throws IOException {
		super(file, true);
		init();
	}

	
	public NewickEventReader(InputStream stream)	throws IOException {
		super(stream, true);
		init();
	}

	
	public NewickEventReader(PeekReader reader) {
		super(reader, true);
		init();
	}

	
	public NewickEventReader(Reader reader) throws IOException {
		super(reader, true);
		init();
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
