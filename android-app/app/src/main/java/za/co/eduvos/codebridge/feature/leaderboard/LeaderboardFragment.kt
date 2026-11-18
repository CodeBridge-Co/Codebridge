package za.co.eduvos.codebridge.feature.leaderboard

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.dto.LeaderboardDto
import za.co.eduvos.codebridge.core.scoring.LeaderboardRank
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.data.UserIdentity
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
        val shown = item.hashId.take(8).uppercase()
        holder.binding.tvHashId.text = if (item.isCurrentUser) "You: $shown" else "Hash: $shown"
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
    private var myHashId: String? = null
    private var myDisplayId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.btnBack.visibility = View.GONE

        myHashId = UserIdentity.hashId(requireContext())
        myDisplayId = UserIdentity.displayId(requireContext())

        allEntries = mutableListOf()
        adapter = LeaderboardAdapter(allEntries)
        binding.rvLeaderboard.adapter = adapter
        binding.tabFriends.visibility = View.GONE // there is no friends feature yet
        loadLeaderboard()

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
            val me = allEntries.indexOfFirst { it.isCurrentUser }
            if (me < 0) {
                adapter.submit(emptyList())
                Toast.makeText(requireContext(), "You are not on the leaderboard yet. Complete a challenge to earn XP.", Toast.LENGTH_LONG).show()
            } else {
                adapter.submit(allEntries.subList(maxOf(0, me - 2), minOf(allEntries.size, me + 3)))
            }
        }
    }

    /** Loads the leaderboard from the server. Rank, level and points come from the XP. */
    private fun loadLeaderboard() {
        RetrofitClient.getInstance().apiService.getLeaderboard().enqueue(object : Callback<List<LeaderboardDto>> {
            override fun onResponse(call: Call<List<LeaderboardDto>>, response: Response<List<LeaderboardDto>>) {
                if (_binding == null) return
                val list = response.body()
                if (!response.isSuccessful || list == null) {
                    Toast.makeText(requireContext(), "Could not load the leaderboard (error ${response.code()})", Toast.LENGTH_LONG).show()
                    return
                }
                allEntries = list.map {
                    LeaderboardEntry(
                        rank = LeaderboardRank.rankOf(list, it.hashId, null),
                        hashId = it.hashId,
                        level = LeaderboardRank.levelFor(it.xp),
                        points = it.xp,
                        streakDays = it.streak,
                        isCurrentUser = LeaderboardRank.isMe(it.hashId, myHashId, myDisplayId)
                    )
                }.toMutableList()
                adapter.submit(allEntries)
                if (allEntries.isEmpty()) {
                    Toast.makeText(requireContext(), "No one is on the leaderboard yet.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<LeaderboardDto>>, t: Throwable) {
                if (_binding == null) return
                Toast.makeText(requireContext(), "Could not load the leaderboard: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
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
