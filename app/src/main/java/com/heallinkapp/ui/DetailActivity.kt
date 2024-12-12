package com.heallinkapp.ui

import android.graphics.Color
import android.graphics.Color.BLACK
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.heallinkapp.R
import com.heallinkapp.customview.BookTextView
import com.heallinkapp.data.local.Note
import com.heallinkapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    val classMapping = mapOf(
        0 to "Anxiety",
        1 to "Bipolar",
        2 to "Depression",
        3 to "Normal",
        4 to "Personality disorder",
        5 to "Stress",
        6 to "Suicidal"
    )

    val colorMapping = mapOf(
        "Anxiety" to R.color.anxiety_color,
        "Bipolar" to R.color.bipolar_color,
        "Depression" to R.color.depression_color,
        "Normal" to R.color.normal_color,
        "Personality disorder" to R.color.personality_disorder_color,
        "Stress" to R.color.stress_color,
        "Suicidal" to R.color.suicidal_color
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val note = intent.getParcelableExtra<Note>(EXTRA_NOTE)

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title= "Back"

        toolbar.navigationIcon?.setTint(getColor(R.color.black))
        toolbar.setTitleTextColor(BLACK)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val sortedResults = note?.result?.mapIndexed { index, confidence ->
            classMapping[index] to confidence
        }?.sortedByDescending { it.second }

        val highestConfidence = sortedResults?.first()

        val color = colorMapping[highestConfidence?.first] ?: android.R.color.white
        toolbar.setBackgroundColor(getColor(color))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val details = sortedResults?.drop(1)?.joinToString("\n") {
            "${String.format("%.0f", it.second * 100)}% ${it.first}"
        }

        binding.textTitle.text = note?.title
        binding.textDate.text = "Created at ${note?.date}"
        binding.layouContainer.setBackgroundColor(getColor(color))
        val bookTextView: BookTextView = findViewById(R.id.text_story)
        bookTextView.setText(note?.description)
        bookTextView.setLineColor(Color.BLACK)
        bookTextView.setLineThickness(2f)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> {
                showDetailsDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDetailsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_result, null)

        val textDiagnose = dialogView.findViewById<TextView>(R.id.text_diagonose)
        val emotionImageView = dialogView.findViewById<ImageView>(R.id.emotionImageView)
        val percentageTextView = dialogView.findViewById<TextView>(R.id.percentageTextView)
        val labelTextView = dialogView.findViewById<TextView>(R.id.labelTextView)
        val detailsTextView = dialogView.findViewById<TextView>(R.id.detailsTextView)
        val okButton = dialogView.findViewById<Button>(R.id.okButton)

        val note = intent.getParcelableExtra<Note>(EXTRA_NOTE)
        val sortedResults = note?.result?.mapIndexed { index, confidence ->
            classMapping[index] to confidence
        }?.sortedByDescending { it.second }

        val highestConfidence = sortedResults?.first()

        val color = colorMapping[highestConfidence?.first] ?: android.R.color.white
        val drawableResource = when (highestConfidence?.first) {
            "Depression" -> R.drawable.depression
            "Anxiety" -> R.drawable.anxiety
            "Bipolar" -> R.drawable.bipolar
            "Normal" -> R.drawable.normal
            "Personality disorder" -> R.drawable.personality_disorder
            "Stress" -> R.drawable.stress
            "Suicidal" -> R.drawable.suicidal
            else -> R.drawable.heallink_logo
        }

        val details = sortedResults?.drop(1)?.joinToString("\n") {
            "${String.format("%.0f", it.second * 100)}% ${it.first}"
        }

        textDiagnose.text = "Description"
        emotionImageView.setImageResource(drawableResource)
        percentageTextView.text = "${String.format("%.0f", highestConfidence!!.second * 100)}%"
        labelTextView.text = highestConfidence.first
        detailsTextView.text = details

        val dialogBuilder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .setCancelable(true)

        val dialog = dialogBuilder.create()

        okButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }





    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    companion object {
        const val EXTRA_NOTE = "extra_note"
    }
}
