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
package info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter;


import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.map.ListOrderedMap;



public class StoreTreeNetworkDataAdapter extends StoreAnnotatedDataAdapter implements TreeNetworkDataAdapter {
	private StoreLinkedOTUsDataAdapter storeLinkedOTUsAdapter;
	private boolean isTree;
	private boolean considerRooted;
	private ListOrderedMap<String, StoreObjectData<LinkedLabeledIDEvent>> nodes = null;
	private ListOrderedMap<String, StoreObjectData<EdgeEvent>> edges = null;
	

	public StoreTreeNetworkDataAdapter(List<JPhyloIOEvent> annotations, LinkedLabeledIDEvent treeOrNetworkStartEvent, 
			boolean isTree, boolean considerRooted) {
		super(annotations);
		this.storeLinkedOTUsAdapter = new StoreLinkedOTUsDataAdapter(treeOrNetworkStartEvent);
		this.isTree = isTree;
		this.considerRooted = considerRooted;
	}


	@Override
	public void writeMetadata(JPhyloIOEventReceiver receiver) throws IOException {
		super.writeMetadata(receiver);
	}
	

	@Override
	public boolean hasMetadata() {
		return super.hasMetadata();
	}
	

	@Override
	public List<JPhyloIOEvent> getAnnotations() {
		return super.getAnnotations();
	}
	

	public LinkedLabeledIDEvent getStartEvent() {
		return storeLinkedOTUsAdapter.getStartEvent();
	}


	public void setStartEvent(LinkedLabeledIDEvent startEvent) {
		storeLinkedOTUsAdapter.setStartEvent(startEvent);
	}


	@Override
	public boolean isTree() {
		return isTree;
	}
	

	@Override
	public boolean considerRooted() {
		return considerRooted;
	}


	public ListOrderedMap<String, StoreObjectData<LinkedLabeledIDEvent>> getNodes() {
		return nodes;
	}


	public ListOrderedMap<String, StoreObjectData<EdgeEvent>> getEdges() {
		return edges;
	}


	@Override
	public Iterator<String> getRootEdgeIDs() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public EdgeEvent getEdgeStartEvent(String id) {
		return edges.get(id).getObjectStartEvent();
	}
	

	@Override
	public Iterator<String> getEdgeIDsFromNode(String nodeID) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public void writeEdgeContentData(JPhyloIOEventReceiver receiver, String edgeID)	throws IOException, IllegalArgumentException {
		for (JPhyloIOEvent event : edges.get(edgeID).getObjectContent()){
			receiver.add(event);
		}
	}
	

	@Override
	public LinkedLabeledIDEvent getNodeStartEvent(String id) {
		return nodes.get(id).getObjectStartEvent();
	}
	

	@Override
	public void writeNodeContentData(JPhyloIOEventReceiver receiver, String nodeID)	throws IOException {
		for (JPhyloIOEvent event : edges.get(nodeID).getObjectContent()){
			receiver.add(event);
		}		
	}	
}
