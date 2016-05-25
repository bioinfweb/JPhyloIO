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


import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReaderKey;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



/**
 * Event reader for the PDE format used by the alignment editor <a href="http://phyde.de/">PhyDE</a>.
 * 
 * <h3><a name="parameters"></a>Recognized parameters</h3> 
 * <ul>
 *   <li>{@link ReadWriteParameterMap#KEY_ALLOW_DEFAULT_NAMESPACE}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_LOGGER}</li>
 * </ul>
 * 
 * @author Sarah Wiechers
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class PDEEventReader extends AbstractXMLEventReader<PDEReaderStreamDataProvider> implements PDEConstants {
	private static final Pattern META_DEFINITION_PATTERN = Pattern.compile("(\\d+)\\s+\\\"([^\\\"]*)\\\"\\s+(\\w+)\\s*");
	

	private static XMLEventReader createXMLEventReader(InputStream stream) throws XMLStreamException, IOException {
		try {  //TODO Check whether this is still necessary or if the implementation in according factory methods is already sufficient.
			stream = new BufferedInputStream(stream);
			stream.mark(1024);
			stream = new GZIPInputStream(stream);  // If an exception occurs here, stream is not set and still references the BufferedInputStream.
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
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
					}
			});
			
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_DESCRIPTION, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						streamDataProvider.getCurrentEventCollection().add(
								new LiteralMetadataEvent(streamDataProvider.getEventReader().getID(null, EventContentType.META_LITERAL), null, 
								new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE));						
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_DESCRIPTION, null, XMLStreamConstants.CHARACTERS), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						boolean isContinued = streamDataProvider.getEventReader().peek().getType().equals(XMLStreamConstants.CHARACTERS);
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(
								new URIOrStringIdentifier(null, null), event.asCharacters().getData(), isContinued)); //TODO use constant for string data type
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_DESCRIPTION, XMLStreamConstants.END_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));						
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_ALIGNMENT, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						StartElement element = event.asStartElement();
						String tokenSetType = XMLUtils.readStringAttr(element, ATTR_DATATYPE, null);
						int alignmentLength = XMLUtils.readIntAttr(element, ATTR_ALIGNMENT_LENGTH, 0);
						
						CharacterStateSetType type = null;
						
						if (tokenSetType.equals(DNA_TYPE)) {
							type = CharacterStateSetType.DNA;
						}
						else {
							type = CharacterStateSetType.AMINO_ACID;
						}						
						
						streamDataProvider.setCharacterSetType(type);
						streamDataProvider.setAlignmentLength(alignmentLength);
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ALIGNMENT, TAG_HEADER, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						String otuListID = getID(null, EventContentType.OTU_LIST);
						streamDataProvider.setOtuListID(otuListID);
						streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.OTU_LIST, otuListID, null));
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ALIGNMENT, TAG_HEADER, XMLStreamConstants.END_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.OTU_LIST));
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_META_TYPE_DEFINITIONS, null, XMLStreamConstants.CHARACTERS), 
			new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
				@Override
				public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
					String data = event.asCharacters().getData();
					
					if (!data.matches("\\s+")) {
						if (streamDataProvider.hasIncompleteToken()) {
							data = streamDataProvider.getIncompleteToken() + data;
						}
						Matcher matcher = META_DEFINITION_PATTERN.matcher(data);
						int offset = 0;
						while (matcher.find(offset)) {
							long index = Long.parseLong(matcher.group(1));
							streamDataProvider.getMetaColumns().put(index, new PDEMetaColumnDefintion(
									index, matcher.group(2), PDEMetaColumnType.parseColumnType(matcher.group(3))));
							offset = matcher.end();
						}
						
						data = data.substring(offset);
						
						if (data.length() > 0) {
							if (streamDataProvider.getXMLReader().peek().isCharacters()) {
								streamDataProvider.setIncompleteToken(data);
							}
							else {
								throw new JPhyloIOReaderException("Invalid meta column definition in " + TAG_META_TYPE_DEFINITIONS.getLocalPart() 
										+ " tag ending with \"" + data + "\" found.", event.getLocation());  //TODO Shorten data strings which are too long?
							}
						}
						else {
							streamDataProvider.setIncompleteToken(null);
						}
					}
				}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_HEADER, TAG_SEQUENCE_INFORMATION, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						streamDataProvider.setCurrentSequenceIndex(XMLUtils.readIntAttr(event.asStartElement(), ATTR_SEQUENCE_INDEX, -1));
						streamDataProvider.getSequenceInformations().add(new HashMap<Integer, String>());
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_SEQUENCE_INFORMATION, TAG_SEQUENCE_META_INFORMATION, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						StartElement element = event.asStartElement();
						int metaColumnID = XMLUtils.readIntAttr(element, ATTR_ID, 0);
						String value = null;
						XMLEvent nextEvent = streamDataProvider.getEventReader().getXMLReader().peek();
						
						if (nextEvent.getEventType() == XMLStreamConstants.CHARACTERS) {
							String characterData = nextEvent.asCharacters().getData();
							if (!characterData.matches("\\s+")) {
								value = characterData;
							}
						}
						
						int index = streamDataProvider.getCurrentSequenceIndex();
						
						streamDataProvider.getSequenceInformations().get(index).put(metaColumnID, value);
						
						if (metaColumnID == META_ID_SEQUENCE_LABEL) {
							String otuID = getID(DEFAULT_OTU_ID_PREFIX + index, EventContentType.OTU);
							
							streamDataProvider.getSequenceIndexToOTUID().put(index, otuID);
							streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.OTU, otuID, value));
							streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.OTU));
						}
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_CHARSETS, TAG_CHARSET, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						StartElement element = event.asStartElement();						
						String label = XMLUtils.readStringAttr(element, ATTR_CHARSET_LABEL, null);
						String hexColor = element.getAttributeByName(ATTR_COLOR).getValue();
						
						streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.CHARACTER_SET, getID(null, EventContentType.CHARACTER_SET), label));						
						
						if (element.getAttributeByName(ATTR_VISIBILITY) != null) {
							boolean visibility = XMLUtils.readBooleanAttr(element, ATTR_VISIBILITY, false);
							
							streamDataProvider.getCurrentEventCollection().add(
									new LiteralMetadataEvent(streamDataProvider.getEventReader().getID(null, EventContentType.META_LITERAL), null, 
									new URIOrStringIdentifier(null, PREDICATE_CHARSET_VISIBILITY), LiteralContentSequenceType.SIMPLE));		
							streamDataProvider.getCurrentEventCollection().add(
									new LiteralMetadataContentEvent(null, Boolean.toString(visibility), visibility)); //TODO use constant for boolean data type
							streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
						}
						
						if (hexColor != null) {
							Color charSetColor = Color.decode("#" + hexColor);
									
							streamDataProvider.getCurrentEventCollection().add(
									new LiteralMetadataEvent(streamDataProvider.getEventReader().getID(null, EventContentType.META_LITERAL), null, 
									new URIOrStringIdentifier(null, PREDICATE_CHARSET_COLOR), LiteralContentSequenceType.SIMPLE));		
							streamDataProvider.getCurrentEventCollection().add(
									new LiteralMetadataContentEvent(null, charSetColor.toString(), charSetColor)); //TODO use constant for color data type								
							streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
						}
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_CHARSET, null, XMLStreamConstants.CHARACTERS), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						String charSetIntervals = event.asCharacters().getData();
						StringBuffer start = new StringBuffer();
						StringBuffer end = new StringBuffer();
						boolean writeStart = true;
						
						if (streamDataProvider.getIncompleteToken() != null) {
							charSetIntervals = streamDataProvider.getIncompleteToken() + charSetIntervals;
							streamDataProvider.setIncompleteToken(null);
						}
						
						for (int i = 0; i < charSetIntervals.length(); i++) {
							Character currentChar = charSetIntervals.charAt(i);							
							
							if (currentChar == '-') {
								writeStart = false;
							}
							else if (Character.isWhitespace(currentChar)) {
								if (start.length() > 0 || end.length() > 0) {
									if (end.length() == 0) {
										end.append(start.toString());
									}
									streamDataProvider.getCurrentEventCollection().add(new CharacterSetIntervalEvent(Long.parseLong(start.toString()), Long.parseLong(end.toString())));
								}								
								writeStart = true;
								start.delete(0, start.length());
								end.delete(0, end.length());
							}
							else {
								if (writeStart) {
									start.append(currentChar);
								}
								else {
									end.append(currentChar);
								}
							}
						}
						
						if (getXMLReader().peek().getEventType() == XMLStreamConstants.CHARACTERS) {
							String incompleteToken = start.toString();
							if (end.length() > 0) {
								incompleteToken += "-" + end.toString();
							}
							streamDataProvider.setIncompleteToken(incompleteToken);
						}
						else if (start.length() > 0 || end.length() > 0) {
							if (end.length() == 0) {
								end.append(start.toString());
							}							
							streamDataProvider.getCurrentEventCollection().add(new CharacterSetIntervalEvent(Long.parseLong(start.toString()), Long.parseLong(end.toString())));
						}					
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_CHARSETS, TAG_CHARSET, XMLStreamConstants.END_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						streamDataProvider.getCurrentEventCollection().add(new PartEndEvent(EventContentType.CHARACTER_SET, true));
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ALIGNMENT, TAG_MATRIX, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, getID(null, EventContentType.ALIGNMENT), null, 
								streamDataProvider.getOtuListID()));
						
						String charSetID = getID(null, EventContentType.CHARACTER_SET);
						streamDataProvider.getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.CHARACTER_SET, charSetID, null));
						streamDataProvider.getCurrentEventCollection().add(new CharacterSetIntervalEvent(0, streamDataProvider.getAlignmentLength()));  //TODO interval shall be part of token set definition event as soon as these changes are implemented
						streamDataProvider.getCurrentEventCollection().add(new PartEndEvent(EventContentType.CHARACTER_SET, true));
						
						streamDataProvider.getCurrentEventCollection().add(new TokenSetDefinitionEvent(streamDataProvider.getCharacterSetType(), 
								getID(null, EventContentType.TOKEN_SET_DEFINITION), null, charSetID));
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TOKEN_SET_DEFINITION));
						
						streamDataProvider.setCurrentSequenceIndex(0);
						streamDataProvider.setIncompleteToken("");						
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_ALIGNMENT, TAG_MATRIX, XMLStreamConstants.END_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.ALIGNMENT));
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_MATRIX, TAG_BLOCK, XMLStreamConstants.START_ELEMENT), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
						readAttributes(streamDataProvider, event.asStartElement(), ATTR_ALIGNMENT_LENGTH, PREDICATE_CHARACTER_COUNT, ATTR_SEQUENCE_COUNT, PREDICATE_SEQUENCE_COUNT);
						
						int seqIndex = streamDataProvider.getCurrentSequenceIndex();
						streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.SEQUENCE, 
								getID(DEFAULT_SEQUENCE_ID_PREFIX + seqIndex, EventContentType.SEQUENCE), streamDataProvider.getSequenceInformations().get(seqIndex).get(1), DEFAULT_OTU_ID_PREFIX + seqIndex));
						addSequenceMetaData(streamDataProvider);
					}
			});
		
		putElementReader(new XMLElementReaderKey(TAG_BLOCK, null, XMLStreamConstants.CHARACTERS), 
				new AbstractXMLElementReader<PDEReaderStreamDataProvider>() {
					@Override
					public void readEvent(PDEReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {						
						String sequenceData = event.asCharacters().getData().replaceAll("\\s", "");						
						String specialToken = streamDataProvider.getIncompleteToken();
						
						List<String> sequence = new ArrayList<String>();
						Character previousChar = ' ';
						
						for (int i = 0; i < sequenceData.length(); i++) {
							Character nextChar = sequenceData.charAt(i);
							if (!nextChar.equals('\\') && specialToken.isEmpty()) {
								sequence.add(Character.toString(nextChar));
							}
							else {
								while ((i < sequenceData.length()) && (sequenceData.charAt(i) != ':') 
										&& !((sequenceData.charAt(i) == 'F') && previousChar == 'F')) {
									previousChar = sequenceData.charAt(i);
									specialToken += previousChar;									
									i++;									
								}
								
								if (i == sequenceData.length()) {
									streamDataProvider.setIncompleteToken(specialToken);
								}
								else {
									if (specialToken.equals("\\F")) {
										if (sequence.size() != 0) {
											streamDataProvider.getCurrentEventCollection().add(getSequenceTokensEventManager().createEvent(
													streamDataProvider.getSequenceInformations().get(streamDataProvider.getCurrentSequenceIndex()).get(1), sequence));
											sequence = new ArrayList<String>();
										}
										streamDataProvider.getCurrentEventCollection().add(new PartEndEvent(EventContentType.SEQUENCE, true));
										
										int seqIndex = streamDataProvider.getCurrentSequenceIndex() + 1;
										
										if (seqIndex < streamDataProvider.getSequenceInformations().size()) {
											streamDataProvider.setCurrentSequenceIndex(seqIndex);
											
											streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.SEQUENCE, 
													getID(DEFAULT_SEQUENCE_ID_PREFIX + seqIndex, EventContentType.SEQUENCE), streamDataProvider.getSequenceInformations().get(seqIndex).get(1), 
													streamDataProvider.getSequenceIndexToOTUID().get(seqIndex)));
											addSequenceMetaData(streamDataProvider);
										}
									}
									else if (specialToken.contains("\\FE")) {
										specialToken = specialToken.replaceAll("\\\\FE", "");
										
										for (int j = 0; j < Integer.parseInt(StringUtils.invert(specialToken)); j++) {
											sequence.add("?");
										}
										streamDataProvider.getCurrentEventCollection().add(getSequenceTokensEventManager().createEvent(
												streamDataProvider.getSequenceInformations().get(streamDataProvider.getCurrentSequenceIndex()).get(1), sequence));
										sequence = new ArrayList<String>();
									}
									
									specialToken = "";
									streamDataProvider.setIncompleteToken(specialToken);
								}								
							}
						}
							
						if (sequence.size() != 0) {
							streamDataProvider.getCurrentEventCollection().add(getSequenceTokensEventManager().createEvent(streamDataProvider.getSequenceInformations().get(streamDataProvider.getCurrentSequenceIndex()).get(1), sequence));
						}
					}
			});
	}
	
	
	private void addSequenceMetaData(PDEReaderStreamDataProvider streamDataProvider) {
		Map<Integer, String> sequenceInfo = streamDataProvider.getSequenceInformations().get(streamDataProvider.getCurrentSequenceIndex());	
		URIOrStringIdentifier datatype = null;
		URIOrStringIdentifier predicate = null;
		
		for (int key : sequenceInfo.keySet()) {
			PDEMetaColumnDefintion metaColumn = streamDataProvider.getMetaColumns().get(((long)key));
			
			if (key == META_ID_ACCESS_NUMBER) {
				datatype = new URIOrStringIdentifier("int", null); //TODO use constant for int data type
				predicate = new URIOrStringIdentifier(null, PREDICATE_ACCESS_NUMBER);
			}
			else if (key == META_ID_COMMENT) {
				datatype = new URIOrStringIdentifier("string", null); //TODO use constant for string data type
				predicate = new URIOrStringIdentifier(null, PREDICATE_COMMENT);
			}
			else if (key == META_ID_SEQUENCE_LABEL) {
				datatype = null;
				predicate = null;
			}
			else if (key == META_ID_LINKED_FILE) {
				datatype = null;
				predicate = new URIOrStringIdentifier(null, PREDICATE_LINKED_FILE);
			}
			else {
				if (metaColumn != null) {
					predicate = new URIOrStringIdentifier(metaColumn.getName(), PREDICATE_HAS_LITERAL_METADATA);
					
					switch (metaColumn.getType()) {
						case UNKNOWN:
						case STRING:
							datatype = new URIOrStringIdentifier("string", null); //TODO use constant for string data type
							break;
						case NUMBER:
							datatype = new URIOrStringIdentifier("int", null); //TODO use constant for int data type
							break;
						case FILE:
							datatype = null;
							predicate = new URIOrStringIdentifier(metaColumn.getName(), PREDICATE_HAS_RESOURCE_METADATA);
							break;
					}					
				}
			}
				
			if (predicate != null) {
				if (datatype != null) {
					getCurrentEventCollection().add(new LiteralMetadataEvent(getID(null, EventContentType.META_LITERAL), null, 
							predicate, LiteralContentSequenceType.SIMPLE));
					getCurrentEventCollection().add(new LiteralMetadataContentEvent(datatype, sequenceInfo.get(key), false));
					getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				}
				else {
					try {
						File file = new File(sequenceInfo.get(key).replace("\\\\", "\\")); //PhyDE replaces backslashes by double backslashes in file paths
						URI resource;
						
						if (file.isAbsolute()) { //TODO check operating system beforehand, use leading "/" to identify Linux paths, under Linux the Windows specific drive identifiers ("C:") might be problematic
							resource = file.toURI();
						}
						else {
							resource = new URI(file.getPath());
						}

						getCurrentEventCollection().add(new ResourceMetadataEvent(getID(null, EventContentType.META_RESOURCE), null, predicate.getURI(), resource, null)); //TODO use custom meta identifier instead of predicate when resource meta event has been refactored
						getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
					}
					catch (URISyntaxException e) {}
				}
			}
		}
	}


	@Override
	protected PDEReaderStreamDataProvider createStreamDataProvider() {
		return new PDEReaderStreamDataProvider(this);
	}
}
