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
import za.co.eduvos.codebridge.backend.HashIdManager
import za.co.eduvos.codebridge.backend.TestExecutionService
import za.co.eduvos.codebridge.data.ProblemDetail
import za.co.eduvos.codebridge.data.ProblemRepository
import za.co.eduvos.codebridge.databinding.FragmentProblemsBinding
import za.co.eduvos.codebridge.feature.git.GitFragment

class ProblemsFragment : Fragment() {

    private var _binding: FragmentProblemsBinding? = null
    private val binding get() = _binding!!

    private var problemId: Int = 1
    private lateinit var detail: ProblemDetail

    private var activeLanguage: String = "Java"
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
        detail = ProblemRepository.getById(problemId)
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

        // Run Tests Handler calling Java Sandboxed Test Execution Service
        binding.btnRun.setOnClickListener {
            val userHashId = HashIdManager.getOrGenerateHashId(requireContext())
            val currentCode = binding.etCode.text?.toString().orEmpty()
            val result = TestExecutionService.executeCode(userHashId, detail.id, currentCode, activeLanguage)

            if (result.isSuccess) {
                binding.tvPassed.text = "Passed ${result.passedCases}/${result.totalCases}"
                listOf(binding.tvCaseStatus1, binding.tvCaseStatus2, binding.tvCaseStatus3).forEach {
                    it.text = "Passed"
                    it.setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_green_dark))
                }
                Toast.makeText(requireContext(), "Tests executed: All ${result.passedCases} cases passed!", Toast.LENGTH_SHORT).show()
            } else {
                binding.tvPassed.text = "Incomplete 0/${result.totalCases}"
                listOf(binding.tvCaseStatus1, binding.tvCaseStatus2, binding.tvCaseStatus3).forEach {
                    it.text = "Incomplete"
                    it.setTextColor(Color.parseColor("#FF9800"))
                }
                Toast.makeText(requireContext(), result.errorMessage ?: "Write your solution in the code editor before running tests", Toast.LENGTH_LONG).show()
            }
        }

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
        val popup = PopupMenu(requireContext(), v)
        popup.menu.add("Java")
        popup.menu.add("Python")
        popup.menu.add("Kotlin")
        popup.menu.add("C++")
        popup.setOnMenuItemClickListener { item ->
            activeLanguage = item.title.toString()
            binding.tvLanguageSelector.text = "Solution · $activeLanguage ∨"
            binding.etCode.setText(getTemplateFor(activeLanguage, detail))
            Toast.makeText(requireContext(), "Switched to $activeLanguage", Toast.LENGTH_SHORT).show()
            true
        }
        popup.show()
    }

    private fun getTemplateFor(lang: String, p: ProblemDetail): String {
        val funcName = when (p.title) {
            "Reverse Linked List" -> "reverseList"
            "Valid Parentheses" -> "isValid"
            "Merge Two Sorted Lists" -> "mergeTwoLists"
            "Container With Most Water" -> "maxArea"
            else -> "twoSum"
        }
        return when (lang) {
            "Python" -> "class Solution:\n    def $funcName(self, input_val):\n        # Write your solution here\n        pass"
            "Kotlin" -> "class Solution {\n    fun $funcName(inputVal: Any): Any {\n        // Write your solution here\n    }\n}"
            "C++" -> "class Solution {\npublic:\n    auto $funcName(auto inputVal) {\n        // Write your solution here\n    }\n};"
            else -> p.initialCode
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

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
