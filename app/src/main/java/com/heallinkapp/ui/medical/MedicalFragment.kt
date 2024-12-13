// MedicalFragment.kt
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
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.LocationServices

class MedicalFragment : Fragment() {
    private var _binding: FragmentMedicalBinding? = null
    private val binding get() = _binding!!
    // Di MedicalFragment
    private val viewModel: MedicalViewModel by activityViewModels()

//    private val viewModel: MedicalViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var hospitalAdapter: HospitalAdapter

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
        Log.d("medical fragment", "onViewCreated: hospitals=${viewModel.hospitals.value?.size} ")
        setupRecyclerView()
        setupObservers()
        if (viewModel.hospitals.value.isNullOrEmpty()) {
            Log.d("ini", "${viewModel.hospitals}")
            initializeLocation()
        } else {
            Log.d("medical fragment", "Data sudah ada: ")
        }
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

    private fun setupObservers() {
        viewModel.hospitals.observe(viewLifecycleOwner) { hospitals ->
            hospitalAdapter.submitList(hospitals)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
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
                    viewModel.findNearbyHospitals(it.latitude, it.longitude)
                } ?: run {
                    Toast.makeText(requireContext(),
                        "Could not get location, please enable GPS", Toast.LENGTH_LONG).show()
                }
            }
        }
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
        Log.d("medical fragment", "onDestroyView: ")
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}