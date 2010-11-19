package patterns.factory;

public class NewYorkPizza extends Pizza {
    
    public NewYorkPizza() {
        crust = new Crust("regular");        
    }
}
