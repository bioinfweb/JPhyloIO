package info.bioinfweb.jphyloio.demo.eventwebview;



/**
 * Bean modeling a property and its value for output in a JSP. 
 * 
 * @author Ben St&ouml;ver
 */
public class EventProperty {
	private String name;
	private String value;
	
	
	public EventProperty(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}


	public String getName() {
		return name;
	}


	public String getValue() {
		return value;
	}


	@Override
	public String toString() {
		return getName() + ": \"" + getValue() + "\"";
	}
}
