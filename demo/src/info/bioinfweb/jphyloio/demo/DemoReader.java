package info.bioinfweb.jphyloio.demo;


import java.util.List;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventReader;



public class DemoReader {
	public static void readNeXML(DemoModel collection, NeXMLEventReader reader) throws Exception {
		if (reader.hasNextEvent()) {
			int i = 0;
			while (reader.hasNextEvent()) {
				JPhyloIOEvent event = reader.next();
				System.out.println(event.getEventType());
				
				if (event.getEventType() == EventType.TOKEN_SET_DEFINITION) {
					System.out.println("Character State Type: " + event.asTokenSetDefinitionEvent().getSetType());
				}
				else if (event.getEventType() == EventType.SINGLE_TOKEN_DEFINITION) {
					System.out.println("Token Definition: " + event.asSingleTokenDefinitionEvent().getTokenName());
				}
				else if (event.getEventType() == EventType.SEQUENCE_CHARACTERS) {
					i ++;
//					System.out.println("Name: " + event.asSequenceTokensEvent().getSequenceName());
//					System.out.println(i + ": " + event.asSequenceTokensEvent().getCharacterValues());
//					System.out.println("Size: " + event.asSequenceTokensEvent().getCharacterValues().size());
				}
			}
		}	
	}
	
	
	public static void readFasta(DemoModel collection, JPhyloIOEventReader reader) throws Exception {
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
	
	
	public static void addSequenceToElementCollection(DemoModel collection, JPhyloIOEvent event) {
		if (event.getEventType().equals(EventType.SEQUENCE_CHARACTERS)) {
			SequenceTokensEvent tokensEvent = event.asSequenceTokensEvent();
			String sequenceName = tokensEvent.getSequenceName();
			List<String> sequence = collection.getSequence(sequenceName);
			
			for (int i = 0; i < tokensEvent.getCharacterValues().size(); i++) {
				sequence.add(tokensEvent.getCharacterValues().get(i));
			}
			
			collection.getAlignmentData().put(sequenceName, sequence);
		}
	}
}
