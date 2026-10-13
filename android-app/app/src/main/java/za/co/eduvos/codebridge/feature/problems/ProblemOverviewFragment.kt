package za.co.eduvos.codebridge.feature.problems

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.data.ProblemDetail
import za.co.eduvos.codebridge.data.ProblemRepository
import za.co.eduvos.codebridge.databinding.FragmentProblemOverviewBinding

class ProblemOverviewFragment : Fragment() {

    private var _binding: FragmentProblemOverviewBinding? = null
    private val binding get() = _binding!!

    private var problemId: Int = 1
    private lateinit var detail: ProblemDetail

    companion object {
        fun newInstance(problemId: Int): ProblemOverviewFragment {
            val fragment = ProblemOverviewFragment()
            val args = Bundle()
            args.putInt("problem_id", problemId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        problemId = arguments?.getInt("problem_id") ?: 1
        detail = ProblemRepository.getById(problemId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProblemOverviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.tvTitle.text = detail.title
        binding.tvCategoryChip.text = detail.category
        binding.tvDifficultyChip.text = detail.difficulty
        binding.tvDescription.text = detail.description

        binding.tvEx1Input.text = "Input: ${detail.example1Input}"
        binding.tvEx1Output.text = "Output: ${detail.example1Output}"

        binding.tvEx2Input.text = "Input: ${detail.example2Input}"
        binding.tvEx2Output.text = "Output: ${detail.example2Output}"

        binding.btnStartChallenge.setOnClickListener {
            // Launch coding IDE stage for this specific problem
            (requireActivity() as MainActivity).open(
                ProblemsFragment.newInstance(detail.id, detail.title, detail.difficulty, detail.category)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
