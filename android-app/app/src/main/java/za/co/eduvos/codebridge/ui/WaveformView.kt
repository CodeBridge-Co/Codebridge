package za.co.eduvos.codebridge.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import za.co.eduvos.codebridge.R

class WaveformView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val d = resources.displayMetrics.density
    private val barW = 4 * d
    private val gap = 3 * d
    private val amps = ArrayDeque<Float>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.cb_green)
    }
    private val rect = RectF()

    fun push(value: Float) {
        amps.addLast(value.coerceIn(0.06f, 1f))
        val max = (width / (barW + gap)).toInt().coerceAtLeast(1)
        while (amps.size > max) amps.removeFirst()
        invalidate()
    }

    fun clear() { amps.clear(); invalidate() }

    override fun onDraw(canvas: Canvas) {
        val cy = height / 2f
        val step = barW + gap
        val startX = width - amps.size * step
        amps.forEachIndexed { i, a ->
            val h = a * height
            val x = startX + i * step
            rect.set(x, cy - h / 2, x + barW, cy + h / 2)
            canvas.drawRoundRect(rect, barW / 2, barW / 2, paint)
        }
    }
}
