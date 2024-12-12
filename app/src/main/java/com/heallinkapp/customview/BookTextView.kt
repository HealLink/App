package com.heallinkapp.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class BookTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val linePaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    private val minLines = 10 // Minimum number of lines to display

    override fun onDraw(canvas: Canvas) {
        // Draw horizontal lines
        val lineHeight = lineHeight
        val actualLines = (height / lineHeight).toInt()
        val lineCount = maxOf(minLines, actualLines)

        // Draw lines
        for (i in 0 until lineCount) {
            val baseline = (i + 1) * lineHeight
            canvas.drawLine(
                paddingLeft.toFloat(),
                baseline.toFloat(),
                (width - paddingRight).toFloat(),
                baseline.toFloat(),
                linePaint
            )
        }

        // Draw text on top of the lines
        super.onDraw(canvas)
    }

    // Optional: Method to customize line color
    fun setLineColor(color: Int) {
        linePaint.color = color
        invalidate()
    }

    // Optional: Method to customize line thickness
    fun setLineThickness(thickness: Float) {
        linePaint.strokeWidth = thickness
        invalidate()
    }
}