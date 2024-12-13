package com.heallinkapp.ui.list

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.heallinkapp.R
import com.heallinkapp.TimePickerFragment
import com.heallinkapp.ViewModelFactory
import com.heallinkapp.data.local.UserPreferences
import com.heallinkapp.databinding.FragmentListBinding
import com.heallinkapp.di.Injection
import com.heallinkapp.notification.AlarmReceiver
import com.heallinkapp.ui.add.AddActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Locale

class ListFragment : Fragment(), TimePickerFragment.DialogTimeListener {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: NoteAdapter
    private lateinit var alarmReceiver: AlarmReceiver
    private lateinit var userPreferences: UserPreferences
    private var greetingJob: Job? = null

    private val listViewModel: ListViewModel by viewModels {
        ViewModelFactory(Injection.provideNoteRepository(requireContext()))
    }

    private companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)

        // Initialize dependencies
        userPreferences = UserPreferences.newInstance(requireContext())
        alarmReceiver = AlarmReceiver()

        setupViews()
        setupUserGreeting()
        setupNotificationObserver()
        observeViewModel()

        return binding.root
    }

    private fun setupViews() {
        // Setup RecyclerView
        adapter = NoteAdapter()
        binding.rvNotes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ListFragment.adapter
        }

        // Setup FAB
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddActivity::class.java))
        }

        // Setup notification bell
        binding.iconBell.setOnClickListener {
            handleNotificationClick()
        }
    }

    private fun setupUserGreeting() {
        greetingJob = viewLifecycleOwner.lifecycleScope.launch {
            val username = userPreferences.userName.firstOrNull() ?: "User"
            binding.textGreet.text = getString(R.string.greeting_text, username)
        }
    }

    private fun setupNotificationObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            userPreferences.isAlarmOnFlow.collect { isAlarmSet ->
                updateNotificationIcon(isAlarmSet)
            }
        }
    }

    private fun updateNotificationIcon(isAlarmSet: Boolean) {
        binding.iconBell.setImageResource(
            if (isAlarmSet) R.drawable.baseline_notifications_active_24
            else R.drawable.baseline_notifications_none_24
        )
    }

    private fun handleNotificationClick() {
        if (checkNotificationPermission()) {
            toggleNotificationState()
        } else {
            requestNotificationPermission()
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun toggleNotificationState() {
        viewLifecycleOwner.lifecycleScope.launch {
            val isAlarmSet = userPreferences.isAlarmOnFlow.firstOrNull() ?: false
            if (isAlarmSet) {
                alarmReceiver.cancelAlarm(requireContext())
                userPreferences.saveAlarmStatus(false)
            } else {
                showTimePicker()
            }
        }
    }

    private fun requestNotificationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            NOTIFICATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun showTimePicker() {
        TimePickerFragment().show(parentFragmentManager, "TimePicker")
    }

    private fun observeViewModel() {
        // Get notes
        viewLifecycleOwner.lifecycleScope.launch {
            val token = userPreferences.userToken.firstOrNull().toString()
            listViewModel.getAllNotes(token)
        }

        // Observe notes
        listViewModel.notes.observe(viewLifecycleOwner) { notes ->
            adapter.setNotes(notes)
        }

        // Observe loading state
        listViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDialogTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        val time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        val message = getString(R.string.notification_message)

        alarmReceiver.setRepeatingAlarm(requireContext(), time, message)

        viewLifecycleOwner.lifecycleScope.launch {
            userPreferences.saveAlarmStatus(true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showTimePicker()
                Toast.makeText(requireContext(), R.string.permission_granted, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        greetingJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}