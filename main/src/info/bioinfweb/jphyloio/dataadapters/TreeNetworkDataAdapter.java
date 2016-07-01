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


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.utils.NodeEdgeIDLister;

import java.io.IOException;
import java.util.Iterator;



/**
 * Data adapter interface that provides data for a tree or a network.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 * @see NodeEdgeIDLister
 */
public interface TreeNetworkDataAdapter extends ElementDataAdapter<LabeledIDEvent> {
	//TODO Additional methods are needed, since NeXML expects all node definitions before the first edge definition. Alternative iterators or returning events in WLR order may solve the problem.
	
	/**
	 * Determines whether this instance represents a phylogenetic tree or a phylogenetic network.
	 * (Not all formats accept networks.)
	 *
	 * @param parameters the parameter map of the calling writer that provides context information for the data request
	 * @return {@code true} if this instance represents a tree or {@code false} otherwise
	 */
	public boolean isTree(ReadWriteParameterMap parameters);
	
	/**
	 * Defines whether the represented tree or network shall be considered as rooted (at the specified root edge(s))
	 * or if it shall be considered as an unrooted network (where the specified root edge(s) just specify the position
	 * to start drawing the network or unrooted tree). 
	 *
	 * @param parameters the parameter map of the calling writer that provides context information for the data request
	 * @return {@code true} if the tree or network shall be considered rooted or {@code false} otherwise
	 */
	@Deprecated
	public boolean considerRooted(ReadWriteParameterMap parameters);
	
	/**
	 * Returns an iterator returning the IDs of all root edges of the represented phylogenetic
	 * tree or network.
	 *
	 * @param parameters the parameter map of the calling writer that provides context information for the data request
	 * @return an iterator returning the edge IDs (Must return at least one element.)
	 */
	public Iterator<String> getRootEdgeIDs(ReadWriteParameterMap parameters);

	/**
	 * Returns an iterator returning the IDs of all edges starting at the specified node. This includes 
	 * all edges that reference the specified node as their source, but not edges that have this node as
	 * their target.
	 *
	 * @param parameters the parameter map of the calling writer that provides context information for the data request
	 * @param nodeID the ID of the parent node
	 * @return an iterator returning the edge IDs (Maybe an empty iterator but not {@code null}.)
	 * @throws IllegalArgumentException if an unknown node ID was specified
	 */
	public Iterator<String> getEdgeIDsFromNode(ReadWriteParameterMap parameters, String nodeID) throws IllegalArgumentException;  //TODO Using this pattern may include circular references in networks.

	/**
	 * Returns the start event of a node determined by the specified node ID.
	 *
	 * @param parameters the parameter map of the calling writer that provides context information for the data request
	 * @param id the ID of the requested node
	 * @return the event that describes the specified node
	 * @throws IllegalArgumentException if no node start event for the specified ID is present
	 */
	public NodeEvent getNodeStartEvent(ReadWriteParameterMap parameters, String id);
	
	/**
	 * Writes the events nested in the specified node.
	 *
	 * @param parameters the parameter map of the calling writer that provides context information for the data request
	 * @param receiver the receiver for the events
	 * @param nodeID the ID of the requested node
	 * @throws IllegalArgumentException if an unknown node ID was specified
	 */
	public void writeNodeContentData(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, String nodeID) throws IOException;  //TODO Can metadata be written directly to all formats, without storing metaevents?
	
	/**
	 * Returns the start event of an edge determined by the specified ID.
	 *
	 * @param parameters the parameter map of the calling writer that provides context information for the data request
	 * @param id the ID of the requested edge
	 * @return an instance of EdgeEvent that describes the specified edge
	 * @throws IllegalArgumentException if no to EdgeEvent for the specified ID is present 
	 */
	public EdgeEvent getEdgeStartEvent(ReadWriteParameterMap parameters, String id);
	
	/**
	 * Writes the events nested in the specified node.
	 *
	 * @param parameters the parameter map of the calling writer that provides context information for the data request
	 * @param receiver the receiver for the events
	 * @param edgeID the ID of the requested edge
	 * @throws IOException if a I/O error occurs while writing the data
	 * @throws IllegalArgumentException if an unknown edge ID was specified
	 */
	public void writeEdgeContentData(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, String edgeID) throws IOException, IllegalArgumentException;  //TODO Can metadata be written directly to all formats, without storing metaevents?
	
	/**
	 * Returns a list of node-and-edge-sets defined for the tree modeled by this instance.
	 *
	 * @param parameters the parameter map of the calling writer that provides context information for the data request
	 * @return a (possibly empty) list of node-edge-sets
	 */
	public ObjectListDataAdapter<LinkedLabeledIDEvent> getNodeEdgeSets(ReadWriteParameterMap parameters);
}
