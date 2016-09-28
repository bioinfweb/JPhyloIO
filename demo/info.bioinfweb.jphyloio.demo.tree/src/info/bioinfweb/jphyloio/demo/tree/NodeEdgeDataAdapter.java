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
package info.bioinfweb.jphyloio.demo.tree;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.utils.NumberedIDsIterator;



/**
 * Abstract implementation of the object list data adapter to be used to provide node and branch events from the
 * busness model of this example application to <i>JPhyloIO</i> writers.
 * 
 * @author Ben St&ouml;ver
 *
 * @param <E> the event type of start events representing elements of this list
 */
public abstract class NodeEdgeDataAdapter<E extends LabeledIDEvent> implements ObjectListDataAdapter<E> {
	private List<TreeNode> nodes;
	private String idPrefix;
	
	
	public NodeEdgeDataAdapter(List<TreeNode> nodes, String idPrefix) {
		super();
		this.nodes = nodes;
		this.idPrefix = idPrefix;
	}
	
	
	protected abstract E createEvent(String id, int index, TreeNode node);
	
	
	@Override
	public E getObjectStartEvent(ReadWriteParameterMap parameters, String id) throws IllegalArgumentException {
		int index = (int)NumberedIDsIterator.extractIndexFromID(id, idPrefix);
		return createEvent(id, index, nodes.get(index));
	}

	
	@Override
	public long getCount(ReadWriteParameterMap parameters) {
		return nodes.size();
	}

	
	@Override
	public Iterator<String> getIDIterator(ReadWriteParameterMap parameters) {
		return new NumberedIDsIterator(idPrefix, nodes.size());
	}

	
	@Override
	public void writeContentData(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, String id) 
			throws IOException, IllegalArgumentException {}  // No node or branch metadata present in the model of this application.
}
