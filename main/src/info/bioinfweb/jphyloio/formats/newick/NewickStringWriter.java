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


import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.nexus.NexusEventWriter;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;



/**
 * Implementation to write Newick tree definitions to be used by {@link NewickEventWriter} and {@link NexusEventWriter}.
 * 
 * @author Ben St&ouml;ver
 */
public class NewickStringWriter implements NewickConstants {
	private Writer writer;
	private TreeNetworkDataAdapter tree;
	private NewickWriterNodeLabelProcessor nodeLabelProcessor;
	private ReadWriteParameterMap parameters;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param writer the writer to write the Newick string to
	 * @param tree the tree data adapter providing the tree data to be written
	 * @param otuList the list of OTU definitions to be used to label unlabeled tree nodes (Maybe {@code null}.)
	 * @param useOTUFirst Specify {@code true} here, if 
	 *        {@link AbstractEventWriter#getLinkedOTUNameOTUFirst(LinkedOTUOrOTUsEvent, OTUListDataAdapter)}
	 *        shall be used to determine node names (e.g. for writing Nexus) or {@code false} if
	 *        {@link AbstractEventWriter#getLinkedOTUNameOwnFirst(LinkedOTUOrOTUsEvent, OTUListDataAdapter)}
	 *        should be used instead (e.g. for writing Newick).
	 * @param parameters the write parameter map specified to the calling reader
	 */
	public NewickStringWriter(Writer writer, TreeNetworkDataAdapter tree,	NewickWriterNodeLabelProcessor nodeLabelProcessor, 
			ReadWriteParameterMap parameters) {
		
		super();
		this.writer = writer;
		this.tree = tree;
		this.nodeLabelProcessor = nodeLabelProcessor;
		this.parameters = parameters;
	}
	
	
	public static boolean isFreeNameCharForWriting(char c) {
		return NewickScanner.isFreeNameChar(c) && (c != NAME_DELIMITER) && (c != ALTERNATIVE_NAME_DELIMITER);
	}
	
	
	private static boolean isFreeName(String name) {
		if (name.length() == 0) {
			return true;
		}
		else {
			for (int i = 0; i < name.length(); i++) {
				if (!isFreeNameCharForWriting(name.charAt(i))) {
					return false;
				}
			}
			return true;
		}
	}
	
	
	public static String formatToken(String token, char delimiter) {
		boolean containsUnderscores = token.contains("" + FREE_NAME_BLANK);
		if (!containsUnderscores && isFreeName(token)) {  // Do not write strings as free names, which contain underscores, because they would become spaces when they are read again.
			return token;
		}
		else {
			if (!containsUnderscores) {
				String editedName = token.replace(' ', FREE_NAME_BLANK);
				if (isFreeName(editedName)) { 
					return editedName;  // Replace spaces by underscores, if no underscore was present in the original name.
				}
			}
			
			StringBuffer result = new StringBuffer(token.length() * 2);
			result.append(delimiter);
			for (int i = 0; i < token.length(); i++) {
				if (token.charAt(i) == delimiter) {
					result.append(delimiter);  // Second time 
				}
				result.append(token.charAt(i));
			}
			result.append(delimiter);
			return result.toString();
		}
	}
	
	
//	private String getNodeName(LinkedOTUOrOTUsEvent nodeEvent) {
//		String result;
//		if ((indexMap != null) && nodeEvent.isOTUOrOTUsLinked()) {
//			Long index = indexMap.get(nodeEvent.getID());
//			if (index == null) {
//				throw new InconsistentAdapterDataException("Error when writing tree: The node with the ID " + nodeEvent.getID() + 
//						" references an OTU with the ID " + nodeEvent.getOTUOrOTUsID() + 
//						", which could not be found in the OTU list associated with this tree.");
//			}
//			else {
//				result = index.toString();
//			}
//		}
//		else {
//			if (useOTUFirst) {
//				result = AbstractEventWriter.getLinkedOTUNameOTUFirst(nodeEvent, otuList);
//			}
//			else {
//				result = AbstractEventWriter.getLinkedOTUNameOwnFirst(nodeEvent, otuList);
//			}
//		}
//		return result;
//	}
	
	
	private void writeSubtree(String rootEdgeID) throws Exception {
		
		NewickNodeEdgeEventReceiver<EdgeEvent> edgeReceiver = 
				new NewickNodeEdgeEventReceiver<EdgeEvent>(writer, parameters, EventContentType.EDGE);
		tree.writeEdgeData(edgeReceiver, rootEdgeID);  //TODO It would theoretically possible to save memory, if only the node ID would be read here and the associated metadata and comments would be read after the recursion.
		String nodeID = edgeReceiver.getStartEvent().getTargetID();
		Iterator<String> childEdgeIDIterator = tree.getEdgeIDsFromNode(nodeID);
		if (childEdgeIDIterator.hasNext()) {
			writer.write(SUBTREE_START);
			writeSubtree(childEdgeIDIterator.next());
			while (childEdgeIDIterator.hasNext()) {
				writer.write(ELEMENT_SEPERATOR + " ");
				writeSubtree(childEdgeIDIterator.next());
			}
			writer.write(SUBTREE_END);
		}
		
		NewickNodeEdgeEventReceiver<LinkedOTUOrOTUsEvent> nodeReceiver = 
				new NewickNodeEdgeEventReceiver<LinkedOTUOrOTUsEvent>(writer, parameters, EventContentType.NODE);
		tree.writeNodeData(nodeReceiver, nodeID);
		
		// Write node data:
		writer.write(formatToken(nodeLabelProcessor.createNodeName(nodeReceiver.getStartEvent()), NAME_DELIMITER));
		nodeReceiver.writeMetadata();
		nodeReceiver.writeComments();
		
		// Write edge data:
		if (edgeReceiver.getStartEvent().hasLength()) {
			writer.write(LENGTH_SEPERATOR);
			writer.write(Double.toString(edgeReceiver.getStartEvent().getLength()));
		}
		edgeReceiver.writeMetadata();
		edgeReceiver.writeComments();
	}
	
	
	private void writeRootedInformation() throws IOException {
		writer.write(COMMENT_START);
		if (tree.considerRooted()) {
			writer.write(ROOTED_HOT_COMMENT.toUpperCase());
		}
		else {
			writer.write(UNROOTED_HOT_COMMENT.toUpperCase());
		}
		writer.write(COMMENT_END);
		writer.write(" ");
	}

	
	/**
	 * Writes the tree data specified in the constructor to the specified stream.
	 * <p>
	 * If the specified tree/network data adapter models a phylogenetic network and not a tree,
	 * nothing is written and an according warning is logged. Additionally warnings are logged,
	 * if the tree adapter provides metadata or if multiple root edges are available.
	 * <p>
	 * If an empty tree definition (with no root edge) is specified, the written Newick string
	 * only consists of the terminal symbol {@code ';'}.
	 * 
	 * @throws IOException if an I/O error occurs while writing to specified writer
	 */
	public void write() throws Exception {
		ApplicationLogger logger = parameters.getLogger();
		if (tree.isTree()) {
			if (tree.hasMetadata()) {
				logger.addWarning(
						"A tree definition contains tree metadata, which cannot be written to Newick/NHX and is therefore ignored.");
			}
			
			Iterator<String> rootEdgeIterator = tree.getRootEdgeIDs();
			if (rootEdgeIterator.hasNext()) {
				String rootEdgeID = rootEdgeIterator.next();
				if (rootEdgeIterator.hasNext()) {
					logger.addWarning("A tree definition contains more than one root edge, which is not supported "
							+ "by the Newick/NHX format. Only the first root edge will be considered.");
				}
				writeRootedInformation();
				writeSubtree(rootEdgeID);
			}
			else {
				logger.addWarning("A specified tree does not specify any root edge. (Event unrooted trees need a "
						+ "root edge definition defining the edge to start writing tree to the Newick/NHX format.) No "
						+ "Newick string was written.");
			}
			writer.write(TERMINAL_SYMBOL);
			AbstractEventWriter.writeLineBreak(writer, parameters);
		}
		else {
			logger.addWarning("A provided network definition was ignored, because the Newick/NHX format only supports trees.");  //TODO Reference network label or ID of the network, when available.
		}
	}
}
