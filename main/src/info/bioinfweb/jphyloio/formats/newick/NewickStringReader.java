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


import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.trees.TreeReader;
import info.bioinfweb.jphyloio.formats.text.TextStreamDataProvider;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.regex.Pattern;



/**
 * Implementation to read Newick tree definitions to be used by {@link NewickEventReader} and {@link TreeReader}.
 * 
 * @author Ben St&ouml;ver
 */
public class NewickStringReader implements ReadWriteConstants {
	private static final Pattern HOT_COMMENT_PATTERN = Pattern.compile("\\s*\\&.*");
	private static final int NO_HOT_COMMENT_READ = -2;
	private static final int ONE_HOT_COMMENT_READ = -1;
	
	
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
	
	
	private TextStreamDataProvider<?> streamDataProvider;
	private String treeLabel;
	private NewickNodeLabelProcessor nodeLabelProcessor;
	private NewickScanner scanner;
	private Stack<Queue<NodeInfo>> passedSubnodes;
	private HotCommentDataReader hotCommentDataReader = new HotCommentDataReader();
	private boolean isInTree = false;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param streamDataProvider the stream data provider that allows this reader to access the necessary event reader properties 
	 * @param treeLabel the label of the tree to be read (This parameter also determines whether one or more trees shall be read
	 *        from the underlying reader. If a string is specified, only one tree is read and the specified label is used for it.
	 *        If {@code null} is specified, multiple trees are read until the end of the file is reached. None of them gets a
	 *        defined label.)
	 * @param nodeLabelProcessor the node label processor to be used to possibly translate node labels in Newick strings
	 * @throws NullPointerException if {@code streamDataProvider} or {@code nodeLabelProcessor} are {@code null}
	 */
	public NewickStringReader(TextStreamDataProvider<?> streamDataProvider, String treeLabel, 
			NewickNodeLabelProcessor nodeLabelProcessor) {
		
		super();
		
		if (streamDataProvider == null) {
			throw new NullPointerException("streamDataProvider must not be null.");
		}
		if (nodeLabelProcessor == null) {
			throw new NullPointerException("nodeLabelProcessor must not be null.");
		}
		
		this.streamDataProvider = streamDataProvider;
		this.treeLabel = treeLabel;
		this.nodeLabelProcessor = nodeLabelProcessor;
		
		scanner = new NewickScanner(streamDataProvider.getDataReader(), treeLabel == null);
		passedSubnodes = new Stack<Queue<NodeInfo>>();
	}
	
	
	private boolean isHotComment(String text) {
		return HOT_COMMENT_PATTERN.matcher(text).matches();  // text.trim().startsWith("" + HotCommentDataReader.START_SYMBOL);
	}
	
	
	private Collection<JPhyloIOEvent> createMetaAndCommentEvents(List<NewickToken> tokens, boolean isOnNode) throws IOException {
		Collection<JPhyloIOEvent> result = new ArrayList<JPhyloIOEvent>();
		for (NewickToken token : tokens) {
			if (token.getText().trim().startsWith("" + HotCommentDataReader.HOT_COMMENT_START_SYMBOL)) {  // Condition works for both the TreeAnnotator and the NHX format.
				try {
					hotCommentDataReader.read(token.getText(), result, isOnNode);
				}
				catch (IllegalArgumentException e) {  // Add as comment, if it could not be parsed.
					result.add(new CommentEvent(token.getText(), false));  //TODO Log warning, when logger is available.
				}
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
	@SuppressWarnings("unchecked")
	private List<NewickToken>[] collectNodeEdgeTokens() throws IOException {
		List<NewickToken> nodeTokens = new ArrayList<NewickToken>();
		List<NewickToken> edgeTokens = new ArrayList<NewickToken>();
		if (scanner.hasMoreTokens()) {
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
				
				if (scanner.hasMoreTokens()) {
					token = scanner.peek();
					type = token.getType();
				}
				else {
					token = null;
				}
			}
			
			// Possibly move tokens to edge list. 
			if (lengthExpected && (secondHotCommentPosition > 0)) {  // No length token, but two hot comments were found. (Position 0 is not possible for the second hot comment.)
				List<NewickToken> tokensToMove = nodeTokens.subList(secondHotCommentPosition, nodeTokens.size());
				edgeTokens.addAll(tokensToMove);  // edgeTokens should be empty before this.
				tokensToMove.clear();  // Remove tokens from node list.
			}
			
			//TODO Throw exception for unexpected token, if next is other than SUBTREE_END, TREE_END or ELEMENT_SEPARATOR?^
		}
		return new List[]{nodeTokens, edgeTokens};
	}
	
	
	private LinkedOTUEvent readNode() throws IOException {
		NewickToken token;
		if (scanner.hasMoreTokens()) {
			token = scanner.peek();
		}
		else if (passedSubnodes.size() == 1) {  // Omitted terminal symbol
			token = new NewickToken(NewickTokenType.TERMNINAL_SYMBOL, streamDataProvider.getDataReader());
		}
		else {  // No more tokens available although the top level has not been reached again.
			throw new JPhyloIOReaderException("Unexpected end of file inside a Newick tree definition.", streamDataProvider.getDataReader());
		}
		
		if (token.getType().equals(NewickTokenType.SUBTREE_START)) {  // No name to read.
			return null;
		}
		else {  // In this a case a node is defined, even if no name or length token are present (if this method is called only at appropriate positions). 
			String nodeID = DEFAULT_NODE_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
			
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
			passedSubnodes.peek().add(new NodeInfo(nodeID, length, nestedEdgeEvents));
			String processedLabel = nodeLabelProcessor.processLabel(label);
			LinkedOTUEvent result = new LinkedOTUEvent(EventContentType.NODE, nodeID, processedLabel,
					nodeLabelProcessor.getLinkedOTUID(processedLabel));
			streamDataProvider.getUpcomingEvents().add(result);
			streamDataProvider.getUpcomingEvents().addAll(nestedNodeEvents);
			streamDataProvider.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
			
			return result;
		}		
	}
	
	
	private void addEdgeEvents(String sourceID, Queue<NodeInfo> nodeInfos) {
		while (!nodeInfos.isEmpty()) {
			NodeInfo nodeInfo = nodeInfos.poll();
			streamDataProvider.getUpcomingEvents().add(new EdgeEvent(DEFAULT_EDGE_ID_PREFIX + 
					streamDataProvider.getIDManager().createNewID(), null, sourceID, nodeInfo.id, nodeInfo.length));
			streamDataProvider.getUpcomingEvents().addAll(nodeInfo.nestedEdgeEvents);
			streamDataProvider.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.EDGE, EventTopologyType.END));
		}		
	}
	
	
	private void endTree() {
		addEdgeEvents(null, passedSubnodes.pop());  // Add events for root branch.
		streamDataProvider.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.TREE, EventTopologyType.END));  // End of file without terminal symbol.
		isInTree = false;
	}

	
	/**
	 * Read the contents of a Newick string and generates JPhyloIO events from it.
	 * 
	 * @return {@code true} if the end of the tree was reached or {@code false} if reading this Newick string needs to be continued 
	 * @throws IOException
	 */
	public void processTree() throws IOException {
		while (streamDataProvider.getUpcomingEvents().isEmpty()) {
			if (!scanner.hasMoreTokens()) {
				if (passedSubnodes.size() == 1) {
					endTree();
				}
				else {
					throw new JPhyloIOReaderException("Unexpected end of file inside a subtree defintion.", streamDataProvider.getDataReader());
				}
			}
			else {
				NewickToken token = scanner.nextToken();
				switch (token.getType()) {
					case SUBTREE_START:
						passedSubnodes.add(new ArrayDeque<NodeInfo>());
					case ELEMENT_SEPARATOR:  // fall through
						readNode();  // Will not add an element, if another SUBTREE_START follows.
						break;
					case SUBTREE_END:
						if (scanner.hasMoreTokens() && scanner.peek().getType().equals(NewickTokenType.SUBTREE_START)) {
							throw new JPhyloIOReaderException("Unexpected Newick token \"" + NewickTokenType.SUBTREE_START + "\"", 
									scanner.peek().getLocation());
						}
						else {
							Queue<NodeInfo> levelInfo = passedSubnodes.pop();
							LinkedOTUEvent nodeEvent = readNode();  // Cannot be null, because SUBTREE_START has been handled.
							String sourceID = nodeEvent.getID();
							addEdgeEvents(sourceID, levelInfo);
						}
						break;
					case TERMNINAL_SYMBOL:
						endTree();
						break;
					case COMMENT:
						streamDataProvider.getUpcomingEvents().add(new CommentEvent(token.getText(), false));
						break;
					default:
						throw new JPhyloIOReaderException("Unexpected Newick token \"" + token.getType() + "\"", token.getLocation());
				}
			}
		}
	}
	
	
	/**
	 * Creates the next JPhyloIO event(s) from the Newick string provided by the underlying reader.
	 * 
	 * @return {@code true} if more events were added to the queue or {@code false} if reading of the current tree(s)
	 *         is finished.
	 * @throws IOException
	 */
	public boolean addNextEvents() throws IOException {
		if (!isInTree) {
			boolean readMoreTokens = scanner.hasMoreTokens();
			//TODO Does an unexpected EOF need to be checked here?
			if (readMoreTokens) {
				NewickTokenType type = scanner.peek().getType();
				if (NewickTokenType.COMMENT.equals(type)) {  // Comments before a tree
					streamDataProvider.getUpcomingEvents().add(new CommentEvent(scanner.nextToken().getText(), false));
				}
				else {
					streamDataProvider.getUpcomingEvents().add(new LabeledIDEvent(EventContentType.TREE, 
							DEFAULT_TREE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), treeLabel));
					if (NewickTokenType.ROOTED_COMMAND.equals(type) || NewickTokenType.UNROOTED_COMMAND.equals(type)) {
						boolean currentTreeRooted = NewickTokenType.ROOTED_COMMAND.equals(type);
						scanner.nextToken();  // Skip rooted token.
						streamDataProvider.getUpcomingEvents().add(new MetaInformationEvent(META_KEY_DISPLAY_TREE_ROOTED, null, 
								Boolean.toString(currentTreeRooted), new Boolean(currentTreeRooted)));
						streamDataProvider.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
					}
					passedSubnodes.add(new ArrayDeque<NodeInfo>());  // Add queue for top level.
					isInTree = true;
				}
			}
			return readMoreTokens;
		}
		else {
			processTree();
			return true;  // At least the tree end event is still to come.
		}
	}
}
