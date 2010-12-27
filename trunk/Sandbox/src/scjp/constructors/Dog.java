package scjp.constructors;

public class Dog extends Animal {
	
	private static final String MUTT = "mutt dog";
	private String name;

	Dog() 
	{
		super(Dog.MUTT); //try removing this line or the argument!
	}
	
	Dog(String name) 
	{
		this();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
