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



public class NewickEventWriter extends AbstractEventWriter implements NewickConstants {
	//TODO Add writeMetadataComment() method and also use it in NodeDataEventReceiver if possible (One gets its events from a receiver, one from a list.)
	
	
	private void writeSubtree(Writer writer, TreeNetworkDataAdapter tree, String rootEdgeID, EventWriterParameterMap parameters) 
			throws IllegalArgumentException, IOException {  //TODO Use instance variables for writer, tree and parameters to save stack memory?
		
		EdgeDataEventReceiver edgeReceiver = new EdgeDataEventReceiver(writer, parameters);
		tree.writeEdgeData(edgeReceiver, rootEdgeID);
		String nodeID = edgeReceiver.getEdgeEvent().getTargetID();
		Iterator<String> childEdgeIDIterator = tree.getEdgeIDsFromNode(nodeID);
		if (childEdgeIDIterator.hasNext()) {
			writer.write(SUBTREE_START);
			writeSubtree(writer, tree, childEdgeIDIterator.next(), parameters);
			while (childEdgeIDIterator.hasNext()) {
				writer.write(ELEMENT_SEPERATOR + " ");
				writeSubtree(writer, tree, childEdgeIDIterator.next(), parameters);
			}
			writer.write(SUBTREE_END);
		}
		
		tree.writeNodeData(new NodeDataEventReceiver(writer, parameters), nodeID);  //TODO The metadata of this receiver can be written directly.
		//TODO Write leaf or internal branch length and metadata
	}
	
	
	@Override
	public void writeDocument(DocumentDataAdapter document, Writer writer, EventWriterParameterMap parameters) throws Exception {
		ApplicationLogger logger = parameters.getApplicationLogger(EventWriterParameterMap.KEY_LOGGER);
		
		OTUListDataAdapter firstOTUList = null; 
		Iterator<OTUListDataAdapter> otuListIterator = document.getOTUListIterator();
		if (otuListIterator.hasNext()) {
			firstOTUList = otuListIterator.next();
		}
		
		if (firstOTUList != null) {
			logger.addWarning("The specified OTU list(s) will not be written, since the Newick/NHX format does not support this. "
					+ "The first list will though be used to try to label tree nodes that do not carry a label themselves."); 
		}
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
						writeSubtree(writer, treeNetwork, rootEdgeID, parameters);
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
