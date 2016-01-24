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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Pattern;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public class NewickEventReader extends AbstractBufferedReaderBasedEventReader implements NewickConstants {
	private static final String NODE_ID_PREFIX = "n";
	private static final Pattern HOT_COMMENT_PATTERN = Pattern.compile("\\s*\\&.*");
	private static final int NO_HOT_COMMENT_READ = -2;
	private static final int ONE_HOT_COMMENT_READ = -1;
	
	
	private static enum State {
		START,
		IN_DOCUMENT,
		IN_TREE,
		END;
	}
	
	
	private static class NodeInfo {
		public String id;
		public double length;
		public Collection<JPhyloIOEvent> nestedEdgeEvents;

		public NodeInfo(String id, double length, Collection<JPhyloIOEvent> nestedEdgeEvents) {
			super();
			this.id = id;
			this.length = length;
			this.nestedEdgeEvents = nestedEdgeEvents;
		}
	}
	
	
	private State state = State.START;
	private boolean currentTreeRooted = false;
	private NewickScanner scanner;
	private Stack<Queue<NodeInfo>> passedSubnodes;
	private HotCommentDataReader hotCommentDataReader = new HotCommentDataReader();
	
	
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
	
	
	private boolean isHotComment(String text) {
		return HOT_COMMENT_PATTERN.matcher(text).matches();  // text.trim().startsWith("" + HotCommentDataReader.START_SYMBOL);
	}
	
	
	private Collection<JPhyloIOEvent> createMetaAndCommentEvents(List<NewickToken> tokens, boolean isOnNode) throws IOException {
		Collection<JPhyloIOEvent> result = new ArrayList<JPhyloIOEvent>();
		for (NewickToken token : tokens) {
			if (token.getText().trim().startsWith("" + HotCommentDataReader.START_SYMBOL)) {
				hotCommentDataReader.read(token.getText(), result, isOnNode);
			}
			else {
				result.add(new CommentEvent(token.getText(), false));
			}
		}
		return result;
	}
	
	
	/**
	 * Collects events relevant for the upcoming node and edge.
	 * 
	 * @return a list of the events
	 * @throws IOException 
	 */
	private List<NewickToken>[] collectNodeEdgeTokens() throws IOException {
		List<NewickToken> nodeTokens = new ArrayList<NewickToken>();
		List<NewickToken> edgeTokens = new ArrayList<NewickToken>();
		boolean nameExpected = true;
		boolean lengthExpected = true;
		int secondHotCommentPosition = NO_HOT_COMMENT_READ;
		NewickToken token = scanner.peek();
		NewickTokenType type = token.getType();
		
		while ((token != null) && ((nameExpected && type.equals(NewickTokenType.NAME)) || 
				(lengthExpected && type.equals(NewickTokenType.LENGTH)) || type.equals(NewickTokenType.COMMENT))) {
			
			switch (type) {
				case NAME:
					nodeTokens.add(scanner.nextToken());
					nameExpected = false;
					break;
				case LENGTH:
					edgeTokens.add(scanner.nextToken());
					lengthExpected = false;
					break;
				case COMMENT:  // Note that comment tokens before a name token are not possible, because this method would not have been called then.
					if (lengthExpected) {  // Before possible length token
						if (isHotComment(token.getText())) {
							if (secondHotCommentPosition == NO_HOT_COMMENT_READ) {
								secondHotCommentPosition = ONE_HOT_COMMENT_READ;
							}
							else if (secondHotCommentPosition == ONE_HOT_COMMENT_READ) {
								secondHotCommentPosition = nodeTokens.size();
							}
						}
						nodeTokens.add(token);
					}
					else {  // After length token
						edgeTokens.add(token);
					}
					scanner.nextToken();  // Skip added token.
					break;
				default:
					throw new InternalError("Impossible case");  // If this happens, the loop condition has errors.
			}
			
			token = scanner.peek();
			type = token.getType();
		}
		
		// Possibly move tokens to edge list. 
		if (lengthExpected && (secondHotCommentPosition > 0)) {  // No length token, but two hot comments were found. (Position 0 is not possible for the second hot comment.)
			List<NewickToken> tokensToMove = nodeTokens.subList(secondHotCommentPosition, nodeTokens.size());
			edgeTokens.addAll(tokensToMove);  // edgeTokens should be empty before this.
			tokensToMove.clear();  // Remove tokens from node list.
		}
		
		//TODO Throw exception for unexpected token, if next is other than SUBTREE_END, TREE_END or ELEMENT_SEPARATOR?
		return new List[]{nodeTokens, edgeTokens};
	}
	
	
	private LinkedOTUEvent readNode() throws IOException {
		NewickToken token = scanner.peek();  //TODO Catch NoSuchElementException for unexpected EOF here or somewhere above.
		if (token.getType().equals(NewickTokenType.SUBTREE_START)) {  // No name to read.
			return null;
		}
		else {  // In this a case a node is defined, even if no name or length token are present (if this method is called only at appropriate positions). 
			String nodeID = NODE_ID_PREFIX + getIDManager().createNewID();
			
			List<NewickToken>[] tokens = collectNodeEdgeTokens();  // All tokens need to be read before, to determine if a length definition exists behind a possible second hot comment.
			
			// Read node data:
			String label = null;
			if (!tokens[0].isEmpty() && tokens[0].get(0).getType().equals(NewickTokenType.NAME)) {
				label = tokens[0].get(0).getText();
				tokens[0].remove(0);
			}
			Collection<JPhyloIOEvent> nestedNodeEvents = createMetaAndCommentEvents(tokens[0], true);
			
			// Read edge data:
			double length = Double.NaN;
			if (!tokens[1].isEmpty() && tokens[1].get(0).getType().equals(NewickTokenType.LENGTH)) {
				length = tokens[1].get(0).getLength();
				tokens[1].remove(0);
			}
			Collection<JPhyloIOEvent> nestedEdgeEvents = createMetaAndCommentEvents(tokens[1], false);
			
			// Generate node information:
			if (!passedSubnodes.isEmpty()) {  // Nodes on top level do not have to be stored.
				passedSubnodes.peek().add(new NodeInfo(nodeID, length, nestedEdgeEvents));			
			}
			LinkedOTUEvent result = new LinkedOTUEvent(EventContentType.NODE, nodeID, label, null);  //TODO Possibly replace by translation table when used in Nexus.
			                                              //TODO Possibly use OTU link, if used in Nexus.
			getUpcomingEvents().add(result);
			getUpcomingEvents().addAll(nestedNodeEvents);
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
					throw new JPhyloIOReaderException("Unexpected end of file inside a subtree defintion.", getReader());
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
							throw new JPhyloIOReaderException("Unexpected Newick token \"" + NewickTokenType.SUBTREE_START + "\"", 
									scanner.peek().getLocation());
						}
						else {
							Queue<NodeInfo> levelInfo = passedSubnodes.pop();
							LinkedOTUEvent nodeEvent = readNode();  // Cannot be null, because SUBTREE_START has been handled.
							String sourceID = nodeEvent.getID();
							while (!levelInfo.isEmpty()) {
								NodeInfo nodeInfo = levelInfo.poll();
								getUpcomingEvents().add(new EdgeEvent(DEFAULT_EDGE_ID_PREFIX + getIDManager().createNewID(), 
										null, sourceID, nodeInfo.id, nodeInfo.length));
								getUpcomingEvents().addAll(nodeInfo.nestedEdgeEvents);
								getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.EDGE, EventTopologyType.END));
							}
						}
						break;
					case TERMNINAL_SYMBOL:
						state = State.IN_DOCUMENT;
						getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.END));
						break;
					case COMMENT:
						getUpcomingEvents().add(new CommentEvent(token.getText(), false));
						break;
					default:
						throw new JPhyloIOReaderException("Unexpected Newick token \"" + token.getType() + "\"", token.getLocation());
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
					NewickTokenType type = scanner.peek().getType();
					if (NewickTokenType.COMMENT.equals(type)) {  // Comments before a tree
						getUpcomingEvents().add(new CommentEvent(scanner.nextToken().getText(), false));
					}
					else {
						getUpcomingEvents().add(new LabeledIDEvent(EventContentType.TREE, 
								DEFAULT_TREE_ID_PREFIX + getIDManager().createNewID(), null));  //TODO Use label from Nexus, if available.
						if (NewickTokenType.ROOTED_COMMAND.equals(type) || NewickTokenType.UNROOTED_COMMAND.equals(type)) {
							currentTreeRooted = NewickTokenType.ROOTED_COMMAND.equals(type);
							scanner.nextToken();  // Skip rooted token.
							getUpcomingEvents().add(new MetaInformationEvent(META_KEY_DISPLAY_TREE_ROOTED, null, 
									Boolean.toString(currentTreeRooted), new Boolean(currentTreeRooted)));
							getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
						}
						state = State.IN_TREE;
					}
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
