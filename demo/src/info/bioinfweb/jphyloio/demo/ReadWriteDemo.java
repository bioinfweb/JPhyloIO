package info.bioinfweb.jphyloio.demo;


import info.bioinfweb.jphyloio.formats.fasta.FASTAEventReader;

import java.io.File;



public class ReadWriteDemo {	
	public static void main(String[] args) throws Exception {
		DemoModel demoModel = new DemoModel(); 
		FASTAEventReader reader = new FASTAEventReader(new File("data/test.fasta"), false);
//	NexusCommandReaderFactory factory = new NexusCommandReaderFactory();
//	factory.addJPhyloIOReaders();
//	NexusEventReader reader = new NexusEventReader(new File("data/MatrixInterleaved.nex"), false, factory);
		try {
			DemoReader.readFasta(demoModel, reader);
		}
		finally {
			reader.close();
		}
	}
}
