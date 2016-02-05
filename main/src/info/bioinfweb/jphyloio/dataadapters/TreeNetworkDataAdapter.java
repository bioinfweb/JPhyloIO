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
package info.bioinfweb.jphyloio.dataadapters;


import java.io.IOException;
import java.util.Iterator;



public interface TreeNetworkDataAdapter extends AnnotatedDataAdapter {
	//TODO Additional methods are needed, since NeXML expects all node definitions before the first edge definition.
	
	/**
	 * Determines whether this instance represents a phylogenetic tree or a phylogenetic network.
	 * (Not all formats accept networks.)
	 * 
	 * @return {@code true} if this instance represents a tree or {@code false} otherwise
	 */
	public boolean isTree();
	
	/**
	 * Returns an iterator returning the IDs of all root edges of the represented phylogenetic
	 * tree or network.
	 * 
	 * @return an iterator returning the edge IDs (Must return at least one element.)
	 */
	public Iterator<String> getRootEdgeIDs();

	/**
	 * Writes the events describing the specified node and possible nested metadata.
	 * 
	 * @param receiver the receiver for the events
	 * @param nodeID the ID of the requested node
	 * @throws IllegalArgumentException if an unknown node ID was specified
	 */
	public void writeNodeData(JPhyloIOEventReceiver receiver, String nodeID) throws IllegalArgumentException, IOException;  //TODO Can metadata be written directly to all formats, without storing metaevents?
	
	/**
	 * Returns an iterator returning the IDs of all edges starting at the specified node. This includes 
	 * all edges that reference the specified node as their source, but not edges that have this node as
	 * their target.
	 * 
	 * @param nodeID the ID of the parent node
	 * @return an iterator returning the edge IDs (Maybe an empty iterator but not {@code null}.)
	 * @throws IllegalArgumentException if an unknown node ID was specified
	 */
	public Iterator<String> getEdgeIDsFromNode(String nodeID) throws IllegalArgumentException;  //TODO Using this pattern may include circular references in networks.

	public void writeEdgeData(JPhyloIOEventReceiver receiver, String edgeID) throws IllegalArgumentException, IOException;  //TODO Can metadata be written directly to all formats, without storing metaevents?
}
