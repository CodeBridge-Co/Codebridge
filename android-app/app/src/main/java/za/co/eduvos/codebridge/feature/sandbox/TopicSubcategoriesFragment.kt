package za.co.eduvos.codebridge.feature.sandbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.data.SubTopic
import za.co.eduvos.codebridge.databinding.FragmentTopicSubcategoriesBinding
import za.co.eduvos.codebridge.feature.problems.ChallengesListFragment

class TopicSubcategoriesFragment : Fragment() {

    private var _binding: FragmentTopicSubcategoriesBinding? = null
    private val binding get() = _binding!!

    private var mainTopicName: String = "DSA"

    private val subTopicsMap = mapOf(
        "DSA" to listOf(
            SubTopic(1, "Arrays", 0, 2, "code"),
            SubTopic(2, "Linked Lists", 0, 2, "branch"),
            SubTopic(3, "Stacks & Queues", 0, 2, "code"),
            SubTopic(4, "Trees", 0, 2, "tree"),
            SubTopic(5, "Graphs", 0, 2, "chart"),
            SubTopic(6, "Dynamic Programming", 0, 8, "spark")
        ),
        "Algorithms" to listOf(
            SubTopic(11, "Sorting Algorithms", 0, 4, "code"),
            SubTopic(12, "Binary Search", 0, 3, "spark"),
            SubTopic(13, "Recursion & Backtracking", 0, 3, "branch")
        ),
        "Cloud Computing" to listOf(
            SubTopic(21, "AWS Lambda & Serverless", 0, 3, "cloud"),
            SubTopic(22, "Docker Containerization", 0, 3, "code"),
            SubTopic(23, "Kubernetes Orchestration", 0, 2, "chart")
        ),
        "Git & GitHub" to listOf(
            SubTopic(31, "Git Commands & Workflow", 0, 3, "branch"),
            SubTopic(32, "Branching & Merge Conflicts", 0, 3, "code")
        ),
        "Database Systems" to listOf(
            SubTopic(41, "SQL Queries & Joins", 0, 4, "chart"),
            SubTopic(42, "Database Index Optimization", 0, 4, "spark")
        )
    )

    companion object {
        fun newInstance(topicName: String): TopicSubcategoriesFragment {
            val fragment = TopicSubcategoriesFragment()
            val args = Bundle()
            args.putString("topic_name", topicName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainTopicName = arguments?.getString("topic_name") ?: "DSA"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicSubcategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val displayTitle = when (mainTopicName) {
            "Data Structures & Algorithms", "DSA" -> "DSA Topics"
            "Cloud Computing", "Cloud" -> "Cloud Computing Topics"
            "Git & GitHub", "Git" -> "Git & GitHub Topics"
            else -> "$mainTopicName Topics"
        }
        binding.tvHeaderTitle.text = displayTitle

        val key = when {
            mainTopicName.contains("Cloud", ignoreCase = true) -> "Cloud Computing"
            mainTopicName.contains("Git", ignoreCase = true) -> "Git & GitHub"
            mainTopicName.contains("Database", ignoreCase = true) -> "Database Systems"
            mainTopicName.contains("Algo", ignoreCase = true) -> "Algorithms"
            else -> "DSA"
        }

        val list = subTopicsMap[key] ?: subTopicsMap["DSA"]!!
        val adapter = SubTopicAdapter(list) { subTopic ->
            // Open Challenges list pre-filtered for this specific subtopic
            (requireActivity() as MainActivity).open(
                ChallengesListFragment.newInstance(subTopic.name)
            )
        }
        binding.rvSubtopics.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
