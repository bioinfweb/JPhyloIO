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


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.factory.JPhyloIOReaderWriterFactory;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public abstract class AbstractApplication {
	/** Factory instance to be used, to create format specific <i>JPhyloIO</i> readers and writers. */
	private JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();
	
	
	protected abstract RelatedResource readMetadata(JPhyloIOEventReader reader) throws IOException;
	
	
	private List<RelatedResource> read(File file) throws Exception {
		List<RelatedResource> result = new ArrayList<RelatedResource>();
		
		JPhyloIOEventReader eventReader = factory.guessReader(file, new ReadWriteParameterMap());
		if (eventReader != null) {
			try {
				new ApplicationReader() {
					@Override
					protected RelatedResource readRelatedResource(JPhyloIOEventReader reader) throws IOException {
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

	
	protected abstract void writeMetadata(File file, String formatID, List<RelatedResource> resources);

	
	protected void write(File file, String formatID, List<RelatedResource> resources) {
		
	}
	
	
	protected void run() {
		try {
  		List<RelatedResource> list = read(new File("data/Input.xml"));
  		
  		for (RelatedResource relatedResource : list) {
  			System.out.println(relatedResource);
  		}
  		
  		write(new File("data/Output.xml"), JPhyloIOFormatIDs.NEXML_FORMAT_ID, list);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
	}
}
