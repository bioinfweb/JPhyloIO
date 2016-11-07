/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.formats.newick;


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.trees.TreeReader;
import info.bioinfweb.jphyloio.formats.text.TextReaderStreamDataProvider;

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
 * Any whitespace, as well as comments contained in {@code '['} and {@code ']'} is allowed between all tokens.
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
 * In addition, <a href="http://sites.google.com/site/cmzmasek/home/software/forester/nhx">NHX hot comments</a> can be read and 
 * are associated with according PhyloXML predicates, of possible. Note that the NHX key {@value NewickConstants#NHX_KEY_EVENT} 
 * is not converted, since there is no directly equivalent PhyloXML predicate.
 * <p>
 * Hot comments following a node name or a subtree are considered a metadata attached to a node and hot comments 
 * following a branch length definition are considered to be attached to an edge (branch). Subsequent hot comments
 * are combined, with the exception that a branch length definition is omitted. In such a case, the first hot 
 * comment is considered to attached to the node and all subsequent hot comments are considered to be attached to
 * the edge.
 * 
 * @author Ben St&ouml;ver
 */
public class NewickStringReader implements ReadWriteConstants {
	private static final Pattern HOT_COMMENT_PATTERN = Pattern.compile("\\s*\\&.*");
	private static final int NO_HOT_COMMENT_READ = -2;
	private static final int ONE_HOT_COMMENT_READ = -1;	
	
	private TextReaderStreamDataProvider<?> streamDataProvider;
	private boolean currentTreeRooted = false;
	private String treeID;
	private String treeLabel;
	private NewickReaderNodeLabelProcessor nodeLabelProcessor;
	private NewickScanner scanner;
	private Stack<Queue<NodeEdgeInfo>> passedSubnodes;
	private HotCommentDataReader hotCommentDataReader = new HotCommentDataReader();
	private boolean isInTree = false;
	private boolean afterTree = false;
	
	
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
	public NewickStringReader(TextReaderStreamDataProvider<?> streamDataProvider, String treeID, String treeLabel, 
			NewickReaderNodeLabelProcessor nodeLabelProcessor) {
		
		super();
		
		if (streamDataProvider == null) {
			throw new NullPointerException("streamDataProvider must not be null.");
		}
		if (nodeLabelProcessor == null) {
			throw new NullPointerException("nodeLabelProcessor must not be null.");
		}
		
		this.streamDataProvider = streamDataProvider;
		if (treeID == null) {
			this.treeID = DEFAULT_TREE_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
		}
		else {
			this.treeID = treeID;
		}
		this.treeLabel = treeLabel;
		this.nodeLabelProcessor = nodeLabelProcessor;
		
		scanner = new NewickScanner(streamDataProvider.getDataReader(), treeLabel == null);
		passedSubnodes = new Stack<Queue<NodeEdgeInfo>>();
	}
	
	
	private boolean isHotComment(String text) {
		return HOT_COMMENT_PATTERN.matcher(text).matches();  // text.trim().startsWith("" + HotCommentDataReader.START_SYMBOL);
	}
	
	
	private Collection<JPhyloIOEvent> createMetaAndCommentEvents(List<NewickToken> tokens, boolean isOnNode) throws IOException {
		Collection<JPhyloIOEvent> result = new ArrayList<JPhyloIOEvent>();
		for (NewickToken token : tokens) {
			if (token.getText().trim().startsWith("" + HotCommentDataReader.HOT_COMMENT_START_SYMBOL)) {  // Condition works for both the TreeAnnotator and the NHX format.
				try {
					hotCommentDataReader.read(token.getText(), streamDataProvider, result, isOnNode);
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
	
	
	private NodeEvent readNode() throws IOException {
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
			passedSubnodes.peek().add(new NodeEdgeInfo(nodeID, length, nestedEdgeEvents));
			String processedLabel = nodeLabelProcessor.processLabel(label);
			NodeEvent result = new NodeEvent(nodeID, processedLabel,	nodeLabelProcessor.getLinkedOTUID(processedLabel), 
					currentTreeRooted && (passedSubnodes.size() == 1));  //TODO Does the rooted expression work?
			streamDataProvider.getCurrentEventCollection().add(result);
			streamDataProvider.getCurrentEventCollection().addAll(nestedNodeEvents);
			streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
			
			return result;
		}		
	}
	
	
	private void addEdgeEvents(String sourceID, Queue<NodeEdgeInfo> nodeInfos) {
		while (!nodeInfos.isEmpty()) {
			NodeEdgeInfo nodeInfo = nodeInfos.poll();
			streamDataProvider.getCurrentEventCollection().add(new EdgeEvent(DEFAULT_EDGE_ID_PREFIX + 
					streamDataProvider.getIDManager().createNewID(), null, sourceID, nodeInfo.getID(), nodeInfo.getLength()));
			streamDataProvider.getCurrentEventCollection().addAll(nodeInfo.nestedNodeEvents);
			streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(
					sourceID == null ? EventContentType.ROOT_EDGE : EventContentType.EDGE, EventTopologyType.END));
		}		
	}
	
	
	private void addCommentEvent(NewickToken token) {
		streamDataProvider.getCurrentEventCollection().add(new CommentEvent(token.getText(), false));
	}

	
	private void endTree() {
		addEdgeEvents(null, passedSubnodes.pop());  // Add events for root branch.
		streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));  // End of file without terminal symbol.
		isInTree = false;
		currentTreeRooted = false;
	}
	
	
	/**
	 * Read the contents of a Newick string and generates JPhyloIO events from it.
	 * 
	 * @return {@code true} if the end of the tree was reached or {@code false} if reading this Newick string needs to be continued 
	 * @throws IOException
	 */
	public void processTree() throws IOException {
		while (streamDataProvider.getCurrentEventCollection().isEmpty()) {
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
						passedSubnodes.add(new ArrayDeque<NodeEdgeInfo>());
					case ELEMENT_SEPARATOR:  // fall through
						readNode();  // Will not add an element, if another SUBTREE_START follows.
						break;
					case SUBTREE_END:
						if (scanner.hasMoreTokens() && scanner.peek().getType().equals(NewickTokenType.SUBTREE_START)) {
							throw new JPhyloIOReaderException("Unexpected Newick token \"" + NewickTokenType.SUBTREE_START + "\"", 
									scanner.peek().getLocation());
						}
						else {
							Queue<NodeEdgeInfo> levelInfo = passedSubnodes.pop();  // Must be called before readNode().
							addEdgeEvents(readNode().getID(), levelInfo);  // readNode() is (and needs to be) executed before addEdgeEvents().
						}
						break;
					case TERMNINAL_SYMBOL:
						endTree();
						break;
					case ROOTED_COMMAND:
						streamDataProvider.getParameters().getLogger().addWarning(
								"More than one rooting hot comment was found. All but the first one are treated as ordinary comments.");
					case COMMENT:
						addCommentEvent(token);
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
		boolean readMoreTokens = scanner.hasMoreTokens();
		if (afterTree) {  // This state cannot be handled elsewhere, since passedNodes is now empty.
			if (scanner.hasMoreTokens()) {
				NewickToken token = scanner.nextToken();
				if (token.getType().equals(NewickTokenType.COMMENT)) {
					addCommentEvent(token);
				}
				else if (token.getType().equals(NewickTokenType.TERMNINAL_SYMBOL)) {
					afterTree = false;
					endTree();
				}
				else {
					throw new JPhyloIOReaderException("Tree end expected, but found " + token.getType() + " \"" + 
							token.getText() + "\".", streamDataProvider.getDataReader());
				}
			}
			else {
				endTree();
			}
		}
		else if (!isInTree) {
			//TODO Does an unexpected EOF need to be checked here?
			if (readMoreTokens) {
				NewickTokenType type = scanner.peek().getType();
				if (NewickTokenType.COMMENT.equals(type)) {  // Comments before a tree
					addCommentEvent(scanner.nextToken());
				}
				else {
					streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.TREE, treeID, treeLabel));
					if (NewickTokenType.ROOTED_COMMAND.equals(type) || NewickTokenType.UNROOTED_COMMAND.equals(type)) {
						currentTreeRooted = NewickTokenType.ROOTED_COMMAND.equals(type);
						scanner.nextToken();  // Skip rooted token.
					}
					passedSubnodes.add(new ArrayDeque<NodeEdgeInfo>());  // Add queue for top level.
					
					if (scanner.hasMoreTokens()) {
						type = scanner.peek().getType();
						if (type.equals(NewickTokenType.NAME) || type.equals(NewickTokenType.LENGTH)) {
							readNode();  // Read tree that only consists of one node.
							afterTree = true;
							return true;
						}
						else if (type.equals(NewickTokenType.COMMENT)) {
							return true;  // Read comment in next call.
						}
					}
					isInTree = true;
				}
			}
		}
		else {
			processTree();
			return true;  // At least the tree end event is still to come.
		}
		return readMoreTokens;
	}
}
