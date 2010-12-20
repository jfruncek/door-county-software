/*
 * patterns.factory.ChicagoPizzaFactory.java
 * Created on Nov 19, 2010 by jfruncek
 * Copyright 2010, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package patterns.factory;


public class ChicagoPizzaFactory extends PizzaFactory {

    @Override
    public Pizza createPizza() {
        Pizza pizza = new ChicagoPizza();
        return pizza;
    }

}
