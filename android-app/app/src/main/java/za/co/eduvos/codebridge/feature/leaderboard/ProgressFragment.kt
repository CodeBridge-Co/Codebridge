package za.co.eduvos.codebridge.feature.leaderboard

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.dto.SessionHistoryDto
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.databinding.FragmentProgressBinding
import za.co.eduvos.codebridge.feature.problems.ProblemsFragment

class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    private var inProgressSessions: List<SessionHistoryDto> = emptyList()
    private var completedSessions: List<SessionHistoryDto> = emptyList()
    private var currentTab = "ALL" // ALL, IN_PROGRESS, COMPLETED

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val hashId = UserIdentity.displayId(requireContext())
        
        setupTabs()
        fetchSessionHistory(hashId)
    }

    private fun setupTabs() {
        binding.tabAll.setOnClickListener { switchTab("ALL") }
        binding.tabInProgress.setOnClickListener { switchTab("IN_PROGRESS") }
        binding.tabCompleted.setOnClickListener { switchTab("COMPLETED") }
    }

    private fun switchTab(tab: String) {
        currentTab = tab
        // Reset tab styles
        listOf(binding.tabAll, binding.tabInProgress, binding.tabCompleted).forEach {
            it.setBackgroundResource(0)
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_ink))
        }
        // Highlight selected tab
        val selectedView = when(tab) {
            "ALL" -> binding.tabAll
            "IN_PROGRESS" -> binding.tabInProgress
            else -> binding.tabCompleted
        }
        selectedView.setBackgroundResource(R.drawable.bg_chip_selected)
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_bg))

        updateUI()
    }

    private fun fetchSessionHistory(hashId: String) {
        RetrofitClient.getInstance().apiService.getStudentSessions(hashId)
            .enqueue(object : Callback<List<SessionHistoryDto>> {
                override fun onResponse(call: Call<List<SessionHistoryDto>>, response: Response<List<SessionHistoryDto>>) {
                    if (_binding == null) return
                    
                    if (response.isSuccessful && response.body() != null) {
                        val allSessions = response.body()!!
                        inProgressSessions = allSessions.filter { !it.isCompleted }
                        completedSessions = allSessions.filter { it.isCompleted }
                        
                        updateSummary()
                        generateAiTip()
                        updateUI()
                    }
                }

                override fun onFailure(call: Call<List<SessionHistoryDto>>, t: Throwable) {
                    if (_binding == null) return
                    Toast.makeText(requireContext(), "Failed to load history", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateSummary() {
        val solved = completedSessions.size
        val total = completedSessions.size + inProgressSessions.size
        val pct = if (total == 0) 0 else (solved * 100) / total
        binding.tvSummary.text = "$solved Solved · $pct% Avg"
    }

    private fun generateAiTip() {
        if (completedSessions.isEmpty() && inProgressSessions.isEmpty()) {
            binding.tvAiTipTitle.text = "Welcome to CodeBridge"
            binding.tvAiTipBody.text = "Start your first challenge to receive personalized AI performance insights!"
            return
        }

        if (inProgressSessions.isNotEmpty()) {
            binding.tvAiTipTitle.text = "Tip: Finish What You Start"
            binding.tvAiTipBody.text = "You have unfinished challenges. Resuming an existing session builds muscle memory better than starting fresh."
        } else {
            binding.tvAiTipTitle.text = "Tip: Optimize Your Time Complexity"
            binding.tvAiTipBody.text = "Great job completing challenges! Try reviewing your past solutions to see if you can improve an O(N²) solution to O(N)."
        }
    }

    private fun updateUI() {
        binding.llInProgressContainer.removeAllViews()
        binding.llCompletedContainer.removeAllViews()

        val showInProgress = currentTab == "ALL" || currentTab == "IN_PROGRESS"
        val showCompleted = currentTab == "ALL" || currentTab == "COMPLETED"

        binding.tvInProgressTitle.visibility = if (showInProgress) View.VISIBLE else View.GONE
        binding.llInProgressContainer.visibility = if (showInProgress) View.VISIBLE else View.GONE
        
        binding.tvCompletedTitle.visibility = if (showCompleted) View.VISIBLE else View.GONE
        binding.llCompletedContainer.visibility = if (showCompleted) View.VISIBLE else View.GONE

        if (showInProgress) {
            if (inProgressSessions.isEmpty()) {
                addEmptyState(binding.llInProgressContainer, "No challenges in progress.")
            } else {
                inProgressSessions.forEach { session ->
                    addSessionCard(binding.llInProgressContainer, "Problem #${session.problemId}", "Resume →", false)
                }
            }
        }

        if (showCompleted) {
            if (completedSessions.isEmpty()) {
                addEmptyState(binding.llCompletedContainer, "No completed challenges yet.")
            } else {
                completedSessions.forEach { session ->
                    addSessionCard(binding.llCompletedContainer, "Problem #${session.problemId}", "Completed", true)
                }
            }
        }
    }

    private fun addEmptyState(container: LinearLayout, message: String) {
        val tv = TextView(requireContext()).apply {
            text = message
            setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_text_secondary))
            textSize = 12f
            setPadding(16, 16, 16, 16)
        }
        container.addView(tv)
    }

    private fun addSessionCard(container: LinearLayout, title: String, actionText: String, isCompleted: Boolean) {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundResource(if (isCompleted) R.drawable.bg_card else R.drawable.bg_card_outline)
            
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            layoutParams = params
        }

        val textLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvTitle = TextView(requireContext()).apply {
            text = title
            setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_ink))
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val tvSub = TextView(requireContext()).apply {
            text = if (isCompleted) "Successfully completed" else "Session started"
            setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_text_secondary))
            textSize = 11f
        }

        textLayout.addView(tvTitle)
        textLayout.addView(tvSub)

        val btnAction = TextView(requireContext()).apply {
            text = actionText
            setTextColor(ContextCompat.getColor(requireContext(), if (isCompleted) R.color.cb_green_dark else R.color.cb_bg))
            setBackgroundResource(if (isCompleted) R.drawable.bg_chip else R.drawable.bg_chip_selected)
            setPadding(20, 10, 20, 10)
            textSize = 11f
            
            setOnClickListener {
                if (!isCompleted) {
                    // Resume logic: Open the sandbox for this specific problem
                    (requireActivity() as MainActivity).open(ProblemsFragment())
                }
            }
        }

        card.addView(textLayout)
        card.addView(btnAction)
        container.addView(card)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
