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
package info.bioinfweb.jphyloio.demo.metadata.adapters;


import java.util.List;

import javax.swing.tree.TreeNode;

import info.bioinfweb.commons.IntegerIDManager;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.demo.tree.NodeEdgeListDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;



/**
 * {@link NodeEdgeListDataAdapter} from the tree demo project provides the foundation for data adapters implementing node and
 * edge lists in this demo application. This class is extends it by shared functionality that is inherited to
 * {@link NodeListDataAdapter} and {@link EdgeListDataAdapter}.
 * 
 * @author Ben St&ouml;ver
 *
 * @param <E>
 */
public abstract class AbstractMetadataNodeEdgeListDataAdapter<E extends LabeledIDEvent> extends NodeEdgeListDataAdapter<E> 
		implements ReadWriteConstants {
	
	private IntegerIDManager idManager;
	
	
	public AbstractMetadataNodeEdgeListDataAdapter(List<TreeNode> nodes, String idPrefix, IntegerIDManager idManager) {
		super(nodes, idPrefix);
		this.idManager = idManager;
	}


	/**
	 * Creates a new unique ID for metadata events.
	 */
	protected String createNewMetadataID() {
		return DEFAULT_META_ID_PREFIX + idManager.createNewID();
	}
}
