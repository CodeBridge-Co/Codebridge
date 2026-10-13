package za.co.eduvos.codebridge.feature.problems

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.data.Challenge
import za.co.eduvos.codebridge.data.ChallengeAdapter
import za.co.eduvos.codebridge.data.ProblemRepository
import za.co.eduvos.codebridge.databinding.FragmentChallengesListBinding
import za.co.eduvos.codebridge.feature.company.CompanyChallengesFragment

class ChallengesListFragment : Fragment() {

    private var _binding: FragmentChallengesListBinding? = null
    private val binding get() = _binding!!

    private var topicName: String? = null

    // Filled from ApiService.getProblems() (Room cache when offline) by ProblemRepository.load().
    private var allChallenges: List<Challenge> = emptyList()

    private var currentFilter = "All"
    private lateinit var adapter: ChallengeAdapter

    companion object {
        fun newInstance(topicName: String? = null): ChallengesListFragment {
            val fragment = ChallengesListFragment()
            val args = Bundle()
            if (topicName != null) {
                args.putString("topic_name", topicName)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        topicName = arguments?.getString("topic_name")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!topicName.isNullOrEmpty()) {
            currentFilter = topicName!!
        }

        binding.bannerCompanyChallenges.setOnClickListener {
            (requireActivity() as MainActivity).open(CompanyChallengesFragment())
        }

        adapter = ChallengeAdapter(emptyList()) { challenge ->
            // Open Problem Overview screen matching Screen 2 Right in your wireframe!
            (requireActivity() as MainActivity).open(
                ProblemOverviewFragment.newInstance(challenge.id)
            )
        }
        binding.rvChallenges.adapter = adapter

        binding.chipAll.setOnClickListener { filter("All") }
        binding.chipEasy.setOnClickListener { filter("Easy") }
        binding.chipMedium.setOnClickListener { filter("Medium") }
        binding.chipHard.setOnClickListener { filter("Hard") }
        binding.chipArray.setOnClickListener { filter("Arrays") }
        binding.chipHash.setOnClickListener { filter("Linked Lists") }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        applyFilter("")
        binding.tvCount.text = "Loading..."

        ProblemRepository.load(requireContext()) { problems, fromCache, error ->
            if (_binding == null) return@load
            allChallenges = problems.map { Challenge(it.id, it.title, it.difficulty, it.category, "-") }
            applyFilter(binding.etSearch.text?.toString().orEmpty())
            if (problems.isEmpty()) {
                binding.tvCount.text = "Couldn't load problems"
            } else if (fromCache) {
                Toast.makeText(requireContext(), "Offline: showing saved problems", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filter(cat: String) {
        currentFilter = cat
        applyFilter(binding.etSearch.text?.toString().orEmpty())
    }

    private fun applyFilter(query: String) {
        val filtered = allChallenges.filter { c ->
            val matchesQuery = query.isEmpty() || c.title.contains(query, ignoreCase = true) || c.category.contains(query, ignoreCase = true)
            val matchesCat = when {
                currentFilter == "All" -> true
                currentFilter in listOf("Easy", "Medium", "Hard") -> c.difficulty.equals(currentFilter, ignoreCase = true)
                else -> c.category.contains(currentFilter, ignoreCase = true) || currentFilter.contains(c.category, ignoreCase = true)
            }
            matchesQuery && matchesCat
        }
        adapter.submit(filtered)
        binding.tvCount.text = "${filtered.size} available"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
