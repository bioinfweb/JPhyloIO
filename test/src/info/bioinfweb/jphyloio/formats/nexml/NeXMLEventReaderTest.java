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


import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.File;

import org.junit.Test;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class NeXMLEventReaderTest {
	@Test
	public void testReadingMetaElements() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/MetaElements.xml"), false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertMetaEvent("id", "S794", true, true, reader);
				
				assertMetaEvent("tb:identifier.analysis", "http://purl.org/phylo/treebase/phylows/study/TB2:A1269", false, true, reader);
				assertMetaEvent("id", "meta1833", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertMetaEvent("tb:identifier.analysisstep", "http://purl.org/phylo/treebase/phylows/study/TB2:As1269", false, true, reader);
				assertMetaEvent("id", "meta1834", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertMetaEvent("tb:identifier.algorithm", "http://purl.org/phylo/treebase/phylows/study/TB2:Al1269", false, true, reader);
				assertMetaEvent("id", "meta1837", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertMetaEvent("dc:description", "neighbor joining", false, true, reader);
				assertMetaEvent("id", "meta1839", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertMetaEvent("dc:publisher", "Mycologia", false, true, reader);
				assertMetaEvent("id", "meta17", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertMetaEvent("tb:identifier.taxonVariant", "29804", false, true, reader);
				assertMetaEvent("id", "meta23", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertMetaEvent("skos:closeMatch", "http://purl.uniprot.org/taxonomy/172312" , false, true, reader);
				assertMetaEvent("id", "meta22", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertMetaEvent("tb:identifier.taxonVariant", "29801" , false, true, reader);
				assertMetaEvent("id", "meta30", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertMetaEvent("skos:closeMatch", "http://purl.uniprot.org/taxonomy/5627" , false, true, reader);
				assertMetaEvent("id", "meta29", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				assertMetaEvent("id", "M83", true, true, reader);
				
				assertEventType(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.SOLE, reader);
				
				assertMetaEvent("tb:type.matrix", "DNA" , false, true, reader);
				assertMetaEvent("id", "meta1615", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertCharactersEvent("Tl126179", "AG?TCTGAAACGG--TGTAG", reader);

				assertCharactersEvent("Tl261", "AGTT--GAAAAGGGT?GTCG", reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
				
				assertMetaEvent("rdfs:isDefinedBy", "http://purl.org/phylo/treebase/phylows/study/TB2:S794" , false, true, reader);
				assertMetaEvent("id", "meta1621", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertMetaEvent("tb:output.analysisstep", "http://purl.org/phylo/treebase/phylows/study/TB2:As1269" , false, true, reader);
				assertMetaEvent("id", "meta1841", true, true, reader);
				assertEventType(EventContentType.META_INFORMATION, EventTopologyType.END, reader);
				
				assertLinkedOTUEvent(null, reader);
				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
				assertLinkedOTUEvent("Grifola frondosa", reader);
				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
				assertLinkedOTUEvent(null, reader);
				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
				
				assertEdgeEvent("Tn274900", "Tn274857", reader);
				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
				assertEdgeEvent("Tn274900", "Tn274833", reader);
				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
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
	
	
	@Test
	public void testReadingUnknownTags() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/UnknownTag.xml"), false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertMetaEvent("id", "test", true, true, reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				assertMetaEvent("id", "alignment", true, true, reader);
				
				assertEventType(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.SOLE, reader);
				
				assertCharactersEvent("taxon1", "AG?TCTGAAACGG--TGTAG", reader);
				
				assertCharactersEvent("taxon2", "AGTT--GAAAAGGGT?GTCG", reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
				
				assertLinkedOTUEvent(null, reader);
				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
				assertLinkedOTUEvent(null, reader);
				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
				assertLinkedOTUEvent(null, reader);
				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
				
				assertEdgeEvent("internal1", "taxon1", 5.0, reader);
				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
				assertEdgeEvent("internal1", "taxon2", reader);
				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
				
				assertLinkedOTUEvent(null, reader);
				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
				assertLinkedOTUEvent(null, reader);
				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
				assertLinkedOTUEvent(null, reader);
				assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
				
				assertEdgeEvent(null, "internal1", reader);
				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
				assertEdgeEvent("internal1", "taxon1", 5.0, reader);
				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
				assertEdgeEvent("internal1", "taxon2", 4.0, reader);
				assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
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
	
	
	@Test
	public void testReadingDNACells() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/DNACells.xml"), false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertMetaEvent("id", "test", true, true, reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				assertMetaEvent("id", "alignment", true, true, reader);
				
				assertEventType(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.SOLE, reader);
				
				assertCharactersEvent("taxon1", "A", reader);
				assertCharactersEvent("taxon1", "G", reader);
				assertCharactersEvent("taxon1", "A", reader);
				
				assertCharactersEvent("taxon2", "A", reader);
				assertCharactersEvent("taxon2", "G", reader);
				assertCharactersEvent("taxon2", "T", reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
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