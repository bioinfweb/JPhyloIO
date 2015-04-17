/*
 * PhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
 * <http://bioinfweb.info/PhyloIO>
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
package info.bioinfweb.phyloio.formats.mega;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.regex.Pattern;

import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.phyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.phyloio.events.CharacterSetEvent;
import info.bioinfweb.phyloio.events.ConcretePhyloIOEvent;
import info.bioinfweb.phyloio.events.EventType;
import info.bioinfweb.phyloio.events.MetaInformationEvent;
import info.bioinfweb.phyloio.events.PhyloIOEvent;
import info.bioinfweb.phyloio.events.SequenceCharactersEvent;



/**
 * Event based reader for MEGA alignment files.
 * 
 * @author Ben St&ouml;ver
 */
public class MEGAEventReader extends AbstractBufferedReaderBasedEventReader {
	public static final String FIRST_LINE = "#MEGA";
	public static final char COMMAND_END = ';';
	public static final char COMMAND_START = '!';
	public static final char SEUQUENCE_START = '#';
	public static final char COMMENT_START = '[';
	public static final char COMMENT_END = ']';
	public static final char DEFAULT_LABEL_CHAR = '_';

	public static final String COMMAND_NAME_TITLE = "TITLE";
	public static final String COMMAND_NAME_DESCRIPTION = "DESCRIPTION"; 
	public static final String COMMAND_NAME_FORMAT = "FORMAT";
	public static final String COMMAND_NAME_LABEL = "LABEL";
	public static final String COMMAND_NAME_GENE = "GENE";
	public static final String COMMAND_NAME_DOMAIN = "DOMAIN";

	public static final Pattern READ_COMMAND_PATTERN = 
			Pattern.compile(".+(\\" + COMMENT_START + "|\\" + COMMAND_END + ")", Pattern.DOTALL);
	public static final Pattern SEQUENCE_NAME_PATTERN = Pattern.compile(".+\\s+");
	
	
	private String currentSequenceName = null;
	private String firstSequenceName = null;
	private long charactersRead = 0;
	private long currentLabelPos = 0;
	private String currentGeneOrDomainName = null;
	private long currentGeneOrDomainStart = -1;
	private int nestedNextCalls = 0;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the MEGA data to be read 
	 */
	public MEGAEventReader(BufferedReader reader) throws IOException {
		super(reader);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the MEGA file to be read 
	 */
	public MEGAEventReader(File file) throws IOException {
		super(file);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the MEGA data to be read 
	 */
	public MEGAEventReader(InputStream stream) throws IOException {
		super(stream);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the MEGA data to be read 
	 */
	public MEGAEventReader(Reader reader) throws IOException {
		super(reader);
	}
	
	
	private void consumeWhiteSpaceAndComments() throws IOException {
		int c = getReader().peek();
		while ((c != -1) && (Character.isWhitespace(c) || ((char)c == COMMENT_START))) {
			if (((char)c == COMMENT_START)) {
				getReader().skip(1);  // Consume comment start.
				readComment(COMMENT_START, COMMENT_END);
			}
			else {
				getReader().skip(1);  // Consume white space.
			}
			c = getReader().peek();
		}
	}
	
	
	private void checkStart() throws IOException {
		if (!FIRST_LINE.equals(getReader().readString(FIRST_LINE.length()).toUpperCase())) { 
			throw new IOException("All MEGA files must start with \"" + FIRST_LINE + "\".");
		}
	}
	
	
	private MetaInformationEvent createMetaEventFromCommand(String content) {
		int afterNameIndex = StringUtils.indexOfWhiteSpace(content);
		if (afterNameIndex < 1) {
			return new MetaInformationEvent("", content);
		}
		else {
			return new MetaInformationEvent(content.substring(0, afterNameIndex), content.substring(afterNameIndex).trim());
		}
	}
	
	
	private void createCharacterSetEventsFromLabel() throws IOException {
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
							upcommingEvents.add(new CharacterSetEvent("" + currentName, start, pos));
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
			upcommingEvents.add(new CharacterSetEvent("" + currentName, start, pos));
		}
		currentLabelPos = pos;
	}
	
	
	private PhyloIOEvent readCommand() throws Exception {
		int c = getReader().peek();
		if ((c != -1) && ((char)c == COMMAND_START)) {
			getReader().read();  // Consume command start
			
			// Read command:
			consumeWhiteSpaceAndComments();
			if (getReader().peekString(COMMAND_NAME_LABEL.length()).toUpperCase().equals(COMMAND_NAME_LABEL)) {  // Process label events directly from the reader because they might contain many characters.
				createCharacterSetEventsFromLabel();  // Add event to the queue.
				return readNextEvent();  // If character set events have been added to the queue, the first one will be returned here.
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
					
					CharacterSetEvent result = null;
					if (currentGeneOrDomainName != null) {
						result = new CharacterSetEvent(currentGeneOrDomainName, currentGeneOrDomainStart, charactersRead);
					}
					currentGeneOrDomainName = content;
					currentGeneOrDomainStart = charactersRead;
					return result;
				}
				else {
					return createMetaEventFromCommand(content);
				}
			}
		}
		else {
			return null;
		}
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
	
	
	private void countCharacters(PhyloIOEvent event) {
		if (event.getEventType().equals(EventType.SEQUENCE_CHARACTERS)) {
			SequenceCharactersEvent charactersEvent = event.asSequenceCharactersEvent();
			if (charactersEvent.getSequenceName().equals(firstSequenceName)) {
				charactersRead += charactersEvent.getCharacterValues().size();
			}
		}
	}
	
	
	@Override
	protected PhyloIOEvent readNextEvent() throws Exception {
		if (isBeforeFirstAccess()) {
			checkStart();
			consumeWhiteSpaceAndComments();
			return new ConcretePhyloIOEvent(EventType.DOCUMENT_START);
		}
		else if (!upcommingEvents.isEmpty()) {
			return upcommingEvents.poll();
		}
		else {
			PhyloIOEvent event;
			
			switch (getLastNonCommentEvent().getEventType()) {
				case DOCUMENT_START:
					return new ConcretePhyloIOEvent(EventType.ALIGNMENT_START);
					
				case ALIGNMENT_START:
				case SEQUENCE_CHARACTERS:
				case CHARACTER_SET:
				case META_INFORMATION:
				case COMMENT:
					// Read commands:
					event = readCommand();
					consumeWhiteSpaceAndComments();
					if (event != null) {
						return event;
					}
					else if (!upcommingEvents.isEmpty()) {
						return upcommingEvents.poll();  // Return comment that was possibly read in the last call of readCommand().
					}
					
					int c = getReader().peek();
					if (c == -1) {
						if (currentGeneOrDomainName != null) {
							event = new CharacterSetEvent(currentGeneOrDomainName, currentGeneOrDomainStart, charactersRead);
							currentGeneOrDomainName = null;  // Avoid multiple firing of this event
							return event;
						}
						else {
							return new ConcretePhyloIOEvent(EventType.ALIGNMENT_END);
						}
					}
					else if (c == SEUQUENCE_START) {
						readSequenceName();
						consumeWhiteSpaceAndComments();
					}
					
					// Parse characters (either after sequence name or continue previous read):
					nestedNextCalls++;
					event = readCharacters(currentSequenceName, COMMENT_START, COMMENT_END);
					nestedNextCalls--;
					if (nestedNextCalls == 0) {  // readCharacters() makes recursive calls of readNextEvent(). -> Make sure not to count the same characters several times.
						consumeWhiteSpaceAndComments();
						countCharacters(event);
					}
					return event;
										
				case ALIGNMENT_END:
					return new ConcretePhyloIOEvent(EventType.DOCUMENT_END);

				case DOCUMENT_END:
					return null;  // Calling method will throw a NoSuchElementException.

				default:
					throw new InternalError("Impossible case");
			}
		}
	}
}
