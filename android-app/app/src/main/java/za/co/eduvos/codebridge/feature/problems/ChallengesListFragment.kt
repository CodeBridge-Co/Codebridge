package za.co.eduvos.codebridge.feature.problems

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.data.Challenge
import za.co.eduvos.codebridge.data.ChallengeAdapter
import za.co.eduvos.codebridge.databinding.FragmentChallengesListBinding
import za.co.eduvos.codebridge.feature.company.CompanyChallengesFragment

class ChallengesListFragment : Fragment() {

    private var _binding: FragmentChallengesListBinding? = null
    private val binding get() = _binding!!

    private var topicName: String? = null

    private val allChallenges = listOf(
        Challenge(1, "Two Sum", "Easy", "Arrays", "52%"),
        Challenge(2, "Reverse Linked List", "Medium", "Linked Lists", "75%"),
        Challenge(3, "Valid Parentheses", "Easy", "Stacks", "63%"),
        Challenge(4, "Merge Two Sorted Lists", "Medium", "Linked Lists", "68%"),
        Challenge(5, "Container With Most Water", "Medium", "Arrays", "54%")
    )

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

        adapter = ChallengeAdapter(allChallenges) { challenge ->
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
