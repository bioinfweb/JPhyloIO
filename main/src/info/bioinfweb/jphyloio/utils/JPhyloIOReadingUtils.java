/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.utils;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.exception.IllegalEventException;
import info.bioinfweb.jphyloio.objecttranslation.ObjectTranslator;

import java.io.IOException;
import java.util.NoSuchElementException;



/**
 * Provides tool methods to be used by application developers when implementing a reader class that processes
 * events from an implementations of {@link JPhyloIOEventReader} and stores relevant content in the application
 * business model. 
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class JPhyloIOReadingUtils {
	private static final EventType LITERAL_META_END = new EventType(EventContentType.LITERAL_META, EventTopologyType.END);
	
	
	/**
   * Reads all events from the reader until one more end element than start elements is found.
   * 
   * @param reader the <i>JPhyloIO</i> event reader providing the event stream
   * @return {@code true} if any other event was encountered before the next end event
   * @throws IOException if an I/O error occurs while reading from {@code reader}
   */
  public static void reachElementEnd(JPhyloIOEventReader reader) throws IOException {
  	JPhyloIOEvent event = reader.next();
		 
		while (!event.getType().getTopologyType().equals(EventTopologyType.END)) {			
		  if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
	 	    reachElementEnd(reader);
		  }
	    event = reader.next();
		}  	
  }
  
  
  /**
   * Reads a subsequence from an <i>JPhyloIO</i> event stream modeling contents of a literal metadata element into a string 
   * builder. The string representation of the modeled object is returned, even if it is not an instance of 
   * {@link CharSequence}. Encountered comment events are skipped and ignored.
   * <p>
   * This tool method can be called after a start event with the content type {@link EventContentType#LITERAL_META} was read 
   * from {@code reader} and will consume all following events including the respective end event with the type 
   * {@link EventContentType#LITERAL_META}. Note that this method may only be used if the sequence type of the literal 
   * metadata start event is {@link LiteralContentSequenceType#SIMPLE}.
   * <p>
   * Large strings maybe split among multiple content events by <i>JPhyloIO</i> readers. (See the documentation of
   * {@link LiteralMetadataContentEvent} for details.) A sequence of such events is concatenated into a single value returned 
   * by this method. Note that sequential reading of large strings that do not need to be in memory as a whole to be processed 
   * by the application is more efficient, if the application handles the content events directly. This method should be used 
   * with care if such cases are likely.
   * 
   * @param reader the <i>JPhyloIO</i> event reader providing the event stream
   * @return an instance of {@link StringBuilder} containing the string content or {@code null} if no content events were
   *         encountered before the literal metadata end event
   * @throws IOException if an I/O error occurs while reading from {@code reader} or if another content event was encountered
   *         although sequence was declared to be terminated by the last event (The latter case would indicate an invalid event
   *         sequence produced by {@code reader}, which is not to expect from a well-tested reader. If you encounter such 
   *         problems with a built-in reader from <i>JPhyloIO</i>, inform the developers.)
   * @throws NoSuchElementException if the event stream ends before a literal metadata end event is encountered (This should
   *         not happen, if {@code reader} produces an event stream according to the grammar defined in the documentation of
   *         {@link JPhyloIOEventReader}.)
   * @see LiteralMetadataContentEvent
   * @see ObjectTranslator
   */
  public static StringBuilder readLiteralMetadataContentAsStringBuilder(JPhyloIOEventReader reader) throws IOException {
  	JPhyloIOEvent event = reader.next();
  	if (!event.getType().equals(LITERAL_META_END)) {
  		StringBuilder result = new StringBuilder();
  		boolean isUnfinished = true;
    	do {
    		if (event.getType().getContentType().equals(EventContentType.LITERAL_META_CONTENT)) {
    			LiteralMetadataContentEvent contentEvent = event.asLiteralMetadataContentEvent();
    			if (isUnfinished) {
      			if (contentEvent.getStringValue() == null) {
      				result.append(contentEvent.getObjectValue().toString());
      			}
      			else {
      				result.append(contentEvent.getStringValue());  // Object values are null, if a string is split among mutliple events.
      			}
    				isUnfinished = contentEvent.isContinuedInNextEvent();
    			}
    			else {
    				throw new IOException("Another literal metadata content event was encountered, although the sequence was declared "
    						+ "to be terminated by the last event.");  //TODO Use other exception type.
    			}
    		}
    		else if (!event.getType().getTopologyType().equals(EventTopologyType.SOLE)) {
    			reachElementEnd(reader);  // Skip over possibly nested events. (The current grammar does not allow such events here, so this implementation treats possible future extensions.)
    		}
    		
    		event = reader.next();  // May throw a NoSuchElementException, if the sequence ends before a literal metadata end event is encountered (which would be invalid).
    	} while (!event.getType().equals(LITERAL_META_END));
    	
    	return result;
  	}
  	else {  // Empty literal metadata content sequence encountered.
  		return null;
  	}
  }
  
  
  /**
   * This convenience method calls {@link #readLiteralMetadataContentAsStringBuilder(JPhyloIOEventReader)} internally and
   * converts its content to a {@link String} if the builder is not {@code null}.
   * 
   * @param reader the <i>JPhyloIO</i> event reader providing the event stream
   * @return an string content or {@code null} if no content events were
   *         encountered before the literal metadata end event
   * @throws IOException if an I/O error occurs while reading from {@code reader} or if another content event was encountered
   *         although sequence was declared to be terminated by the last event (See the documentation of 
   *         {@link #readLiteralMetadataContentAsStringBuilder(JPhyloIOEventReader)} for details.)
   * @throws NoSuchElementException if the event stream ends before a literal metadata end event is encountered (See the 
   *         documentation of {@link #readLiteralMetadataContentAsStringBuilder(JPhyloIOEventReader)} for details.)
   * @see #readLiteralMetadataContentAsStringBuilder(JPhyloIOEventReader)
   */
  public static String readLiteralMetadataContentAsString(JPhyloIOEventReader reader) throws IOException {
  	StringBuilder result = readLiteralMetadataContentAsStringBuilder(reader);
  	if (result == null) {
  		return null;
  	}
  	else {
  		return result.toString();
  	}
  }
}
