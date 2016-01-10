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
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;

import info.bioinfweb.commons.LongIDManager;
import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public class NewickEventReader extends AbstractBufferedReaderBasedEventReader implements NewickConstants {
	private static final String NODE_ID_PREFIX = "n";
	
	
	private static enum State {
		START,
		IN_DOCUMENT,
		IN_TREE,
		END;
	}
	
	
	private static class NodeInfo {
		public String id;
		public double length;

		public NodeInfo(String id, double length) {
			super();
			this.id = id;
			this.length = length;
		}
	}
	
	
	private State state = State.START;
	private NewickScanner scanner;
	private LongIDManager nodeIDManager = new LongIDManager();
	private Stack<Queue<NodeInfo>> passedSubnodes;
	
	
	private void init() {
		scanner = new NewickScanner(getReader());
		passedSubnodes = new Stack<Queue<NodeInfo>>();
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
	
	
	private NodeEvent readNode() throws IOException {
		NewickToken token = scanner.peek();  //TODO Catch NoSuchElementException for unexpected EOF here or somewhere above.
		if (token.getType().equals(NewickTokenType.SUBTREE_START)) {  // No name to read.
			return null;
		}
		else {  // In this a case a node is defined, even if no name or length token are present (if this method is called only at appropriate positions). 
			String id = NODE_ID_PREFIX + nodeIDManager.createNewID();
			
			// Read label:
			String label = null;
			if (token.getType().equals(NewickTokenType.NAME)) {
				label = token.getText();
				scanner.nextToken();  // Skip name token
				token = scanner.peek();
			}
			
			// Read length:
			double length = Double.NaN;
			if (token.getType().equals(NewickTokenType.LENGTH)) {
				length = token.getLength();
				scanner.nextToken();  // Skip length token
			}
			
			// Generate node information:
			if (!passedSubnodes.isEmpty()) {  // Nodes on top level do not have to be stored.
				passedSubnodes.peek().add(new NodeInfo(id, length));			
			}
			NodeEvent result = new NodeEvent(id, label);  //TODO Possibly replace by translation table when used in Nexus. 
			getUpcomingEvents().add(result);
			getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));  //TODO Put possible annotations and comments in the queue first
			return result;
		}		
	}

	
	private void processTree() throws IOException {
		while (getUpcomingEvents().isEmpty()) {
			NewickToken token = scanner.nextToken();
			if (token == null) {
				if (passedSubnodes.isEmpty()) {
					state = State.IN_DOCUMENT;
					getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.END));  // End of file without terminal symbol.
				}
				else {
					throw new IOException("Unexpected EOF.");  //TODO Replace by special exception.
				}
			}
			else {
				switch (token.getType()) {
					case SUBTREE_START:
						passedSubnodes.add(new ArrayDeque<NodeInfo>());
					case ELEMENT_SEPARATOR:  // fall through
						readNode();  // Will not add an element, if another SUBTREE_START follows.
						break;
					case SUBTREE_END:
						if (scanner.peek().getType().equals(NewickTokenType.SUBTREE_START)) {
							throw new IOException("Unexpected token " + NewickTokenType.SUBTREE_START);  //TODO Replace by special exception
						}
						else {
							Queue<NodeInfo> levelInfo = passedSubnodes.pop();
							NodeEvent nodeEvent = readNode();  // Cannot be null, because SUBTREE_START has been handled.
							String sourceID = nodeEvent.getID();
							while (!levelInfo.isEmpty()) {
								NodeInfo nodeInfo = levelInfo.poll();
								getUpcomingEvents().add(new EdgeEvent(sourceID, nodeInfo.id, nodeInfo.length));
								//TODO Put possible metadata and comments in between here.
								getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.EDGE, EventTopologyType.END));
							}
						}
						break;
					case TERMNINAL_SYMBOL:
						state = State.IN_DOCUMENT;
						getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.END));
						break;
					default:
						throw new IOException("Unexpected token " + token.getType());  //TODO Replace by special exception
				}
			}
		}
	}
	
	
	@Override
	protected void readNextEvent() throws Exception {
		switch (state) {
			case START:
				state = State.IN_DOCUMENT;
				getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
				break;
			case IN_DOCUMENT:
				if (scanner.hasMoreTokens()) {
					state = State.IN_TREE;
					getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.START));
				}
				else {
					state = State.END;
					getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
				}
				break;
			case IN_TREE:
				processTree();
				break;
			case END:
				break;
			default:
				throw new InternalError("Unsupported state " + state + ".");
		}
	}
}
