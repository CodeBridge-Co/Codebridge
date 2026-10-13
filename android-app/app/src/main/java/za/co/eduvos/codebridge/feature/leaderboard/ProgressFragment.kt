package za.co.eduvos.codebridge.feature.leaderboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.backend.ProgressRepository
import za.co.eduvos.codebridge.ui.Week3Label
import za.co.eduvos.codebridge.databinding.FragmentProgressBinding
import za.co.eduvos.codebridge.feature.problems.ProblemsFragment

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Week3Label.attach(binding.root, "progress sync coming. Sample data shown")

        val hashId = UserIdentity.displayId(requireContext())
        val completedCount = ProgressRepository.getCompletedCount(hashId)
        val overallPct = ProgressRepository.getOverallProgressPercent(hashId)

        binding.tvSummary.text = "$completedCount Solved · $overallPct% Progress"

        binding.btnResume1.setOnClickListener {
            (requireActivity() as MainActivity).open(ProblemsFragment())
        }
        binding.btnResume2.setOnClickListener {
            (requireActivity() as MainActivity).open(ProblemsFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
