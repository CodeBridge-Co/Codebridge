package za.co.eduvos.codebridge.feature.git

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.ui.Week3Label
import za.co.eduvos.codebridge.databinding.FragmentGitBinding
import za.co.eduvos.codebridge.feature.speech.SpeechFragment

class GitFragment : Fragment() {

    private var _binding: FragmentGitBinding? = null
    private val binding get() = _binding!!

    private val lines = mutableListOf<String>()
    private var modified = false
    private var staged = false
    private var committed = false
    private var pushed = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentGitBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Week3Label.attach(binding.root, "Git simulator coming. Terminal is demo only")
        binding.stepper.setStep(1)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.btnRunCmd.setOnClickListener { runCommand() }
        binding.etCommand.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) { runCommand(); true } else false
        }
        listOf(
            binding.chip0 to "git status", binding.chip1 to "git add .",
            binding.chip2 to "git commit -m \"solution\"", binding.chip3 to "git push"
        ).forEach { (chip, cmd) -> chip.setOnClickListener { binding.etCommand.setText(cmd) } }

        binding.btnClear.setOnClickListener { lines.clear(); render() }
        binding.btnTrash.setOnClickListener { lines.clear(); render() }

        binding.btnViewGuide.setOnClickListener {
            Toast.makeText(requireContext(), "Opening Git Concepts Guide...", Toast.LENGTH_SHORT).show()
        }

        binding.btnPush.setOnClickListener {
            (requireActivity() as MainActivity).open(SpeechFragment())
        }

        updateWorkflowUi()
        render()
    }

    private fun runCommand() {
        val cmd = binding.etCommand.text.toString().trim()
        if (cmd.isEmpty()) return
        add("<font color='#58CC02'>$</font> $cmd")
        when {
            cmd == "git status" -> {
                modified = true
                add(
                    if (!staged) "<font color='#9AA39C'>modified: src/TwoSum.java</font>"
                    else "<font color='#9AA39C'>Changes to be committed: src/TwoSum.java</font>"
                )
            }
            cmd.startsWith("git add") -> {
                modified = true
                staged = true
                add("<font color='#9AA39C'>1 file staged</font>")
            }
            cmd.startsWith("git commit") -> {
                if (staged) {
                    committed = true
                    add("[main c8d4e2] solution")
                } else {
                    add("<font color='#FF8A8A'>nothing staged to commit</font>")
                }
            }
            cmd.startsWith("git push") -> {
                if (committed) {
                    pushed = true
                    add("Pushed to origin/main")
                } else {
                    add("<font color='#FF8A8A'>nothing to push — commit first</font>")
                }
            }
            else -> add("<font color='#FF8A8A'>command not recognised</font>")
        }
        binding.etCommand.text?.clear()
        updateWorkflowUi()
        render()
    }

    private fun updateWorkflowUi() {
        binding.tvWorkingBadge.text = if (modified) "1" else "0"
        binding.tvWorkingSubtitle.text = if (modified) "1 Changed file" else "0 Changed files"

        binding.tvStagedBadge.text = if (staged) "1" else "0"
        binding.tvStagedSubtitle.text = if (staged) "1 file staged" else "0 files staged"

        binding.tvCommittedSubtitle.text = if (committed) "1 Commit" else "0 Commits"
        binding.tvPushedSubtitle.text = if (pushed) "Published" else "Not published"

        if (committed) {
            binding.cardCommitHistory.visibility = View.VISIBLE
            binding.tvEmptyCommitHistory.visibility = View.GONE
        } else {
            binding.cardCommitHistory.visibility = View.GONE
            binding.tvEmptyCommitHistory.visibility = View.VISIBLE
        }
    }

    private fun add(line: String) { lines.add(line); if (lines.size > 8) lines.removeAt(0) }

    private fun render() {
        binding.tvTerminal.text = HtmlCompat.fromHtml(lines.joinToString("<br>"), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
