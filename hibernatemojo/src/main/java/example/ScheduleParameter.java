package example;

import java.util.HashSet;
import java.util.Set;

public class ScheduleParameter {

	int id;
	String name;
	Set<String> values = new HashSet<String>();
	
	public void setValues(Set<String> values) {
		this.values = values;
	}

	public ScheduleParameter(String[] values) {
		for (String value : values) {
			this.values.add(value);
		}
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

	public Set<String> getValues() {
		return values;
	}
}
