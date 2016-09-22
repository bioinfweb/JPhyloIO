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
package info.bioinfweb.jphyloio.demo.simplealignment;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.utils.JPhyloIOReadingUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * This is the <i>JPhyloIO</i> reader of this demo application that connects the readers available in <i>JPhyloIO</i> with the
 * business model of the application by extracting relevant data from the <i>JPhyloIO</i> event sequence and storing it in the 
 * application model.
 * <p>
 * Each application based on <i>JPhyloIO</i> needs to implement one such reader to support all available formats. The application 
 * can use this reader by calling {@link #read(JPhyloIOEventReader, Map)}.
 * <p>
 * Each method in this reader handles a sequence of events nested between a start and end event. although there are alternatives,
 * this is usually a good way to implement pull parsing. Each of these methods corresponds to a grammar node of the <i>JPhyloIO</i>
 * event sequence that can be found in the documentation of {@link JPhyloIOEventReader}. 
 * 
 * @author Ben St&ouml;ver
 */
public class AlignmentReader {
	private JPhyloIOEventReader reader;
	private Map<String, List<String>> model;
	
	
	/**
	 * Main method of this reader. It reads an alignment using the specified <i>JPhyloIO</i> reader to the specified application 
	 * business model.
	 * <p>
	 * The loop in this method processes the event sequence defined by the <i>JPhyloIO</i> grammar node <code>Document</code>. 
	 * (The grammar can be found in the documentation of {@link JPhyloIOEventReader}.)
	 * 
	 * @param reader the <i>JPhyloIO</i> reader providing the event stream to be processed
	 * @param model the model to take up the loaded alignment data
	 * @throws IOException exceptions thrown during the I/O operation
	 */
	public void read(JPhyloIOEventReader reader, Map<String, List<String>> model) 
			throws IOException {  // Possible exceptions from JPhyloIO readers are forwarded.
		
		// Store parameters in instance variables to have them available in all methods:
		this.reader = reader;
		this.model = model;
		
		// Process JPhyloIO events:
		while (reader.hasNextEvent()) {  // This loop will run until all events of the JPhyloIO reader are consumed (and the end of the 
			                               // document is reached). 
			JPhyloIOEvent event = reader.next();  // Read the next event from the JPhyloIO reader.	      
      switch (event.getType().getContentType()) {  // This switch statement handles all types of elements on the top level that are 
      	                                           // relevant for this application. The others are skipped in the default block. 
      	case DOCUMENT:
      		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {  // There can be a document start and a document end event.
      			model.clear();  // Remove possible previous data from the model instance.
      		}
      		// Document end events do not need any treatment in this application.
      		break;
      		
      	case ALIGNMENT:
      		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {  // There can be a document start and a document end event.
      			if (model.isEmpty()) {
      				readAlignment();  // Delegate reading the alignment contents to another method (that corresponds to a nested grammar node).
      			}
      			else {
      				System.out.println("Since this application does not support multiple alignments, the alignment with the ID "
      						+ event.asLinkedLabeledIDEvent().getID() + " was skipped.");
      			}
      		}
        	break;
        	
        default:  // Here possible additional events on the top level are handled (e.g. tree group events).
        	JPhyloIOReadingUtils.reachElementEnd(reader);  // This tool method will skip over all elements until the end event to the
        	                                               // current event was read. This way all nested events not relevant for this
        	                                               // application are skipped. If the current event would have the topology
        	                                               // type SOLE, the method will skip no additional events.
        	break;
      }
		}
	}
	
	
	/**
	 * This method reads the contents of an alignment to the application business model.
	 * <p>
	 * The loop in this method processes the event sequence defined by the <i>JPhyloIO</i> grammar node <code>Matrix</code>. (The 
	 * grammar can be found in the documentation of {@link JPhyloIOEventReader}.)
	 * 
	 * @throws IOException exceptions thrown during the I/O operation
	 */
	private void readAlignment() throws IOException {
		// Process JPhyloIO events:
		JPhyloIOEvent event = reader.next();  
		while ((!event.getType().getTopologyType().equals(EventTopologyType.END))) {  // This loop will run until all events for the 
			                                                                            // current alignment are consumed. 
      if (event.getType().getContentType().equals(EventContentType.SEQUENCE)) {  // This application is only interested in sequence events 
      	                                                                         // and will skip others on this level (e.g. character set 
      	                                                                         // or sequence set definitions).
      	readSequencePart(event.asLinkedLabeledIDEvent().getID());  // Delegate reading the sequence contents to another method.
      }
      else {
      	JPhyloIOReadingUtils.reachElementEnd(reader);  // Skip events not processed by this application.
      }
			event = reader.next();  // Read the next event from the JPhyloIO reader.	      
		}
	}
	
	
	/**
	 * Reads the contents (tokens) of a sequence (part).
	 * <p>
	 * Note that JPhyloIO allows to split sequences into multiple parts (i.e. multiple sequence start events with the same ID).
	 * If an ID is encountered the second time, contained tokens must be appended to the current sequence. The JPhyloIO grammar
	 * allows this to be able to efficiently process interleaved formats versions (of e.g. Nexus or Phylip). Selecting or creating 
	 * the correct sequence object is handled by {@link #getSequence(String)}.
	 * <p>
	 * The loop in this method processes the event sequence defined by the <i>JPhyloIO</i> grammar node <code>SequencePart</code>. 
	 * (The grammar can be found in the documentation of {@link JPhyloIOEventReader}.)
	 * 
	 * @param sequenceID the ID of the sequence to be read
	 * @throws IOException 
	 */
	private void readSequencePart(String sequenceID) throws IOException {
		List<String> sequence = getSequence(sequenceID);  // Fetch an existing or create a new sequence object from the model.
		
		JPhyloIOEvent event = reader.next();  
		while ((!event.getType().getTopologyType().equals(EventTopologyType.END))) { 
			switch (event.getType().getContentType()) { 
				case SEQUENCE_TOKENS:  // This event always has the topology type SOLE, so there is no further if-statement necessary. 
					sequence.addAll(event.asSequenceTokensEvent().getTokens());  // Append the tokens from this event to the current sequence. 
					break;
					
				case SINGLE_SEQUENCE_TOKEN:  // As an alternative to the SEQUENCE_TOKENS event that contains a list of tokens, JPhyloIO also 
					                           // allows to represents a single tokens in one event to be able to nest metadata of that token.
					sequence.add(event.asSingleSequenceTokenEvent().getToken());  // Append the tokens from this event to the current sequence.
					                                                              // No check of the topology type is required here, since the 
					                                                              // first event will always be the start event and the end 
					                                                              // event will directly be consumed in the next line.
					JPhyloIOReadingUtils.reachElementEnd(reader);  // Skip all events nested under this event and the respective end event.
					break;
					
				default:
					JPhyloIOReadingUtils.reachElementEnd(reader);  // Skip possible events on this level, the application is not interested in.
					break;
			}
			event = reader.next();	      
		}
	}
	
	
	/**
	 * Returns a sequence from the model. If none with the specified ID is currently present, a new one will be created, added to the
	 * model and returned.
	 * 
	 * @param sequenceID the ID of the requested sequence
	 * @return the sequence object from the model
	 */
	private List<String> getSequence(String sequenceID) {
		List<String> result = model.get(sequenceID);
		if (result == null) {
			result = new ArrayList<String>();
			model.put(sequenceID, result);
		}
		return result;
	}
}
