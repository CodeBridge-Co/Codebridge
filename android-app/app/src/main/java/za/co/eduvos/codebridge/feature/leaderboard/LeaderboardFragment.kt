package za.co.eduvos.codebridge.feature.leaderboard

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.backend.HashIdManager
import za.co.eduvos.codebridge.databinding.FragmentLeaderboardBinding
import za.co.eduvos.codebridge.databinding.ItemLeaderboardEntryBinding

data class LeaderboardEntry(
    val rank: Int,
    val hashId: String,
    val level: Int,
    val points: Int,
    val streakDays: Int,
    val isCurrentUser: Boolean = false
)

class LeaderboardAdapter(private var list: List<LeaderboardEntry>) :
    RecyclerView.Adapter<LeaderboardAdapter.VH>() {

    class VH(val binding: ItemLeaderboardEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemLeaderboardEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]
        holder.binding.tvRank.text = "#${item.rank}"
        holder.binding.tvHashId.text = if (item.isCurrentUser) "You: ${item.hashId}" else "Hash: ${item.hashId}"
        holder.binding.tvLevelPoints.text = "Level ${item.level}  ·  ${item.points} pts"
        holder.binding.tvStreak.text = "🔥 ${item.streakDays}d"

        if (item.isCurrentUser) {
            holder.binding.root.setBackgroundResource(R.drawable.bg_chip_selected)
            holder.binding.tvHashId.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.cb_bg))
            holder.binding.tvLevelPoints.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.cb_bg))
            holder.binding.tvStreak.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.cb_green_light))
        } else {
            holder.binding.root.setBackgroundResource(R.drawable.bg_card_outline)
            holder.binding.tvHashId.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.cb_ink))
            holder.binding.tvLevelPoints.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.cb_text_secondary))
            holder.binding.tvStreak.setTextColor(Color.parseColor("#E65100"))
        }
    }

    override fun getItemCount() = list.size

    fun submit(newList: List<LeaderboardEntry>) {
        list = newList
        notifyDataSetChanged()
    }
}

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    private var allEntries = mutableListOf<LeaderboardEntry>()
    private lateinit var adapter: LeaderboardAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        val myHashId = HashIdManager.getOrGenerateHashId(requireContext())

        // Populate sample leaderboard matching screenshot 2
        allEntries = mutableListOf(
            LeaderboardEntry(1, "7F3A9C2B", 12, 980, 12),
            LeaderboardEntry(2, "4E8D1F6A", 11, 870, 9),
            LeaderboardEntry(3, "A6C2D9E4", 10, 760, 7),
            LeaderboardEntry(4, "B3F7A1C0", 9, 690, 5),
            LeaderboardEntry(5, "D2E6F8B7", 8, 620, 4),
            LeaderboardEntry(6, "C8A1D3F9", 8, 590, 3),
            LeaderboardEntry(7, "F1D3C5A7", 7, 540, 2),
            LeaderboardEntry(8, "9B7E2D6F", 7, 510, 2),
            LeaderboardEntry(42, myHashId, 12, 980, 12, isCurrentUser = true)
        )

        adapter = LeaderboardAdapter(allEntries)
        binding.rvLeaderboard.adapter = adapter

        // Search bar listener: filter entries by typed Hash ID in real time!
        binding.etSearchHash.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.tabGlobal.setOnClickListener { styleTab(binding.tabGlobal); adapter.submit(allEntries) }
        binding.tabMyRank.setOnClickListener {
            styleTab(binding.tabMyRank)
            val myRankEntries = allEntries.filter { it.isCurrentUser || it.rank <= 5 }
            adapter.submit(myRankEntries)
        }
        binding.tabFriends.setOnClickListener {
            styleTab(binding.tabFriends)
            adapter.submit(allEntries.take(3))
        }
    }

    private fun filter(query: String) {
        val filtered = if (query.isEmpty()) {
            allEntries
        } else {
            allEntries.filter { it.hashId.contains(query, ignoreCase = true) }
        }
        adapter.submit(filtered)
    }

    private fun styleTab(selected: TextView) {
        val tabs = listOf(binding.tabGlobal, binding.tabMyRank, binding.tabFriends)
        tabs.forEach {
            if (it == selected) {
                it.setBackgroundResource(R.drawable.bg_chip_selected)
                it.setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_bg))
            } else {
                it.background = null
                it.setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_ink))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
