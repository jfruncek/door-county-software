package patterns.factory;

public class ChicagoPizza extends Pizza {

    public ChicagoPizza() {
        crust = new Crust("thick");        
    }
}
