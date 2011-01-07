/*
 * patterns.observer.TestWeatherDisplays.java
 * Created on Jan 7, 2011 by jfruncek
 * Copyright 2011, TeraMedica Healthcare Technology, Inc. All Rights Reserved.
 */
package patterns.observer;

import junit.framework.TestCase;

/**
 * Tests 3 weather displays: Current conditions, weather stats, and forecast. 
 * 
 * @author jfruncek
 */
public class TestWeatherDisplays extends TestCase {

    public void testCurrentConditionsIsNotified() {
        WeatherData data =  new WeatherData();
        assertEquals("No observers", data.countObservers(), 0);
        CurrentConditions currentConditions = new CurrentConditions(data);
        assertEquals("One observer", data.countObservers(), 1);
        assertFalse ("Observer not notified yet", currentConditions.notified());
        assertEquals("temp is unknown (0)", 0, currentConditions.getTemperature());
        data.measurementsChanged();
        assertTrue ("Observer notified", currentConditions.notified());
        assertEquals("temp is 76", 76, currentConditions.getTemperature());
    }
}
