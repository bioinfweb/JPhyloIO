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
package info.bioinfweb.jphyloio.objecttranslation.implementations;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.bioinfweb.jphyloio.ReaderStreamDataProvider;
import info.bioinfweb.jphyloio.WriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.newick.NewickConstants;
import info.bioinfweb.jphyloio.formats.newick.NewickUtils;
import info.bioinfweb.jphyloio.objecttranslation.InvalidObjectSourceDataException;



public class ListTranslator extends SimpleValueTranslator<List<Object>> implements NewickConstants {
	@SuppressWarnings("unchecked")
	@Override
	public Class<List<Object>> getObjectClass() {
		return (Class<List<Object>>)(Object)List.class;
	}
		
	
	@Override
	public List<Object> representationToJava(String representation,	ReaderStreamDataProvider<?> streamDataProvider)
			throws InvalidObjectSourceDataException, UnsupportedOperationException {

		representation = representation.trim();
		if (representation.startsWith("" + FIELD_START_SYMBOL) && representation.endsWith("" + FIELD_END_SYMBOL)) {
			List<Object> result = new ArrayList<Object>();
			NewickUtils.ReadElement element = NewickUtils.readNextElement(representation, 1, representation.length() - 1);
			while (element != null) {
				if (element.getNumericValue() != null) {
					result.add(element.getNumericValue());
				}
				else {
					result.add(element.getText());
				}
				element = NewickUtils.readNextElement(representation, element.getEndPos(), representation.length() - 1);
			}
			return result;
		}
		else {
			throw new InvalidObjectSourceDataException("List representations must be encolsed between '" + FIELD_START_SYMBOL + "' and '"
					+ FIELD_END_SYMBOL + "'.");
		}
	}


	@Override
	public String javaToRepresentation(Object object, WriterStreamDataProvider<?> streamDataProvider)	throws UnsupportedOperationException, ClassCastException {
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>)object;  // Possible ClassCastException is intended.
		StringBuilder result = new StringBuilder();
		result.append(FIELD_START_SYMBOL);
		
		Iterator<Object> iterator = list.iterator();
		while (iterator.hasNext()) {
			Object element = iterator.next();
			if (element instanceof Number) {  // Write numeric value
				result.append(element.toString());
			}
			else {  // Write string
				result.append(NAME_DELIMITER);
				result.append(element.toString().replaceAll("\\" + NAME_DELIMITER, "" + NAME_DELIMITER + NAME_DELIMITER));  // Mask name delimiters contained in the string.
				result.append(NAME_DELIMITER);
			}
			
			if (iterator.hasNext()) {
				result.append(ELEMENT_SEPERATOR);
				result.append(' ');
			}
		}
		
		result.append(FIELD_END_SYMBOL);
		return result.toString();
	}
}
