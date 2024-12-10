package com.heallinkapp.customview

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Patterns
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.heallinkapp.R

class EditTextEmail @JvmOverloads constructor(
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
                if (!s.isNullOrEmpty() && !Patterns.EMAIL_ADDRESS.matcher(s).matches()) {
                    error = context.getString(R.string.invalid_email)
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
}
