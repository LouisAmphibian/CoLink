package com.xdevstudio.colink.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.xdevstudio.colink.R
import com.xdevstudio.colink.WeatherActivity
import com.xdevstudio.colink.api.WeatherApiService
import kotlinx.coroutines.*
import java.util.*
import kotlin.math.roundToInt

class WeatherNotificationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var weatherApiService: WeatherApiService
    private val API_KEY = "fce1775dc658ea5b5a7cefb94b4fbcfe"

    private val NOTIFICATION_ID = 1002
    private val CHANNEL_ID = "weather_updates_channel"

    override fun onCreate() {
        super.onCreate()
        weatherApiService = WeatherApiService.create()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startPeriodicWeatherChecks()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Updates"
            val descriptionText = "Periodic weather condition updates"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(): Notification {
        val intent = Intent(this, WeatherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Weather Monitoring")
            .setContentText("Monitoring weather conditions for alerts")
            .setSmallIcon(R.drawable.ic_weather_notification)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startPeriodicWeatherChecks() {
        serviceScope.launch {
            while (isActive) {
                checkWeatherAndNotify()
                delay(30 * 60 * 1000) // Check every 30 minutes
            }
        }
    }

    private suspend fun checkWeatherAndNotify() {
        try {
            // Get last known location from shared preferences or use default
            val prefs = getSharedPreferences("weather_prefs", Context.MODE_PRIVATE)
            val city = prefs.getString("last_city", "Johannesburg") ?: "Johannesburg"

            val response = weatherApiService.getCurrentWeatherByCity(city, API_KEY)

            if (response.isSuccessful) {
                response.body()?.let { weather ->
                    val weatherMain = weather.weather?.firstOrNull()?.main ?: "Clear"
                    val temp = weather.main?.temp?.roundToInt() ?: 0
                    val description = weather.weather?.firstOrNull()?.description ?: ""

                    // Check if this weather condition warrants a notification
                    if (shouldNotifyForWeather(weatherMain, temp)) {
                        sendWeatherUpdateNotification(weatherMain, temp, description, city)
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }

    private fun shouldNotifyForWeather(weatherMain: String, temp: Int): Boolean {
        return when {
            weatherMain.lowercase().contains("thunderstorm") -> true
            weatherMain.lowercase().contains("rain") && !weatherMain.lowercase().contains("drizzle") -> true
            weatherMain.lowercase().contains("snow") -> true
            temp > 35 -> true
            temp < 5 -> true
            weatherMain.lowercase().contains("fog") -> true
            else -> false
        }
    }

    private fun sendWeatherUpdateNotification(weatherMain: String, temp: Int, description: String, city: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)

        val intent = Intent(this, WeatherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val (title, message) = createNotificationContent(weatherMain, temp, description, city)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(getNotificationIcon(weatherMain))
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + Random().nextInt(1000), notification)
    }

    private fun createNotificationContent(weatherMain: String, temp: Int, description: String, city: String): Pair<String, String> {
        return when {
            weatherMain.lowercase().contains("thunderstorm") ->
                "⚡ Thunderstorm Alert!" to "Thunderstorm in $city. Stay safe indoors. Temp: ${temp}°C"

            weatherMain.lowercase().contains("rain") ->
                "🌧️ Rain Update" to "Raining in $city. Carry an umbrella. Current: ${temp}°C"

            weatherMain.lowercase().contains("snow") ->
                "❄️ Snow Alert" to "Snow in $city. Drive carefully. Temp: ${temp}°C"

            temp > 35 ->
                "🔥 Heat Warning" to "Extreme heat in $city ($temp°C). Stay hydrated."

            temp < 5 ->
                "🥶 Cold Alert" to "Very cold in $city ($temp°C). Dress warmly."

            weatherMain.lowercase().contains("fog") ->
                "🌫️ Fog Warning" to "Low visibility in $city due to fog. Drive safely."

            else ->
                "🌤️ Weather Update" to "Current in $city: ${description}, ${temp}°C"
        }
    }

    private fun getNotificationIcon(weatherMain: String): Int {
        return when (weatherMain.lowercase()) {
            "thunderstorm" -> R.drawable.ic_storm_notification
            "rain" -> R.drawable.ic_rain_notification
            "snow" -> R.drawable.ic_snow_notification
            "clear" -> R.drawable.ic_sunny_notification
            "clouds" -> R.drawable.ic_cloudy_notification
            else -> R.drawable.ic_weather_notification
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}