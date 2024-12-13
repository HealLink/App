package com.heallinkapp.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.heallinkapp.MainActivity
import com.heallinkapp.R

class ResultActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18s", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)

        overridePendingTransition(R.anim.slide_up, R.anim.stay)

        setupViews()
    }

    private fun setupViews() {
        val percentageTextView = findViewById<TextView>(R.id.percentageTextView)
        val labelTextView = findViewById<TextView>(R.id.labelTextView)
        val detailsTextView = findViewById<TextView>(R.id.detailsTextView)
        val emotionImageView = findViewById<ImageView>(R.id.emotionImageView)
        val hospitalButton = findViewById<Button>(R.id.hospitalButton)
        val playButton = findViewById<Button>(R.id.playButton)

        processResults(percentageTextView, labelTextView, detailsTextView, emotionImageView)
        setupButtonListeners(hospitalButton, playButton)
    }

    private fun processResults(
        percentageTextView: TextView,
        labelTextView: TextView,
        detailsTextView: TextView,
        emotionImageView: ImageView
    ) {
        val resultArray = intent.getFloatArrayExtra("RESULT_ARRAY")

        val classMapping = mapOf(
            0 to "Anxiety",
            1 to "Bipolar",
            2 to "Depression",
            3 to "Normal",
            4 to "Personality disorder",
            5 to "Stress",
            6 to "Suicidal"
        )

        val recommendationMapping = mapOf(
            "Anxiety" to R.string.anxiety_recommendation,
            "Bipolar" to R.string.bipolar_recommendation,
            "Depression" to R.string.depression_recommendation,
            "Normal" to R.string.normal_recommendation,
            "Personality disorder" to R.string.personality_disorder_recommendation,
            "Stress" to R.string.stress_recommendation,
            "Suicidal" to R.string.suicidal_recommendation
        )

        if (resultArray != null) {
            val sortedResults = resultArray.mapIndexed { index, confidence ->
                classMapping[index] to confidence
            }.sortedByDescending { it.second }

            val highestConfidence = sortedResults.first()
            val details = sortedResults.drop(1).joinToString("\n") {
                "${String.format("%.0f", it.second * 100)}% ${it.first}"
            }

            // Update UI elements
            percentageTextView.text = "${String.format("%.0f", highestConfidence.second * 100)}%"
            labelTextView.text = highestConfidence.first
            detailsTextView.text = details

            // Update recommendation text
            findViewById<TextView>(R.id.textRecomendation).text =
                getString(recommendationMapping[highestConfidence.first] ?: R.string.normal_recommendation)

            // Set emotion icon based on the result
            val drawableResource = when (highestConfidence.first) {
                "Depression" -> R.drawable.depression
                "Anxiety" -> R.drawable.anxiety
                "Bipolar" -> R.drawable.bipolar
                "Normal" -> R.drawable.normal
                "Personality disorder" -> R.drawable.personality_disorder
                "Stress" -> R.drawable.stress
                "Suicidal" -> R.drawable.suicidal
                else -> R.drawable.heallink_logo
            }
            emotionImageView.setImageResource(drawableResource)
        } else {
            labelTextView.text = "No data available"
        }
    }

    private fun setupButtonListeners(hospitalButton: Button, playButton: Button) {
        hospitalButton.setOnClickListener {
            navigateToMain("openMedical")
        }

        playButton.setOnClickListener {
            navigateToMain("openMusic")
        }
    }

    private fun navigateToMain(extra: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(extra, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(extra,true)
        }
        startActivity(intent)
        overridePendingTransition(R.anim.stay, R.anim.slide_down)

    }

    override fun finish() {
        super.finish()
        // Animate exit
        overridePendingTransition(R.anim.stay, R.anim.slide_down)
    }

    companion object {
        private const val TAG = "ResultActivity"
    }
}