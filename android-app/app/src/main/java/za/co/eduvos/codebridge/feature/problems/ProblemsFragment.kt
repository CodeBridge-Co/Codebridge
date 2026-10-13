package za.co.eduvos.codebridge.feature.problems

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.backend.ProgressRepository
import za.co.eduvos.codebridge.core.sandbox.CodeExecutionEngine
import za.co.eduvos.codebridge.core.sandbox.ExecutionResult
import za.co.eduvos.codebridge.core.sandbox.TestResult
import za.co.eduvos.codebridge.data.ProblemDetail
import za.co.eduvos.codebridge.data.ProblemRepository
import za.co.eduvos.codebridge.databinding.FragmentProblemsBinding
import za.co.eduvos.codebridge.feature.git.GitFragment

class ProblemsFragment : Fragment() {

    private var _binding: FragmentProblemsBinding? = null
    private val binding get() = _binding!!

    private var problemId: Int = 1
    private lateinit var detail: ProblemDetail

    // The offline sandbox runs JavaScript (Rhino); other languages can't be executed on-device.
    private val activeLanguage: String = "JavaScript"
    private lateinit var engine: CodeExecutionEngine
    private var isExpanded: Boolean = false
    private var aiAttemptsLeft: Int = 3

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

    private val problem = Triple(
        "Given an array of integers and a target, return the indices of the two numbers that add up to the target.",
        "Example 1:",
        "nums = [2,7,11,15], target = 9\noutput = [0,1]"
    )
    private val examples = Triple(
        "Each input has exactly one solution, and you may not use the same element twice.",
        "Examples:",
        "nums = [2,7,11,15], target = 9 → [0,1]\nnums = [3,2,4], target = 6 → [1,2]\nnums = [3,3], target = 6 → [0,1]"
    )
    private val constraints = Triple(
        "Keep these limits in mind when choosing your approach.",
        "Constraints:",
        "2 ≤ nums.length ≤ 10⁴\n-10⁹ ≤ nums[i] ≤ 10⁹\n-10⁹ ≤ target ≤ 10⁹\nOnly one valid answer exists."
    )

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
        binding.stepper.setStep(0)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        // Bind dynamic top header and problem title
        binding.tvHeaderTitle.text = "${detail.title} · ${detail.difficulty}"
        binding.tvTitle.text = detail.title

        // Bind dynamic problem description and example
        binding.tvDesc.text = detail.description
        binding.tvExampleLabel.text = "Example 1:\nInput: ${detail.example1Input}\nOutput: ${detail.example1Output}"

        // Bind dynamic testcase text
        binding.tvCase1.text = detail.example1Input
        binding.tvCase2.text = detail.example2Input

        // Bind starter code for active language
        binding.etCode.setText(getTemplateFor(activeLanguage, detail))

        binding.tabProblem.setOnClickListener { showTab(0) }
        binding.tabExamples.setOnClickListener { showTab(1) }
        binding.tabConstraints.setOnClickListener { showTab(2) }
        showTab(0)

        // Language Dropdown Menu
        binding.tvLanguageSelector.setOnClickListener { showLanguageMenu(it) }

        // Reset Template Button
        binding.btnReset.setOnClickListener {
            binding.etCode.setText(getTemplateFor(activeLanguage, detail))
            Toast.makeText(requireContext(), "Code reset to $activeLanguage template", Toast.LENGTH_SHORT).show()
        }

        // Format Code Button
        binding.btnFormat.setOnClickListener {
            val unformatted = binding.etCode.text?.toString().orEmpty()
            val formatted = unformatted.lines().joinToString("\n") { it.trimEnd() }
            binding.etCode.setText(formatted)
            Toast.makeText(requireContext(), "Code formatted", Toast.LENGTH_SHORT).show()
        }

        // Copy Code Button
        binding.btnCopy.setOnClickListener {
            val codeToCopy = binding.etCode.text?.toString().orEmpty()
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Solution Code", codeToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Code copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        // Expand Editor Button
        binding.btnExpand.setOnClickListener {
            isExpanded = !isExpanded
            binding.etCode.minLines = if (isExpanded) 14 else 6
            val status = if (isExpanded) "expanded" else "collapsed"
            Toast.makeText(requireContext(), "Terminal $status", Toast.LENGTH_SHORT).show()
        }

        binding.btnRun.setOnClickListener { runTests() }

        // AI Assistance Banner -> Opens CodeMate Chat BottomSheet
        val openCodeMate = View.OnClickListener {
            if (aiAttemptsLeft > 0) {
                CodeMateBottomSheet.newInstance(aiAttemptsLeft) { updatedAttempts ->
                    aiAttemptsLeft = updatedAttempts
                    binding.tvAiBannerTitle.text = "Slide right for AI assistance ($aiAttemptsLeft attempts left)"
                }.show(childFragmentManager, "CodeMateBottomSheet")
            } else {
                Toast.makeText(requireContext(), "No CodeMate AI attempts remaining for this challenge", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardAiBanner.setOnClickListener(openCodeMate)
        binding.btnAiSlide.setOnClickListener(openCodeMate)

        binding.btnSubmit.setOnClickListener { (requireActivity() as MainActivity).open(GitFragment()) }
    }

    private fun showLanguageMenu(v: View) {
        Toast.makeText(requireContext(), "Only JavaScript can be run offline on this device", Toast.LENGTH_SHORT).show()
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
            // Arrives on the sandbox worker thread, so hop to the UI thread.
            activity?.runOnUiThread { if (_binding != null) showResult(result) }
        }
    }

    private fun showResult(result: ExecutionResult) {
        binding.btnRun.isEnabled = true
        val statusViews = listOf(binding.tvCaseStatus1, binding.tvCaseStatus2, binding.tvCaseStatus3)

        if (result.fatalError != null) {
            binding.tvPassed.text = "Error"
            statusViews.forEach { it.text = "-"; it.setTextColor(Color.parseColor("#FF9800")) }
            Toast.makeText(requireContext(), result.fatalError, Toast.LENGTH_LONG).show()
            return
        }

        binding.tvPassed.text = "Passed ${result.passedCount()}/${result.totalCount()}"
        statusViews.forEachIndexed { i, tv ->
            val r = result.results.getOrNull(i)
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

        val firstBad = result.results.firstOrNull { !it.isPassed }
        if (firstBad == null) {
            ProgressRepository.recordVerifiedCompletion(UserIdentity.displayId(requireContext()), detail.id)
            Toast.makeText(requireContext(), "All ${result.totalCount()} cases passed!", Toast.LENGTH_SHORT).show()
        } else {
            val why = when (firstBad.status) {
                TestResult.Status.FAILED -> "expected ${firstBad.expected}, got ${firstBad.actual}"
                TestResult.Status.TIMEOUT -> "time limit exceeded"
                else -> firstBad.error ?: firstBad.status.name
            }
            Toast.makeText(requireContext(), "Case ${firstBad.index + 1}: $why", Toast.LENGTH_LONG).show()
        }
    }

    private fun showTab(index: Int) {
        val tabs = listOf(binding.tabProblem, binding.tabExamples, binding.tabConstraints)
        tabs.forEachIndexed { i, t -> style(t, i == index) }
        val (desc, label, body) = when (index) {
            0 -> Triple(detail.description, "Example 1:", "Input: ${detail.example1Input}\nOutput: ${detail.example1Output}")
            1 -> Triple("Each input has exactly one valid solution.", "Examples:", "Input: ${detail.example2Input}\nOutput: ${detail.example2Output}")
            else -> Triple("Keep time and space complexity limits in mind.", "Constraints:", "1 <= N <= 10^4\nO(N) time complexity target.")
        }
        binding.tvDesc.text = desc
        binding.tvExampleLabel.text = label
        binding.tvExample.text = body
    }

    private fun style(t: TextView, selected: Boolean) {
        t.setBackgroundResource(if (selected) R.drawable.bg_chip_selected else 0)
        t.setTextColor(ContextCompat.getColor(requireContext(), if (selected) R.color.cb_bg else R.color.cb_ink))
    }

    override fun onDestroyView() { super.onDestroyView(); engine.shutdown(); _binding = null }
}
