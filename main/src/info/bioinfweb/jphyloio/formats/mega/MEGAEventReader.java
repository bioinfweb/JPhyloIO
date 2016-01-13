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
import java.util.regex.Pattern;

import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.jphyloio.events.CharacterSetEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



/**
 * Event based reader for MEGA alignment files.
 * 
 * @author Ben St&ouml;ver
 */
public class MEGAEventReader extends AbstractBufferedReaderBasedEventReader implements MEGAConstants {
	private static final Pattern READ_COMMAND_PATTERN = 
			Pattern.compile(".+(\\" + COMMENT_START + "|\\" + COMMAND_END + ")", Pattern.DOTALL);
	private static final Pattern SEQUENCE_NAME_PATTERN = Pattern.compile(".+\\s+");

	public static final String FORMAT_KEY_PREFIX = "info.bioinfweb.jphyloio.formats.mega.format.";
	
	
	private String currentSequenceName = null;
	private String firstSequenceName = null;
	private long charactersRead = 0;
	private long currentLabelPos = 0;
	private String currentGeneOrDomainName = null;
	private long currentGeneOrDomainStart = -1;
	private int nestedNextCalls = 0; //TODO is never used now, possible future use?
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the MEGA data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 */
	public MEGAEventReader(BufferedReader reader, boolean translateMatchToken) throws IOException {
		super(reader, translateMatchToken);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the MEGA file to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 */
	public MEGAEventReader(File file, boolean translateMatchToken) throws IOException {
		super(file, translateMatchToken);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the MEGA data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 */
	public MEGAEventReader(InputStream stream, boolean translateMatchToken) throws IOException {
		super(stream, translateMatchToken);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the MEGA data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 */
	public MEGAEventReader(Reader reader, boolean translateMatchToken) throws IOException {
		super(reader, translateMatchToken);
	}
	
	
	private void checkStart() throws IOException {
		if (!FIRST_LINE.equals(getReader().readString(FIRST_LINE.length()).toUpperCase())) { 
			throw new IOException("All MEGA files must start with \"" + FIRST_LINE + "\".");
		}
	}
	
	
	private void processFormatSubcommand(String key, String value) {
		if (key.substring(FORMAT_KEY_PREFIX.length()).toUpperCase().equals(FORMAT_SUBCOMMAND_IDENTICAL)) {
			setMatchToken(value);
		}
	}
	
	
	private void readFormatCommand() throws IOException {
		try {
			getReader().skip(COMMAND_NAME_FORMAT.length());  // Consume command name.
			consumeWhiteSpaceAndComments(COMMAND_START, COMMENT_END);
			
			boolean eventAdded = false;
			while (getReader().peekChar() != COMMAND_END) {
				MetaInformationEvent event = readKeyValueMetaInformation(FORMAT_KEY_PREFIX, COMMAND_END, COMMENT_START, COMMENT_END, '=', '"');
				processFormatSubcommand(event.getKey(), event.getStringValue());
				getUpcomingEvents().add(event);
				getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
				eventAdded = true;
			}
			
			getReader().skip(1); // Consume ';'.
			if (!eventAdded) {  // No content found.
				getUpcomingEvents().add(new MetaInformationEvent(COMMAND_NAME_FORMAT, null, ""));
				getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
			}
		}
		catch (EOFException e) {
			throw new IOException("Unexpected end of file in " + COMMAND_NAME_FORMAT + " command.");  //TODO Replace by ParseException
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
		while (c != COMMAND_END) {
			if (!Character.isWhitespace(c)) {
				if (c == COMMENT_START) {
					readComment(COMMENT_START, COMMENT_END);
				}
				else {
			    //     start of new character set                                      || end of current set
					if (((currentName == DEFAULT_LABEL_CHAR) && (c != DEFAULT_LABEL_CHAR)) || (c != currentName)) {
						if ((c != currentName) && (currentName != DEFAULT_LABEL_CHAR)) {  // end current set
							getUpcomingEvents().add(new CharacterSetEvent("" + currentName, start, pos));
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
			getUpcomingEvents().add(new CharacterSetEvent("" + currentName, start, pos));
			result = true;
		}
		currentLabelPos = pos;
		return result;
	}
	
	
	/**
	 * @return {@code true} if at least one event was added to the event queue as a result of calling this method
	 * @throws Exception
	 */
	private boolean readCommand() throws Exception {
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
						(upperCaseContent.contains(COMMAND_NAME_DOMAIN + "=") || upperCaseContent.contains(COMMAND_NAME_GENE + "="))) {
					
					if (currentGeneOrDomainName != null) {
						getUpcomingEvents().add(new CharacterSetEvent(currentGeneOrDomainName, currentGeneOrDomainStart, charactersRead));
						result = true;
					}
					currentGeneOrDomainName = content;
					currentGeneOrDomainStart = charactersRead;
				}
				else {
					getUpcomingEvents().add(createMetaInformationEventFromCommand(content));
					getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
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
	}
	
	
	private void countCharacters(JPhyloIOEvent event) {
		if (event.getType().getContentType().equals(EventContentType.SEQUENCE_CHARACTERS)) {
			SequenceTokensEvent charactersEvent = event.asSequenceTokensEvent();
			if (charactersEvent.getSequenceName().equals(firstSequenceName)) {
				charactersRead += charactersEvent.getCharacterValues().size();
			}
		}
	}
	
	
	@Override
	protected void readNextEvent() throws Exception {
		if (isBeforeFirstAccess()) {
			checkStart();
			consumeWhiteSpaceAndComments(COMMENT_START, COMMENT_END);
			getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
		}
		else {
			JPhyloIOEvent event;
			
			switch (getLastNonCommentEvent().getType().getContentType()) {
				case DOCUMENT:
					if (getLastNonCommentEvent().getType().getTopologyType().equals(EventTopologyType.START)) {
						getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.START));
					}
					break;
					
				case ALIGNMENT:
					if (getLastNonCommentEvent().getType().getTopologyType().equals(EventTopologyType.END)) {
						getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
						break;
					}  // fall through in else case
				case SEQUENCE_CHARACTERS:
				case CHARACTER_SET:
				case META_INFORMATION:
				case COMMENT:
					// Read commands:
					readCommand();
					consumeWhiteSpaceAndComments(COMMENT_START, COMMENT_END);
					if (getUpcomingEvents().isEmpty()) {
						int c = getReader().peek();
						if (c == -1) {
							if (currentGeneOrDomainName != null) {
								getUpcomingEvents().add(new CharacterSetEvent(currentGeneOrDomainName, currentGeneOrDomainStart, charactersRead));
								currentGeneOrDomainName = null;  // Avoid multiple firing of this event
							}
							else {
								getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
							}
							break;
						}
						else if (c == SEUQUENCE_START) {
							readSequenceName();
							consumeWhiteSpaceAndComments(COMMENT_START, COMMENT_END);
						}
						
						// Parse characters (either after sequence name or continue previous read):
						//nestedNextCalls++;
						event = readCharacters(currentSequenceName, COMMENT_START, COMMENT_END);
						//nestedNextCalls--;
						//if (nestedNextCalls == 0) {  // readCharacters() makes recursive calls of readNextEvent(). -> Make sure not to count the same characters several times.
						if (event != null) {
							consumeWhiteSpaceAndComments(COMMENT_START, COMMENT_END);
							countCharacters(event);
							getUpcomingEvents().add(event);
						}
						else {
							readNextEvent();  // Make sure to add an event to the list.
						}
						//return event;
					}
					break;
										
				default:
					throw new InternalError("Impossible case");
			}
		}
	}
}
