package com.heallinkapp.ui

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.heallinkapp.R

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)

        // Apply system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

        // Find the TextView to display the result
        val resultTextView = findViewById<TextView>(R.id.resultTextView)

        // Check if the resultArray is not null and display the sorted results
        if (resultArray != null) {
            // Pair each confidence value with its class name
            val sortedResults = resultArray.mapIndexed { index, confidence ->
                classMapping[index] to confidence
            }.sortedByDescending { it.second }

            val resultText = sortedResults.joinToString(separator = "\n") {
                "${it.first}: ${String.format("%.2f", it.second)}"
            }

            resultTextView.text = resultText
        } else {
            resultTextView.text = "No result data found."
        }
    }
}