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



public class NewickToken {
	private NewickTokenType type = NewickTokenType.SUBTREE_START;
	private int textPos;
	private String text = "";
	private double length = 0;
	private boolean delimited = false;
	private String comment = "";
	//TODO Adjust properties to JPhyloIO
	
	
	public NewickToken(NewickTokenType type, int textPos) {
		this.type = type;
		this.textPos = textPos;
	}

	
	public NewickToken(int textPos, String text, boolean delimited) {
		this.type = NewickTokenType.NAME;
		this.textPos = textPos;
		this.text = text;
		this.delimited = delimited;
	}
	
	
	public NewickToken(int textPos, double length) {
		this.type = NewickTokenType.LENGTH;
		this.textPos = textPos;
		this.length = length;
	}


	public double getLength() {
		return length;
	}


	public String getText() {
		return text;
	}


	public NewickTokenType getType() {
		return type;
	}


	public void setLength(double length) {
		this.length = length;
	}


	public void setText(String text) {
		this.text = text;
	}


	public int getTextPos() {
		return textPos;
	}


	public boolean wasDelimited() {
		return delimited;
	}


	public void setDelimited(boolean delimited) {
		this.delimited = delimited;
	}


	/**
	 * Returns the comment which was located behind this element in the Newick string (if there was 
	 * any).
	 * @return
	 */
	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}
}