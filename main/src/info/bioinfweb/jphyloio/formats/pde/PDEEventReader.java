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


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;



/**
 * Event reader for the PDE format used by the alignment editor <a href="http://phyde.de/">PhyDE</a>.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class PDEEventReader extends AbstractXMLEventReader<PDEStreamDataProvider> implements PDEConstants {
	private static final Pattern META_DEFINITION_PATTERN = Pattern.compile("(\\d+)\\s+\\\"([^\\\"]*)\\\"\\s+(\\w+)\\s*");
	private static final Pattern UNKNOWN_CHARACTERS_PATTERN = Pattern.compile("\\\\FE(\\d+):");
	

	private static XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException, IOException {
		try {
			stream = new BufferedInputStream(stream);
			stream.mark(1024);
			stream = new GZIPInputStream(stream);
		}
		catch (ZipException e) { //read uncompressed files
			stream.reset();
		}
		
		return XMLInputFactory.newInstance().createXMLEventReader(stream);
	}

	public PDEEventReader(File file, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		this(new FileInputStream(file), parameters);
	}

	
	public PDEEventReader(InputStream stream, ReadWriteParameterMap parameters)	throws IOException, XMLStreamException {
		super(createXMLEventReader(stream), parameters);
	}


	public PDEEventReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(reader, parameters);
	}


	public PDEEventReader(XMLEventReader xmlReader,	ReadWriteParameterMap parameters) {
		super(xmlReader, parameters);
	}
	

	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.PDE_FORMAT_ID;
	}


	@Override
	protected void fillMap() {
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), 
				new AbstractXMLElementReader<PDEStreamDataProvider>() {
					@Override
					public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
						streamDataProvider.setFormat(PDE);
					}
			});
			
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), 
				new AbstractXMLElementReader<PDEStreamDataProvider>() {
					@Override
					public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_ALIGNMENT, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEStreamDataProvider>() {
					@Override
					public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						StartElement element = event.asStartElement();
						String tokenSetType = XMLUtils.readStringAttr(element, ATTR_DATATYPE, null);
						int alignmentLength = XMLUtils.readIntAttr(element, ATTR_ALIGNEMNT_LENGTH, 0);
						
						CharacterStateType type = null;
						
						if (tokenSetType.equals(DNA_TYPE)) {
							type = CharacterStateType.DNA;
						}
						else {
							type = CharacterStateType.AMINO_ACID;
						}
						
						streamDataProvider.setCharacterSetType(type);
						streamDataProvider.setAlignmentLength(alignmentLength);
						
						streamDataProvider.setSequences(new ArrayList<Map<Integer, String>>());
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ALIGNMENT, TAG_HEADER, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEStreamDataProvider>() {
					@Override
					public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						String otuListID = getID(null, EventContentType.OTU_LIST);
						streamDataProvider.setOtuListID(otuListID);
						streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.OTU_LIST, otuListID, null));
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ALIGNMENT, TAG_HEADER, XMLStreamConstants.END_ELEMENT), 
				new AbstractXMLElementReader<PDEStreamDataProvider>() {
					@Override
					public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.OTU_LIST));
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_HEADER, TAG_META_TYPE_DEFINITIONS, XMLStreamConstants.CHARACTERS), 
			new AbstractXMLElementReader<PDEStreamDataProvider>() {
				@Override
				public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
					String data = event.asCharacters().getData();
					if (streamDataProvider.hasRemainingCharacters()) {
						data = streamDataProvider.getRemainingCharacters() + data;
					}
					Matcher matcher = META_DEFINITION_PATTERN.matcher(data);
					int offset = 0;
					while (matcher.find(offset)) {
						long index = Long.parseLong(matcher.group(1));
						streamDataProvider.getMetaColumns().put(index, new PDEMetaColumnDefintion(
								index, matcher.group(2), PDEMetaColumnType.parseColumnType(matcher.group(3))));
						offset = matcher.end();
					}
					
					data = data.substring(offset);  //TODO Can an IndexOutOfBoundsException happen?
					
					if (data.length() > 0) {
						if (streamDataProvider.getXMLReader().peek().isCharacters()) {
							streamDataProvider.setRemainingCharacters(data);
						}
						else {
							throw new JPhyloIOReaderException("Invalid meta column definition in " + TAG_META_TYPE_DEFINITIONS.getLocalPart() 
									+ " tag ending with \"" + data + "\" found.", event.getLocation());  //TODO Shorten data strings which are too long?
						}
					}
					else {
						streamDataProvider.setRemainingCharacters(null);
					}
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_HEADER, TAG_SEQUENCE_INFORMATION, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEStreamDataProvider>() {
					@Override
					public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						streamDataProvider.setCurrentSequenceIndex(XMLUtils.readIntAttr(event.asStartElement(), ATTR_SEQUENCE_INDEX, -1));
						streamDataProvider.getSequences().add(new HashMap<Integer, String>());
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_INFORMATION, TAG_SEQUENCE_META_INFORMATION, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEStreamDataProvider>() {
					@Override
					public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						StartElement element = event.asStartElement();
						int id = XMLUtils.readIntAttr(element, ATTR_ID, 0);
						String value = readCharacterData(streamDataProvider, element);
						int index = streamDataProvider.getCurrentSequenceIndex();
						
						streamDataProvider.getSequences().get(index).put(id, value);
						
						if (id == META_ID_SEQUENCE_LABEL) {
							streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.OTU, DEFAULT_OTU_ID_PREFIX + index, value)); //TODO maybe give meta information for OTU here?
							streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.OTU));
						}						
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ALIGNMENT, TAG_MATRIX, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEStreamDataProvider>() {
					@Override
					public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						streamDataProvider.getCurrentEventCollection().add(new LinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, getID(null, EventContentType.ALIGNMENT), null, 
								streamDataProvider.getOtuListID()));
						
						String charSetID = getID(null, EventContentType.CHARACTER_SET);
						streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.CHARACTER_SET, charSetID, null));
						streamDataProvider.getCurrentEventCollection().add(new CharacterSetIntervalEvent(0, streamDataProvider.getAlignmentLength()));
						streamDataProvider.getCurrentEventCollection().add(new PartEndEvent(EventContentType.CHARACTER_SET, true));
						
						streamDataProvider.getCurrentEventCollection().add(new TokenSetDefinitionEvent(streamDataProvider.getCharacterSetType(), 
								getID(null, EventContentType.TOKEN_SET_DEFINITION), null, charSetID));
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TOKEN_SET_DEFINITION));
						
						streamDataProvider.setCurrentSequenceIndex(0);
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ALIGNMENT, TAG_MATRIX, XMLStreamConstants.END_ELEMENT), 
				new AbstractXMLElementReader<PDEStreamDataProvider>() {
					@Override
					public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.ALIGNMENT));
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_BLOCK, null, XMLStreamConstants.CHARACTERS), 
				new AbstractXMLElementReader<PDEStreamDataProvider>() {
					@Override
					public void readEvent(PDEStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
						String sequenceData = event.asCharacters().getData().replaceAll("\\s", "");									
						Matcher unknownMatcher = UNKNOWN_CHARACTERS_PATTERN.matcher(sequenceData);
						List<String> sequence = new ArrayList<String>();
						
						while (unknownMatcher.find()) {
							String unknownSymbols = "";						
							for (int i = 0; i < Integer.parseInt(StringUtils.invert(unknownMatcher.group(1))); i++) {
								unknownSymbols += "?";
							}
							sequenceData = sequenceData.replace(unknownMatcher.group(), unknownSymbols);
						}
						
						for (int i = 0; i < sequenceData.length(); i++) {
							Character nextChar = sequenceData.charAt(i);
							if (!nextChar.equals('\\')) {
								sequence.add(Character.toString(nextChar));
							}
							else {
								if (sequence.size() != 0) {
									System.out.println("Sequence: " + sequence.size());
									streamDataProvider.getCurrentEventCollection().add(getSequenceTokensEventManager().createEvent(
											streamDataProvider.getSequences().get(streamDataProvider.getCurrentSequenceIndex()).get(1), sequence));
									sequence = new ArrayList<String>();
								}
								streamDataProvider.setCurrentSequenceIndex(streamDataProvider.getCurrentSequenceIndex() + 1);
								i += 2;
							}
						}
						
						if (sequence.size() != 0) {
							System.out.println("Sequence: " + sequence.size());
							streamDataProvider.getCurrentEventCollection().add(getSequenceTokensEventManager().createEvent(streamDataProvider.getSequences().get(streamDataProvider.getCurrentSequenceIndex()).get(1), sequence));
						}
					}
			});
	}


	@Override
	protected PDEStreamDataProvider createStreamDataProvider() {
		return new PDEStreamDataProvider(this);
	}
}
