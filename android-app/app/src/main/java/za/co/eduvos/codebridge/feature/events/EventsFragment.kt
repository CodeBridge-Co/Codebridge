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
import za.co.eduvos.codebridge.core.network.dto.ApiResponse 

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: EventAdapter

    // We will populate this from the backend
    private var all: List<Event> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentEventsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Week3Label.attach(binding.root, "Live events from company partners")
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

        fetchEvents()
    }

    private fun fetchEvents() {
    // 1. Match the exact return type of ApiService (Call<ApiResponse<List<EventDto>>>)
    RetrofitClient.getInstance().apiService.getEvents().enqueue(object : Callback<za.co.eduvos.codebridge.core.network.dto.ApiResponse<List<EventDto>>> {
        override fun onResponse(
            call: Call<za.co.eduvos.codebridge.core.network.dto.ApiResponse<List<EventDto>>>,
            response: Response<za.co.eduvos.codebridge.core.network.dto.ApiResponse<List<EventDto>>>
        ) {
            if (_binding == null) return
            
            // 2. Unwrap the data from the ApiResponse
            val eventList = response.body()?.data
            
            if (response.isSuccessful && eventList != null) {
                all = eventList.map { dto ->
                    Event(
                        company = "Company ID: ${dto.companyId}", // Fixed: camelCase
                        title = dto.title,
                        description = "Join us for ${dto.title}!",
                        date = dto.eventDate?.take(10) ?: "TBD", // Fixed: camelCase & null safety
                        venue = "Online / Campus",
                        type = "Tech Talk", // Default type
                        tileColor = R.color.cb_tile_lime
                    )
                }
                adapter.submit(all)
            }
        }

        override fun onFailure(
            call: Call<za.co.eduvos.codebridge.core.network.dto.ApiResponse<List<EventDto>>>,
            t: Throwable
        ) {
            if (_binding == null) return
            Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
        }
    })
}

    private fun styleChip(c: TextView, selected: Boolean) {
        c.setBackgroundResource(if (selected) R.drawable.bg_chip_selected else R.drawable.bg_chip)
        c.setTextColor(ContextCompat.getColor(requireContext(), if (selected) R.color.cb_bg else R.color.cb_ink))
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
