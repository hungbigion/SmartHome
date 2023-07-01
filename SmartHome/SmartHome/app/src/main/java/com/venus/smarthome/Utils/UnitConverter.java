package com.venus.smarthome.Utils;

import de.interaapps.localweather.utils.Units;

public class UnitConverter {
    public static String getTemperatureUnit(Units unit) {
        return unit == Units.METRIC ? "°C" : "°F";
    }

    public static String formatTemperature(double temperature) {
        return String.valueOf((int) temperature);
    }

    public static String formatWindSpeed(double windSpeed) {
        return String.valueOf(windSpeed);
    }
}

