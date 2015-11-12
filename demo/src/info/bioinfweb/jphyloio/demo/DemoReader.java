package info.bioinfweb.jphyloio.demo;


import java.io.File;
import java.util.List;

import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.formats.fasta.FASTAEventReader;



public class DemoReader {	
	public static void readFasta() {
		try {
			ModelImplementation collection = new ModelImplementation();
			FASTAEventReader reader = new FASTAEventReader(new File("data/test.fasta"), false);
//			NexusCommandReaderFactory factory = new NexusCommandReaderFactory();
//			factory.addJPhyloIOReaders();
//			NexusEventReader reader = new NexusEventReader(new File("data/MatrixInterleaved.nex"), false, factory);
			try {
				reader.setMaxTokensToRead(10);
				if (reader.hasNextEvent() && reader.next().getEventType().equals(EventType.DOCUMENT_START)) {
					if (reader.hasNextEvent() && reader.next().getEventType().equals(EventType.ALIGNMENT_START)) {
						while (reader.hasNextEvent()) {
							JPhyloIOEvent event = reader.next();
							if (!event.getEventType().equals(EventType.ALIGNMENT_END)) {							
								addSequenceToElementCollection(collection, event);
							}
						}
					}
				}
			}
			finally {
				reader.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void addSequenceToElementCollection(ModelImplementation collection, JPhyloIOEvent event) {
		if (event.getEventType().equals(EventType.SEQUENCE_CHARACTERS)) {
			SequenceTokensEvent tokensEvent = event.asSequenceTokensEvent();
			String sequenceName = tokensEvent.getSequenceName();
			List<String> sequence = collection.getSequence(sequenceName);
			
			for (int i = 0; i < tokensEvent.getCharacterValues().size(); i++) {
				sequence.add(tokensEvent.getCharacterValues().get(i));
			}
			
			collection.getAlignmentData().put(sequenceName, sequence);
			System.out.println(sequenceName);
			System.out.println(sequence);
		}
	}
}
