package za.co.eduvos.codebridge.ui

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import za.co.eduvos.codebridge.R

/**
 * Marks a screen that still shows demo data until its Week 3 engine is built.
 * Adds a small notice at the top of the screen's vertical content column.
 */
object Week3Label {

    fun attach(root: View, message: String) {
        val column = findColumn(root) ?: return
        val ctx = root.context
        val dp = ctx.resources.displayMetrics.density

        val label = TextView(ctx).apply {
            text = "Week 3  ·  $message"
            textSize = 11f
            setTextColor(ContextCompat.getColor(ctx, R.color.cb_dark_green))
            typeface = ResourcesCompat.getFont(ctx, R.font.nunito_bold)
            setBackgroundResource(R.drawable.bg_week3_label)
            setPadding((12 * dp).toInt(), (6 * dp).toInt(), (12 * dp).toInt(), (6 * dp).toInt())
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                val side = (16 * dp).toInt()
                setMargins(side, (4 * dp).toInt(), side, (8 * dp).toInt())
            }
        }
        column.addView(label, 0)
    }

    /** The screen's vertical LinearLayout: the root itself, or the child of a scroll view. */
    private fun findColumn(root: View): LinearLayout? {
        if (root is LinearLayout && root.orientation == LinearLayout.VERTICAL) return root
        if (root is ViewGroup && root.childCount > 0) {
            val child = root.getChildAt(0)
            if (child is LinearLayout && child.orientation == LinearLayout.VERTICAL) return child
        }
        return null
    }
}
