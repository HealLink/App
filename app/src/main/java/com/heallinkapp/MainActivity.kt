package com.heallinkapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.heallinkapp.data.local.UserPreferences
import com.heallinkapp.databinding.ActivityMainBinding
import com.heallinkapp.di.Injection
import com.heallinkapp.notification.AlarmReceiver
import com.heallinkapp.ui.auth.LoginActivity
import com.heallinkapp.ui.music.MusicFragment
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), TimePickerFragment.DialogTimeListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private var lastSelectedItemId: Int = R.id.navigation_list


    private lateinit var alarmReceiver: AlarmReceiver


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(com.heallinkapp.R.id.nav_host_fragment_activity_main)
        if (savedInstanceState == null) {
            when {
                intent.getBooleanExtra("openMedical", false) -> {
                    navController.navigate(R.id.navigation_medical)
                    binding.navView.selectedItemId = R.id.navigation_medical
                }
                intent.getBooleanExtra("openMusic", false) -> {
                    navController.navigate(R.id.navigation_music)
                    binding.navView.selectedItemId = R.id.navigation_music
                }
            }
        }


        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_list,
                R.id.navigation_medical,
                R.id.navigation_music
            )
        )


        alarmReceiver = AlarmReceiver()
        lastSelectedItemId = navView.selectedItemId

        binding.navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    showLogoutConfirmationDialog()
                    false // Don't process item selection yet
                }
                else -> {
                    lastSelectedItemId = item.itemId
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
    }

    //    private fun handleToggleNotification() {
//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
//        val menuItem = toolbar.menu.findItem(R.id.menu_toggle_notification)
//
//        if (isNotificationOn) {
//            // Tampilkan dialog untuk mematikan notifikasi
//            AlertDialog.Builder(this)
//                .setTitle("Turn Off Notification")
//                .setMessage("Are you sure you want to turn off the notification?")
//                .setPositiveButton("Yes") { _, _ ->
//                    alarmReceiver.cancelAlarm(this)
//                    isNotificationOn = false
//                    menuItem.setIcon(R.drawable.baseline_notifications_none_24) // Ganti ikon menjadi OFF
//                    Toast.makeText(this, "Notification turned off", Toast.LENGTH_SHORT).show()
//                }
//                .setNegativeButton("No", null)
//                .show()
//        } else {
//            // Tampilkan TimePickerFragment untuk mengatur waktu
//            val timePickerFragment = TimePickerFragment()
//            timePickerFragment.show(supportFragmentManager, "timePicker")
//        }
//    }



    override fun onDialogTimeSet(tag: String?, hourOfDay: Int, minute: Int) {
        // Siapkan time formatter-nya terlebih dahulu
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)

        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val time = dateFormat.format(calendar.time)
        alarmReceiver.setRepeatingAlarm(this, time, "Tell me your story today!")

        // Update notification status
        lifecycleScope.launch {
            val userPreferences = UserPreferences.newInstance(this@MainActivity)
            userPreferences.saveAlarmStatus(true)
        }
        // Update icon to show notification is ON
//        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
//        val menuItem = toolbar.menu.findItem(R.id.menu_toggle_notification)
//        menuItem.setIcon(R.drawable.baseline_notifications_active_24) // Change icon to ON

        Toast.makeText(this, "Notification set for $time", Toast.LENGTH_SHORT).show()
    }

    private fun showLogoutConfirmationDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Confirm Logout")
            .setMessage("Apakah anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                logoutUser()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun logoutUser() {
        val userPreferences = UserPreferences(this)
        lifecycleScope.launch {
            userPreferences.clearPreferences()
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (intent.getStringExtra("destination") == "music") {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MusicFragment())
                .commit()
        }
    }

}