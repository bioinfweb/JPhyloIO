package info.bioinfweb.jphyloio.demo.gccontent;


import java.io.File;
import java.io.IOException;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.factory.JPhyloIOReaderWriterFactory;



public class GCContent {
	public static final String DEFAULT_INPUT_FILE = "";  //TODO Define file
	
	
	private static long countGC(String token) {
		token = token.toUpperCase();
		if ("G".equals(token) || "C".equals(token)) {
			return 1;
		}
		else {
			return 0;
		}
	}


	private static void processFile(JPhyloIOEventReader eventReader) throws IOException {
		long gcCount = 0;
		long nucleotideCount = 0;
		
		while (eventReader.hasNextEvent()) {
			JPhyloIOEvent event = eventReader.next();
			if (event.getType().equals(new EventType(EventContentType.SEQUENCE_TOKENS, EventTopologyType.START))) {
				for (String token : event.asSequenceTokensEvent().getTokens()) {
					nucleotideCount++;
					gcCount += countGC(token);
				}
			}
			else if (event.getType().equals(new EventType(EventContentType.SINGLE_SEQUENCE_TOKEN, EventTopologyType.START))) {
				nucleotideCount++;
				gcCount += countGC(event.asSingleSequenceTokenEvent().getToken().toUpperCase());
			}
			else if (event.getType().equals(new EventType(EventContentType.ALIGNMENT, EventTopologyType.END))) {
				System.out.println("GC content: " + ((double)nucleotideCount / (double)gcCount));
				nucleotideCount = 0;
				gcCount = 0;
			}
		}
	}

	
	public static void main(String[] args) {
		// Determine file to be loaded:
		String inputFile = DEFAULT_INPUT_FILE;
		if (args.length > 0) {
			inputFile = args[0];
		}
		
		// Process file:
		try {
			JPhyloIOEventReader eventReader = new JPhyloIOReaderWriterFactory().guessReader(new File(inputFile), new ReadWriteParameterMap());
			if (eventReader != null) {
				try {
					System.out.println("Processing file \"" + inputFile + "\"...");
					processFile(eventReader);
				}
				finally {
					eventReader.close();
				}
			}
			else {
				System.out.println("The format of the file \"" + inputFile + "\" is not supported.");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	
}
