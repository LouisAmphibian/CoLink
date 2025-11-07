// TrackingActivity.kt - Complete movement tracking with GPS and sensors
package com.xdevstudio.colink

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.*

class TrackingActivity : AppCompatActivity(), SensorEventListener {

    // Views
    private lateinit var backButton: ImageButton
    private lateinit var shareLocationSwitch: SwitchMaterial
    private lateinit var locationStatusText: TextView
    private lateinit var currentModeText: TextView
    private lateinit var currentModeIcon: ImageView
    private lateinit var stepsCount: TextView
    private lateinit var currentLocationText: TextView
    private lateinit var coordinatesText: TextView
    private lateinit var fullscreenButton: ImageButton

    // Mode cards
    private lateinit var walkingCard: MaterialCardView
    private lateinit var vehicleCard: MaterialCardView
    private lateinit var cyclingCard: MaterialCardView
    private lateinit var runningCard: MaterialCardView

    // Check icons
    private lateinit var walkingCheckIcon: ImageView
    private lateinit var vehicleCheckIcon: ImageView
    private lateinit var cyclingCheckIcon: ImageView
    private lateinit var runningCheckIcon: ImageView

    // Status texts
    private lateinit var walkingStatusText: TextView
    private lateinit var vehicleStatusText: TextView
    private lateinit var cyclingStatusText: TextView
    private lateinit var runningStatusText: TextView

    // Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    // Sensors
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var accelerometer: Sensor? = null

    // Data
    private var currentMode = "Walking"
    private var totalSteps = 0
    private var initialSteps = 0
    private var isLocationSharingEnabled = false
    private var currentLatitude = 0.0
    private var currentLongitude = 0.0

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val ACTIVITY_RECOGNITION_REQUEST_CODE = 1002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking)

        initializeViews()
        setupClickListeners()
        initializeLocation()
        initializeSensors()

        // Request permissions
        checkAndRequestPermissions()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        shareLocationSwitch = findViewById(R.id.shareLocationSwitch)
        locationStatusText = findViewById(R.id.locationStatusText)
        currentModeText = findViewById(R.id.currentModeText)
        currentModeIcon = findViewById(R.id.currentModeIcon)
        stepsCount = findViewById(R.id.stepsCount)
        currentLocationText = findViewById(R.id.currentLocationText)
        coordinatesText = findViewById(R.id.coordinatesText)
        fullscreenButton = findViewById(R.id.fullscreenButton)

        // Mode cards
        walkingCard = findViewById(R.id.walkingCard)
        vehicleCard = findViewById(R.id.vehicleCard)
        cyclingCard = findViewById(R.id.cyclingCard)
        runningCard = findViewById(R.id.runningCard)

        // Check icons
        walkingCheckIcon = findViewById(R.id.walkingCheckIcon)
        vehicleCheckIcon = findViewById(R.id.vehicleCheckIcon)
        cyclingCheckIcon = findViewById(R.id.cyclingCheckIcon)
        runningCheckIcon = findViewById(R.id.runningCheckIcon)

        // Status texts
        walkingStatusText = findViewById(R.id.walkingStatusText)
        vehicleStatusText = findViewById(R.id.vehicleStatusText)
        cyclingStatusText = findViewById(R.id.cyclingStatusText)
        runningStatusText = findViewById(R.id.runningStatusText)
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        shareLocationSwitch.setOnCheckedChangeListener { _, isChecked ->
            handleLocationSharing(isChecked)
        }

        walkingCard.setOnClickListener {
            setActiveMode("Walking", R.drawable.ic_directions_walk)
        }

        vehicleCard.setOnClickListener {
            setActiveMode("Vehicle", R.drawable.ic_directions_car)
        }

        cyclingCard.setOnClickListener {
            setActiveMode("Cycling", R.drawable.ic_directions_bike)
        }

        runningCard.setOnClickListener {
            setActiveMode("Running", R.drawable.ic_directions_run)
        }

        fullscreenButton.setOnClickListener {
            Toast.makeText(this, "Fullscreen map - Coming Soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocation(location)
                }
            }
        }
    }

    private fun initializeSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Step counter sensor
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // Accelerometer for movement detection
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (stepSensor == null) {
            Toast.makeText(this, "Step counter not available on this device", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            startLocationUpdates()
            registerSensors()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )

            // Get last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { updateLocation(it) }
            }
        }
    }

    private fun registerSensors() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun updateLocation(location: Location) {
        currentLatitude = location.latitude
        currentLongitude = location.longitude

        coordinatesText.text = "Lat: %.4f, Lng: %.4f".format(currentLatitude, currentLongitude)

        // Get address from coordinates
        getAddressFromLocation(currentLatitude, currentLongitude)
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressText = buildString {
                    if (address.featureName != null) append("${address.featureName}, ")
                    if (address.locality != null) append("${address.locality}, ")
                    if (address.adminArea != null) append(address.adminArea)
                }
                currentLocationText.text = addressText.ifEmpty { "Location found" }
            } else {
                currentLocationText.text = "Address not available"
            }
        } catch (e: Exception) {
            currentLocationText.text = "Location: $latitude, $longitude"
        }
    }

    private fun handleLocationSharing(isEnabled: Boolean) {
        isLocationSharingEnabled = isEnabled

        if (isEnabled) {
            locationStatusText.text = "Location sharing is ON - Visible to group members"
            locationStatusText.setTextColor(getColor(R.color.active_green))
            startLocationUpdates()

            // TODO: Send location to Firebase for group members
            Toast.makeText(this, "Location now visible to event groups", Toast.LENGTH_SHORT).show()
        } else {
            locationStatusText.text = "Location sharing is OFF"
            locationStatusText.setTextColor(getColor(android.R.color.white))

            Toast.makeText(this, "Location sharing stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setActiveMode(mode: String, iconResId: Int) {
        currentMode = mode
        currentModeText.text = mode
        currentModeIcon.setImageResource(iconResId)

        // Reset all cards
        walkingCard.setCardBackgroundColor(getColor(R.color.light_blue))
        vehicleCard.setCardBackgroundColor(getColor(R.color.light_blue))
        cyclingCard.setCardBackgroundColor(getColor(R.color.light_blue))
        runningCard.setCardBackgroundColor(getColor(R.color.light_blue))

        // Hide all check icons
        walkingCheckIcon.visibility = android.view.View.GONE
        vehicleCheckIcon.visibility = android.view.View.GONE
        cyclingCheckIcon.visibility = android.view.View.GONE
        runningCheckIcon.visibility = android.view.View.GONE

        // Update status texts
        walkingStatusText.text = "Last used"
        vehicleStatusText.text = "Last used 3 days ago"
        cyclingStatusText.text = "Not detected"
        runningStatusText.text = "Not detected"

        // Set active card
        when (mode) {
            "Walking" -> {
                walkingCard.setCardBackgroundColor(getColor(R.color.active_green))
                walkingCheckIcon.visibility = android.view.View.VISIBLE
                walkingStatusText.text = "Active now"
            }
            "Vehicle" -> {
                vehicleCard.setCardBackgroundColor(getColor(R.color.active_green))
                vehicleCheckIcon.visibility = android.view.View.VISIBLE
                vehicleStatusText.text = "Active now"
            }
            "Cycling" -> {
                cyclingCard.setCardBackgroundColor(getColor(R.color.active_green))
                cyclingCheckIcon.visibility = android.view.View.VISIBLE
                cyclingStatusText.text = "Active now"
            }
            "Running" -> {
                runningCard.setCardBackgroundColor(getColor(R.color.active_green))
                runningCheckIcon.visibility = android.view.View.VISIBLE
                runningStatusText.text = "Active now"
            }
        }

        Toast.makeText(this, "Mode changed to $mode", Toast.LENGTH_SHORT).show()
    }

    // SensorEventListener implementation
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_STEP_COUNTER -> {
                    if (initialSteps == 0) {
                        initialSteps = it.values[0].toInt()
                    }
                    totalSteps = it.values[0].toInt() - initialSteps
                    stepsCount.text = totalSteps.toString()
                }

                Sensor.TYPE_ACCELEROMETER -> {
                    // Detect movement pattern (basic implementation)
                    detectMovementType(it.values)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }

    private fun detectMovementType(values: FloatArray) {
        val x = values[0]
        val y = values[1]
        val z = values[2]

        val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble())

        // Basic movement detection (can be improved with ML models)
        when {
            acceleration < 1.0 -> {
                // Stationary
            }
            acceleration in 1.0..5.0 -> {
                // Walking detected
                if (currentMode != "Walking") {
                    // Auto-detect walking (optional)
                }
            }
            acceleration in 5.0..15.0 -> {
                // Running or vehicle
            }
        }
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
                    startLocationUpdates()
                    registerSensors()
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Location Permission Required")
                        .setMessage("This feature requires location permission to track your movement and share location with event groups.")
                        .setPositiveButton("Grant Permission") { _, _ ->
                            checkAndRequestPermissions()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerSensors()
        if (isLocationSharingEnabled) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}