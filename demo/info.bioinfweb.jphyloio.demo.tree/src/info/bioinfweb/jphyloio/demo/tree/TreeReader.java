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


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.utils.JPhyloIOReadingUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;



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
public class TreeReader {
	/** Stores the <i>JPhyloIO</i> event reader that is currently used by this instance. */
	private JPhyloIOEventReader reader;
	
	/** Stores the application model that is the current target for data read by this instance. */
	private DefaultTreeModel model;
	
	/** 
	 * A map for internal use storing node objects created from encountered node events until they are combined to 
	 * form a tree topology. 
	 */
	private Map<String, MutableTreeNode> idToNodeMap = new HashMap<String, MutableTreeNode>();
	
	/** 
	 * A list of nodes that could become the tree root. (After all branches have been processed this list will 
	 * contain only one entry in case of trees.) 
	 */
	private List<String> possiblePaintStartIDs = new ArrayList<String>();
	
	
	/**
	 * Main method of this reader. It reads a tree using the specified <i>JPhyloIO</i> reader to the specified application 
	 * business model.
	 * <p>
	 * The loop in this method processes the event sequence defined by the <i>JPhyloIO</i> grammar node <code>Document</code>. 
	 * (The grammar can be found in the documentation of {@link JPhyloIOEventReader}.)
	 * 
	 * @param reader the <i>JPhyloIO</i> reader providing the event stream to be processed
	 * @param model the application business model to take up the loaded alignment data
	 * @throws IOException exceptions thrown during the I/O operation
	 */
	public void read(JPhyloIOEventReader reader, DefaultTreeModel model) throws IOException {
		
		// Store parameters in instance variables to have them available in all methods:
		this.reader = reader;
		this.model = model;
		
		// Process JPhyloIO events:
		while (reader.hasNextEvent()) {  // This loop will run until all events of the JPhyloIO reader are consumed (and the end of the 
			                               // document is reached). 
			JPhyloIOEvent event = reader.next();  // Read the next event from the JPhyloIO reader.
  		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
	      switch (event.getType().getContentType()) {  // This switch statement handles all types of elements on the top level that are 
	      	                                           // relevant for this application. The others are skipped in the default block. 
	      	case DOCUMENT:
	      		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {  // There can be a document start and a document end event.
	      			model.setRoot(null);  // Remove possible previous data from the model instance.
	      		}
	      		// Document end events do not need any treatment in this application.
	      		break;
	      		
	      	case TREE_NETWORK_GROUP:
	      		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {  // There can be a document start and a document end event.
	      			readTreeNetworkGroup();
	      			
	      		}
	        	break;
	        	
	        default:  // Here possible additional events on the top level are handled.
      			JPhyloIOReadingUtils.reachElementEnd(reader);
	        	break;
	      }
			}
		}
	}
	
	
	private void readTreeNetworkGroup() throws IOException {
		// Process JPhyloIO events:
		JPhyloIOEvent event = reader.next();  
		while ((!event.getType().getTopologyType().equals(EventTopologyType.END))) { 
  		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
	      if (event.getType().getContentType().equals(EventContentType.TREE)) {  // This application is only interested in tree events 
	      	                                                                     // and will skip others on this level (e.g. networks).
	      	if (model.getRoot() == null) {
	      		readTree();
	      	}
	    		else {
	    			LabeledIDEvent treeEvent = event.asLabeledIDEvent();
	    			System.out.println("Since this application does not support multiple trees, the tree with the ID "
	    					+ treeEvent.getID() + " and the name \"" + treeEvent.getLabel() + "\" was skipped.");
	    		}
	      }
	      else {
	  			JPhyloIOReadingUtils.reachElementEnd(reader);
	  		}  // Otherwise SOLE elements are skipped.
  		}
			event = reader.next();  // Read the next event from the JPhyloIO reader.	      
		}
	}
	
	
	private void readTree() throws IOException {
		possiblePaintStartIDs.clear();
		idToNodeMap.clear();
		
    JPhyloIOEvent event = reader.next();
    while (!event.getType().getTopologyType().equals(EventTopologyType.END)) {
  		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
	    	if (event.getType().getContentType().equals(EventContentType.NODE)) {
	    		readNode(event.asNodeEvent());
	    	}
	    	else if (event.getType().getContentType().equals(EventContentType.EDGE) || event.getType().getContentType().equals(EventContentType.ROOT_EDGE)) {
	    		readEdge(event.asEdgeEvent());
	    	}
	      else {  // Possible additional element, which is not read
	      	JPhyloIOReadingUtils.reachElementEnd(reader);
	      }
  		}
      event = reader.next();
    }
    
    if (possiblePaintStartIDs.size() > 1) {
    	throw new IOException("More than one root node was found.");
    }
    
    model.setRoot(idToNodeMap.get(possiblePaintStartIDs.get(0)));
  }
	
	
	private void readNode(NodeEvent nodeEvent) throws IOException {
		MutableTreeNode node = new DefaultMutableTreeNode(nodeEvent.getLabel());
		idToNodeMap.put(nodeEvent.getID(), node);
		possiblePaintStartIDs.add(nodeEvent.getID());
		
  	JPhyloIOReadingUtils.reachElementEnd(reader);  // Consume possible nested events.
  }
	
	
	private void readEdge(EdgeEvent edgeEvent) throws IOException {
		MutableTreeNode targetNode = idToNodeMap.get(edgeEvent.getTargetID());
		MutableTreeNode sourceNode = idToNodeMap.get(edgeEvent.getSourceID());		
		
		if (targetNode.getParent() == null) {
			if (sourceNode != null) {
				sourceNode.insert(targetNode, sourceNode.getChildCount());  // Will also set sourceNode as the parent of targetNode.
				possiblePaintStartIDs.remove(edgeEvent.getTargetID());  // Nodes that have not yet been referenced as target are possible roots.
			}
		}
		else {  // Edge is network edge
			throw new IOException("Multiple parent nodes were specified for the node \"" + edgeEvent.getTargetID() + 
					", but networks can not be displayed by this application.");
		}
		
   	JPhyloIOReadingUtils.reachElementEnd(reader);  // Consume possible nested events.
  }
}
