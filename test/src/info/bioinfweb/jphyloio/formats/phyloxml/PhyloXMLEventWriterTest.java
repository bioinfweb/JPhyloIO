/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.phyloxml;


import info.bioinfweb.commons.testing.TestXMLEvents;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreTreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.TestTreeDataAdapter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.management.Attribute;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;



public class PhyloXMLEventWriterTest implements PhyloXMLConstants {
	private long idIndex = 1;
	
	
	public long getIdIndex() {
		long index = idIndex;
		idIndex++;
		return index;
	}
	
	
	private void writeDocument(StoreDocumentDataAdapter document, File file) throws IOException {
		PhyloXMLEventWriter writer = new PhyloXMLEventWriter();
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();		
		writer.writeDocument(document, file, parameters);
	}
	
	
	@Test
	public void createSingleTreeDocument() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(null, 
				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));
		
		trees.getTreesAndNetworks().add(new TestTreeDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, file);
		
		// Validate file:
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
		try {
			TestXMLEvents.assertStartDocument(reader);
			
			TestXMLEvents.assertStartElement(TAG_ROOT, reader);
			
			TestXMLEvents.assertStartElement(TAG_PHYLOGENY, reader);
			TestXMLEvents.assertShortElement(TAG_ID, "tree2", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnRoot", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.5", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnRoot", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node '_1", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.0", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDn1", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.1", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnA", reader);			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "0.9", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnB", reader);
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "2.0", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnC", reader);
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			TestXMLEvents.assertEndElement(TAG_PHYLOGENY, reader);
			TestXMLEvents.assertEndElement(TAG_ROOT, reader);			
			
			TestXMLEvents.assertEndDocument(reader);
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
//	@Test
//	public void createSingleTreeDocumentWithMetadata() throws IOException {
//		// Write file
//		File file = new File("data/testOutput/PhyloXMLTest.xml");
//		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
//		URI example = null;
//		
//		try {
//			example = new URI("somePath/#fragment");
//		} 
//		catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
//		
//		document.getAnnotations().add(new ResourceMetadataEvent("meta" + getIdIndex(), "ResourceMeta", new QName("http://meta.net/", "relations"), 
//				example, null));
//		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
//		
//		document.getAnnotations().add(new LiteralMetadataEvent("meta" + getIdIndex(), "LiteralMeta", new QName("http://meta.net/", "predicate"), "literal value", LiteralContentSequenceType.SIMPLE));
//		document.getAnnotations().add(new LiteralMetadataContentEvent(NeXMLConstants.TYPE_STRING, "My literal value", true));
//		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
//		
//		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(null, 
//				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));
//		trees.getTreesAndNetworks().add(new TestTreeDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
//		document.getTreesNetworks().add(trees);
//		
//		writeDocument(document, file);
//		
//// Validate file:
//		BufferedReader reader = new BufferedReader(new FileReader(file));
//		try {
////			assertEquals("", reader.readLine());			
////			assertEquals(-1, reader.read());
//		}
//		finally {
//			reader.close();
//			file.delete();
//		}
//	}
	
	
	@Test
	public void createEmptyDocument() throws IOException, XMLStreamException {
		// Write file
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		writeDocument(document, file);
		
		// Validate file:
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
		try {
			TestXMLEvents.assertStartDocument(reader); //TODO does the xml tag have an end element?
//			TestXMLEvents.assertEndDocument(reader);
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
//	@Test
//	public void createNoTreesDocument() throws IOException, XMLStreamException {		
//		File file = new File("data/testOutput/PhyloXMLTest.xml");
//		
//		// Write file
//		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
//		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(null, 
//				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));		
//		
//		document.getTreesNetworks().add(trees);
//		
//		writeDocument(document, file);
//		
//		// Validate file:
//		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
//		try {
//			TestXMLEvents.assertStartDocument(reader); //TODO does the xml tag have an end element?
////			TestXMLEvents.assertEndDocument(reader);
//		}
//		finally {
//			reader.close();
//			file.delete();
//		}
//	}
	
	
	@Test
	public void createMultipleTreesDocument() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(null, 
				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));
		
		trees.getTreesAndNetworks().add(new TestTreeDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
		trees.getTreesAndNetworks().add(new TestTreeDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, file);
		
		// Validate file:
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
		try {
			TestXMLEvents.assertStartDocument(reader);
			
			TestXMLEvents.assertStartElement(TAG_ROOT, reader);
			
			TestXMLEvents.assertStartElement(TAG_PHYLOGENY, reader);			
			TestXMLEvents.assertShortElement(TAG_ID, "tree2", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnRoot", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.5", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnRoot", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node '_1", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.0", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDn1", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.1", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnA", reader);			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "0.9", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnB", reader);
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "2.0", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnC", reader);
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			TestXMLEvents.assertEndElement(TAG_PHYLOGENY, reader);
			
			TestXMLEvents.assertStartElement(TAG_PHYLOGENY, reader);			
			TestXMLEvents.assertShortElement(TAG_ID, "tree3", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnRoot", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.5", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnRoot", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node '_1", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.0", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDn1", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.1", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnA", reader);			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "0.9", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnB", reader);
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "2.0", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnC", reader);
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			TestXMLEvents.assertEndElement(TAG_PHYLOGENY, reader);
			
			TestXMLEvents.assertEndElement(TAG_ROOT, reader);			
			
			TestXMLEvents.assertEndDocument(reader);
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void createMultipleTreegroupsDocument() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		StoreTreeNetworkGroupDataAdapter trees1 = new StoreTreeNetworkGroupDataAdapter(null, 
				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));
		trees1.getTreesAndNetworks().add(new TestTreeDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));		
		
		StoreTreeNetworkGroupDataAdapter trees2 = new StoreTreeNetworkGroupDataAdapter(null, 
				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));
		trees1.getTreesAndNetworks().add(new TestTreeDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
		
		document.getTreesNetworks().add(trees1);
		document.getTreesNetworks().add(trees2);
		
		writeDocument(document, file);
		
		// Validate file:
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
		try {
			TestXMLEvents.assertStartDocument(reader);
			
			TestXMLEvents.assertStartElement(TAG_ROOT, reader);
			
			TestXMLEvents.assertStartElement(TAG_PHYLOGENY, reader);			
			TestXMLEvents.assertShortElement(TAG_ID, "tree2", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnRoot", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.5", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnRoot", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node '_1", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.0", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDn1", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.1", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnA", reader);			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "0.9", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnB", reader);
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "2.0", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnC", reader);
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			TestXMLEvents.assertEndElement(TAG_PHYLOGENY, reader);
			
			TestXMLEvents.assertStartElement(TAG_PHYLOGENY, reader);			
			TestXMLEvents.assertShortElement(TAG_ID, "tree4", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnRoot", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.5", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnRoot", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node '_1", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.0", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDn1", reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "1.1", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnA", reader);			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "0.9", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnB", reader);
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertStartElement(TAG_CLADE, reader);
			TestXMLEvents.assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
			TestXMLEvents.assertShortElement(TAG_BRANCH_LENGTH, "2.0", reader);
			TestXMLEvents.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnC", reader);
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			
			TestXMLEvents.assertEndElement(TAG_CLADE, reader);
			TestXMLEvents.assertEndElement(TAG_PHYLOGENY, reader);
			
			TestXMLEvents.assertEndElement(TAG_ROOT, reader);			
			
			TestXMLEvents.assertEndDocument(reader);
		}
		finally {
			reader.close();
			file.delete();
		}
	}
}
