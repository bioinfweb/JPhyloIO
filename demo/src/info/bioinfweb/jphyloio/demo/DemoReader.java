package info.bioinfweb.jphyloio.demo;


import java.util.List;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.EventContentType;
import info.bioinfweb.jphyloio.events.EventTopologyType;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaEventParameterMap;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventReader;



public class DemoReader {
	public static void readNeXML(DemoModel collection, NeXMLEventReader reader) throws Exception {
		if (reader.hasNextEvent()) {
			int i = 0;
			while (reader.hasNextEvent()) {
				JPhyloIOEvent event = reader.next();
//				System.out.println(event.getEventType());
				
				if (event.getType().getContentType().equals(EventContentType.TOKEN_SET_DEFINITION)) {
//					System.out.println("Character State Type: " + event.asTokenSetDefinitionEvent().getSetType());
				}
				else if (event.getType().getContentType().equals(EventContentType.SEQUENCE_CHARACTERS)) {
//					i ++;
//					System.out.println("Name: " + event.asSequenceTokensEvent().getSequenceName());
//					System.out.println(i + ": " + event.asSequenceTokensEvent().getCharacterValues());
//					System.out.println("Size: " + event.asSequenceTokensEvent().getCharacterValues().size());
				}
				else if (event.getType().getContentType().equals(EventContentType.META_INFORMATION)) {
					i++;
					System.out.println(i + ": ID: " + event.asMetaInformationEvent().getKey());
				}
			}
		}	
	}
	
	
	public static void readFasta(DemoModel collection, JPhyloIOEventReader reader) throws Exception {
		reader.setMaxTokensToRead(10);
		if (reader.hasNextEvent() && reader.next().getType().equals(new EventType(EventContentType.DOCUMENT, EventTopologyType.START))) {
			if (reader.hasNextEvent() && reader.next().getType().equals(new EventType(EventContentType.ALIGNMENT, EventTopologyType.START))) {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
					if (!event.getType().equals(new EventType(EventContentType.ALIGNMENT, EventTopologyType.END))) {							
						addSequenceToElementCollection(collection, event);
					}
				}
			}
		}		
	}
	
	
	public static void addSequenceToElementCollection(DemoModel collection, JPhyloIOEvent event) {
		if (event.getType().getContentType().equals(EventContentType.SEQUENCE_CHARACTERS)) {
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
