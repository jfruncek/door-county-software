package patterns.factory;

import junit.framework.TestCase;

/**
 * Tests different factory patterns: simple, method, abstract (see Head First Design Patterns book)
 * 
 * @author jfruncek
 */
public class FactoryTest extends TestCase {

    public void testSimpleFactory() {
        assertTrue("Creates a sicilian style pizza",
                   SimpleFactory.createPizza(Pizza.type.SICILIAN) instanceof SicilianPizza);
        assertTrue("Creates a New York style pizza",
                   SimpleFactory.createPizza(Pizza.type.NEW_YORK) instanceof NewYorkPizza);
        assertTrue("Creates a Chicago style pizza",
                   SimpleFactory.createPizza(Pizza.type.CHICAGO) instanceof ChicagoPizza);
    }

    public void testFactoryMethod() {
        Pizza pizza;
        assertTrue("Creates a sicilian style pizza",
                   (pizza = new SicilianPizzaFactory().order()) instanceof SicilianPizza);
        assertEquals("Sicilian pizza has thin crust", pizza.getCrust().getThickness(), "thin");
        assertTrue("Creates a New York style pizza",
                   (pizza = new NewYorkPizzaFactory().order()) instanceof NewYorkPizza);
        assertEquals("New York pizza has regular crust", pizza.getCrust().getThickness(), "regular");
        assertTrue("Creates a Chicago style pizza",
                   (pizza = new ChicagoPizzaFactory().order()) instanceof ChicagoPizza);
        assertEquals("Chicago pizza has thick crust", pizza.getCrust().getThickness(), "thick");
    }
    
    public void testIngredientFactory() {
        assertTrue("Sicilian style pizza has a thin crust", new SicilianIngredientFactory().makeCrust() instanceof ThinCrust);
        assertTrue("New York style pizza has a thin crust", new NewYorkIngredientFactory().makeCrust() instanceof ThinCrust);
        assertTrue("Chicago style pizza has a thick crust", new ChicagoIngredientFactory().makeCrust() instanceof ThickCrust);
    }
}
