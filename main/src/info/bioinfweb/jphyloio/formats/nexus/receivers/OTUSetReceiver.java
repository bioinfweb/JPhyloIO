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
package info.bioinfweb.jphyloio.formats.nexus.receivers;


import java.io.IOException;

import info.bioinfweb.jphyloio.events.SetElementEvent;
import info.bioinfweb.jphyloio.formats.nexus.NexusWriterStreamDataProvider;



public class OTUSetReceiver extends AbstractNexusSetsEventReceiver {
	public OTUSetReceiver(NexusWriterStreamDataProvider streamDataProvider) {
		super(streamDataProvider);
	}

	
	@Override
	protected boolean handleSetElement(SetElementEvent event) throws IOException {
		switch (event.getLinkedObjectType()) {
			case OTU:
				writeElementReference(event);  //TODO Should all set elements be collected to reconstruct possible intervals or should just all single references by written?
				return true;
			case OTU_SET:
				writeElementReference(event);
				return true;
			default:
				return false;
		}
	}
}
