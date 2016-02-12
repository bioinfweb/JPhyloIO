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


import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public class NewickEventWriter extends AbstractEventWriter implements NewickConstants {
	private Writer writer;
	private TreeNetworkDataAdapter tree;
	private EventWriterParameterMap parameters;
	private OTUListDataAdapter firstOTUList;
	
	
	private void writeSubtree(String rootEdgeID) 
			throws IllegalArgumentException, IOException {
		
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
		
		NewickNodeEdgeEventReceiver<LinkedOTUEvent> nodeReceiver = 
				new NewickNodeEdgeEventReceiver<LinkedOTUEvent>(writer, parameters, EventContentType.NODE);
		tree.writeNodeData(nodeReceiver, nodeID);
		
		// Write node data:
		writer.write(getLinkedOTUName(nodeReceiver.getStartEvent(), firstOTUList));
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
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer, EventWriterParameterMap parameters) throws Exception {
		this.writer = writer;
		this.parameters = parameters;
		ApplicationLogger logger = parameters.getApplicationLogger(EventWriterParameterMap.KEY_LOGGER);
		
		firstOTUList = getFirstOTUList(document, logger, "Newick/NHX", "tree nodes"); 
		if (document.getMatrixIterator().hasNext()) {
			logger.addWarning(
					"The specified matrix (matrices) will not be written, since the Newick/NHX format does not support such data."); 
		}
		
		Iterator<TreeNetworkDataAdapter> treeNetworkIterator = document.getTreeNetworkIterator();
		if (treeNetworkIterator.hasNext()) {
			while (treeNetworkIterator.hasNext()) {
				TreeNetworkDataAdapter treeNetwork = treeNetworkIterator.next();
				if (treeNetwork.isTree()) {
					Iterator<String> rootEdgeIterator = treeNetwork.getRootEdgeIDs();
					if (rootEdgeIterator.hasNext()) {
						String rootEdgeID = rootEdgeIterator.next();
						if (rootEdgeIterator.hasNext()) {
							logger.addWarning("One of the specified tree definitions contains more than one root edge, which is not supported "
									+ "by the Newick/NHX format. Only the first root edge will be considered.");
						}
						this.tree = treeNetwork;
						writeSubtree(rootEdgeID);
					}
					else {
						throw new IllegalArgumentException("A specified tree does not specify any root edge. (Event unrooted trees need a "
								+ "root edge definition defining the edge to start writing tree to the Newick/NHX format.)");
					}
				}
				else {
					logger.addWarning("A provided network definition was ignored, because the Newick/NHX format only supports trees.");  //TODO Reference network label or ID of the network, when available.
				}
			}
		}
		else {
			logger.addWarning(
					"An empty document was written, since no tree definitions were affered by the specified document adapter.");  //TODO Use message, that would be more understandable by application users (which does not use library-specific terms)?
		}
	}
}
