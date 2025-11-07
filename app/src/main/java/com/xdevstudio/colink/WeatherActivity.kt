//  weather updates with OpenWeatherMap API and real-time notifications
package com.xdevstudio.colink

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xdevstudio.colink.adapters.ForecastAdapter
import com.xdevstudio.colink.api.WeatherApiService
import com.xdevstudio.colink.models.ForecastItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class WeatherActivity : AppCompatActivity() {

    // Views
    private lateinit var backButton: ImageButton
    private lateinit var refreshButton: ImageButton
    private lateinit var changeLocationButton: ImageButton
    private lateinit var locationText: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var temperatureText: TextView
    private lateinit var weatherDescriptionText: TextView
    private lateinit var feelsLikeText: TextView
    private lateinit var humidityText: TextView
    private lateinit var windSpeedText: TextView
    private lateinit var pressureText: TextView
    private lateinit var visibilityText: TextView
    private lateinit var alertsCard: MaterialCardView
    private lateinit var alertText: TextView
    private lateinit var forecastRecyclerView: RecyclerView
    private lateinit var lastUpdatedText: TextView
    private lateinit var notificationSwitch: Switch

    // Data
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherApiService: WeatherApiService
    private lateinit var forecastAdapter: ForecastAdapter

    private val API_KEY = "fce1775dc658ea5b5a7cefb94b4fbcfe"
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0
    private var currentCity = ""
    private var isNotificationsEnabled = true
    private var lastNotificationTime: Long = 0
    private var lastWeatherCondition = ""

    // Notification
    private val CHANNEL_ID = "weather_alerts_channel"
    private val NOTIFICATION_ID = 1001

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2001
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 2002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        initializeViews()
        setupClickListeners()
        setupRecyclerView()
        createNotificationChannel()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        weatherApiService = WeatherApiService.create()

        checkPermissionsAndLoadWeather()

        // Start periodic weather updates
        startPeriodicWeatherUpdates()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        refreshButton = findViewById(R.id.refreshButton)
        changeLocationButton = findViewById(R.id.changeLocationButton)
        locationText = findViewById(R.id.locationText)
        weatherIcon = findViewById(R.id.weatherIcon)
        temperatureText = findViewById(R.id.temperatureText)
        weatherDescriptionText = findViewById(R.id.weatherDescriptionText)
        feelsLikeText = findViewById(R.id.feelsLikeText)
        humidityText = findViewById(R.id.humidityText)
        windSpeedText = findViewById(R.id.windSpeedText)
        pressureText = findViewById(R.id.pressureText)
        visibilityText = findViewById(R.id.visibilityText)
        alertsCard = findViewById(R.id.alertsCard)
        alertText = findViewById(R.id.alertText)
        forecastRecyclerView = findViewById(R.id.forecastRecyclerView)
        lastUpdatedText = findViewById(R.id.lastUpdatedText)
        notificationSwitch = findViewById(R.id.notificationSwitch)

        // Set initial notification state
        notificationSwitch.isChecked = isNotificationsEnabled
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        refreshButton.setOnClickListener {
            loadWeatherData()
        }

        changeLocationButton.setOnClickListener {
            showChangeLocationDialog()
        }

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            isNotificationsEnabled = isChecked
            if (isChecked) {
                Toast.makeText(this, "Weather notifications enabled", Toast.LENGTH_SHORT).show()
                // Request notification permission if not granted
                checkNotificationPermission()
            } else {
                Toast.makeText(this, "Weather notifications disabled", Toast.LENGTH_SHORT).show()
                cancelAllNotifications()
            }
        }
    }

    private fun setupRecyclerView() {
        forecastAdapter = ForecastAdapter(mutableListOf())
        forecastRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@WeatherActivity)
            adapter = forecastAdapter
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Weather Alerts"
            val descriptionText = "Real-time weather condition alerts and notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager: NotificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkPermissionsAndLoadWeather() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getCurrentLocationAndLoadWeather()
        }

        // Check notification permission
        checkNotificationPermission()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun getCurrentLocationAndLoadWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude

                    getAddressFromLocation(currentLatitude, currentLongitude)
                    loadWeatherData()
                } else {
                    Toast.makeText(this, "Unable to get location. Using default city.", Toast.LENGTH_SHORT).show()
                    loadWeatherByCity("Johannesburg")
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                loadWeatherByCity("Johannesburg")
            }
        }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                currentCity = address.locality ?: address.adminArea ?: "Unknown"
                locationText.text = currentCity
            }
        } catch (e: Exception) {
            locationText.text = "Current Location"
        }
    }

    private fun loadWeatherData() {
        if (currentLatitude != 0.0 && currentLongitude != 0.0) {
            loadWeatherByCoordinates(currentLatitude, currentLongitude)
            loadForecastByCoordinates(currentLatitude, currentLongitude)
        } else if (currentCity.isNotEmpty()) {
            loadWeatherByCity(currentCity)
            loadForecastByCity(currentCity)
        }
    }

    private fun loadWeatherByCoordinates(lat: Double, lon: Double) {
        lifecycleScope.launch {
            try {
                val response = weatherApiService.getCurrentWeather(lat, lon, API_KEY)

                if (response.isSuccessful) {
                    response.body()?.let { weather ->
                        updateWeatherUI(weather)
                        checkAndSendWeatherNotification(weather)
                    }
                } else {
                    Toast.makeText(this@WeatherActivity, "Failed to load weather data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@WeatherActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadWeatherByCity(city: String) {
        lifecycleScope.launch {
            try {
                val response = weatherApiService.getCurrentWeatherByCity(city, API_KEY)

                if (response.isSuccessful) {
                    response.body()?.let { weather ->
                        updateWeatherUI(weather)
                        checkAndSendWeatherNotification(weather)
                    }
                } else {
                    Toast.makeText(this@WeatherActivity, "City not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@WeatherActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateWeatherUI(weather: com.xdevstudio.colink.models.WeatherResponse) {
        // Temperature
        val temp = weather.main?.temp?.roundToInt() ?: 0
        temperatureText.text = "${temp}°C"

        // Feels like
        val feelsLike = weather.main?.feelsLike?.roundToInt() ?: 0
        feelsLikeText.text = "Feels like ${feelsLike}°C"

        // Description
        val description = weather.weather?.firstOrNull()?.description?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        } ?: "Unknown"
        weatherDescriptionText.text = description

        // Weather icon
        val weatherMain = weather.weather?.firstOrNull()?.main ?: "Clear"
        weatherIcon.setImageResource(getWeatherIcon(weatherMain))

        // Humidity
        humidityText.text = "${weather.main?.humidity ?: 0}%"

        // Wind
        val windSpeed = weather.wind?.speed ?: 0.0
        windSpeedText.text = "${(windSpeed * 3.6).roundToInt()} km/h"

        // Pressure
        pressureText.text = "${weather.main?.pressure ?: 0} hPa"

        // Visibility
        val visibility = (weather.visibility ?: 0) / 1000.0
        visibilityText.text = "${"%.1f".format(visibility)} km"

        // Weather alerts/recommendations
        showWeatherAlert(weatherMain, temp)

        // Last updated
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        lastUpdatedText.text = "Last updated: $currentTime"
    }

    private fun checkAndSendWeatherNotification(weather: com.xdevstudio.colink.models.WeatherResponse) {
        if (!isNotificationsEnabled) return

        val currentTime = System.currentTimeMillis()
        val weatherMain = weather.weather?.firstOrNull()?.main ?: "Clear"
        val temp = weather.main?.temp?.roundToInt() ?: 0
        val description = weather.weather?.firstOrNull()?.description ?: ""

        // Only send notification if weather condition changed or after 1 hour
        if (weatherMain != lastWeatherCondition || currentTime - lastNotificationTime > 3600000) {
            sendWeatherNotification(weatherMain, temp, description)
            lastWeatherCondition = weatherMain
            lastNotificationTime = currentTime
        }
    }

    private fun sendWeatherNotification(weatherMain: String, temp: Int, description: String) {
        val notificationManager = NotificationManagerCompat.from(this)

        // Check if we have notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val (title, message) = createNotificationContent(weatherMain, temp, description)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(getNotificationIcon(weatherMain))
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationContent(weatherMain: String, temp: Int, description: String): Pair<String, String> {
        return when {
            weatherMain.lowercase().contains("thunderstorm") ->
                "⚡ Thunderstorm Alert!" to "Thunderstorm detected in $currentCity. Stay indoors and avoid open areas."

            weatherMain.lowercase().contains("rain") || weatherMain.lowercase().contains("drizzle") ->
                "🌧️ Rain Alert" to "Rain expected in $currentCity. Don't forget your umbrella! Current temp: ${temp}°C"

            weatherMain.lowercase().contains("snow") ->
                "❄️ Snow Alert" to "Snowfall in $currentCity. Drive carefully and dress warmly! Current temp: ${temp}°C"

            temp > 35 ->
                "🔥 Extreme Heat" to "Very hot in $currentCity ($temp°C). Stay hydrated and avoid direct sun exposure."

            temp < 5 ->
                "🥶 Cold Alert" to "Very cold in $currentCity ($temp°C). Dress warmly and limit outdoor exposure."

            weatherMain.lowercase().contains("fog") || weatherMain.lowercase().contains("mist") ->
                "🌫️ Low Visibility" to "Foggy conditions in $currentCity. Drive carefully and use headlights."

            else ->
                "🌤️ Weather Update" to "Current weather in $currentCity: ${description.capitalize()}, ${temp}°C"
        }
    }

    private fun getNotificationIcon(weatherMain: String): Int {
        return when (weatherMain.lowercase()) {
            "thunderstorm" -> R.drawable.ic_storm_notification
            "rain", "drizzle" -> R.drawable.ic_rain_notification
            "snow" -> R.drawable.ic_snow_notification
            "clear" -> R.drawable.ic_sunny_notification
            "clouds" -> R.drawable.ic_cloudy_notification
            "fog", "mist" -> R.drawable.ic_fog_notification
            else -> R.drawable.ic_weather_notification
        }
    }

    private fun showWeatherAlert(weatherMain: String, temp: Int) {
        var alertMessage = ""
        var showAlert = false

        when {
            weatherMain.lowercase().contains("rain") -> {
                alertMessage = "☂️ Rain expected. Don't forget your umbrella!"
                showAlert = true
            }
            weatherMain.lowercase().contains("storm") || weatherMain.lowercase().contains("thunder") -> {
                alertMessage = "⚡ Thunderstorm alert! Stay indoors if possible."
                showAlert = true
            }
            weatherMain.lowercase().contains("snow") -> {
                alertMessage = "❄️ Snow expected. Drive carefully and dress warmly!"
                showAlert = true
            }
            temp < 10 -> {
                alertMessage = "🧥 It's cold! Bring a jacket or coat."
                showAlert = true
            }
            temp > 30 -> {
                alertMessage = "🌡️ It's hot! Stay hydrated and wear sunscreen."
                showAlert = true
            }
            weatherMain.lowercase().contains("fog") || weatherMain.lowercase().contains("mist") -> {
                alertMessage = "🌫️ Foggy conditions. Drive carefully."
                showAlert = true
            }
        }

        if (showAlert) {
            alertsCard.visibility = View.VISIBLE
            alertText.text = alertMessage
        } else {
            alertsCard.visibility = View.GONE
        }
    }

    private fun getWeatherIcon(weatherMain: String): Int {
        return when (weatherMain.lowercase()) {
            "clear" -> R.drawable.ic_weather_sunny
            "clouds" -> R.drawable.ic_weather_cloudy
            "rain", "drizzle" -> R.drawable.ic_weather_rainy
            "thunderstorm" -> R.drawable.ic_weather_storm
            "snow" -> R.drawable.ic_weather_snowy
            "mist", "fog", "haze" -> R.drawable.ic_weather_fog
            else -> R.drawable.ic_weather_sunny
        }
    }

    private fun loadForecastByCoordinates(lat: Double, lon: Double) {
        lifecycleScope.launch {
            try {
                val response = weatherApiService.getForecast(lat, lon, API_KEY)

                if (response.isSuccessful) {
                    response.body()?.let { forecast ->
                        updateForecastUI(forecast.list ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                // Forecast loading failed
            }
        }
    }

    private fun loadForecastByCity(city: String) {
        lifecycleScope.launch {
            try {
                val response = weatherApiService.getForecastByCity(city, API_KEY)

                if (response.isSuccessful) {
                    response.body()?.let { forecast ->
                        updateForecastUI(forecast.list ?: emptyList())
                    }
                }
            } catch (e: Exception) {
                // Forecast loading failed
            }
        }
    }

    private fun updateForecastUI(forecastList: List<ForecastItem>) {
        // Get one forecast per day (around noon)
        val dailyForecasts = forecastList
            .filter { it.dtTxt?.contains("12:00:00") == true }
            .take(5)

        forecastAdapter.updateForecast(dailyForecasts)
    }

    private fun showChangeLocationDialog() {
        val input = EditText(this)
        input.hint = "Enter city name"
        input.setText(currentCity)

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.setPadding(60, 20, 60, 0)
        container.addView(input)

        MaterialAlertDialogBuilder(this)
            .setTitle("Change Location")
            .setView(container)
            .setPositiveButton("Get Weather") { _, _ ->
                val city = input.text.toString().trim()
                if (city.isNotEmpty()) {
                    currentCity = city
                    locationText.text = city
                    loadWeatherByCity(city)
                    loadForecastByCity(city)
                }
            }
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Use Current Location") { _, _ ->
                getCurrentLocationAndLoadWeather()
            }
            .show()
    }

    private fun startPeriodicWeatherUpdates() {

        Toast.makeText(this, "Weather updates will notify you of significant changes", Toast.LENGTH_LONG).show()
    }

    private fun cancelAllNotifications() {
        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.cancel(NOTIFICATION_ID)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocationAndLoadWeather()
                } else {
                    Toast.makeText(this, "Location permission denied. Using default city.", Toast.LENGTH_SHORT).show()
                    loadWeatherByCity("Johannesburg")
                }
            }

            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Weather notifications enabled", Toast.LENGTH_SHORT).show()
                } else {
                    notificationSwitch.isChecked = false
                    Toast.makeText(this, "Notifications disabled. Enable in app settings.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up any ongoing operations
    }
}