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
package info.bioinfweb.jphyloio.events;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * Event indicating a node in a tree or network. If a node represents a root node of a tree, 
 * {@link isRootNode()} will return {@code true}. A node in a network will never be a root node.
 * <p>
 * This event is a start event, which is followed by an end event of the same content type. Comment
 * and metainformation events maybe nested between this and its according end event. (See the description
 * of {@link JPhyloIOEventReader} for the complete grammar definition of JPhyloIO event streams.)
 * 
 * @author Sarah Wiechers
 */
public class NodeEvent extends LinkedLabeledIDEvent {
	private boolean isRootNode;

	
	public NodeEvent(String id, String label, String linkedID, boolean isRootNode) {
		super(EventContentType.NODE, id, label, linkedID);
		this.isRootNode = isRootNode;
	}

	
	/**
	 * Indicates whether this node is a true root of the tree it belongs to.
	 * <p>
	 * As opposed to the presence of a root edge, that merely specifies if an edge with a certain length 
	 * leading to a root node should be displayed, a true root node is only present if the tree should be 
	 * considered rooted and not only be displayed as rooted.
	 * <p>
	 * A network node will never be a true root.
	 * 
	 * @return {@code true} if this node is a true root or {@code false} otherwise
	 */
	public boolean isRootNode() {
		return isRootNode;
	}
}
