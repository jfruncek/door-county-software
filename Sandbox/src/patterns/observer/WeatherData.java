/*
 * patterns.observer.WeatherData.java
 * Created on Jan 7, 2011 by jfruncek
 * Copyright 2011, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package patterns.observer;

import java.util.Observable;

/**
 * From Design Patterns Chapter 2 - this is the class that produces the weather measurements.
 * 
 * This uses the "pull" style of getting data to the observers. The fact that Observable must be sub-classes
 * is a design limitation -  it probably makes sense to roll your own if you need different behavior.
 * 
 * @author jfruncek
 */
public class WeatherData extends Observable {

    int temp = 75;
    
    public int getTemperature() {
        return temp;
    }
    
    public int getHumidity() {
        return 80;
    }
    
    public int getPressure() {
        return 200;
    }
    
    public void measurementsChanged() {
        temp++;
        setChanged();
        notifyObservers();
    }

}
