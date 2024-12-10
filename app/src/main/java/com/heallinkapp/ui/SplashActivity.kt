package com.heallinkapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.heallinkapp.MainActivity
import com.heallinkapp.R
import com.heallinkapp.databinding.ActivitySplashBinding
import com.heallinkapp.di.Injection
import com.heallinkapp.ui.auth.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supportActionBar?.hide()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigateToNextScreenWithDelay()
    }

    private fun navigateToNextScreenWithDelay() {
        val userRepository = Injection.provideUserRepository(this)

        lifecycleScope.launch {
            delay(2000)

            val token = userRepository.userToken.first()
            val nextActivity = if (!token.isNullOrEmpty()) {
                MainActivity::class.java
            } else {
                LoginActivity::class.java
            }

            startActivity(Intent(this@SplashActivity, nextActivity))
            finish()
        }
    }
}
