package patterns.factory;

import java.util.HashSet;
import java.util.Set;

public abstract class Pizza {

    public enum type {
        SICILIAN, NEW_YORK, CHICAGO
    }
    
    public Pizza() {
        
    }
    
    Crust crust;
    Sauce sauce;
    Set<Topping> toppings = new HashSet<Topping>();;
    
    public void addTopping(Topping item) {
        toppings.add(item);
    }

    public Crust getCrust() {
        return crust;
    }

    public Sauce getSauce() {
        return sauce;
    }
}
