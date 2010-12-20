/*
 * patterns.factory.NewYorkIngredientFactory.java
 * Created on Dec 20, 2010 by jfruncek
 * Copyright 2010, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package patterns.factory;

public class NewYorkIngredientFactory implements IngredientFactory {

    public Crust makeCrust() {
        return new ThinCrust();
    }

    public Meat makeMeat() {
        // TODO Auto-generated method stub
        return null;
    }

    public Veggie makeVeggie() {
        // TODO Auto-generated method stub
        return null;
    }

}
