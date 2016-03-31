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
package info.bioinfweb.jphyloio.tools;


import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

import org.apache.commons.collections4.set.ListOrderedSet;



/**
 * Generates sets of all node and all edge IDs provided by an implementation of {@link TreeNetworkDataAdapter}.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 * @see TreeNetworkDataAdapter
 */
public class NodeEdgeIDLister {
	private ListOrderedSet<String> edgeIDs = new ListOrderedSet<String>();
	private ListOrderedSet<String> nodeIDs = new ListOrderedSet<String>();
	
	
	/**
	 * Creates a new instance of this class with sets of all node and edge IDs.
	 * 
	 * @param adapter the adapter providing the node and edge IDs
	 */
	public NodeEdgeIDLister(TreeNetworkDataAdapter adapter) {
		super();
		fillSets(adapter);
	}
	
	
	private void fillSets(TreeNetworkDataAdapter adapter) {
		Queue<String> waitingRootNodes = new ArrayDeque<String>();
		
		Iterator<String> iterator = adapter.getRootEdgeIDs();
		do {
			while (iterator.hasNext()) {
				String edgeID = iterator.next();
				if (edgeIDs.add(edgeID)) {
					String nodeID = adapter.getEdgeStartEvent(edgeID).getTargetID();
					if (nodeIDs.add(nodeID)) {
						waitingRootNodes.add(nodeID);
					}
				}
			}
			if (waitingRootNodes.isEmpty()) {
				iterator = null;
			}
			else {
				iterator = adapter.getEdgeIDsFromNode(waitingRootNodes.poll());
			}
		} while (iterator != null);
	}


	/**
	 * A set of all edge IDs provided by the {@link TreeNetworkDataAdapter} specified in the constructor.
	 * <p>
	 * Note that this set is filled in the constructor of this class and is modifiable.
	 * 
	 * @return a set of all edge IDs
	 */
	public ListOrderedSet<String> getEdgeIDs() {
		return edgeIDs;
	}


	/**
	 * A set of all node IDs provided by the {@link TreeNetworkDataAdapter} specified in the constructor.
	 * <p>
	 * Note that this set is filled in the constructor of this class and is modifiable.
	 * 
	 * @return a set of all node IDs
	 */
	public ListOrderedSet<String> getNodeIDs() {
		return nodeIDs;
	}
}
