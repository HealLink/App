package com.heallinkapp.ui.list

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.heallinkapp.R
import com.heallinkapp.ViewModelFactory
import com.heallinkapp.data.UserRepository
import com.heallinkapp.databinding.FragmentListBinding
import com.heallinkapp.di.Injection
import com.heallinkapp.ui.add.AddActivity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import android.widget.ImageView
import com.heallinkapp.notification.AlarmReceiver
import com.heallinkapp.TimePickerFragment
import com.heallinkapp.data.local.UserPreferences
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import java.util.Locale

class ListFragment : Fragment(), TimePickerFragment.DialogTimeListener {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NoteAdapter
    private lateinit var alarmReceiver: AlarmReceiver
    private lateinit var userPreferences: UserPreferences

    private val listViewModel: ListViewModel by viewModels {
        ViewModelFactory(Injection.provideNoteRepository(requireContext()))
    }

    private val notificationPermissionRequestCode = 1001

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentListBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), AddActivity::class.java)
            startActivity(intent)
        }

        userPreferences = UserPreferences.newInstance(requireContext())
        alarmReceiver = AlarmReceiver()

        val iconBell = binding.root.findViewById<ImageView>(R.id.icon_bell)

        lifecycleScope.launch {
            // Observing alarm state from UserPreferences
            userPreferences.isAlarmOnFlow.collect { isAlarmSet ->
                if (isAlarmSet) {
                    iconBell.setImageResource(R.drawable.baseline_notifications_active_24)
                } else {
                    iconBell.setImageResource(R.drawable.baseline_notifications_none_24)
                }
            }
        }

        iconBell.setOnClickListener {
            // Check if notification permission is granted
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {

                lifecycleScope.launch {
                    val isAlarmSet = userPreferences.isAlarmOnFlow.firstOrNull() ?: false
                    if (isAlarmSet) {
                        alarmReceiver.cancelAlarm(requireContext())
                        userPreferences.saveAlarmStatus(false)
                    } else {
                        showTimePicker()
                    }
                }
            } else {
                requestNotificationPermission()
            }
        }

        lifecycleScope.launch {
            val username = userPreferences.userName.firstOrNull() ?: "User"
            binding.textGreet.text = "Hi $username\nHere are your stories!"
        }

        adapter = NoteAdapter()
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = adapter

        observeViewModel()
        return root
    }

    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            notificationPermissionRequestCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == notificationPermissionRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission granted", Toast.LENGTH_SHORT).show()
                showTimePicker()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onDialogTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        val time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)

        val message = "Tell me your story today!"
        alarmReceiver.setRepeatingAlarm(requireContext(), time, message)

        lifecycleScope.launch {
            userPreferences.saveAlarmStatus(true)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            val token = userPreferences.userToken.firstOrNull().toString()
            listViewModel.getAllNotes(token)
        }

        listViewModel.notes.observe(viewLifecycleOwner) { notes ->
            adapter.setNotes(notes)
        }
        listViewModel.isLoading.observe(viewLifecycleOwner) { isloading ->
            binding.progressBar.visibility = if (isloading) View.VISIBLE else View.GONE
        }
    }

    private fun showTimePicker() {
        val timePickerFragment = TimePickerFragment()
        timePickerFragment.show(parentFragmentManager, "TimePicker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
