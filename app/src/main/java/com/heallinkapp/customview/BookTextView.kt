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

    private val minLines = 10

    override fun onDraw(canvas: Canvas) {
        val lineHeight = lineHeight
        val actualLines = (height / lineHeight).toInt()
        val lineCount = maxOf(minLines, actualLines)

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

        super.onDraw(canvas)
    }

    fun setLineColor(color: Int) {
        linePaint.color = color
        invalidate()
    }

    fun setLineThickness(thickness: Float) {
        linePaint.strokeWidth = thickness
        invalidate()
    }
}