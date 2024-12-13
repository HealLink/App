// MedicalViewModel.kt
package com.heallinkapp.ui.medical

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.heallinkapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MedicalViewModel : ViewModel() {
    private val _hospitals = MutableLiveData<List<Hospital>>()
    val hospitals: LiveData<List<Hospital>> = _hospitals

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var hasLoadedData = false


    // Menyimpan lokasi terakhir untuk menghindari request yang tidak perlu
    private var lastLatitude: Double? = null
    private var lastLongitude: Double? = null

    // Threshold jarak dalam meter untuk memperbarui data
    private val DISTANCE_THRESHOLD = 1000 // 1 kilometer

    fun findNearbyHospitals(latitude: Double, longitude: Double) {
        // Jika sudah ada data, tidak perlu request API lagi
        if (!hospitals.value.isNullOrEmpty()) {
            Log.d("MedicalViewModel", "Data sudah ada, skip request API")
            return
        }

        Log.d("MedicalViewModel", "Mulai request API")

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val query = buildOverpassQuery(latitude, longitude, 100000)
                val response = ApiConfig.getOverpassApi().searchNearbyHospitals(query)

                if (response.elements.isEmpty()) {
                    val extendedQuery = buildOverpassQuery(latitude, longitude, 200000)
                    val extendedResponse = ApiConfig.getOverpassApi().searchNearbyHospitals(extendedQuery)
                    processHospitalsResponse(extendedResponse, latitude, longitude)
                } else {
                    processHospitalsResponse(response, latitude, longitude)
                }

                // Set flag bahwa data sudah diload
                hasLoadedData = true

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun shouldFetchNewData(newLatitude: Double, newLongitude: Double): Boolean {
        // Jika belum ada data sama sekali
        if (_hospitals.value == null || _hospitals.value?.isEmpty() == true) {
            return true
        }

        // Jika belum ada lokasi terakhir
        if (lastLatitude == null || lastLongitude == null) {
            return true
        }

        // Hitung jarak antara lokasi baru dan lokasi terakhir
        val distance = calculateDistance(
            lastLatitude!!, lastLongitude!!,
            newLatitude, newLongitude
        )

        // Jika jarak lebih dari threshold, update data
        return distance > DISTANCE_THRESHOLD
    }

    private fun fetchHospitalsFromApi(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val query = buildOverpassQuery(latitude, longitude, 100000)
                val response = ApiConfig.getOverpassApi().searchNearbyHospitals(query)

                if (response.elements.isEmpty()) {
                    // Try with larger radius
                    val extendedQuery = buildOverpassQuery(latitude, longitude, 200000)
                    val extendedResponse = ApiConfig.getOverpassApi().searchNearbyHospitals(extendedQuery)
                    processHospitalsResponse(extendedResponse, latitude, longitude)
                } else {
                    processHospitalsResponse(response, latitude, longitude)
                }

                // Simpan lokasi terakhir setelah berhasil mendapatkan data
                lastLatitude = latitude
                lastLongitude = longitude

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun buildOverpassQuery(latitude: Double, longitude: Double, radius: Int): String {
        return """
            [out:json];
            (
                node["amenity"="hospital"](around:$radius,$latitude,$longitude);
                way["amenity"="hospital"](around:$radius,$latitude,$longitude);
                node["healthcare"="hospital"](around:$radius,$latitude,$longitude);
                way["healthcare"="hospital"](around:$radius,$latitude,$longitude);
            );
            out body;
            >;
            out skel qt;
        """.trimIndent()
    }

    private suspend fun processHospitalsResponse(
        response: OverpassResponse,
        latitude: Double,
        longitude: Double
    ) {
        val hospitals = response.elements.mapNotNull { element ->
            try {
                if (element.tags == null) return@mapNotNull null

                val baseDistance = calculateDistance(
                    latitude, longitude,
                    element.lat, element.lon
                )

                val correctionFactor = when {
                    element.tags["place"] == "city" -> 1.3
                    element.tags["place"] == "suburb" -> 1.4
                    else -> 1.5
                }

                val estimatedDistance = baseDistance * correctionFactor

                Hospital(
                    name = element.tags["name"] ?: element.tags["operator"] ?: "Unknown Hospital",
                    distance = (estimatedDistance / 1000).toInt(),
                    latitude = element.lat,
                    longitude = element.lon,
                    image = R.drawable.medical
                )
            } catch (e: Exception) {
                Log.e("MedicalFragment", "Error processing hospital", e)
                null
            }
        }
            .sortedBy { it.distance } // Sort berdasarkan jarak
            .take(30)    // Ambil 30 rumah sakit terdekat saja

        withContext(Dispatchers.Main) {
            _hospitals.value = hospitals
        }
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    fun clearError() {
        _error.value = null
    }
}