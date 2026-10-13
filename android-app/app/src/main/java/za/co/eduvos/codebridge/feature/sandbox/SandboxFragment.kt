package za.co.eduvos.codebridge.feature.sandbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
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
