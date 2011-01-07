/*
 * patterns.observer.CurrentConditions.java
 * Created on Jan 7, 2011 by jfruncek
 * Copyright 2011, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package patterns.observer;

import java.util.Observable;
import java.util.Observer;

public class CurrentConditions implements Observer {

    private boolean notified;
    private int temp;
    private WeatherData data;
    
    public CurrentConditions(WeatherData data) {
        this.data = data;
        data.addObserver(this);
    }

    public void update(Observable o, Object arg) {
        notified = true;
        temp = data.getTemperature();
    }

    public boolean notified() {
        return notified;
    }

    public Object getTemperature() {
        return temp;
    }

}
