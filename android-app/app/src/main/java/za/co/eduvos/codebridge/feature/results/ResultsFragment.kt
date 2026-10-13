package za.co.eduvos.codebridge.feature.results

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.databinding.FragmentResultsBinding

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    // TODO: fill from the scoring response of your backend
    private val overall = 87
    private val scores = listOf(92, 85, 86, 84) // code, git, verbal, complexity

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentResultsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.stepper.setStep(3)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.ringScore.setProgress(overall, true)
        binding.tvScore.text = "$overall%"
        listOf(binding.barCode, binding.barGit, binding.barVerbal, binding.barComplexity)
            .zip(scores).forEach { (bar, v) -> bar.setProgress(v, true) }
        listOf(binding.pctCode, binding.pctGit, binding.pctVerbal, binding.pctComplexity)
            .zip(scores).forEach { (t, v) -> t.text = "$v%" }

        binding.btnShare.setOnClickListener {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "I scored $overall% on Two Sum on CodeBridge!")
            }
            startActivity(Intent.createChooser(send, "Share results"))
        }
        binding.btnFullFeedback.setOnClickListener {
            // TODO: open the full AI feedback screen
        }
        binding.btnContinue.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(MainActivity.Tab.CHALLENGES)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
