package com.dongfang.rx.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class WeatherAPI {

    @SerializedName("HeWeather data service 3.0")
    @Expose
    public List<Weather> mHeWeatherDataService30s = new ArrayList<>();

    @Override
    public String toString() {
        return "WeatherAPI{" +
                "mHeWeatherDataService30s=" + mHeWeatherDataService30s +
                '}';
    }
}