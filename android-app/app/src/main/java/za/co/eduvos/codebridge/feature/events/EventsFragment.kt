package za.co.eduvos.codebridge.feature.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.ui.Week3Label
import za.co.eduvos.codebridge.databinding.FragmentEventsBinding

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: EventAdapter

    // TODO: replace with a call to your backend (Retrofit / Firebase)
    private val all = listOf(
        Event("Northstar Labs", "Developer Hackathon 2026", "Learn, build and connect with engineers working on real products.", "OCT 28", "Campus Hub", "Hackathon", R.color.cb_tile_lime),
        Event("Kinetic Systems", "Tech Talk: AI in African Enterprise", "How local teams are shipping AI features to real customers.", "NOV 06", "Online", "Tech Talk", R.color.cb_tile_sky),
        Event("Openframe", "Engineering Better Together", "A hands-on session on code review, testing and team habits.", "NOV 12", "Lab 4", "Workshop", R.color.cb_tile_peach),
        Event("Circuit House", "IoT Innovation Challenge", "Prototype a connected device with help from mentors.", "NOV 19", "Innovation Lab", "Challenge", R.color.cb_tile_mint)
    )

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentEventsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Week3Label.attach(binding.root, "live events coming. Sample data shown")
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        adapter = EventAdapter(all) { /* TODO: open event details */ }
        binding.rvEvents.adapter = adapter

        val chips = listOf(
            binding.chipEvt0 to null, binding.chipEvt1 to "Hackathon",
            binding.chipEvt2 to "Workshop", binding.chipEvt3 to "Tech Talk"
        )
        chips.forEach { (chip, type) ->
            chip.setOnClickListener {
                chips.forEach { (c, _) -> styleChip(c, c == chip) }
                adapter.submit(if (type == null) all else all.filter { it.type == type })
            }
        }
    }

    private fun styleChip(c: TextView, selected: Boolean) {
        c.setBackgroundResource(if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip)
        c.setTextColor(ContextCompat.getColor(requireContext(), if (selected) R.color.cb_bg else R.color.cb_ink))
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
