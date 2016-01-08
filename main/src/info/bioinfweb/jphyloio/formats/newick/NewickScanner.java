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
package info.bioinfweb.jphyloio.formats.newick;


import java.io.IOException;
import java.util.NoSuchElementException;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;



public class NewickScanner implements NewickConstants {
	private PeekReader reader; 
	private NewickToken next = null;
	private NewickToken previous = null;
	private boolean beforeFirstAccess = true;
	private boolean dataSourceClosed = false;
	
	
	public NewickScanner(PeekReader reader) {
		super();
		this.reader = reader;
	}


	public static boolean isCharAfterLength(char c) {
		return Character.isWhitespace(c) || (c == ELEMENT_SEPERATOR) || (c == SUBTREE_END) || (c == COMMENT_START) || (c == TERMINAL_SYMBOL);
	}
	
	
	public static boolean isFreeNameChar(char c) {
		return (c != SUBTREE_END) && (c != LENGTH_SEPERATOR) && (c != ELEMENT_SEPERATOR) && (c != COMMENT_START) && (c != TERMINAL_SYMBOL) && !Character.isWhitespace(c); 
	}
	
	
	private NewickToken readDelimitedName() throws IOException {
		reader.read();  // skip NAME_DELIMITER.
		StringBuilder result = new StringBuilder();
		do {
			while ((reader.peek() != -1) && (reader.peekChar() != NAME_DELIMITER)) {
				result.append(reader.readChar());
			}
			if ((reader.peek(1) != -1) && (reader.peekChar(1) == NAME_DELIMITER)) {
				result.append(NAME_DELIMITER);  // Allow 'abc'''
				reader.read();
				reader.read();
			}
		} while ((reader.peek() != -1) && (reader.peekChar() != NAME_DELIMITER));
		
		if (reader.peek() == -1) {
			throw new IOException("Unterminated Newick name");  //TODO Replace by special exception
		}
		else {
			NewickToken token = new NewickToken(NewickTokenType.NAME, -1);
			token.setText(result.toString());
			token.setDelimited(true);
			return token;
		}
	}
	
	
	private NewickToken readFreeName() throws IOException {
		StringBuilder result = new StringBuilder();
		result.append(reader.readChar());
		while ((reader.peek() != -1) && isFreeNameChar(reader.peekChar())) {
			char c = reader.readChar();
			if (c == FREE_NAME_BLANK) {
				result.append(' ');
			}
			else {
				result.append(c);
			}
		}
		
		if (reader.peek() == -1) {
			throw new IOException("Unterminated Newick name");  //TODO Replace by special exception
		}
		else {
			return new NewickToken(-1, result.toString(), false);  //TODO Determine position from reader.
		}
	}
	
	
	/**
	 * Reads a length statement in an Newick string.
	 */
	private  NewickToken readBranchLength() throws IOException {
		StringBuilder text = new StringBuilder();
		while ((reader.peek() != -1) && !isCharAfterLength(reader.peekChar())) {
			text.append(reader.readChar());
		}
		
		double value; 
		try {
			value = Double.parseDouble(text.toString());
		}
		catch (NumberFormatException e) {
			throw new IOException("Illegal length statement");  //TODO Replace by special exception with position information. EOF after ':' could also have caused this exception.
		}
		
		NewickToken token = new NewickToken(NewickTokenType.LENGTH,  -1);  //TODO Determine position from reader.
		token.setLength(value);
		return token; 
	}

	
	private NewickToken readNextToken() throws IOException {
		//TODO ConsumeWhiteSpaceAndComments
		//TODO According comment events or meta events must be fired at the correct position. (Before or associated with a node or an edge).
		reader.readRegExp("\\s*", true);  // Skip whitespace. Can be removed, when ConsumeWhiteSpaceAndComments is called above.
		
		if (reader.peek() == -1) {
			return null; 
		}
		else {
			switch (reader.peekChar()) {
			  case SUBTREE_START:
			  	reader.read();
			  	return new NewickToken(NewickTokenType.SUBTREE_START, -1);  //TODO Determine position by reader (line and column properties to be implemented in reader)
			  case SUBTREE_END:
			  	reader.read();
			  	return new NewickToken(NewickTokenType.SUBTREE_END, -1);  //TODO Determine position by reader (line and column properties to be implemented in reader)
			  case LENGTH_SEPERATOR:
			  	reader.read(); // skip LENGTH_SEPERATOR
					//TODO ConsumeWhiteSpaceAndComments
					reader.readRegExp("\\s*", true);  // Skip whitespace. Can be removed, when ConsumeWhiteSpaceAndComments is called above.
			  	return readBranchLength();
			  case NAME_DELIMITER:
			  	return readDelimitedName();
	//		  case COMMENT_START:  // Can be skipped, if comments are consumed above
	//		  	pos = readComment(text, pos, result);
	//		  	break;
			  case TERMINAL_SYMBOL:
			  	reader.read();
			  	return new NewickToken(NewickTokenType.TERMNINAL_SYMBOL, -1);  //TODO Determine position by reader (line and column properties to be implemented in reader)
			  case ELEMENT_SEPERATOR:
			  	reader.read();  // Skip element separator
			  	return new NewickToken(NewickTokenType.ELEMENT_SEPARATOR, -1);  //TODO Determine position by reader (line and column properties to be implemented in reader)
			  default:
			    if (isFreeNameChar(reader.peekChar())) {
			    	return readFreeName();
			    }
			    else {  // Whitespaces have been consumed before.
			    	throw new IOException("Unexpected token '" + reader.peekChar() + "'.");  //TODO Replace by special exception.
			    }
			}
		}
	}
	
	
	public NewickToken peek() throws IOException {
		// ensureFirstEvent() is called in hasMoreTokens()
		if (!hasMoreTokens()) {  //
			throw new NoSuchElementException("The end of the document was already reached.");
		}
		else {
			return next;
		}
	}


	private void ensureFirstEvent() throws IOException {
		if (beforeFirstAccess) {
			next = readNextToken();
			beforeFirstAccess = false;
		}
	}
	
	
	public boolean hasMoreTokens() throws IOException {
		ensureFirstEvent();
		return next != null;
	}

	
	public NewickToken nextToken() throws IOException {
		// ensureFirstEvent() is called in hasMoreTokens()
		if (!hasMoreTokens()) {  //
			throw new NoSuchElementException("The end of the document was already reached.");
		}
		else {
			previous = next;  // previous needs to be set before readNextEvent() is called, because it could be accessed in there.
			next = readNextToken();
			return previous;
		}
	}
}
