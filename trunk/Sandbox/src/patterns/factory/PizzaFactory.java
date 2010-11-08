package patterns.factory;

/**
 * Demonstrates a more extensible abstract pizza factory.
 * 
 * @author jfruncek
 *
 */
public abstract class PizzaFactory {

    protected Crust crust;
    protected Sauce sauce;
    
    public Pizza createPizza(PizzaType type) {
        
        // TODO: here we create a factory, but based upon type? doesn't that mean our factory changes as much as the simple does?
        createCrust();
        createSauce();
        
        return null; // TODO: return Pizza here
    }
    public abstract void createCrust();
    public abstract void createSauce();
}
