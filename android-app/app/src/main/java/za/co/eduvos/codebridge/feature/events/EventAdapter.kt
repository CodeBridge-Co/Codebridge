package za.co.eduvos.codebridge.feature.events

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import za.co.eduvos.codebridge.databinding.ItemEventBinding

class EventAdapter(
    private var items: List<Event>,
    private val onClick: (Event) -> Unit = {}
) : RecyclerView.Adapter<EventAdapter.VH>() {

    class VH(val b: ItemEventBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(p: ViewGroup, t: Int) =
        VH(ItemEventBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) {
        val e = items[pos]
        h.b.tvCompany.text = e.company
        h.b.tvTitle.text = e.title
        h.b.tvDesc.text = e.description
        h.b.tvDate.text = e.date
        h.b.tvVenue.text = e.venue
        h.b.tvType.text = e.type
        ViewCompat.setBackgroundTintList(
            h.b.tile, ColorStateList.valueOf(ContextCompat.getColor(h.b.root.context, e.tileColor))
        )
        h.b.root.setOnClickListener { onClick(e) }
    }

    override fun getItemCount() = items.size

    fun submit(list: List<Event>) { items = list; notifyDataSetChanged() }
}
