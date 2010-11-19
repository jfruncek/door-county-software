package patterns.factory;

/**
 * Demonstrates an extensible Pizza factory that is only dependent on the Pizza interface. It has a factory method that
 * each concrete factor must implement.
 * 
 * @author jfruncek
 * 
 */
public abstract class PizzaFactory {

    public Pizza order() {
        Pizza pizza;
        pizza = createPizza();
        return pizza;
    }
    
    public abstract Pizza createPizza();
}
