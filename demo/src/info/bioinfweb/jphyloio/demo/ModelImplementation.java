package info.bioinfweb.jphyloio.demo;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ModelImplementation {
	private Map<String, List<String>> alignmentData;

	
	public ModelImplementation() {
		super();
		this.alignmentData = new HashMap<String, List<String>>();
	}


	public Map<String, List<String>> getAlignmentData() {
		return alignmentData;
	}
	
	
	public List<String> getSequence(String sequenceName) {
		List<String> sequenceList = alignmentData.get(sequenceName);
		if (sequenceList == null) {
			sequenceList = new ArrayList<String>();
			alignmentData.put(sequenceName, sequenceList);
			return sequenceList;
		}
		else {
			return sequenceList;
		}
	}
}