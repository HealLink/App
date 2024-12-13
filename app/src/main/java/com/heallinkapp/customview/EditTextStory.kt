package com.heallinkapp.customview

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.heallinkapp.R

class EditTextStory @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    init {
        setTextColor(context.getColor(R.color.black))
        setHintTextColor(context.getColor(R.color.black))

        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak digunakan
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    val wordCount = countWords(s.toString())
                    when {
                        wordCount < 10 -> {
                            error = context.getString(R.string.min_10_words)
                        }
                        wordCount > 200 -> {
                            error = context.getString(R.string.max_200_words)
                        }
                        else -> {
                            // You can remove any previous error if the text is valid
                            error = null
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Tidak digunakan
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    private fun countWords(text: String): Int {
        val words = text.trim().split("\\s+".toRegex())
        return words.size
    }
}