package example;

import java.util.HashMap;
import java.util.Map;

public class ReportSchedule {

	int id;
	String name;
	Map<String, ScheduleParameter> parameters = new HashMap<String, ScheduleParameter>();
	private String[] deliveryMethods;
	
	public ReportSchedule(String name) {
		this.name = name;
	}
	
	public void addParameter(String name, ScheduleParameter value) {
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

	public Map<String, ScheduleParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, ScheduleParameter> parameters) {
		this.parameters = parameters;
	}

	public void setDeliveryMethods(String[] deliveryMethods) {
		this.deliveryMethods = deliveryMethods;
	}

	public String[] getDeliveryMethods() {
		return deliveryMethods;
	}
}
