package za.co.eduvos.codebridge.feature.results

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.backend.ProgressRepository
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.ui.Week3Label
import za.co.eduvos.codebridge.databinding.FragmentResultsBinding

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentResultsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Week3Label.attach(binding.root, "Live results from your profile")
        binding.stepper.setStep(3)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        val hashId = UserIdentity.displayId(requireContext())
        
        // GET REAL DATA
        val overall = ProgressRepository.getOverallProgressPercent(hashId)
        val completed = ProgressRepository.getCompletedCount(hashId)

        // Fake individual scores based on real progress (since we don't have granular DB fetch yet)
        val scores = listOf(overall, overall, overall, overall) 

        binding.ringScore.setProgress(overall, true)
        binding.tvScore.text = "$overall%"
        
        listOf(binding.barCode, binding.barGit, binding.barVerbal, binding.barComplexity)
            .zip(scores).forEach { (bar, v) -> bar.setProgress(v, true) }
        listOf(binding.pctCode, binding.pctGit, binding.pctVerbal, binding.pctComplexity)
            .zip(scores).forEach { (t, v) -> t.text = "$v%" }

        // Dynamic text based on real performance
        binding.tvStrengths.text = if (overall > 70) "Strong algorithmic logic and good execution speed." else "You are making progress. Keep practicing!"
        binding.tvImprove.text = if (overall > 70) "Review edge cases before submitting." else "Focus on syntax and test case coverage."
        binding.tvDifficulty.text = if (completed > 5) "Medium" else "Easy"
        binding.tvRank.text = if (overall > 0) "Top ${100 - overall}%" else "Unranked"

        binding.btnShare.setOnClickListener {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "I scored $overall% on a challenge on CodeBridge!")
            }
            startActivity(Intent.createChooser(send, "Share results"))
        }
        
        // FIXED DEAD BUTTON
        binding.btnFullFeedback.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("AI Feedback Breakdown")
                .setMessage("Strengths:\n${binding.tvStrengths.text}\n\nAreas for Improvement:\n${binding.tvImprove.text}\n\nKeep up the good work!")
                .setPositiveButton("Got it", null)
                .show()
        }
        
        binding.btnContinue.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(MainActivity.Tab.CHALLENGES)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
