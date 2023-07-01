package com.venus.smarthome.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.venus.smarthome.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OpenWeatherIcons {
    private final Context context;
    private final String weatherIcon;

    private static final Map<String, Integer> iconMap = new HashMap<String, Integer>() {{
        put("01n", R.drawable.weather_01n);
        put("01d", R.drawable.weather_01d);
        put("02n", R.drawable.weather_02n);
        put("02d", R.drawable.weather_02d);
        put("03n", R.drawable.weather_03n);
        put("03d", R.drawable.weather_03d);
        put("04n", R.drawable.weather_04n);
        put("04d", R.drawable.weather_04d);
        put("09n", R.drawable.weather_09n);
        put("09d", R.drawable.weather_09d);
        put("10n", R.drawable.weather_10n);
        put("10d", R.drawable.weather_10d);
        put("11n", R.drawable.weather_11n);
        put("11d", R.drawable.weather_11d);
        put("13n", R.drawable.weather_13n);
        put("13d", R.drawable.weather_13d);
        put("50n", R.drawable.weather_50n);
        put("50d", R.drawable.weather_50d);
    }};

    public OpenWeatherIcons(Context context, String weatherIcon, ImageView imageView) {
        this.context = context;
        this.weatherIcon = weatherIcon;

        imageView.setImageDrawable(getImage());
    }

    private Drawable getImage() {
        try {
            Integer resourceId = iconMap.get(weatherIcon.toLowerCase(Locale.getDefault()));
            if (resourceId != null) {
                return ContextCompat.getDrawable(context, resourceId);
            } else {
                Log.e("Drawable TAG", weatherIcon);
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
