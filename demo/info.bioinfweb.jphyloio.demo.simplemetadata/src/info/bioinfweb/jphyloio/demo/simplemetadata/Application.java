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
package info.bioinfweb.jphyloio.demo.simplemetadata;


import java.io.File;
import java.io.IOException;
import java.util.Collections;

import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreTreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.factory.JPhyloIOReaderWriterFactory;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;



public class Application {
	/** Factory instance to be used, to create format specific <i>JPhyloIO</i> readers and writers. */
	private JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();
	
	/** Provides the data to be written by <i>JPhyloIO</i>. */
	private DocumentDataAdapter document;

	
	public Application() {
		super();

		// Prepare data adapters:
		ListBasedDocumentDataAdapter document = new ListBasedDocumentDataAdapter();
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(
				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "trees1", "someTreeGroup", null), Collections.emptyList());
		trees.getTreesAndNetworks().add(new TreeDataAdapter());
		document.getTreeNetworkGroups().add(trees);
		this.document = document;
	}


	private void writeTree(File file, String formatID) {
		JPhyloIOEventWriter writer = factory.getWriter(formatID);
		try {
			writer.writeDocument(document, file, new ReadWriteParameterMap());
			System.out.println("\"" + file.getAbsolutePath() + "\" written.");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		Application application = new Application();
		application.writeTree(new File("data/NeXML.xml"), JPhyloIOFormatIDs.NEXML_FORMAT_ID);
		application.writeTree(new File("data/PhyloXML.xml"), JPhyloIOFormatIDs.PHYLOXML_FORMAT_ID);
		application.writeTree(new File("data/Nexus.nex"), JPhyloIOFormatIDs.NEXUS_FORMAT_ID);
	}
}
