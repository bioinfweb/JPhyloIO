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
package info.bioinfweb.jphyloio.demo;


import info.bioinfweb.jphyloio.formats.fasta.FASTAEventReader;
import info.bioinfweb.jphyloio.formats.fasta.FastaModelWriter;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventReader;
import info.bioinfweb.jphyloio.formats.nexml.TranslateTokens;
import info.bioinfweb.jphyloio.model.CharacterData;
import info.bioinfweb.jphyloio.model.ModelWriterParameterMap;
import info.bioinfweb.jphyloio.model.PhyloDocument;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;



public class ReadWriteDemo {	
	public static void main(String[] args) throws Exception {
		DemoModel demoModel = new DemoModel(); 
//		FASTAEventReader reader = new FASTAEventReader(new File("data/test.fasta"), false);
		NeXMLEventReader neXMLReader = new NeXMLEventReader(new File("data/LargeNeXMLFile.xml"), TranslateTokens.SYMBOL_TO_LABEL);
//	NexusCommandReaderFactory factory = new NexusCommandReaderFactory();
//	factory.addJPhyloIOReaders();
//	NexusEventReader reader = new NexusEventReader(new File("data/MatrixInterleaved.nex"), false, factory);
		try {
//			DemoReader.readFasta(demoModel, reader);
			DemoReader.readNeXML(demoModel, neXMLReader);
		}
		finally {			
//			reader.close();
			neXMLReader.close();
		}
		List<CharacterData> characterData = new ArrayList<CharacterData>();
		characterData.add(new ModelCharacterData(demoModel.getAlignmentData()));
		PhyloDocument document = new DemoPhyloDocument(new CollectionToElementCollectionAdapter<>(characterData), null);
		ModelWriterParameterMap parameters = new ModelWriterParameterMap();
		parameters.put(ModelWriterParameterMap.KEY_ALLOW_LONG_TOKENS, false);
		FastaModelWriter writer = new FastaModelWriter();
		Writer outputFileWriter = new BufferedWriter(new FileWriter("data/output_test.fasta"));
		try {
			DemoWriter.writeFasta(document, outputFileWriter, writer, parameters);
		}
		finally {
			outputFileWriter.close();
		}		
	}
}
