package com.xdevstudio.colink.api

import com.xdevstudio.colink.models.WeatherResponse
import com.xdevstudio.colink.models.ForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>

    @GET("weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<WeatherResponse>

    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<ForecastResponse>

    @GET("forecast")
    suspend fun getForecastByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): Response<ForecastResponse>

    companion object {
        fun create(): WeatherApiService {
            val baseUrl = "https://api.openweathermap.org/data/2.5/"

            return retrofit2.Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}