/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.dataadapters.implementations.store;


import java.io.IOException;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.idediting.IDEditor;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;



public class StoreReader {
	@SuppressWarnings("unchecked")
	private static <E extends LabeledIDEvent> E cloneEventWithNewID(E event, IDEditor idEditor) {
		if (idEditor == null) {
			return event;
		}
		else {
			return (E)event.cloneWithNewID(idEditor.editID(event.getID(), event));
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static <E extends LabeledIDEvent> void readIntoObjectList(JPhyloIOEventReader reader, IDEditor idEditor, 
			StoreObjectListDataAdapter<E> adapter, EventContentType objectType) throws IllegalArgumentException, ClassCastException, IOException {

		JPhyloIOEvent startEvent = reader.next();
		if (startEvent.getType().equals(objectType, EventTopologyType.START)) {
			// Create new entry and store start event:
			E objectStartEvent = cloneEventWithNewID((E)startEvent, idEditor);
			adapter.setObjectStartEvent(objectStartEvent);
			
			// Store nested events:
			StoreObjectData<E> objectData = adapter.getObjectMap().get(objectStartEvent.getID());
			JPhyloIOEvent event = reader.next();
			while (!event.getType().equals(objectType, EventTopologyType.END)) {
				if (event instanceof LabeledIDEvent) {
					event = cloneEventWithNewID((LabeledIDEvent)event, idEditor);
				}
				objectData.getObjectContent().add(event);
				event = reader.next();
			}
		}
		else {
			throw new IOException("Cannot read information from the stream. Was expecting " + new EventType(objectType, EventTopologyType.START) + 
					" but found " + startEvent.getType() + ".");
		}
	}
	
	
	private static void readTreeNetworkContents(JPhyloIOEventReader reader, IDEditor idEditor, StoreTreeNetworkDataAdapter adapter, 
			EventContentType endType) throws IOException {
		
		JPhyloIOEvent event = reader.peek();
		while (!event.getType().equals(endType, EventTopologyType.END)) {  //TODO It would be sufficient to check the content type. Another start event of that type should trigger an exception.
			switch (event.getType().getContentType()) {
				case LITERAL_META:
				case RESOURCE_META:
				case LITERAL_META_CONTENT:
					if (event instanceof LabeledIDEvent) {
						event = cloneEventWithNewID((LabeledIDEvent) event, idEditor);
					}
					adapter.getAnnotations().add(event);
					reader.next();  // Consume event.
					break;

				case NODE:
					readIntoObjectList(reader, idEditor, adapter.getNodes(null), event.getType().getContentType());  //TODO Add other getter so null does not have to be provided?
					break;
					
				case EDGE:
				case ROOT_EDGE:
					readIntoObjectList(reader, idEditor, adapter.getEdges(null), event.getType().getContentType());
					break;
					
				case NODE_EDGE_SET:
					readIntoObjectList(reader, idEditor, adapter.getNodeEdgeSets(null), event.getType().getContentType());
					break;
					
				default:
					reader.next();  // Consume and ignore possible additional events.  //TODO Log or throw exception?
					break;
			}
			event = reader.peek();
		}
	}
	
	
	/**
	 * Reads the contents of a tree or network definition from the specified reader into a new instance of {@link StoreTreeNetworkDataAdapter}.
	 * <p>
	 * If an id editor is specified, all stored events with an ID will be generated using {@link LabeledIDEvent#cloneWithNewID(String)} instead
	 * of storing the event directly. If {@code null} is specified the original event instance from the reader are stored. 
	 * 
	 * @param reader the reader providing the event stream. (Note that the next element to be returned must be a start event with the type
	 *        {@link EventContentType#TREE} or {@link EventContentType#NETWORK}.)
	 * @param idEditor an optional ID editor that is used to create new IDs for the events to be stored (May be {@code null}.)
	 * @return a store adapter instance that can be used to write the tree or network that was read by this method
	 * 
	 * @throws IOException if an error occurs when requesting new events from the reader or if the first event is not an appropriate start 
	 *         event as described above. 
	 */
	public static StoreTreeNetworkDataAdapter readTreeNetwork(JPhyloIOEventReader reader, IDEditor idEditor) throws IOException {
		if (reader.hasNextEvent()) {
			JPhyloIOEvent startEvent = reader.next();
			if (EventTopologyType.START.equals(startEvent.getType().getTopologyType())) {
				boolean isTree = EventContentType.TREE.equals(startEvent.getType().getContentType());
				if (isTree || EventContentType.NETWORK.equals(startEvent.getType().getContentType())) {
					StoreTreeNetworkDataAdapter result = new StoreTreeNetworkDataAdapter();
					
					result.setStartEvent(cloneEventWithNewID(startEvent.asLabeledIDEvent(), idEditor));
					result.setTree(isTree);
					readTreeNetworkContents(reader, idEditor, result, startEvent.getType().getContentType());
					
					return result;
				}
				else {
					throw new IOException("No tree or network information can be read from the stream, since the next element is a start event with the type " + 
							startEvent.getType().getContentType() + ". Start events must have the types " + EventContentType.TREE + " or " + 
							EventContentType.NETWORK + ".");
				}
			}
			else {
				throw new IOException("No tree or network information can be read from the stream, since the next element is an end element.");
			}
		}
		else {
			throw new IOException("No tree or network information can be read from the stream, since there are not more events to consume.");
		}
	}
}
