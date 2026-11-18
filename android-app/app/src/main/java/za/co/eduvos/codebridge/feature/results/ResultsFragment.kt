package za.co.eduvos.codebridge.feature.results

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.dto.ChallengeCompleteRequest
import za.co.eduvos.codebridge.core.network.dto.CreateSessionRequest
import za.co.eduvos.codebridge.core.network.dto.FeedbackRequest
import za.co.eduvos.codebridge.core.network.dto.FeedbackResponse
import za.co.eduvos.codebridge.core.scoring.ScoringRules
import za.co.eduvos.codebridge.core.sandbox.ChallengeClock
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.databinding.FragmentResultsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentResultsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.stepper.setStep(3)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        val hashId = UserIdentity.displayId(requireContext())
        val prefs = requireContext().getSharedPreferences("codebridge_prefs", Context.MODE_PRIVATE)

        // 1. GATHER REAL METRICS FROM ALL 3 STAGES
        val difficulty = prefs.getString("session_difficulty", "Easy") ?: "Easy"
        val sessionId = prefs.getString("current_session_id", "1") ?: "1"
        val problemId = prefs.getInt("current_problem_id", 1)

        val input = ScoringRules.Input()
        input.testsPassed = prefs.getInt("tests_passed", 0)
        input.testsTotal = prefs.getInt("tests_total", 0)
        input.timedOut = prefs.getBoolean("timed_out", false)
        input.timeUsedMs = prefs.getLong("solve_time_ms", 0L)
        input.timeLimitMs = prefs.getLong("time_limit_ms", 0L)
        input.gitCompleted = prefs.getBoolean("git_completed", false)

        input.speechAnswers = prefs.getInt("speech_answers", 0)
        input.speechQuestions = 3
        input.speechDensitySum = prefs.getFloat("speech_density_sum", 0f).toDouble()

        // 2. RUN THE REAL MATH ENGINE
        val result = ScoringRules.compute(input)
        val finalScore = result.total

        // Calculate XP
        var baseXp = 50
        if (difficulty.equals("Medium", true)) baseXp = 100
        if (difficulty.equals("Hard", true)) baseXp = 200
        // If speech is null, it means they skipped Stage 3, so they lose 20% of XP
        val finalXp = if (result.speech == null) (baseXp * 0.8).toInt() else baseXp

        // 3. UPDATE THE UI WITH ACTUAL SCORES
        binding.ringScore.setProgress(finalScore, true)
        binding.tvScore.text = "$finalScore%"

        binding.barCode.setProgress(result.code, true)
        binding.pctCode.text = "${result.code}%"

        binding.barGit.setProgress(result.git, true)
        binding.pctGit.text = "${result.git}%"

        val speechVal = result.speech ?: 0
        binding.barVerbal.setProgress(speechVal, true)
        binding.pctVerbal.text = "${speechVal}%"

        val complexityVal = if (result.timeBonus > 0) 100 else result.code
        binding.barComplexity.setProgress(complexityVal, true)
        binding.pctComplexity.text = "${complexityVal}%"

        binding.tvCelebrate.text = if (result.speech != null) "You cleared all stages!" else "Challenge complete! (Interview skipped)"
        binding.tvTime.text = ChallengeClock.format(input.timeUsedMs)
        binding.tvDifficulty.text = difficulty
        binding.tvRank.text = "Top ${100 - finalScore}%"

        // 4. MARK SESSION COMPLETE & AWARD XP
        val endTimeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
        val completeReq = CreateSessionRequest(sessionId, hashId, problemId, "started", endTimeStr, false)
        RetrofitClient.getInstance().apiService.createSession(completeReq).enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {}
            override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {}
        })

        val xpReq = ChallengeCompleteRequest(hashId, difficulty, sessionId)
        RetrofitClient.getInstance().apiService.completeChallenge(xpReq).enqueue(object : Callback<okhttp3.ResponseBody> {
            override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Earned $finalXp XP!", Toast.LENGTH_LONG).show()
                }
            }
            override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {}
        })

        // 5. FETCH AI FEEDBACK
        binding.tvStrengths.text = "Analyzing..."
        binding.tvImprove.text = "Analyzing..."
        binding.tvAiFeedback.text = "Generating feedback..."

        val fbReq = FeedbackRequest("Coding Challenge", difficulty, ChallengeClock.format(input.timeUsedMs))
        RetrofitClient.getInstance().apiService.getAiFeedback(fbReq).enqueue(object : Callback<FeedbackResponse> {
            override fun onResponse(call: Call<FeedbackResponse>, response: Response<FeedbackResponse>) {
                if (_binding == null) return
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!
                    binding.tvStrengths.text = data.strengths
                    binding.tvImprove.text = data.improvements
                    binding.tvAiFeedback.text = data.aiFeedback
                }
            }
            override fun onFailure(call: Call<FeedbackResponse>, t: Throwable) {
                if (_binding == null) return
                binding.tvStrengths.text = "Completed the challenge."
                binding.tvImprove.text = "Keep practicing."
                binding.tvAiFeedback.text = "Feedback unavailable."
            }
        })

        // Buttons
        binding.btnShare.setOnClickListener {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "I scored $finalScore% on a $difficulty challenge on CodeBridge!")
            }
            startActivity(Intent.createChooser(send, "Share results"))
        }

        binding.btnFullFeedback.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("AI Feedback Breakdown")
                .setMessage("Strengths:\n${binding.tvStrengths.text}\n\nAreas for Improvement:\n${binding.tvImprove.text}")
                .setPositiveButton("Got it", null)
                .show()
        }

        binding.btnContinue.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(MainActivity.Tab.CHALLENGES)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}