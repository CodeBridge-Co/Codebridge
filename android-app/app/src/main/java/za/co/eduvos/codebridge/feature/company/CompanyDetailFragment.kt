package za.co.eduvos.codebridge.feature.company

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.core.network.RetrofitClient
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
        // UPDATED: Now requires companyId so we can fetch the right data
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

        // Set up empty adapter first
        adapter = ChallengeAdapter(emptyList()) { }
        binding.rvCompanyChallenges.adapter = adapter

        binding.chipAll.setOnClickListener { filter("All") }
        binding.chipEasy.setOnClickListener { filter("Easy") }
        binding.chipMedium.setOnClickListener { filter("Medium") }
        binding.chipHard.setOnClickListener { filter("Hard") }

        fetchLiveChallenges()
    }

    private fun fetchLiveChallenges() {
        RetrofitClient.getInstance().apiService.getProblems().enqueue(object : Callback<List<ProblemDto>> {
            override fun onResponse(call: Call<List<ProblemDto>>, response: Response<List<ProblemDto>>) {
                if (_binding == null) return
                
                if (response.isSuccessful && response.body() != null) {
                    // FILTER: Only show problems where the companyId matches this company!
                    val companyProblems = response.body()!!.filter { it.companyId == companyId }
                    
                    allChallenges = companyProblems.map {
                        Challenge(it.problemId, it.title, it.difficultyLevel, "General", "0%")
                    }

                    adapter = ChallengeAdapter(allChallenges) { challenge ->
                        // THE FIX: Pass the real challenge details to ProblemsFragment!
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

            override fun onFailure(call: Call<List<ProblemDto>>, t: Throwable) {
                if (_binding == null) return
                Toast.makeText(requireContext(), "Failed to load challenges: ${t.message}", Toast.LENGTH_SHORT).show()
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
