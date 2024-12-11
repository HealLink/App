package com.heallinkapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.heallinkapp.R

class ResultActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n", "DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)

        // Retrieve the FloatArray from the Intent
        val resultArray = intent.getFloatArrayExtra("RESULT_ARRAY")

        // Define the class mapping
        val classMapping = mapOf(
            0 to "Anxiety",
            1 to "Bipolar",
            2 to "Depression",
            3 to "Normal",
            4 to "Personality disorder",
            5 to "Stress",
            6 to "Suicidal"
        )

        val percentageTextView = findViewById<TextView>(R.id.percentageTextView)
        val labelTextView = findViewById<TextView>(R.id.labelTextView)
        val detailsTextView = findViewById<TextView>(R.id.detailsTextView)
        val emotionImageView = findViewById<ImageView>(R.id.emotionImageView)

        if (resultArray != null) {
            // Sort results and get the most likely label
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
}
