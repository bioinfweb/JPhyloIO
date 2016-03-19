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
package info.bioinfweb.jphyloio.formats.mega;


import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.text.AbstractTextEventReader;
import info.bioinfweb.jphyloio.formats.text.KeyValueInformation;
import info.bioinfweb.jphyloio.formats.text.TextReaderStreamDataProvider;
import info.bioinfweb.jphyloio.tools.IDToNameManager;



/**
 * Event based reader for MEGA alignment files.
 * 
 * @author Ben St&ouml;ver
 */
public class MEGAEventReader extends AbstractTextEventReader<TextReaderStreamDataProvider<MEGAEventReader>> implements MEGAConstants {
	private static final Pattern READ_COMMAND_PATTERN = 
			Pattern.compile(".+(\\" + COMMENT_START + "|\\" + COMMAND_END + ")", Pattern.DOTALL);
	private static final Pattern SEQUENCE_NAME_PATTERN = Pattern.compile(".+\\s+");
	
	private static final String GENE_DONAIN_COMMAND_PATTERN_SUFFIX = "\\s*\\=\\s*(\\w+).*";
	private static final Pattern GENE_COMMAND_PATTERN = 
			Pattern.compile(".*" + COMMAND_NAME_GENE + GENE_DONAIN_COMMAND_PATTERN_SUFFIX, Pattern.CASE_INSENSITIVE);
	private static final Pattern DOMAIN_COMMAND_PATTERN = 
			Pattern.compile(".*" + COMMAND_NAME_DOMAIN + GENE_DONAIN_COMMAND_PATTERN_SUFFIX, Pattern.CASE_INSENSITIVE);

	public static final String FORMAT_KEY_PREFIX = "info.bioinfweb.jphyloio.formats.mega.format.";
	
	
	private String currentSequenceName = null;
	private String firstSequenceName = null;
	private long charactersRead = 0;
	private long currentLabelPos = 0;
	private String currentGeneOrDomainName = null;
	private long currentGeneOrDomainStart = -1;
	private IDToNameManager sequenceIDToNameManager = new IDToNameManager(DEFAULT_SEQUENCE_ID_PREFIX);
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the MEGA data to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public MEGAEventReader(BufferedReader reader, ReadWriteParameterMap parameters) throws IOException {
		super(reader, parameters, parameters.getMatchToken());
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the MEGA file to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public MEGAEventReader(File file, ReadWriteParameterMap parameters) throws IOException {
		super(file, parameters, parameters.getMatchToken());
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the MEGA data to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public MEGAEventReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException {
		super(stream, parameters, parameters.getMatchToken());
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the MEGA data to be read 
	 * @param parameters the parameter map for this reader instance 
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public MEGAEventReader(Reader reader, ReadWriteParameterMap parameters) throws IOException {
		super(reader, parameters, parameters.getMatchToken());
	}
	
	
	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.MEGA_FORMAT_ID;
	}


	private void checkStart() throws IOException {
		if (!FIRST_LINE.equals(getReader().readString(FIRST_LINE.length()).toUpperCase())) { 
			throw new JPhyloIOReaderException("All MEGA files must start with \"" + FIRST_LINE + "\".", 0, 0, 0);
		}
	}
	
	
	private void processFormatSubcommand(String key, String value) {
		if (key.substring(FORMAT_KEY_PREFIX.length()).toUpperCase().equals(FORMAT_SUBCOMMAND_IDENTICAL)) {
			getSequenceTokensEventManager().setMatchToken(value);  //TODO The generation of an according meta event could be suppressed, since match characters are anyway replaced by JPhyloIO (and the according event would be a SingleTokenDefinitionEvent).
		}
	}
	
	
	private void readFormatCommand() throws IOException {
		try {
			getReader().skip(COMMAND_NAME_FORMAT.length());  // Consume command name.
			consumeWhiteSpaceAndComments(COMMAND_START, COMMENT_END);
			
			boolean eventAdded = false;
			while (getReader().peekChar() != COMMAND_END) {
				KeyValueInformation info = readKeyValueInformation(FORMAT_KEY_PREFIX, COMMAND_END, COMMENT_START, COMMENT_END, '=');
				processFormatSubcommand(info.getKey(), info.getValue());
				getCurrentEventCollection().add(new MetaInformationEvent(info.getKey(), null, info.getValue()));
				getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
				eventAdded = true;
			}
			
			getReader().skip(1); // Consume ';'.
			if (!eventAdded) {  // No content found.
				getCurrentEventCollection().add(new MetaInformationEvent(COMMAND_NAME_FORMAT, null, ""));
				getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
			}
		}
		catch (EOFException e) {
			throw new JPhyloIOReaderException("Unexpected end of file in " + COMMAND_NAME_FORMAT + " command.", getReader(), e);
		}
	}
	
	
	private MetaInformationEvent createMetaInformationEventFromCommand(String content) throws IOException {
		int afterNameIndex = StringUtils.indexOfWhiteSpace(content);
		if (afterNameIndex < 1) {
			return new MetaInformationEvent("", null, content);
		}
		else {
			return new MetaInformationEvent(content.substring(0, afterNameIndex).toLowerCase(), null, content.substring(afterNameIndex).trim());
		}
	}
	
	
	private boolean createCharacterSetEventsFromLabel() throws IOException {
		boolean result = false;
		getReader().skip(COMMAND_NAME_LABEL.length());
		
		long pos = currentLabelPos;
		long start = -1;
		char currentName = DEFAULT_LABEL_CHAR;
		char c = getReader().readChar();
		getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.CHARACTER_SET, LABEL_CHAR_SET_ID, 
				Character.toString(DEFAULT_LABEL_CHAR)));
		while (c != COMMAND_END) {
			if (!Character.isWhitespace(c)) {
				if (c == COMMENT_START) {
					readComment(COMMENT_START, COMMENT_END);
				}
				else {
			    //     start of new character set                                      || end of current set
					if (((currentName == DEFAULT_LABEL_CHAR) && (c != DEFAULT_LABEL_CHAR)) || (c != currentName)) {
						if ((c != currentName) && (currentName != DEFAULT_LABEL_CHAR)) {  // end current set
							getCurrentEventCollection().add(new CharacterSetIntervalEvent(start, pos));
							result = true;
						}
						start = pos;
						currentName = c;  // Either DEFAULT_LABEL_CHAR if a set ended or a new name if a new set started will be set here.
					}
					pos++;
				}
			}
			c = getReader().readChar();
		}
		if (currentName != DEFAULT_LABEL_CHAR) {
			getCurrentEventCollection().add(new CharacterSetIntervalEvent(start, pos));
			result = true;
		}
		currentLabelPos = pos;
		getCurrentEventCollection().add(new PartEndEvent(EventContentType.CHARACTER_SET, false));
		return result;
	}
	
	
	private static String extractGeneOrDomainID(String geneOrDomainCommand) {
		Matcher matcher = GENE_COMMAND_PATTERN.matcher(geneOrDomainCommand);
		if (matcher.matches()) {
			return COMMAND_NAME_GENE + "." + matcher.group(1);
		}
		else {
			matcher = DOMAIN_COMMAND_PATTERN.matcher(geneOrDomainCommand);
			if (matcher.matches()) {
				return COMMAND_NAME_DOMAIN + "." + matcher.group(1);
			}
			else {
				return null;
			}
		}
	}
	
	
	private void createGeneOrDomainCharSetEvents() {
		getCurrentEventCollection().add(new LabeledIDEvent(EventContentType.CHARACTER_SET, 
				extractGeneOrDomainID(currentGeneOrDomainName), currentGeneOrDomainName));  // Throws NullPointerException if no ID can be extracted.
		getCurrentEventCollection().add(new CharacterSetIntervalEvent(currentGeneOrDomainStart, charactersRead));
		getCurrentEventCollection().add(new PartEndEvent(EventContentType.CHARACTER_SET, false));  // For simplification, it is not tested whether gene or domain character sets are extended later on, because they usually consist of only one part. 
	}
	
	
	/**
	 * @return {@code true} if at least one event was added to the event queue as a result of calling this method
	 * @throws Exception
	 */
	private boolean readCommand() throws IOException {
		boolean result = false;
		int c = getReader().peek();
		if ((c != -1) && ((char)c == COMMAND_START)) {
			getReader().read();  // Consume command start
			
			// Read command:
			consumeWhiteSpaceAndComments(COMMENT_START, COMMENT_END);
			if (getReader().peekString(COMMAND_NAME_LABEL.length()).toUpperCase().equals(COMMAND_NAME_LABEL)) {  // Process label events directly from the reader because they might contain many characters.
				result = createCharacterSetEventsFromLabel();
			}
			else if (getReader().peekString(COMMAND_NAME_FORMAT.length()).toUpperCase().equals(COMMAND_NAME_FORMAT)) {
				readFormatCommand();  // Always adds an event.
				result = true;
			}
			else {
				StringBuilder contentBuffer = new StringBuilder();
				CharSequence currentPart;
				do {
					currentPart = getReader().readRegExp(READ_COMMAND_PATTERN, false).getSequence();
					if (currentPart.charAt(currentPart.length() - 1) == COMMENT_START) {
						readComment(COMMENT_START, COMMENT_END);
					}
					contentBuffer.append(currentPart.subSequence(0, currentPart.length() - 1));
				} while (currentPart.charAt(currentPart.length() - 1) != COMMAND_END);
				
				// Process command:
				String content = contentBuffer.toString().trim();
				String upperCaseContent = content.toUpperCase();
				
				if (!upperCaseContent.startsWith(COMMAND_NAME_TITLE) && !upperCaseContent.startsWith(COMMAND_NAME_DESCRIPTION) &&
						(DOMAIN_COMMAND_PATTERN.matcher(content).matches() || GENE_COMMAND_PATTERN.matcher(content).matches())) {
					
					if (currentGeneOrDomainName != null) {
						createGeneOrDomainCharSetEvents();
						result = true;
					}
					currentGeneOrDomainName = content;
					currentGeneOrDomainStart = charactersRead;
				}
				else {
					getCurrentEventCollection().add(createMetaInformationEventFromCommand(content));
					getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
					result = true;
				}
			}
		}
		return result;
	}
	
	
	private void readSequenceName() throws IOException {
		getReader().read();  // Consume "#"
		currentSequenceName = getReader().readRegExp(SEQUENCE_NAME_PATTERN, false).getSequence().toString().trim();
		if (firstSequenceName == null) {
			firstSequenceName = currentSequenceName;
		}
		else if (firstSequenceName.equals(currentSequenceName)) {
			currentLabelPos = Math.max(currentLabelPos, charactersRead);  // Label command can be omitted in interleaved format.
		}
		getCurrentEventCollection().add(new LinkedOTUOrOTUsEvent(EventContentType.SEQUENCE,
				sequenceIDToNameManager.getID(currentSequenceName), currentSequenceName, null));
	}
	
	
	private void countCharacters(JPhyloIOEvent event) {
		if (event.getType().getContentType().equals(EventContentType.SEQUENCE_TOKENS)) {
			SequenceTokensEvent charactersEvent = event.asSequenceTokensEvent();
			if (currentSequenceName.equals(firstSequenceName)) {
				charactersRead += charactersEvent.getCharacterValues().size();
			}
		}
	}
	
	
	@Override
	protected void readNextEvent() throws IOException {
		if (isBeforeFirstAccess()) {
			checkStart();
			consumeWhiteSpaceAndComments(COMMENT_START, COMMENT_END);
			getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
		}
		else {
			JPhyloIOEvent event;
			
			switch (getLastNonCommentEvent().getType().getContentType()) {
				case DOCUMENT:
					if (getLastNonCommentEvent().getType().getTopologyType().equals(EventTopologyType.START)) {
						getCurrentEventCollection().add(new LinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, 
								DEFAULT_MATRIX_ID_PREFIX + getIDManager().createNewID(), null, null));
					}
					break;
					
				case ALIGNMENT:
					if (getLastNonCommentEvent().getType().getTopologyType().equals(EventTopologyType.END)) {
						getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
						break;
					}  // fall through in else case
				case SEQUENCE:
				case SEQUENCE_TOKENS:
				case CHARACTER_SET:
				case META_INFORMATION:
				case COMMENT:
					// Read commands:
					readCommand();
					consumeWhiteSpaceAndComments(COMMENT_START, COMMENT_END);
					if (getCurrentEventCollection().isEmpty()) {
						int c = getReader().peek();
						if (c == -1) {
							if (currentGeneOrDomainName != null) {
								createGeneOrDomainCharSetEvents();
								currentGeneOrDomainName = null;  // Avoid multiple firing of this event
							}
							else {
								getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
							}
							break;
						}
						else if (c == SEUQUENCE_START) {
							readSequenceName();
							consumeWhiteSpaceAndComments(COMMENT_START, COMMENT_END);
						}
						
						// Parse characters (either after sequence name or continue previous read):
						event = readCharacters(currentSequenceName, COMMENT_START, COMMENT_END);
						if (event != null) {
							consumeWhiteSpaceAndComments(COMMENT_START, COMMENT_END);
							countCharacters(event);
						}
						else {
							readNextEvent();  // Make sure to add an event to the list.
						}
					}
					break;
										
				default:
					throw new InternalError("Impossible case");
			}
		}
	}
}
