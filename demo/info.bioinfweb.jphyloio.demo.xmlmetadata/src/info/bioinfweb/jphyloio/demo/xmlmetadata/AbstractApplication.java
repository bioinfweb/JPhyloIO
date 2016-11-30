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
package info.bioinfweb.jphyloio.demo.xmlmetadata;


import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.ReadWriteParameterNames;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyDocumentDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.factory.JPhyloIOReaderWriterFactory;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.xml.JPhyloIOXMLEventReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;



public abstract class AbstractApplication {
	/** Factory instance to be used, to create format specific <i>JPhyloIO</i> readers and writers. */
	private JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();
	
	private String outputFolder;
	
	
	public AbstractApplication(String outputFolder) {
		super();
		this.outputFolder = outputFolder;
	}


	protected abstract RelatedResource readMetadata(JPhyloIOXMLEventReader reader) throws IOException, XMLStreamException;
	
	
	private List<RelatedResource> read(File file) throws Exception {
		System.out.println("Reading file \"" + file.getAbsolutePath() + "\".");
		
		List<RelatedResource> result = new ArrayList<RelatedResource>();
		JPhyloIOXMLEventReader eventReader = (JPhyloIOXMLEventReader)factory.guessReader(file, new ReadWriteParameterMap());  // This code only works for XML formats, otherwise a ClassCastException would be thrown. In real-world applications this should be handled properly.
		
		if (eventReader != null) {
			try {
				new ApplicationReader() {
					@Override
					protected RelatedResource readRelatedResource(JPhyloIOXMLEventReader reader) throws IOException, XMLStreamException {
						return readMetadata(reader);
					}
				}.read(eventReader, result);
			}
			finally {
				eventReader.close();
			}
		}
		else {
			System.out.println("The format of the file \"" + file.getAbsolutePath() + "\" is not supported.");
		}
		return result;
	}	

	
	protected abstract void writeMetadata(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, 
			RelatedResource resource) throws IOException, XMLStreamException;

	
	protected void write(File file, String formatID, final List<RelatedResource> resources) {
		System.out.println("Writing file \"" + file.getAbsolutePath() + "\".");
		
		// Prepare document adapter:
		final AbstractApplication application = this;
		DocumentDataAdapter document = new EmptyDocumentDataAdapter() {  // An document containing only metadata shall be written.
			@Override
			public void writeMetadata(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver) throws IOException {
				try {
					for (int i = 0; i < resources.size(); i++) {
						receiver.add(new LiteralMetadataEvent("meta" + i, null, 
								new URIOrStringIdentifier(null, IOConstants.PREDICATE_RELATED_REFERENCE), LiteralContentSequenceType.XML));
						application.writeMetadata(parameters, receiver, resources.get(i));  // In here the XML content is written (in an application specific way).
						receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
					}
				}
				catch (XMLStreamException e) {
					throw new IOException(e);
				}
			}
		};
		
		// Write data:
		JPhyloIOEventWriter writer = factory.getWriter(formatID);
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterNames.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);  // This way namespaces used in custom XML do not need to be declared manually.
			
			writer.writeDocument(document, file, parameters);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	protected void run() {
		try {
			// Read:
  		List<RelatedResource> list = read(new File("data/ExampleInput.xml"));
  		System.out.println();
  		
  		// Output:
  		System.out.println("Loaded contents:");
  		for (RelatedResource relatedResource : list) {
  			System.out.println(relatedResource);
  		}
  		System.out.println();
  		
  		// Write:
  		write(new File("data/output/" + outputFolder + "/NeXML.xml"), JPhyloIOFormatIDs.NEXML_FORMAT_ID, list);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
	}
}
