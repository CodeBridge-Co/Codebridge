package za.co.eduvos.codebridge.feature.company

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.data.Challenge
import za.co.eduvos.codebridge.data.ChallengeAdapter
import za.co.eduvos.codebridge.databinding.FragmentCompanyDetailBinding
import za.co.eduvos.codebridge.feature.problems.ProblemsFragment

class CompanyDetailFragment : Fragment() {

    private var _binding: FragmentCompanyDetailBinding? = null
    private val binding get() = _binding!!

    private var companyName: String = "Amazon"
    private var companyAbout: String = "Solve real-world problems used by Amazon engineers. Build your skills and get noticed!"

    private val companyChallengesMap = mapOf(
        "Amazon" to listOf(
            Challenge(101, "Find Prime Substring", "Easy", "Strings · Math", "58%"),
            Challenge(102, "Product Search Autocomplete", "Medium", "Arrays · Hash Table", "45%"),
            Challenge(103, "AWS Lambda Simulation", "Hard", "System Design · Concurrency", "32%")
        ),
        "Microsoft" to listOf(
            Challenge(201, "Azure Blob Stream Processor", "Medium", "Cloud · Streams", "50%"),
            Challenge(202, "AI Prompt Router", "Easy", "AI/ML · String", "65%")
        ),
        "Deloitte" to listOf(
            Challenge(301, "Audit Trail Anomaly Filter", "Hard", "SQL · Algorithms", "28%"),
            Challenge(302, "Tax Ledger Balancing", "Easy", "Math · Arrays", "72%")
        ),
        "Discovery" to listOf(
            Challenge(401, "Vitality Rewards Calculator", "Medium", "Logic · Arrays", "55%"),
            Challenge(402, "Health Claim Rule Engine", "Easy", "Validation · String", "68%")
        ),
        "FNB" to listOf(
            Challenge(501, "eWallet Transaction Stream", "Medium", "APIs · Banking", "48%"),
            Challenge(502, "Mobile App Auth Token Validator", "Easy", "Security · Hash", "70%")
        ),
        "Capitec" to listOf(
            Challenge(601, "Real-Time Payment Engine", "Medium", "Java / Spring Boot", "52%"),
            Challenge(602, "Account Ledger Reconciliation", "Hard", "Java / Data", "35%")
        )
    )

    private var currentDifficultyFilter = "All"
    private lateinit var adapter: ChallengeAdapter

    companion object {
        fun newInstance(companyName: String, aboutText: String): CompanyDetailFragment {
            val fragment = CompanyDetailFragment()
            val args = Bundle()
            args.putString("company_name", companyName)
            args.putString("about_text", aboutText)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        companyName = arguments?.getString("company_name") ?: "Amazon"
        companyAbout = arguments?.getString("about_text") ?: "Solve real-world problems used by engineers."
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

        val challenges = companyChallengesMap[companyName] ?: companyChallengesMap["Amazon"]!!
        adapter = ChallengeAdapter(challenges) { challenge ->
            (requireActivity() as MainActivity).open(ProblemsFragment())
        }
        binding.rvCompanyChallenges.adapter = adapter

        binding.chipAll.setOnClickListener { filter("All", challenges) }
        binding.chipEasy.setOnClickListener { filter("Easy", challenges) }
        binding.chipMedium.setOnClickListener { filter("Medium", challenges) }
        binding.chipHard.setOnClickListener { filter("Hard", challenges) }
    }

    private fun filter(difficulty: String, fullList: List<Challenge>) {
        currentDifficultyFilter = difficulty
        val filtered = if (difficulty == "All") fullList else fullList.filter {
            it.difficulty.equals(difficulty, ignoreCase = true)
        }
        adapter.submit(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
