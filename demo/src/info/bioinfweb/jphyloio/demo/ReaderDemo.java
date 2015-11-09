package info.bioinfweb.jphyloio.demo;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.formats.fasta.FASTAEventReader;
import info.bioinfweb.jphyloio.formats.nexus.NexusCommandReaderFactory;
import info.bioinfweb.jphyloio.formats.nexus.NexusEventReader;



public class ReaderDemo {
	private static Map<String, List<String>> alignmentData = new HashMap<String, List<String>>();
	
	
	public static void main(String[] args) {
		readFasta();
		for (String sequenceName : alignmentData.keySet()) {
			System.out.println(sequenceName);
			System.out.println(alignmentData.get(sequenceName));
		}
	}
	
	
	public static void readFasta() {
		try {
//			FASTAEventReader reader = new FASTAEventReader(new File("data/test.fasta"), false);
			NexusCommandReaderFactory factory = new NexusCommandReaderFactory();
			factory.addJPhyloIOReaders();
			NexusEventReader reader = new NexusEventReader(new File("data/MatrixInterleaved.nex"), false, factory);
			try {
				reader.setMaxTokensToRead(10);
				if (reader.hasNextEvent() && reader.next().getEventType().equals(EventType.DOCUMENT_START)) {
					if (reader.hasNextEvent() && reader.next().getEventType().equals(EventType.ALIGNMENT_START)) {
						while (reader.hasNextEvent()) {
							JPhyloIOEvent event = reader.next();
							if (!event.getEventType().equals(EventType.ALIGNMENT_END)) {							
								addSequenceToMap(event);
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
	
	
	public static void addSequenceToMap(JPhyloIOEvent event) {
		if (event.getEventType().equals(EventType.SEQUENCE_CHARACTERS)) {
			SequenceTokensEvent tokensEvent = event.asSequenceTokensEvent();
			List<String> sequence;
			if (alignmentData.get(tokensEvent.getSequenceName()) == null) {
				sequence = new ArrayList<String>();
			}
			else {
				sequence = alignmentData.get(tokensEvent.getSequenceName());
			}
			for (int i = 0; i < tokensEvent.getCharacterValues().size(); i++) {
				sequence.add(tokensEvent.getCharacterValues().get(i));
			}
			alignmentData.put(tokensEvent.getSequenceName(), sequence);			
		}
	}
}
