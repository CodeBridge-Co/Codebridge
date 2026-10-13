package za.co.eduvos.codebridge.feature.sandbox

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.data.SubTopic
import za.co.eduvos.codebridge.databinding.ItemSubtopicBinding

class SubTopicAdapter(
    private var items: List<SubTopic>,
    private val onClick: (SubTopic) -> Unit
) : RecyclerView.Adapter<SubTopicAdapter.VH>() {

    class VH(val b: ItemSubtopicBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemSubtopicBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val st = items[pos]
        h.b.tvSubtopicName.text = st.name
        h.b.tvCount.text = "${st.solvedCount} / ${st.totalCount}"

        val pct = if (st.totalCount > 0) (st.solvedCount * 100 / st.totalCount) else 0
        h.b.tvPercent.text = "$pct%"

        val tileColor = when (pos % 5) {
            0 -> R.color.cb_cat_sky
            1 -> R.color.cb_cat_peach
            2 -> R.color.cb_cat_lilac
            3 -> R.color.cb_tile_mint
            else -> R.color.cb_tile_lime
        }
        h.b.tileContainer.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(h.b.root.context, tileColor)
        )

        val iconRes = when (st.iconType) {
            "branch" -> R.drawable.ic_cb_branch
            "chart" -> R.drawable.ic_cb_chart
            "spark" -> R.drawable.ic_cb_spark
            else -> R.drawable.ic_cb_code
        }
        h.b.ivIcon.setImageResource(iconRes)

        h.b.root.setOnClickListener { onClick(st) }
    }

    override fun getItemCount() = items.size

    fun submit(newList: List<SubTopic>) {
        items = newList
        notifyDataSetChanged()
    }
}
