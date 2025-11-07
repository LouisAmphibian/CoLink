// Data models for weather API
package com.xdevstudio.colink.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

// Current Weather Response
data class WeatherResponse(
    @SerializedName("coord") val coord: Coordinates?,
    @SerializedName("weather") val weather: List<Weather>?,
    @SerializedName("main") val main: Main?,
    @SerializedName("visibility") val visibility: Int?,
    @SerializedName("wind") val wind: Wind?,
    @SerializedName("clouds") val clouds: Clouds?,
    @SerializedName("dt") val dt: Long?,
    @SerializedName("sys") val sys: Sys?,
    @SerializedName("name") val name: String?
)

data class Coordinates(
    @SerializedName("lon") val lon: Double?,
    @SerializedName("lat") val lat: Double?
)

data class Weather(
    @SerializedName("id") val id: Int?,
    @SerializedName("main") val main: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String?
)

data class Main(
    @SerializedName("temp") val temp: Double?,
    @SerializedName("feels_like") val feelsLike: Double?,
    @SerializedName("temp_min") val tempMin: Double?,
    @SerializedName("temp_max") val tempMax: Double?,
    @SerializedName("pressure") val pressure: Int?,
    @SerializedName("humidity") val humidity: Int?
)

data class Wind(
    @SerializedName("speed") val speed: Double?,
    @SerializedName("deg") val deg: Int?
)

data class Clouds(
    @SerializedName("all") val all: Int?
)

data class Sys(
    @SerializedName("country") val country: String?,
    @SerializedName("sunrise") val sunrise: Long?,
    @SerializedName("sunset") val sunset: Long?
)

// 5-Day Forecast Response
data class ForecastResponse(
    @SerializedName("list") val list: List<ForecastItem>?,
    @SerializedName("city") val city: City?
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long?,
    @SerializedName("main") val main: Main?,
    @SerializedName("weather") val weather: List<Weather>?,
    @SerializedName("wind") val wind: Wind?,
    @SerializedName("dt_txt") val dtTxt: String?
) {
    fun getDay(): String {
        val date = Date((dt ?: 0) * 1000)
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
    }

    fun getDate(): String {
        val date = Date((dt ?: 0) * 1000)
        return SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
    }
}

data class City(
    @SerializedName("name") val name: String?,
    @SerializedName("country") val country: String?
)