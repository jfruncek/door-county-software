package patterns.factory;

import junit.framework.TestCase;

/**
 * Tests different factory patterns: simple, method, abstract (see Head First Design Patterns book)
 * 
 * @author jfruncek
 */
public class FactoryTest extends TestCase {

    public void testSimpleFactory() {
        assertTrue ("Creates a sicilian style pizza", SimpleFactory.createPizza(PizzaType.SICILIAN) instanceof SicilianPizza);
        assertTrue ("Creates a New York style pizza", SimpleFactory.createPizza(PizzaType.NEW_YORK) instanceof NewYorkPizza);
        assertTrue ("Creates a Chicago style pizza", SimpleFactory.createPizza(PizzaType.CHICAGO) instanceof ChicagoPizza);
    }
    
    public void testAbstractFactory() {
//TODO        assertTrue ("Creates a sicilian style pizza", PizzaFactory.create(PizzaType.SICILIAN) instanceof SicilianPizza);
//        assertTrue ("Creates a New York style pizza", PizzaFactory.create(PizzaType.NEW_YORK) instanceof NewYorkPizza);
//        assertTrue ("Creates a Chicago style pizza", PizzaFactory.create(PizzaType.CHICAGO) instanceof ChicagoPizza);
    }
}
