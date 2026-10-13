package za.co.eduvos.codebridge.data

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.databinding.ItemChallengeBinding

class ChallengeAdapter(
    private var items: List<Challenge>,
    private val onClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.VH>() {

    class VH(val b: ItemChallengeBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemChallengeBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val c = items[pos]
        h.b.tvTitle.text = "${c.id}. ${c.title}"
        h.b.tvDifficulty.text = c.difficulty
        h.b.tvCategory.text = c.category
        h.b.tvAcceptance.text = "Acceptance: ${c.acceptanceRate}"

        val diffColor = when (c.difficulty) {
            "Easy" -> R.color.cb_green_dark
            "Medium" -> R.color.cb_tile_peach
            "Hard" -> R.color.cb_red
            else -> R.color.cb_green_dark
        }
        h.b.tvDifficulty.setTextColor(ContextCompat.getColor(h.b.root.context, diffColor))

        if (c.isCompleted) {
            h.b.ivStatus.setImageResource(R.drawable.ic_cb_check)
            h.b.ivStatus.setColorFilter(ContextCompat.getColor(h.b.root.context, R.color.cb_green_dark))
        } else {
            h.b.ivStatus.setImageResource(R.drawable.ic_cb_code)
            h.b.ivStatus.setColorFilter(ContextCompat.getColor(h.b.root.context, R.color.cb_ink))
        }

        h.b.root.setOnClickListener { onClick(c) }
    }

    override fun getItemCount() = items.size

    fun submit(newList: List<Challenge>) {
        items = newList
        notifyDataSetChanged()
    }
}
