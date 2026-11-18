package za.co.eduvos.codebridge.feature.sandbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.data.ProblemRepository
import za.co.eduvos.codebridge.databinding.FragmentSandboxBinding
import za.co.eduvos.codebridge.feature.problems.ChallengesListFragment

class SandboxFragment : Fragment() {

    private var _binding: FragmentSandboxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSandboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Hide all categories by default
        val allTopics = listOf(
            binding.topicDsa, binding.topicAlgo, binding.topicProg, binding.topicDb,
            binding.topicWeb, binding.topicMobile, binding.topicCloud, binding.topicGit
        )
        allTopics.forEach { it.visibility = View.GONE }

        // 2. Fetch problems from backend and show only the categories that have data
        ProblemRepository.load(requireContext()) { problems, _, _ ->
            if (_binding == null) return@load

            // Get all unique categories from the database
            val availableCategories = problems.map { it.category.lowercase() }.distinct()

            // Show categories based on what is actually in the backend
            if (availableCategories.contains("dsa") || availableCategories.contains("general")) {
                binding.topicDsa.visibility = View.VISIBLE
            }
            if (availableCategories.contains("algorithms")) {
                binding.topicAlgo.visibility = View.VISIBLE
            }
            if (availableCategories.contains("programming concepts")) {
                binding.topicProg.visibility = View.VISIBLE
            }
            if (availableCategories.contains("database systems")) {
                binding.topicDb.visibility = View.VISIBLE
            }
            if (availableCategories.contains("web development")) {
                binding.topicWeb.visibility = View.VISIBLE
            }
            if (availableCategories.contains("mobile development")) {
                binding.topicMobile.visibility = View.VISIBLE
            }
            if (availableCategories.contains("cloud computing")) {
                binding.topicCloud.visibility = View.VISIBLE
            }
            if (availableCategories.contains("git & github")) {
                binding.topicGit.visibility = View.VISIBLE
            }
        }

        // 3. Set the click listeners
        binding.topicDsa.setOnClickListener { openTopic("DSA") }
        binding.topicAlgo.setOnClickListener { openTopic("Algorithms") }
        binding.topicProg.setOnClickListener { openTopic("Programming Concepts") }
        binding.topicDb.setOnClickListener { openTopic("Database Systems") }
        binding.topicWeb.setOnClickListener { openTopic("Web Development") }
        binding.topicMobile.setOnClickListener { openTopic("Mobile Development") }
        binding.topicCloud.setOnClickListener { openTopic("Cloud Computing") }
        binding.topicGit.setOnClickListener { openTopic("Git & GitHub") }

        binding.chipAll.setOnClickListener { openTopic("All") }
        binding.chipTech.setOnClickListener { openTopic("Tech") }
        binding.chipDsaFilter.setOnClickListener { openTopic("DSA") }
        binding.chipSysDesign.setOnClickListener { openTopic("System Design") }
    }

    private fun openTopic(topic: String) {
        if (topic == "All") {
            (requireActivity() as MainActivity).open(ChallengesListFragment.newInstance(null))
        } else {
            (requireActivity() as MainActivity).open(TopicSubcategoriesFragment.newInstance(topic))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}