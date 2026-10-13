package za.co.eduvos.codebridge.feature.company

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.data.Company
import za.co.eduvos.codebridge.databinding.ItemCompanyBinding

class CompanyAdapter(
    private var items: List<Company>,
    private val onClick: (Company) -> Unit
) : RecyclerView.Adapter<CompanyAdapter.VH>() {

    class VH(val b: ItemCompanyBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemCompanyBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val c = items[pos]
        h.b.tvCompanyName.text = c.name
        h.b.tvCompanyTags.text = c.tags
        h.b.tvChallengeCount.text = "${c.challengeCount} challenges"
        h.b.tvLogoText.text = c.name.take(3).uppercase()

        val tileColor = when (pos % 4) {
            0 -> R.color.cb_tile_lime
            1 -> R.color.cb_tile_sky
            2 -> R.color.cb_tile_peach
            else -> R.color.cb_tile_mint
        }
        h.b.companyTile.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(h.b.root.context, tileColor)
        )

        h.b.root.setOnClickListener { onClick(c) }
    }

    override fun getItemCount() = items.size

    fun submit(newList: List<Company>) {
        items = newList
        notifyDataSetChanged()
    }
}
