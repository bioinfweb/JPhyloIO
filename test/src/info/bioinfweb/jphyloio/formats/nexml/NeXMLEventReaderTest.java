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
package info.bioinfweb.jphyloio.formats.nexml;


import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;

import java.io.File;

import org.junit.Test;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class NeXMLEventReaderTest {
	@Test
	public void testOutputNeXML() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_TRANSLATION_STRATEGY, TokenTranslationStrategy.SYMBOL_TO_LABEL);
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/MultipleCharactersTags.xml"), parameters);
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
//					System.out.println(event.getType().getContentType() + " " + event.getType().getTopologyType());
					
					if (event.getType().equals(new EventType(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.START))) {
	//						System.out.println("Character State Type: " + event.asTokenSetDefinitionEvent().getSetType());
					}
					else if (event.getType().equals(new EventType(EventContentType.NODE, EventTopologyType.START))) {
		//					System.out.println(event.asLinkedOTUEvent().getID() + ", " + event.asLinkedOTUEvent().getLabel());
					}
					else if (event.getType().equals(new EventType(EventContentType.EDGE, EventTopologyType.START))) {
		//					System.out.println(event.asEdgeEvent().getLength() + ", " + event.asEdgeEvent().getSourceID() + ", " + event.asEdgeEvent().getTargetID());
					}
					else if (event.getType().equals(new EventType(EventContentType.META_INFORMATION, EventTopologyType.START))) {
//							System.out.println("ID: " + event.asMetaInformationEvent().getStringValue());
					}
					else if (event.getType().equals(new EventType(EventContentType.CHARACTER_SET, EventTopologyType.START))) {						
//					System.out.println("ID: " + event.asLabeledIDEvent().getID());
				}
					else if (event.getType().equals(new EventType(EventContentType.CHARACTER_SET_INTERVAL, EventTopologyType.SOLE))) {
//						System.out.println("Start: " + event.asCharacterSetEvent().getStart() + " End: " + event.asCharacterSetEvent().getEnd());
					}
				}
			}
			finally {
				reader.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	
//	@Test
//	public void testReadingMetaElements() {
//		try {
//			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/MetaElements.xml"), false);
//			try {
//				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
//				assertMetaEvent("id", "S794", true, true, reader);
//				
//				assertMetaEvent("tb:identifier.analysis", "http://purl.org/phylo/treebase/phylows/study/TB2:A1269", false, true, reader);
//				assertMetaEvent("id", "meta1833", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertMetaEvent("tb:identifier.analysisstep", "http://purl.org/phylo/treebase/phylows/study/TB2:As1269", false, true, reader);
//				assertMetaEvent("id", "meta1834", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertMetaEvent("tb:identifier.algorithm", "http://purl.org/phylo/treebase/phylows/study/TB2:Al1269", false, true, reader);
//				assertMetaEvent("id", "meta1837", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertMetaEvent("dc:description", "neighbor joining", false, true, reader);
//				assertMetaEvent("id", "meta1839", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertMetaEvent("dc:publisher", "Mycologia", false, true, reader);
//				assertMetaEvent("id", "meta17", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.OTU_LIST, EventTopologyType.START, reader);
//				assertMetaEvent("id", "Tls10691", true, true, reader);
//				
//				assertBasicOTUEvent(EventContentType.OTU, "Grifola sordulenta", "Tl126179", reader);
//				
//				assertMetaEvent("tb:identifier.taxonVariant", "29804", false, true, reader);
//				assertMetaEvent("id", "meta23", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertMetaEvent("skos:closeMatch", "http://purl.uniprot.org/taxonomy/172312" , false, true, reader);
//				assertMetaEvent("id", "meta22", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.OTU, EventTopologyType.END, reader);
//				
//				assertBasicOTUEvent(EventContentType.OTU, "Grifola frondosa WC836", "Tl261", reader);
//				
//				assertMetaEvent("tb:identifier.taxonVariant", "29801" , false, true, reader);
//				assertMetaEvent("id", "meta30", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertMetaEvent("skos:closeMatch", "http://purl.uniprot.org/taxonomy/5627" , false, true, reader);
//				assertMetaEvent("id", "meta29", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.OTU, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.OTU_LIST, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
//				assertMetaEvent("id", "M83", true, true, reader);
//				
//				assertEventType(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.SOLE, reader);
//				
//				assertMetaEvent("tb:type.matrix", "DNA" , false, true, reader);
//				assertMetaEvent("id", "meta1615", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertBasicOTUEvent(EventContentType.SEQUENCE, "Grifola sordulenta", "Tl126179", reader);
//				assertMetaEvent("id", "row1563", true, true, reader);
//				assertCharactersEvent("AG?TCTGAAACGG--TGTAG", reader);
//				assertEventType(EventContentType.SEQUENCE, EventTopologyType.END, reader);
//				
//				assertBasicOTUEvent(EventContentType.SEQUENCE, "Grifola frondosa WC836", "Tl261", reader);
//				assertMetaEvent("id", "row1564", true, true, reader);
//				assertCharactersEvent("AGTT--GAAAAGGGT?GTCG", reader);
//				assertEventType(EventContentType.SEQUENCE, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
//				
//				assertMetaEvent("rdfs:isDefinedBy", "http://purl.org/phylo/treebase/phylows/study/TB2:S794" , false, true, reader);
//				assertMetaEvent("id", "meta1621", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
//				assertMetaEvent("id", "Tr3926", true, true, reader);
//				
//				assertMetaEvent("tb:output.analysisstep", "http://purl.org/phylo/treebase/phylows/study/TB2:As1269" , false, true, reader);
//				assertMetaEvent("id", "meta1841", true, true, reader);
//				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
//				
//
//				assertLinkedOTUEvent(null, reader);
//				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//
//				assertLinkedOTUEvent("Grifola frondosa", reader);
//				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//
//				assertLinkedOTUEvent(null, reader);
//				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//				
//				assertEdgeEvent("Tn274900", "Tn274857", reader);
//				assertMetaEvent("id", "edge1", true, true, reader);
//				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
//				
//				assertEdgeEvent("Tn274900", "Tn274833", reader);
//				assertMetaEvent("id", "edge2", true, true, reader);
//				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
//			}
//			finally {
//				reader.close();
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getLocalizedMessage());
//		}
//	}
	
	
//	@Test
//	public void testReadingUnknownTags() {
//		try {
//			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/UnknownTag.xml"), false);
//			try {
//				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
//				assertMetaEvent("id", "test", true, true, reader);
//				
//				assertEventType(EventContentType.OTU_LIST, EventTopologyType.START, reader);
//				assertMetaEvent("id", "taxa", true, true, reader);
//				
//				assertLinkedOTUEvent(EventContentType.OTU, null, "taxon1", "taxon1", reader);
//				assertEventType(EventContentType.OTU, EventTopologyType.END, reader);
//				
//				assertBasicOTUEvent(EventContentType.OTU, null, "taxon2", reader);
//				assertEventType(EventContentType.OTU, EventTopologyType.END, reader);
//				
//				assertBasicOTUEvent(EventContentType.OTU, null, "taxon3", reader);
//				assertEventType(EventContentType.OTU, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.OTU_LIST, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
//				assertMetaEvent("id", "alignment", true, true, reader);
//				
//				assertEventType(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.SOLE, reader);
//				
//				assertBasicOTUEvent(EventContentType.SEQUENCE, "row1", "row1", reader);
//				assertMetaEvent("id", "row1", true, true, reader);
//				assertCharactersEvent("AG?TCTGAAACGG--TGTAG", reader);
//				assertEventType(EventContentType.SEQUENCE, EventTopologyType.END, reader);
//				
//				assertBasicOTUEvent(EventContentType.SEQUENCE, "row2", "row2", reader);
//				assertMetaEvent("id", "row2", true, true, reader);
//				assertCharactersEvent("AGTT--GAAAAGGGT?GTCG", reader);
//				assertEventType(EventContentType.SEQUENCE, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
//				
//<<<<<<< .mine
//				assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
//				assertMetaEvent("id", "tree1", true, true, reader);
//				
//				assertNodeEvent("taxon1", reader);
//=======
//				assertLinkedOTUEvent(null, reader);
//>>>>>>> .r221
//				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//<<<<<<< .mine
//				
//				assertNodeEvent("taxon2", reader);
//=======
//				assertLinkedOTUEvent(null, reader);
//>>>>>>> .r221
//				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//<<<<<<< .mine
//				
//				assertNodeEvent("internal1", reader);
//=======
//				assertLinkedOTUEvent(null, reader);
//>>>>>>> .r221
//				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//				
//				assertEdgeEvent("internal1", "taxon1", 5.0, reader);
//				assertMetaEvent("id", "edge1", true, true, reader);
//				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
//				
//				assertEdgeEvent("internal1", "taxon2", reader);
//				assertMetaEvent("id", "edge2", true, true, reader);
//				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
//				
//<<<<<<< .mine
//				assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
//				assertMetaEvent("id", "tree2", true, true, reader);
//				
//				assertNodeEvent("taxon1", reader);
//=======
//				assertLinkedOTUEvent(null, reader);
//>>>>>>> .r221
//				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//<<<<<<< .mine
//				
//				assertNodeEvent("taxon2", reader);
//=======
//				assertLinkedOTUEvent(null, reader);
//>>>>>>> .r221
//				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//<<<<<<< .mine
//				
//				assertNodeEvent("internal1", reader);
//=======
//				assertLinkedOTUEvent(null, reader);
//>>>>>>> .r221
//				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//				
//				assertEdgeEvent("edge1", "internal1", reader);
//				assertMetaEvent("id", "edge1", true, true, reader);
//				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
//				
//				assertEdgeEvent("edge2", "taxon1", 5.0, reader);
//				assertMetaEvent("id", "edge2", true, true, reader);
//				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
//				
//				assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
//			}
//			finally {
//				reader.close();
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getLocalizedMessage());
//		}
//	}
	
	
	@Test
	public void testReadingDNACells() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/DNACells.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxa", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon2", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, "alignment", "DNA", "taxa", reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet0", null, reader);
				assertCharacterSetEvent(0, 2, reader);
				assertEndEvent(EventContentType.CHARACTER_SET, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", "charSet0", reader);
				assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("G", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("T", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "row1", "row1", "taxon1", reader);
				assertSingleTokenEvent("A", true, reader);
				assertSingleTokenEvent("G", true, reader);
				assertSingleTokenEvent("A", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "row2", "row2", "taxon2", reader);
				assertSingleTokenEvent("A", true, reader);
				assertSingleTokenEvent("G", true, reader);
				assertSingleTokenEvent("T", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertEndEvent(EventContentType.DOCUMENT, reader);
			}
			finally {
				reader.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
}