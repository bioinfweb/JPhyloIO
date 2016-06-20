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
package info.bioinfweb.jphyloio.formats.nexml.receivers;


import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterAlignmentInformation;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



public class NeXMLCollectSequenceDataReceiver extends NeXMLHandleSequenceDataReceiver {
	public NeXMLCollectSequenceDataReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			boolean longTokens, NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, longTokens, streamDataProvider);
	}


	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		AbstractNeXMLDataReceiverMixin.checkResourceMeta(getStreamDataProvider(), event);
		if (isNestedUnderSingleToken()) {
			getStreamDataProvider().getCurrentAlignmentInfo().setWriteCellsTags(true);
		}
	}


	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		AbstractNeXMLDataReceiverMixin.checkLiteralMeta(getStreamDataProvider(), event);
		if (isNestedUnderSingleToken()) {
			getStreamDataProvider().getCurrentAlignmentInfo().setWriteCellsTags(true);
		}
	}


	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		if (isNestedUnderSingleToken()) {
			getStreamDataProvider().getCurrentAlignmentInfo().setWriteCellsTags(true);
		}
	}


	@Override
	protected void handleToken(String token, String label) throws JPhyloIOWriterException {
		NeXMLWriterAlignmentInformation alignmentInfo = getStreamDataProvider().getCurrentAlignmentInfo();
		
		if (!alignmentInfo.hasTokenDefinitionSet() && alignmentInfo.getAlignmentType().equals(CharacterStateSetType.DISCRETE)) {
			try {
				Double.parseDouble(token); //TODO might be problematic if standard sequences consisting of integer symbols do not specify a token definition (but this case is rather unlikely)
				alignmentInfo.setAlignmentType(CharacterStateSetType.CONTINUOUS);
			}
			catch (NumberFormatException e) {}
		}
		
		if (alignmentInfo.getAlignmentType().equals(CharacterStateSetType.DISCRETE)) {
			alignmentInfo.getOccuringTokens().add(token);
		}
		else if (alignmentInfo.getAlignmentType().equals(CharacterStateSetType.CONTINUOUS)) {
			try {
				Double.parseDouble(token);  //TODO Should BigDecimal or some other test method be used here? (Otherwise values outside the range of double will not be accepted.)
			}
			catch (NumberFormatException e) {
				throw new JPhyloIOWriterException("All tokens in a continuous data characters tag must be numbers.");
			}
		}
		else {
			if (!alignmentInfo.getDefinedTokens().contains(token)) { // Token set definitions were read already, so any new tokens here were not defined previously
				alignmentInfo.setAlignmentType(CharacterStateSetType.DISCRETE);
				alignmentInfo.getOccuringTokens().add(token);
			}
		}
	}
}
