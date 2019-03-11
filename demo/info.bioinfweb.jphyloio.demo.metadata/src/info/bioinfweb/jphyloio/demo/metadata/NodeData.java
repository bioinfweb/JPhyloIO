/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.demo.metadata;


import java.util.List;
import java.util.ArrayList;



/**
 * The object modeling data attached to a node and the edge leading to it.
 * 
 * @author Ben St&ouml;ver
 */
public class NodeData {
	public static class Taxonomy {
		private String scientificName = "";
		private String ncbiID = "";
		
		
		public String getScientificName() {
			return scientificName;
		}
		
		
		public void setScientificName(String genus) {
			this.scientificName = genus;
		}
		
		
		public String getNCBIID() {
			return ncbiID;
		}
		
		
		public void setNCBIID(String species) {
			this.ncbiID = species;
		}
		
		
		public boolean isEmpty() {
			return ((ncbiID == null) || ncbiID.isEmpty()) && ((scientificName == null) || scientificName.isEmpty());
		}
	}
	
	
	private String label = "";
	private double support = Double.NaN;
	private Taxonomy taxonomy = new Taxonomy();
	private List<Double> sizeMeasurements = new ArrayList<Double>();
	
	
	public NodeData(String label) {
		super();
		this.label = label;
	}


	public String getLabel() {
		return label;
	}
	
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	
	public double getSupport() {
		return support;
	}
	
	
	public void setSupport(double support) {
		this.support = support;
	}
	
	
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}
	
	
	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
	}


	public List<Double> getSizeMeasurements() {
		return sizeMeasurements;
	}


	public void setSizeMeasurements(List<Double> sizeMeasurements) {
		this.sizeMeasurements = sizeMeasurements;
	}


	@Override
	public String toString() {
		return getLabel();
	}
}
