package za.co.eduvos.codebridge.feature.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.dto.EventDto
import za.co.eduvos.codebridge.ui.Week3Label
import za.co.eduvos.codebridge.databinding.FragmentEventsBinding

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: EventAdapter
    private var all: List<Event> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentEventsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Week3Label.attach(binding.root, "Live events from company partners")
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        
        adapter = EventAdapter(all) { }
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

        fetchEvents()
    }

    private fun fetchEvents() {
        RetrofitClient.getInstance().apiService.getEvents().enqueue(object : Callback<List<EventDto>> {
            override fun onResponse(call: Call<List<EventDto>>, response: Response<List<EventDto>>) {
                if (_binding == null) return
                
                if (response.isSuccessful && response.body() != null) {
                    val eventList = response.body()!!
                    all = eventList.map { dto ->
                        Event(
                            company = "Company ID: ${dto.companyId}", 
                            title = dto.title,
                            description = "Join us for ${dto.title}!",
                            date = dto.eventDate?.take(10) ?: "TBD",
                            venue = "Online / Campus",
                            type = "Tech Talk",
                            tileColor = R.color.cb_tile_lime
                        )
                    }
                    adapter.submit(all)
                    
                    if (all.isEmpty()) {
                        Toast.makeText(requireContext(), "No events posted yet.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<EventDto>>, t: Throwable) {
                if (_binding == null) return
                Toast.makeText(requireContext(), "Failed to load events: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun styleChip(c: TextView, selected: Boolean) {
        c.setBackgroundResource(if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip)
        c.setTextColor(ContextCompat.getColor(requireContext(), if (selected) R.color.cb_bg else R.color.cb_ink))
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
