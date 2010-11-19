package patterns.factory;

public class SicilianPizzaFactory extends PizzaFactory {

    @Override
    public Pizza createPizza() {
        Pizza pizza = new SicilianPizza();
        return pizza;
    }

}
