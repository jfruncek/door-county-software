package example;

import java.util.HashMap;
import java.util.Map;

public class Chapter {

	int id;
	String name;
	Map<String, Object> parameters = new HashMap<String, Object>();
	
	public Chapter(String name) {
		this.name = name;
	}
	
	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}
