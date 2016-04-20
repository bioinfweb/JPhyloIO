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


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreDocumentDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLConstants;
import info.bioinfweb.jphyloio.test.dataadapters.TestTreeNetworkGroupDataAdapter;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Test;



public class PhyloXMLEventWriterTest {
	private StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
	private long idIndex = 0;
	
	
	public long getIdIndex() {
		long index = idIndex;
		idIndex++;
		return index;
	}

	
	protected void createTestDocument() {
		for (JPhyloIOEvent event : createMetaData("document")) {
			document.getAnnotations().add(event);
		}
//		String taxaID = ReadWriteConstants.DEFAULT_OTU_LIST_ID_PREFIX + getIdIndex();
//		document.getOTUListsMap().put(taxaID, createOTUList(taxaID));
//		document.getMatrices().add(createMatrix(taxaID));
		document.getTreesNetworks().add(createTrees(""));
	}

	
	protected List<JPhyloIOEvent> createMetaData(String about) {
		List<JPhyloIOEvent> metaData = new ArrayList<JPhyloIOEvent>();
		URI example = null;
		
		try {
			example = new URI("somePath/#fragment");
		} 
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		metaData.add(new ResourceMetadataEvent("meta" + getIdIndex(), "ResourceMeta", new QName("http://meta.net/", "relations"), 
				example, null));
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		metaData.add(new LiteralMetadataEvent("meta" + getIdIndex(), "LiteralMeta", new QName("http://meta.net/", "predicate"), "literal value", LiteralContentSequenceType.SIMPLE));
		metaData.add(new LiteralMetadataContentEvent(NeXMLConstants.TYPE_STRING, "My literal value", true));
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));		
		
		return metaData;
	}
	
	private TestTreeNetworkGroupDataAdapter createTrees(String prefix) {
		String treeID = ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex();
		TestTreeNetworkGroupDataAdapter trees = new TestTreeNetworkGroupDataAdapter(treeID, null, "nodeEdgeID");
		trees.setLinkedOTUsID(prefix);
		return trees;
	}
	
	
	@Test
	public void test_writeDocument() throws Exception {
		createTestDocument();
		PhyloXMLEventWriter writer = new PhyloXMLEventWriter();		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		writer.writeDocument(document, new File("data/testOutput/PhyloXMLTest.xml"), parameters);
	}
}
