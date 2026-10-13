package za.co.eduvos.codebridge.ui

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import za.co.eduvos.codebridge.R

/** Code - Git - Interview - Results progress header. Call setStep(0..3). */
class StepperView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val labels = listOf("Code", "Git", "Interview", "Results")

    init {
        orientation = HORIZONTAL
        gravity = Gravity.TOP
        setStep(0)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun color(id: Int) = ContextCompat.getColor(context, id)

    fun setStep(current: Int) {
        removeAllViews()
        labels.forEachIndexed { i, label ->
            addView(stepView(i, label, current))
            if (i < labels.size - 1) addView(lineView(i < current))
        }
    }

    private fun stepView(i: Int, label: String, current: Int): View {
        val col = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            layoutParams = LayoutParams(dp(64), LayoutParams.WRAP_CONTENT)
        }
        val done = i < current
        val active = i == current
        val circle = TextView(context).apply {
            layoutParams = LayoutParams(dp(30), dp(30))
            gravity = Gravity.CENTER
            text = if (done) "✓" else "${i + 1}"
            textSize = 12f
            typeface = ResourcesCompat.getFont(context, R.font.nunito_extrabold)
            setTextColor(color(if (done || active) R.color.cb_bg else R.color.cb_text_secondary))
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color(if (done || active) R.color.cb_ink else R.color.cb_card))
                if (active) setStroke(dp(2), color(R.color.cb_green))
            }
        }
        val name = TextView(context).apply {
            text = label
            textSize = 11f
            setPadding(0, dp(6), 0, 0)
            typeface = ResourcesCompat.getFont(
                context, if (done || active) R.font.nunito_bold else R.font.nunito_regular
            )
            setTextColor(color(if (done || active) R.color.cb_ink else R.color.cb_text_secondary))
        }
        col.addView(circle); col.addView(name)
        return col
    }

    private fun lineView(done: Boolean) = View(context).apply {
        layoutParams = LayoutParams(0, dp(2), 1f).apply { topMargin = dp(14) }
        setBackgroundColor(color(if (done) R.color.cb_green else R.color.cb_line))
    }
}
