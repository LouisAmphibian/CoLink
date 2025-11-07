package com.xdevstudio.colink

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LocationSelectionActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var confirmLocationButton: Button
    private lateinit var selectedLocationText: TextView

    private var selectedLocation: GeoPoint? = null
    private var selectedAddress: String = ""

    companion object {
        const val EXTRA_SELECTED_LOCATION = "selected_location"
        const val EXTRA_SELECTED_ADDRESS = "selected_address"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_selection)

        // SIMPLIFIED: Initialize osmdroid configuration without PreferenceManager
        Configuration.getInstance().userAgentValue = packageName

        initializeViews()
        setupMap()
        setupClickListeners()
    }

    private fun initializeViews() {
        mapView = findViewById(R.id.mapView)
        searchEditText = findViewById(R.id.searchEditText)
        searchButton = findViewById(R.id.searchButton)
        confirmLocationButton = findViewById(R.id.confirmLocationButton)
        selectedLocationText = findViewById(R.id.selectedLocationText)

        findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun setupMap() {
        // Configure map
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Add rotation gestures
        val rotationGestureOverlay = RotationGestureOverlay(mapView)
        rotationGestureOverlay.isEnabled = true
        mapView.overlays.add(rotationGestureOverlay)

        // Set initial zoom and center (Johannesburg as default for South Africa)
        val mapController = mapView.controller
        mapController.setZoom(15.0)
        val defaultLocation = GeoPoint(-26.2041, 28.0473) // Johannesburg coordinates
        mapController.animateTo(defaultLocation)

        // Add long press listener for location selection
        mapView.overlays.add(object : Overlay() {
            override fun onLongPress(e: MotionEvent?, mapView: MapView?): Boolean {
                e?.let { event ->
                    val geoPoint = mapView?.projection?.fromPixels(event.x.toInt(), event.y.toInt()) as? GeoPoint
                    geoPoint?.let { point ->
                        selectedLocation = point
                        reverseGeocode(point)
                    }
                }
                return true
            }
        })
    }

    private fun setupClickListeners() {
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchLocation(query)
            } else {
                Toast.makeText(this, "Please enter a location to search", Toast.LENGTH_SHORT).show()
            }
        }

        confirmLocationButton.setOnClickListener {
            if (selectedLocation != null) {
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_SELECTED_LOCATION, doubleArrayOf(
                        selectedLocation!!.latitude,
                        selectedLocation!!.longitude
                    ))
                    putExtra(EXTRA_SELECTED_ADDRESS, selectedAddress)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun searchLocation(query: String) {
        searchButton.isEnabled = false
        searchButton.text = "Searching..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = "https://nominatim.openstreetmap.org/search?format=json&q=$encodedQuery&limit=1"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "CoLinkApp/1.0")

                val response = connection.inputStream.bufferedReader().use { it.readText() }

                withContext(Dispatchers.Main) {
                    val jsonArray = JSONArray(response)
                    if (jsonArray.length() > 0) {
                        val firstResult = jsonArray.getJSONObject(0)
                        val lat = firstResult.getString("lat").toDouble()
                        val lon = firstResult.getString("lon").toDouble()
                        val displayName = firstResult.getString("display_name")

                        val geoPoint = GeoPoint(lat, lon)
                        selectedLocation = geoPoint
                        selectedAddress = displayName

                        // Move map to the location
                        mapView.controller.animateTo(geoPoint)
                        updateSelectedLocationMarker()

                        Toast.makeText(this@LocationSelectionActivity, "Location found", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@LocationSelectionActivity, "Location not found", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LocationSelectionActivity, "Search failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                    searchButton.isEnabled = true
                    searchButton.text = "Search"
                }
            }
        }
    }

    private fun reverseGeocode(point: GeoPoint) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = "https://nominatim.openstreetmap.org/reverse?format=json&lat=${point.latitude}&lon=${point.longitude}"

                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "CoLinkApp/1.0")

                val response = connection.inputStream.bufferedReader().use { it.readText() }

                withContext(Dispatchers.Main) {
                    val jsonObject = org.json.JSONObject(response)
                    selectedAddress = jsonObject.getString("display_name")
                    updateSelectedLocationMarker()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    selectedAddress = "Selected Location (${point.latitude}, ${point.longitude})"
                    updateSelectedLocationMarker()
                }
            }
        }
    }

    private fun updateSelectedLocationMarker() {
        // Clear existing markers
        mapView.overlays.removeIf { it is Marker }

        selectedLocation?.let { location ->
            // Add marker at selected location
            val marker = Marker(mapView)
            marker.position = location
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = selectedAddress
            mapView.overlays.add(marker)

            // Update UI
            selectedLocationText.text = selectedAddress
            confirmLocationButton.isEnabled = true

            mapView.invalidate()
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}