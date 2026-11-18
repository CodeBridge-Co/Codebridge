package za.co.eduvos.codebridge.feature.problems

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.backend.ProgressRepository
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.dto.CreateSessionRequest
import za.co.eduvos.codebridge.core.sandbox.ChallengeClock
import za.co.eduvos.codebridge.core.sandbox.CodeExecutionEngine
import za.co.eduvos.codebridge.core.sandbox.ExecutionResult
import za.co.eduvos.codebridge.core.sandbox.TestResult
import za.co.eduvos.codebridge.core.sandbox.TestSuite
import za.co.eduvos.codebridge.core.sandbox.TestSuiteParser
import za.co.eduvos.codebridge.data.ProblemDetail
import za.co.eduvos.codebridge.data.ProblemRepository
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.databinding.FragmentProblemsBinding
import za.co.eduvos.codebridge.feature.git.GitFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ProblemsFragment : Fragment() {

    private var _binding: FragmentProblemsBinding? = null
    private val binding get() = _binding!!

    private var problemId: Int = 1
    private lateinit var detail: ProblemDetail
    private val activeLanguage: String = "JavaScript"
    private lateinit var engine: CodeExecutionEngine
    private var isExpanded: Boolean = false
    private var aiAttemptsLeft: Int = 3

    // Unique ID for this session
    private val currentSessionId = UUID.randomUUID().toString()

    // Timer. The start time is kept here, so turning the phone doesn't restart it or start a new session.
    private var sessionStarted = false
    private var solveStartMs = 0L
    private var solveFinished = false
    private var lastPassed = 0
    private var lastTotal = 0
    private var clock: ChallengeClock? = null
    private val tickHandler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            if (_binding == null || solveFinished) return
            updateTimer()
            if (clock?.isExpired(System.currentTimeMillis()) == true) onTimeUp() else tickHandler.postDelayed(this, 1000)
        }
    }

    companion object {
        fun newInstance(problemId: Int, title: String, difficulty: String, category: String): ProblemsFragment {
            val fragment = ProblemsFragment()
            val args = Bundle()
            args.putInt("problem_id", problemId)
            args.putString("title", title)
            args.putString("difficulty", difficulty)
            args.putString("category", category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        problemId = arguments?.getInt("problem_id") ?: 1
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentProblemsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        engine = CodeExecutionEngine()
        detail = ProblemRepository.getById(problemId) ?: run {
            Toast.makeText(requireContext(), "Problem not loaded. Please reopen the list.", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        // Save the session and create it on the server (only once)
        val prefs = requireContext().getSharedPreferences("codebridge_prefs", Context.MODE_PRIVATE)
        if (!sessionStarted) {
            sessionStarted = true
            solveStartMs = System.currentTimeMillis()
            val startTimeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

            prefs.edit()
                .putLong("session_start_time", solveStartMs)
                .putString("session_difficulty", detail.difficulty)
                .putString("current_session_id", currentSessionId)
                .putInt("current_problem_id", problemId)
                .putBoolean("git_completed", false)
                .remove("speech_answers").remove("speech_density_sum").remove("speech_questions")
                .putLong("time_limit_ms", detail.timeLimitMinutes * 60_000L)
                .remove("solve_time_ms").remove("tests_passed").remove("tests_total").remove("timed_out")
                .apply()

            val hashId = UserIdentity.displayId(requireContext())
            val sessionReq = CreateSessionRequest(currentSessionId, hashId, problemId, startTimeStr, null, false)
            RetrofitClient.getInstance().apiService.createSession(sessionReq).enqueue(object : Callback<okhttp3.ResponseBody> {
                override fun onResponse(call: Call<okhttp3.ResponseBody>, response: Response<okhttp3.ResponseBody>) {}
                override fun onFailure(call: Call<okhttp3.ResponseBody>, t: Throwable) {}
            })
        }
        clock = ChallengeClock(solveStartMs, detail.timeLimitMinutes)

        binding.stepper.setStep(0)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.tvHeaderTitle.text = "${detail.title} · ${detail.difficulty}"
        binding.tvTitle.text = detail.title
        binding.tvDesc.text = detail.description
        binding.tvExampleLabel.text = "Input: ${detail.example1Input}\nOutput: ${detail.example1Output}"
        showCaseLabels()
        binding.etCode.setText(getTemplateFor(activeLanguage, detail))

        binding.tabProblem.setOnClickListener { showTab(0) }
        binding.tabExamples.setOnClickListener { showTab(1) }
        binding.tabConstraints.setOnClickListener { showTab(2) }
        showTab(0)

        binding.tvLanguageSelector.setOnClickListener {
            Toast.makeText(requireContext(), "Only JavaScript can be run offline on this device", Toast.LENGTH_SHORT).show()
        }

        binding.btnReset.setOnClickListener {
            binding.etCode.setText(getTemplateFor(activeLanguage, detail))
            Toast.makeText(requireContext(), "Code reset to $activeLanguage template", Toast.LENGTH_SHORT).show()
        }

        binding.btnFormat.setOnClickListener {
            val unformatted = binding.etCode.text?.toString().orEmpty()
            val formatted = unformatted.lines().joinToString("\n") { it.trimEnd() }
            binding.etCode.setText(formatted)
            Toast.makeText(requireContext(), "Code formatted", Toast.LENGTH_SHORT).show()
        }

        binding.btnCopy.setOnClickListener {
            val codeToCopy = binding.etCode.text?.toString().orEmpty()
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Solution Code", codeToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Code copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        binding.btnExpand.setOnClickListener {
            isExpanded = !isExpanded
            binding.etCode.minLines = if (isExpanded) 14 else 6
            val status = if (isExpanded) "expanded" else "collapsed"
            Toast.makeText(requireContext(), "Terminal $status", Toast.LENGTH_SHORT).show()
        }

        binding.btnRun.setOnClickListener { runTests() }

        val openCodeMate = View.OnClickListener {
            if (aiAttemptsLeft > 0) {
                val currentCodeText = binding.etCode.text?.toString().orEmpty()
                CodeMateBottomSheet.newInstance(
                    sessionId = currentSessionId,
                    problemTitle = detail.title,
                    currentCode = currentCodeText,
                    attemptsLeft = aiAttemptsLeft
                ) { updatedAttempts ->
                    aiAttemptsLeft = updatedAttempts
                    binding.tvAiBannerTitle.text = "Slide right for AI assistance ($aiAttemptsLeft attempts left)"
                }.show(childFragmentManager, "CodeMateBottomSheet")
            } else {
                Toast.makeText(requireContext(), "No CodeMate AI attempts remaining for this challenge", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardAiBanner.setOnClickListener(openCodeMate)
        binding.btnAiSlide.setOnClickListener(openCodeMate)

        binding.btnSubmit.isEnabled = false
        binding.btnSubmit.alpha = 0.5f
        binding.btnSubmit.setOnClickListener {
            finishSolve(timedOut = false)
            (requireActivity() as MainActivity).open(GitFragment())
        }

        // Start the countdown.
        if (solveFinished) {
            lockEditing()
        } else {
            updateTimer()
            tickHandler.removeCallbacks(ticker)
            tickHandler.post(ticker)
        }
    }

    private fun updateTimer() {
        val c = clock ?: return
        val now = System.currentTimeMillis()
        binding.tvTimer.text = ChallengeClock.format(c.remainingMs(now))
        binding.tvTimer.setTextColor(
            when (c.level(now)) {
                ChallengeClock.Level.NORMAL -> ContextCompat.getColor(requireContext(), R.color.cb_ink)
                ChallengeClock.Level.WARNING -> Color.parseColor("#FF9800")
                ChallengeClock.Level.CRITICAL -> ContextCompat.getColor(requireContext(), R.color.cb_red)
            }
        )
    }

    private fun lockEditing() {
        binding.etCode.isEnabled = false
        binding.btnRun.isEnabled = false
        binding.btnReset.isEnabled = false
        binding.btnFormat.isEnabled = false
    }

    /** Saves the time used and tests passed for the results screen. */
    private fun finishSolve(timedOut: Boolean) {
        if (solveFinished) return
        solveFinished = true
        tickHandler.removeCallbacks(ticker)
        val used = clock?.elapsedMs(System.currentTimeMillis()) ?: 0L
        requireContext().getSharedPreferences("codebridge_prefs", Context.MODE_PRIVATE).edit()
            .putLong("solve_time_ms", used)
            .putInt("tests_passed", lastPassed)
            .putInt("tests_total", lastTotal)
            .putBoolean("timed_out", timedOut)
            .apply()
    }

    /** Time is up: lock the editor, run the code once more, then move on. */
    private fun onTimeUp() {
        if (solveFinished) return
        val code = binding.etCode.text?.toString().orEmpty()
        lockEditing()
        binding.tvTimer.text = "00:00"
        binding.tvTimer.setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_red))
        if (code.isBlank()) {
            finishSolve(timedOut = true)
            showTimeUpDialog()
            return
        }
        binding.tvPassed.text = "Running..."
        engine.executeAsync(code, detail.testCasesJson) { result ->
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                showResult(result)
                finishSolve(timedOut = true)
                lockEditing()
                showTimeUpDialog()
            }
        }
    }

    private fun showTimeUpDialog() {
        val summary = if (lastTotal > 0) "$lastPassed of $lastTotal tests passed." else "No tests were run."
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Time is up")
            .setMessage("$summary Your solution has been submitted as it stands.")
            .setCancelable(false)
            .setPositiveButton("Continue") { _, _ ->
                (requireActivity() as MainActivity).open(GitFragment())
            }
            .show()
    }

    /** Order of the case rows: visible ones first, then hidden ones. */
    private fun caseOrder(): List<Int> {
        val suite = try { TestSuiteParser.parse(detail.testCasesJson) } catch (e: TestSuiteParser.ParseException) { null }
            ?: return emptyList()
        val indices = suite.cases.indices
        return indices.filter { !suite.cases[it].hidden } + indices.filter { suite.cases[it].hidden }
    }

    private fun showCaseLabels() {
        val suite = try { TestSuiteParser.parse(detail.testCasesJson) } catch (e: TestSuiteParser.ParseException) { null }
        val order = caseOrder()
        val labels = listOf(binding.tvCase1, binding.tvCase2, binding.tvCase3)
        val statuses = listOf(binding.tvCaseStatus1, binding.tvCaseStatus2, binding.tvCaseStatus3)
        labels.forEachIndexed { row, tv ->
            val c = order.getOrNull(row)?.let { suite?.cases?.get(it) }
            tv.text = when {
                c == null -> "-"
                c.hidden -> "Hidden test"
                else -> c.input
            }
            statuses[row].text = if (c == null) "-" else "Ready"
        }
    }

    private fun getTemplateFor(lang: String, p: ProblemDetail): String = p.initialCode

    private fun runTests() {
        val code = binding.etCode.text?.toString().orEmpty()
        if (code.isBlank()) {
            Toast.makeText(requireContext(), "Write your solution in the code editor before running tests", Toast.LENGTH_LONG).show()
            return
        }
        binding.btnRun.isEnabled = false
        binding.tvPassed.text = "Running..."
        engine.executeAsync(code, detail.testCasesJson) { result ->
            activity?.runOnUiThread { if (_binding != null) showResult(result) }
        }
    }

    private fun showResult(result: ExecutionResult) {
        lastPassed = result.passedCount()
        lastTotal = result.totalCount()
        binding.btnRun.isEnabled = !solveFinished
        val statusViews = listOf(binding.tvCaseStatus1, binding.tvCaseStatus2, binding.tvCaseStatus3)
        val order = caseOrder()

        if (result.fatalError != null) {
            binding.tvPassed.text = "Error"
            statusViews.forEach { it.text = "-"; it.setTextColor(Color.parseColor("#FF9800")) }
            Toast.makeText(requireContext(), result.fatalError, Toast.LENGTH_LONG).show()
            return
        }

        val hiddenTotal = result.results.count { it.hidden }
        binding.tvPassed.text = "Passed ${result.passedCount()}/${result.totalCount()}" +
                if (hiddenTotal > 0) " ($hiddenTotal hidden)" else ""
        statusViews.forEachIndexed { i, tv ->
            val r = order.getOrNull(i)?.let { result.results.getOrNull(it) }
            if (r == null) {
                tv.text = "-"
                tv.setTextColor(Color.parseColor("#8E8E93"))
            } else {
                tv.text = when (r.status) {
                    TestResult.Status.PASSED -> "Passed"
                    TestResult.Status.FAILED -> "Failed"
                    TestResult.Status.ERROR -> "Error"
                    TestResult.Status.TIMEOUT -> "Timed out"
                    TestResult.Status.SKIPPED -> "Skipped"
                }
                tv.setTextColor(
                    when {
                        r.isPassed -> ContextCompat.getColor(requireContext(), R.color.cb_green_dark)
                        r.status == TestResult.Status.FAILED -> ContextCompat.getColor(requireContext(), R.color.cb_red)
                        else -> Color.parseColor("#FF9800")
                    }
                )
            }
        }

        // Report a visible failure first. Hidden ones never show details.
        val firstBad = result.results.firstOrNull { !it.isPassed && !it.hidden }
            ?: result.results.firstOrNull { !it.isPassed }
        if (firstBad == null) {
            ProgressRepository.recordVerifiedCompletion(UserIdentity.displayId(requireContext()), detail.id)
            Toast.makeText(requireContext(), "All ${result.totalCount()} cases passed!", Toast.LENGTH_SHORT).show()

            binding.btnSubmit.isEnabled = true
            binding.btnSubmit.alpha = 1.0f

        } else {
            binding.btnSubmit.isEnabled = false
            binding.btnSubmit.alpha = 0.5f

            if (firstBad.hidden) {
                Toast.makeText(requireContext(), "A hidden test failed", Toast.LENGTH_LONG).show()
            } else {
                val why = when (firstBad.status) {
                    TestResult.Status.FAILED -> "expected ${firstBad.expected}, got ${firstBad.actual}"
                    TestResult.Status.TIMEOUT -> "time limit exceeded"
                    else -> firstBad.error ?: firstBad.status.name
                }
                Toast.makeText(requireContext(), "Case ${firstBad.index + 1}: $why", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showTab(index: Int) {
        val tabs = listOf(binding.tabProblem, binding.tabExamples, binding.tabConstraints)
        tabs.forEachIndexed { i, t -> style(t, i == index) }
        val (desc, label, body) = when (index) {
            0 -> Triple(detail.description, "Example 1:", "Input: ${detail.example1Input}\nOutput: ${detail.example1Output}")
            1 -> Triple("Another example input and the output it should produce.", "Examples:", "Input: ${detail.example2Input}\nOutput: ${detail.example2Output}")
            else -> Triple("What the checker will hold you to.", "Constraints:", constraintsText())
        }
        binding.tvDesc.text = desc
        binding.tvExampleLabel.text = label
        binding.tvExample.text = body
    }

    /** Real facts from the problem. */
    private fun constraintsText(): String {
        val suite = try { TestSuiteParser.parse(detail.testCasesJson) } catch (e: TestSuiteParser.ParseException) { null }
        val lines = mutableListOf("Time limit: ${detail.timeLimitMinutes} minutes")
        if (suite != null) {
            lines.add("Test cases: ${suite.cases.size}" + if (suite.hiddenCount() > 0) " (${suite.hiddenCount()} hidden)" else "")
            if (suite.mode == TestSuite.Mode.UNORDERED) lines.add("The order of items in your answer does not matter.")
            if (suite.mode == TestSuite.Mode.CHECKER) lines.add("Any valid answer is accepted.")
        }
        return lines.joinToString("\n")
    }

    private fun style(t: TextView, selected: Boolean) {
        t.setBackgroundResource(if (selected) R.drawable.bg_chip_selected else 0)
        t.setTextColor(ContextCompat.getColor(requireContext(), if (selected) R.color.cb_bg else R.color.cb_ink))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tickHandler.removeCallbacks(ticker)
        engine.shutdown()
        _binding = null
    }
}

