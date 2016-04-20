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
package info.bioinfweb.jphyloio.formats.phyloxml;


import static org.junit.Assert.assertEquals;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreTreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.TestTreeDataAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;



public class PhyloXMLEventWriterTest {
	private long idIndex = 0;
	
	
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
	
	
//	@Test
//	public void createSingleTreeDocument() throws IOException {
//		// Write file
//		File file = new File("data/testOutput/PhyloXMLTest.xml");
//		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
//		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(null, 
//				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));
//		
//		trees.getTreesAndNetworks().add(new TestTreeDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
//		document.getTreesNetworks().add(trees);
//		
//		writeDocument(document, file);
//		
//		// Validate file:
//		BufferedReader reader = new BufferedReader(new FileReader(file));
//		try {
////			assertEquals("<?xml version=\"1.0\" ?>", reader.readLine());
////			assertEquals("<phyloxml xmlns=\"http://www.phyloxml.org\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">", reader.readLine());
////			assertEquals("<phylogeny rooted=\"true\" branch_length_unit=\"xs:double\">", reader.readLine());
////			assertEquals("<id>tree1</id>", reader.readLine());
////			assertEquals("<clade>", reader.readLine());
//			
////			assertEquals(-1, reader.read());
//		}
//		finally {
//			reader.close();
//			file.delete();
//		}
//	}
	
	
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
	public void createEmptyDocument() throws IOException {
		// Write file
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		writeDocument(document, file);
		
// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {		
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
//	@Test
//	public void createNoTreesDocument() throws IOException {
//		// Write file
//		File file = new File("data/testOutput/PhyloXMLTest.xml");
//		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
//		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(null, 
//				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));		
//		
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
//	
//	
//	@Test
//	public void createMultipleTreesDocument() throws IOException {
//		// Write file
//		File file = new File("data/testOutput/PhyloXMLTest.xml");
//		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
//		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(null, 
//				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));
//		
//		trees.getTreesAndNetworks().add(new TestTreeDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
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
//	
//	
//	@Test
//	public void createMultipleTreegroupsDocument() throws IOException {
//		// Write file
//		File file = new File("data/testOutput/PhyloXMLTest.xml");
//		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
//		
//		StoreTreeNetworkGroupDataAdapter trees1 = new StoreTreeNetworkGroupDataAdapter(null, 
//				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));		
//		trees1.getTreesAndNetworks().add(new TestTreeDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));		
//		
//		StoreTreeNetworkGroupDataAdapter trees2 = new StoreTreeNetworkGroupDataAdapter(null, 
//				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));		
//		trees1.getTreesAndNetworks().add(new TestTreeDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
//		
//		document.getTreesNetworks().add(trees1);
//		document.getTreesNetworks().add(trees2);
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
}
