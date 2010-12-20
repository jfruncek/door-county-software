/*
 * patterns.factory.IngredientFactory.java
 * Created on Dec 20, 2010 by jfruncek
 * Copyright 2010, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package patterns.factory;

/**
 * Defines a full Factory pattern for creating a family of products (each product uses Factory Method pattern).
 * 
 * @author jfruncek
 */
public interface IngredientFactory {

    public Crust makeCrust();
    public Meat makeMeat();
    public Veggie makeVeggie();
}
