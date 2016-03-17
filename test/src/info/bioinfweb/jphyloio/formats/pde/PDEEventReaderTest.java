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
package info.bioinfweb.jphyloio.formats.pde;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;
import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.File;

import org.junit.Test;

public class PDEEventReaderTest {
	@Test
	public void testOutputPDE() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/sequenceWithUnknownTokens.pde"), new ReadWriteParameterMap());
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
//					System.out.println(event.getType().getContentType() + " " + event.getType().getTopologyType());					
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
	
	
	@Test
	public void readDNASequences() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/shortSequences.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus0", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu0", "sequence1", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu1", "sequence2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "sequence3", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, "matrix1", null, "otus0", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet2", null, reader);
				assertCharacterSetEvent(0, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet2", reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq0", "sequence1", "otu0", reader);
				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq1", "sequence2", "otu1", reader);
				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq2", "sequence3", "otu2", reader);
				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
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
	
	
	@Test
	public void readAminoSequences() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/shortAminoSequences.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus0", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu0", "sequence1", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu1", "sequence2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "sequence3", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, "matrix1", null, "otus0", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet2", null, reader);
				assertCharacterSetEvent(0, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.AMINO_ACID, null, "charSet2", reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq0", "sequence1", "otu0", reader);
				assertCharactersEvent("ATWSATWSATWSATWSATWS", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq1", "sequence2", "otu1", reader);
				assertCharactersEvent("ATWSATWSATWSATWSATWS", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq2", "sequence3", "otu2", reader);
				assertCharactersEvent("ATWSATWSATWSATWSATWS", reader);
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
	
	
	@Test
	public void readCompressedFile() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/compressedPDE.pde.gz"), new ReadWriteParameterMap()); //TODO choose simpler file and write asserts
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);		
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
	public void readSequencesWithCustomMeta() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/sequencesWithCustomMeta.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus0", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu0", "sequence1", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu1", "sequence2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "sequence3", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, "matrix1", null, "otus0", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet2", null, reader);
				assertCharacterSetEvent(0, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet2", reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq0", "sequence1", "otu0", reader);
				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq1", "sequence2", "otu1", reader);
				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq2", "sequence3", "otu2", reader);
				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
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
	
	
	@Test
	public void readSequenceWithMatchTokens() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/sequenceWithMatchTokens.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus0", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu0", "sequence1", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu1", "sequence2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "sequence3", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, "matrix1", null, "otus0", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet2", null, reader);
				assertCharacterSetEvent(0, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet2", reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq0", "sequence1", "otu0", reader);
				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq1", "sequence2", "otu1", reader);
				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq2", "sequence3", "otu2", reader);
				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
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
	
	
	@Test
	public void readSequenceWithUnknownTokens() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/sequenceWithUnknownTokens.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus0", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu0", "sequence1", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu1", "sequence2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "sequence3", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, "matrix1", null, "otus0", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet2", null, reader);
				assertCharacterSetEvent(0, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet2", reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq0", "sequence1", "otu0", reader);
				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq1", "sequence2", "otu1", reader);
				assertCharactersEvent("ACTG???????????", reader);
				assertCharactersEvent("GACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, "seq2", "sequence3", "otu2", reader);
				assertCharactersEvent("ACTGACTGA?", reader);
				assertCharactersEvent("TGACT?????", reader);
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
