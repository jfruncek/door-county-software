package scjp.constructors;

public class Animal {

	private String kind;

	//protected Animal() {};
	
	Animal(String kind)
	{
		this.kind = kind;
	}

	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}
}
