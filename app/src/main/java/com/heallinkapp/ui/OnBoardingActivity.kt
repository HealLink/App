package com.heallinkapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.heallinkapp.MainActivity
import com.heallinkapp.R
import com.heallinkapp.data.local.UserPreferences
import kotlinx.coroutines.launch

class OnBoardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        val viewPager: ViewPager2 = findViewById(R.id.viewPager)
        val tabLayout: TabLayout = findViewById(R.id.tabDots)
        val finishButton: Button = findViewById(R.id.finishButton)

        val layouts = listOf(
            R.layout.onboarding_page_note, // Layout untuk halaman 1
            R.layout.onboarding_page_medical, // Layout untuk halaman 2
            R.layout.onboarding_page_music // Layout untuk halaman 3
        )

        // Setel ViewPager dengan layout
        viewPager.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
                val view = layoutInflater.inflate(layouts[viewType], parent, false)
                return object : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {}

            override fun getItemCount(): Int = layouts.size

            override fun getItemViewType(position: Int): Int = position
        }

        // Tambahkan TabLayoutMediator untuk indikator tab
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        // Logika untuk tombol selesai
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                finishButton.visibility = if (position == layouts.size - 1) View.VISIBLE else View.GONE
            }
        })

        finishButton.setOnClickListener {
            // Simpan status isFirstTime menjadi false
            lifecycleScope.launch {
                val userPreferences = UserPreferences.newInstance(this@OnBoardingActivity)
                userPreferences.saveFirstTime(false)

                // Navigasi ke MainActivity
                startActivity(Intent(this@OnBoardingActivity, MainActivity::class.java))
                finish() // Tutup aktivitas onboarding
            }
        }
    }
}