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
package info.bioinfweb.jphyloio.test.tests.boundedstream;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;



public class BufferedReaderResetTest {
	public static void main(String[] args) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File("data/Newick/OneNodeName.nwk")));
		try {
			reader.mark(3);
			while (reader.read() != -1);
			reader.reset();
			System.out.println((char)reader.read());
		}
		finally {
			reader.close();
		}
		
		// => Calling reset after eof was reached is possible.
	}
}
