package com.heallinkapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
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
            R.layout.onboarding_page_note, // Halaman 1
            R.layout.onboarding_page_medical, // Halaman 2
            R.layout.onboarding_page_music // Halaman 3
        )

        // Adapter untuk ViewPager2
        viewPager.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(layouts[viewType], parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {}

            override fun getItemCount(): Int = layouts.size

            override fun getItemViewType(position: Int): Int = position
        }

        // Tambahkan TabLayoutMediator untuk menghubungkan ViewPager2 dengan TabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Konfigurasi tab jika diperlukan
        }.attach()


        // Tampilkan tombol selesai hanya di halaman terakhir
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                finishButton.visibility = if (position == layouts.size - 1) View.VISIBLE else View.GONE
            }
        })

        // Logika untuk tombol selesai
        finishButton.setOnClickListener {
            lifecycleScope.launch {
                val userPreferences = UserPreferences.newInstance(this@OnBoardingActivity)
                userPreferences.saveFirstTime(false)

                // Navigasi ke MainActivity
                startActivity(Intent(this@OnBoardingActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}