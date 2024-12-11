package com.heallinkapp.ui.medical

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.heallinkapp.databinding.FragmentMedicalBinding
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices
import com.heallinkapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MedicalFragment : Fragment() {
    private var _binding: FragmentMedicalBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var hospitalAdapter: HospitalAdapter
    private var currentLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        initializeLocation()
    }

    private fun setupRecyclerView() {
        hospitalAdapter = HospitalAdapter { hospital ->
            openMapsNavigation(hospital)
        }
        binding.rvHospitals.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = hospitalAdapter
        }
    }

    private fun initializeLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (checkLocationPermission()) {
            getCurrentLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = it
                    Log.d("GoFragment", "Got location: ${it.latitude}, ${it.longitude}")
                    findNearbyHospitals(it.latitude, it.longitude)
                } ?: run {
                    Toast.makeText(requireContext(),
                        "Could not get location, please enable GPS", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(),
                    "Error getting location: ${e.message}",
                    Toast.LENGTH_SHORT).show()
                Log.e("GoFragment", "Location error", e)
            }
        }
    }

    private fun findNearbyHospitals(latitude: Double, longitude: Double) {
        lifecycleScope.launch {
            try {
                val query = """
                    [out:json];
                    (
                        node["amenity"="hospital"](around:100000,$latitude,$longitude);
                        way["amenity"="hospital"](around:100000,$latitude,$longitude);
                        node["healthcare"="hospital"](around:100000,$latitude,$longitude);
                        way["healthcare"="hospital"](around:100000,$latitude,$longitude);
                    );
                    out body;
                    >;
                    out skel qt;
                """.trimIndent()

                Log.d("GoFragment", "Searching hospitals at: $latitude, $longitude")
                val response = ApiConfig.getOverpassApi().searchNearbyHospitals(query)
                Log.d("GoFragment", "Got response: ${response.elements.size} hospitals")

                if (response.elements.isEmpty()) {
                    findHospitalsWithLargerRadius(latitude, longitude)
                    return@launch
                }

                processHospitalsResponse(response, latitude, longitude)

            } catch (e: Exception) {
                Log.e("GoFragment", "Error finding hospitals", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun findHospitalsWithLargerRadius(latitude: Double, longitude: Double) {
        try {
            val query = """
                [out:json];
                (
                    node["amenity"="hospital"](around:200000,$latitude,$longitude);
                    way["amenity"="hospital"](around:200000,$latitude,$longitude);
                    node["healthcare"="hospital"](around:200000,$latitude,$longitude);
                    way["healthcare"="hospital"](around:200000,$latitude,$longitude);
                );
                out body;
                >;
                out skel qt;
            """.trimIndent()

            val response = ApiConfig.getOverpassApi().searchNearbyHospitals(query)
            processHospitalsResponse(response, latitude, longitude)

        } catch (e: Exception) {
            Log.e("GoFragment", "Error in extended search", e)
        }
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
        }.sortedBy { it.distance }

        withContext(Dispatchers.Main) {
            hospitalAdapter.submitList(hospitals)
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

    private fun openMapsNavigation(hospital: Hospital) {
        val uri = Uri.parse("google.navigation:q=${hospital.latitude},${hospital.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        startActivity(mapIntent)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    getCurrentLocation()
                } else {
                    Toast.makeText(requireContext(),
                        "Location permission needed to find nearby hospitals",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}