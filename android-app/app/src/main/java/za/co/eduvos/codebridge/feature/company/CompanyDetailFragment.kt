package za.co.eduvos.codebridge.feature.company

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.dto.ApiResponse
import za.co.eduvos.codebridge.core.network.dto.LeaderboardDto
import za.co.eduvos.codebridge.core.network.dto.ProblemDto
import za.co.eduvos.codebridge.data.Challenge
import za.co.eduvos.codebridge.data.ChallengeAdapter
import za.co.eduvos.codebridge.databinding.FragmentCompanyDetailBinding
import za.co.eduvos.codebridge.feature.problems.ProblemsFragment

class CompanyDetailFragment : Fragment() {

    private var _binding: FragmentCompanyDetailBinding? = null
    private val binding get() = _binding!!

    private var companyId: Int = 1
    private var companyName: String = "Company"
    private var companyAbout: String = ""

    private var allChallenges: List<Challenge> = emptyList()
    private lateinit var adapter: ChallengeAdapter

    companion object {
        fun newInstance(companyId: Int, companyName: String, aboutText: String): CompanyDetailFragment {
            val fragment = CompanyDetailFragment()
            val args = Bundle()
            args.putInt("company_id", companyId)
            args.putString("company_name", companyName)
            args.putString("about_text", aboutText)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        companyId = arguments?.getInt("company_id") ?: 1
        companyName = arguments?.getString("company_name") ?: "Company"
        companyAbout = arguments?.getString("about_text") ?: "Explore challenges from $companyName."
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompanyDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.tvHeaderLogo.text = companyName.lowercase()
        binding.tvCompanyTitle.text = "$companyName Challenges"
        binding.tvCompanyAbout.text = companyAbout

        adapter = ChallengeAdapter(emptyList()) { }
        binding.rvCompanyChallenges.adapter = adapter

        binding.chipAll.setOnClickListener { filter("All") }
        binding.chipEasy.setOnClickListener { filter("Easy") }
        binding.chipMedium.setOnClickListener { filter("Medium") }
        binding.chipHard.setOnClickListener { filter("Hard") }

        binding.tabChallenges.setOnClickListener { switchTab("CHALLENGES") }
        binding.tabLeaderboard.setOnClickListener { switchTab("LEADERBOARD") }

        fetchLiveChallenges()
    }

    private fun switchTab(tab: String) {
        if (tab == "CHALLENGES") {
            binding.tabChallenges.setBackgroundResource(R.drawable.bg_chip_selected)
            binding.tabChallenges.setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_bg))
            binding.tabLeaderboard.setBackgroundResource(0)
            binding.tabLeaderboard.setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_ink))

            binding.llFilters.visibility = View.VISIBLE
            binding.rvCompanyChallenges.visibility = View.VISIBLE
            binding.llLeaderboardContainer.visibility = View.GONE
        } else {
            binding.tabLeaderboard.setBackgroundResource(R.drawable.bg_chip_selected)
            binding.tabLeaderboard.setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_bg))
            binding.tabChallenges.setBackgroundResource(0)
            binding.tabChallenges.setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_ink))

            binding.llFilters.visibility = View.GONE
            binding.rvCompanyChallenges.visibility = View.GONE
            binding.llLeaderboardContainer.visibility = View.VISIBLE
            
            fetchLeaderboard()
        }
    }

    private fun fetchLiveChallenges() {
        RetrofitClient.getInstance().apiService.getProblems().enqueue(object : Callback<ApiResponse<List<ProblemDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<ProblemDto>>>, response: Response<ApiResponse<List<ProblemDto>>>) {
                if (_binding == null) return
                
                val problems = response.body()?.data
                if (response.isSuccessful && problems != null) {
                    val companyProblems = problems.filter { it.companyId == companyId }
                    
                    allChallenges = companyProblems.map {
                        Challenge(it.problemId, it.title, it.difficultyLevel, "General", "0%")
                    }

                    adapter = ChallengeAdapter(allChallenges) { challenge ->
                        (requireActivity() as MainActivity).open(
                            ProblemsFragment.newInstance(
                                challenge.id, 
                                challenge.title, 
                                challenge.difficulty, 
                                challenge.category
                            )
                        )
                    }
                    binding.rvCompanyChallenges.adapter = adapter
                    
                    if (allChallenges.isEmpty()) {
                        Toast.makeText(requireContext(), "No challenges posted by $companyName yet.", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<ProblemDto>>>, t: Throwable) {
                if (_binding == null) return
                Toast.makeText(requireContext(), "Failed to load challenges: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchLeaderboard() {
        binding.llLeaderboardItems.removeAllViews()
        
        val loadingTv = TextView(requireContext()).apply {
            text = "Loading leaderboard..."
            setPadding(16, 16, 16, 16)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_text_secondary))
        }
        binding.llLeaderboardItems.addView(loadingTv)

        // 🟢 FIX: Expect ApiResponse wrapper here
        RetrofitClient.getInstance().apiService.getLeaderboard().enqueue(object : Callback<ApiResponse<List<LeaderboardDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<LeaderboardDto>>>, response: Response<ApiResponse<List<LeaderboardDto>>>) {
                if (_binding == null) return
                binding.llLeaderboardItems.removeAllViews()
                
                val list = response.body()?.data
                if (response.isSuccessful && list != null) {
                    if (list.isEmpty()) {
                        val emptyTv = TextView(requireContext()).apply {
                            text = "No leaderboard data available yet."
                            setPadding(16, 16, 16, 16)
                            setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_text_secondary))
                        }
                        binding.llLeaderboardItems.addView(emptyTv)
                    } else {
                        list.forEachIndexed { index, entry ->
                            val row = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding(16, 24, 16, 24)
                                gravity = Gravity.CENTER_VERTICAL
                            }
                            
                            val rankTv = TextView(requireContext()).apply {
                                text = "${index + 1}."
                                textSize = 16f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                setPadding(0, 0, 24, 0)
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_green_dark))
                            }
                            
                            val infoLayout = LinearLayout(requireContext()).apply {
                                orientation = LinearLayout.VERTICAL
                                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            }
                            
                            val nameTv = TextView(requireContext()).apply {
                                text = entry.hashId
                                textSize = 14f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_ink))
                            }
                            
                            val streakTv = TextView(requireContext()).apply {
                                text = "${entry.streak} day streak 🔥"
                                textSize = 12f
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_text_secondary))
                            }
                            
                            val xpTv = TextView(requireContext()).apply {
                                text = "${entry.xp} XP"
                                textSize = 14f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_green_dark))
                            }
                            
                            infoLayout.addView(nameTv)
                            infoLayout.addView(streakTv)
                            
                            row.addView(rankTv)
                            row.addView(infoLayout)
                            row.addView(xpTv)
                            
                            binding.llLeaderboardItems.addView(row)
                            
                            val divider = View(requireContext()).apply {
                                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2)
                                setBackgroundColor(Color.parseColor("#E0E0E0"))
                            }
                            binding.llLeaderboardItems.addView(divider)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<LeaderboardDto>>>, t: Throwable) {
                if (_binding == null) return
                binding.llLeaderboardItems.removeAllViews()
                val errorTv = TextView(requireContext()).apply {
                    text = "Failed to load leaderboard."
                    setPadding(16, 16, 16, 16)
                }
                binding.llLeaderboardItems.addView(errorTv)
            }
        })
    }

    private fun filter(difficulty: String) {
        val filtered = if (difficulty == "All") allChallenges else allChallenges.filter {
            it.difficulty.equals(difficulty, ignoreCase = true)
        }
        adapter.submit(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
