package info.bioinfweb.jphyloio.demo;


import info.bioinfweb.jphyloio.formats.fasta.FASTAEventReader;
import info.bioinfweb.jphyloio.formats.fasta.FastaModelWriter;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventReader;
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
		NeXMLEventReader neXMLReader = new NeXMLEventReader(new File("data/treebase.xml"), false);
//	NexusCommandReaderFactory factory = new NexusCommandReaderFactory();
//	factory.addJPhyloIOReaders();
//		NexusEventReader reader = new NexusEventReader(new File("data/MatrixInterleaved.nex"), false, factory);
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
