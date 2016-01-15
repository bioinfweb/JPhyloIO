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


import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



/**
 * Event that indicates the definition of a tree node in a document. Different nodes maybe linked by
 * subsequent {@link EdgeEvent}s.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class NodeEvent extends ConcreteJPhyloIOEvent {
	private String id;
	private String label;
	//TODO do node events need a "rooted"-attribute or should that information (if given) be in a Meta-Event?
	
	
	/**
	 * Creates a new instance of this event.
	 * 
	 * @param id an ID of this node which is unique for the current document
	 * @param label an optional label the represented node is carrying (maybe {@code null}).
	 */
	public NodeEvent(String id, String label) {
		super(EventContentType.NODE, EventTopologyType.START);
		
		if (id == null) {
			throw new NullPointerException("The ID must not be null.");
		}
		else {
			this.id = id;
			this.label = label;
		}
	}


	/**
	 * An ID identifying this node, which is unique for this document. If a format defines IDs (e.g. NeXML 
	 * or XTG), these are used here. For formats not defining any IDs, the parser will generate its own IDs.
	 * <p>
	 * The ID returned here will also be referenced in up-coming {@link EdgeEvent}s connecting two nodes.
	 * (Applications based on JPhyloIO need this ID only to process up-coming {@link EdgeEvent}s and 
	 * do not necessarily have to store that IDs in their data model. Different IDs may be used for writing
	 * the data in a possible subsequent step.)  
	 * 
	 * @return the ID of this node (unique for this document)
	 */
	public String getID() {
		return id;
	}


	/**
	 * Returns the label attached to this node.
	 * <p>
	 * Note that additional annotations of this node may be specified by up-coming metaevents, even if the node
	 * carries no label.
	 * 
	 * @return the label string of {@code null} if this node carries no label
	 */
	public String getLabel() {
		return label;
	}
}
