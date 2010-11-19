package patterns.factory;

/**
 * Demonstrates a simple Pizza factory. Dependent on concrete Pizza subclasses
 * 
 * @author jfruncek
 *
 */
public class SimpleFactory {

    public static Pizza createPizza(Pizza.type type) {
        if (type == Pizza.type.SICILIAN)
            return new SicilianPizza();
        else if (type == Pizza.type.NEW_YORK)
            return new NewYorkPizza();
        else if (type == Pizza.type.CHICAGO) 
            return new ChicagoPizza();
        else return null;
    }

}
