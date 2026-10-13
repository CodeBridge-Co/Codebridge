package za.co.eduvos.codebridge.feature.company

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import za.co.eduvos.codebridge.data.CompanyChallenge
import za.co.eduvos.codebridge.databinding.ItemCompanyChallengeBinding

class CompanyChallengeAdapter(
    private var items: List<CompanyChallenge>,
    private val onClick: (CompanyChallenge) -> Unit
) : RecyclerView.Adapter<CompanyChallengeAdapter.VH>() {

    class VH(val b: ItemCompanyChallengeBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemCompanyChallengeBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]
        h.b.tvCompany.text = item.companyName
        h.b.tvTitle.text = item.title
        h.b.tvDesc.text = item.description
        h.b.tvDifficulty.text = item.difficulty
        h.b.tvLanguage.text = item.language
        h.b.root.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun submit(newList: List<CompanyChallenge>) {
        items = newList
        notifyDataSetChanged()
    }
}
