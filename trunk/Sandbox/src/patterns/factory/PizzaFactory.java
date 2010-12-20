package patterns.factory;

/**
 * Demonstrates an extensible Pizza factory that is only dependent on the interfaces. It has a factory methods that
 * each concrete factory must implement.
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
