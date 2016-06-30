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
package info.bioinfweb.jphyloio.utils;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.IOException;



public class JPhyloIOUtils {
	/**
   * Reads all events from the reader until one more end element than start elements is found.
   * 
   * @param reader the event reader from <i>JPhyloIO</i>
   * @return {@code true} if any event is found before the next end element
	 * @throws IOException 
   */
  public static boolean reachElementEnd(JPhyloIOEventReader reader) throws IOException {
  	//TODO Are SOLE events correctly handled?
  	
  	boolean result = false;
    JPhyloIOEvent event = reader.next();
		while (!event.getType().getTopologyType().equals(EventTopologyType.END)) {			
		  if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
	 	    reachElementEnd(reader);
		  }
	    event = reader.next();
		}
		return result;
  }
}
