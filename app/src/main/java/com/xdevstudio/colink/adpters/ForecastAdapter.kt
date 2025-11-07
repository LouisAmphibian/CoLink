//  RecyclerView adapter for 5-day forecast
package com.xdevstudio.colink.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.xdevstudio.colink.R
import com.xdevstudio.colink.models.ForecastItem
import kotlin.math.roundToInt

class ForecastAdapter(
    private val forecastList: MutableList<ForecastItem>
) : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    inner class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val forecastWeatherIcon: ImageView = itemView.findViewById(R.id.forecastWeatherIcon)
        val forecastDescriptionText: TextView = itemView.findViewById(R.id.forecastDescriptionText)
        val maxTempText: TextView = itemView.findViewById(R.id.maxTempText)
        val minTempText: TextView = itemView.findViewById(R.id.minTempText)

        fun bind(forecast: ForecastItem) {
            dayText.text = forecast.getDay()
            dateText.text = forecast.getDate()

            val weatherMain = forecast.weather?.firstOrNull()?.main ?: "Clear"
            forecastDescriptionText.text = forecast.weather?.firstOrNull()?.description?.capitalize() ?: "Unknown"

            // Set weather icon
            forecastWeatherIcon.setImageResource(getWeatherIcon(weatherMain))

            // Set temperatures
            val maxTemp = forecast.main?.tempMax?.roundToInt() ?: 0
            val minTemp = forecast.main?.tempMin?.roundToInt() ?: 0
            maxTempText.text = "${maxTemp}°"
            minTempText.text = "${minTemp}°"
        }

        private fun getWeatherIcon(weatherMain: String): Int {
            return when (weatherMain.lowercase()) {
                "clear" -> R.drawable.ic_weather_sunny
                "clouds" -> R.drawable.ic_weather_cloudy
                "rain", "drizzle" -> R.drawable.ic_weather_rainy
                "thunderstorm" -> R.drawable.ic_weather_storm
                "snow" -> R.drawable.ic_weather_snowy
                "mist", "fog", "haze" -> R.drawable.ic_weather_cloudy
                else -> R.drawable.ic_weather_sunny
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        holder.bind(forecastList[position])
    }

    override fun getItemCount(): Int = forecastList.size

    fun updateForecast(newForecast: List<ForecastItem>) {
        forecastList.clear()
        forecastList.addAll(newForecast)
        notifyDataSetChanged()
    }
}