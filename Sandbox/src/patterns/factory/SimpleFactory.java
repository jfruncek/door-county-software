package patterns.factory;

/**
 * Demonstrates a simple pizza factory.
 * 
 * @author jfruncek
 *
 */
public class SimpleFactory {

    public static Pizza createPizza(PizzaType type) {
        if (type == PizzaType.SICILIAN)
            return new SicilianPizza();
        else if (type == PizzaType.NEW_YORK)
            return new NewYorkPizza();
        else if (type == PizzaType.CHICAGO) 
            return new ChicagoPizza();
        else return null;
    }

}
