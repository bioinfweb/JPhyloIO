package info.bioinfweb.jphyloio.demo;


import java.io.Writer;

import info.bioinfweb.jphyloio.JPhyloIOModelWriter;
import info.bioinfweb.jphyloio.model.ModelWriterParameterMap;
import info.bioinfweb.jphyloio.model.PhyloDocument;



public class DemoWriter {
	public static void writeFasta(PhyloDocument document, Writer outputFileWriter, JPhyloIOModelWriter writer, ModelWriterParameterMap parameters) throws Exception {
		writer.writeDocument(document, outputFileWriter, parameters);		
	}
}
