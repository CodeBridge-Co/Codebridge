package za.co.eduvos.codebridge.feature.git

import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.core.database.AppDatabase
import za.co.eduvos.codebridge.core.git.CommandLogger
import za.co.eduvos.codebridge.core.git.GitSimulationManager
import za.co.eduvos.codebridge.core.git.GitTelemetryReporter
import za.co.eduvos.codebridge.core.git.GitTelemetryCollector
import za.co.eduvos.codebridge.core.git.MergeConflictGenerator
import za.co.eduvos.codebridge.core.git.RoomGitLogSink
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.databinding.FragmentGitBinding
import za.co.eduvos.codebridge.feature.speech.SpeechFragment
import java.util.function.LongSupplier
import android.content.Intent

class GitFragment : Fragment() {

    private var _binding: FragmentGitBinding? = null
    private val binding get() = _binding!!

    private lateinit var git: GitSimulationManager
    private val lines = mutableListOf<String>()
    private var editingFile: String? = null
    private var loadedContent: String? = null
    private var announcedComplete = false

    companion object {
        // Placeholder until assessment sessions are created in the app
        private const val SESSION_ID = 1
        private const val MAX_LINES = 80
        private const val GREY = "#9AA39C"
        private const val RED = "#FF8A8A"
        private const val AMBER = "#FFB74D"
        private const val GREEN = "#58CC02"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ctx = requireContext().applicationContext
        val clock = LongSupplier { System.currentTimeMillis() }
        // Same student always gets the same exercise.
        val seed = UserIdentity.displayId(ctx).hashCode().toLong()
        val scenario = MergeConflictGenerator.generate(seed, clock)
        val logger = CommandLogger(RoomGitLogSink(AppDatabase.getInstance(ctx).gitCommandDao()), SESSION_ID, clock)
        git = GitSimulationManager(scenario, logger, GitTelemetryCollector(clock), clock)

        add(GREY, "Your branch ${git.state.currentBranch} has diverged from ${scenario.targetBranch}.")
        add(GREY, "Merge ${scenario.targetBranch} into it, resolve the conflict, then push.")
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentGitBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val target = git.scenario.targetBranch
        binding.stepper.setStep(1)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.tvHeaderTitle.text = "${git.scenario.title} · Git"

        binding.btnRunCmd.setOnClickListener { runCommand() }
        binding.etCommand.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) { runCommand(); true } else false
        }
        listOf(
            binding.chip0 to "git status",
            binding.chip1 to "git merge $target",
            binding.chip2 to "git add .",
            binding.chip3 to "git commit -m \"Merge $target\""
        ).forEach { (chip, cmd) ->
            chip.text = "> $cmd"
            chip.setOnClickListener { binding.etCommand.setText(cmd) }
        }

        binding.btnClear.setOnClickListener { lines.clear(); render() }
        binding.btnTrash.setOnClickListener { lines.clear(); render() }

        binding.btnSaveConflict.setOnClickListener {
            val file = editingFile ?: return@setOnClickListener
            val text = binding.etConflictEditor.text.toString()
            git.writeFile(file, text)
            loadedContent = text
            add(GREY, "Saved $file. Next: git add $file, git commit, git push")
            refresh()
            render()
        }

        binding.btnKeepMine.setOnClickListener { keepSide(MergeConflictGenerator.Resolution.OURS, "your version") }
        binding.btnKeepTheirs.setOnClickListener { keepSide(MergeConflictGenerator.Resolution.THEIRS, "${git.scenario.targetBranch}'s version") }
        binding.btnKeepBoth.setOnClickListener { keepSide(MergeConflictGenerator.Resolution.BOTH, "both versions") }

        binding.btnViewGuide.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://git-scm.com/book/en/v2"))
            startActivity(intent)
        }
        
        binding.btnPush.setOnClickListener { advance() }

        refresh()
        render()
    }

    private fun runCommand() {
        val cmd = binding.etCommand.text.toString().trim()
        if (cmd.isEmpty()) return
        add(GREEN, "$", cmd)

        val result = git.execute(cmd)
        if (result.output.isNotEmpty()) {
            for (line in result.output.split("\n")) {
                val colour = when {
                    result.isError -> RED
                    line.startsWith("CONFLICT") || line.startsWith("warning:") -> AMBER
                    else -> GREY
                }
                add(colour, line)
            }
        }
        binding.etCommand.text?.clear()

        if (git.isTaskComplete && !announcedComplete) {
            announcedComplete = true
            add(GREEN, "Merge resolved and pushed. You can advance to the interview stage.")
            // Queue the Git results for Lebo's sync worker (stored locally, sent when online).
            GitTelemetryReporter.report(requireContext(), SESSION_ID, git.telemetry.snapshot())
        }
        refresh()
        render()
    }

    /** Resolves every conflict block in the open file in one tap, then saves it. Staging is still up to the student. */
    private fun keepSide(side: MergeConflictGenerator.Resolution, label: String) {
        val file = editingFile ?: return
        val resolved = MergeConflictGenerator.resolve(binding.etConflictEditor.text.toString(), side)
        binding.etConflictEditor.setText(resolved)
        git.writeFile(file, resolved)
        loadedContent = resolved
        add(GREY, "Kept $label in $file and saved it. Next: git add $file, git commit, git push")
        refresh()
        render()
    }

    /** Pushes the Git stage forward only once the merge is committed cleanly and pushed. */
    private fun advance() {
        if (git.isTaskComplete) {
            (requireActivity() as MainActivity).open(SpeechFragment())
        } else {
            Toast.makeText(requireContext(), nextHint(), Toast.LENGTH_LONG).show()
        }
    }

    private fun nextHint(): String {
        val st = git.state
        val target = git.scenario.targetBranch
        val marked = git.filesWithMarkers()
        return when {
            st.conflictedFiles.isNotEmpty() ->
                "Resolve ${st.conflictedFiles.first()}: tap Keep mine, Keep theirs or Keep both (or edit it), then git add it"
            st.isMerging -> "Conflicts fixed. Run git commit to finish the merge"
            !git.hasMergedTarget() -> "Merge $target into your branch: git merge $target"
            marked.isNotEmpty() ->
                "${marked.first()} still has conflict markers. Fix it in the Merge conflict card, then git add, git commit and git push"
            st.unstagedCount() > 0 -> "Stage your fix: git add ."
            st.stagedCount() > 0 -> "Commit your fix: git commit -m \"Fix conflict\""
            else -> "Push your work: git push"
        }
    }

    private fun refresh() {
        val st = git.state

        val unstaged = st.unstagedCount()
        binding.tvWorkingBadge.text = unstaged.toString()
        binding.tvWorkingSubtitle.text = if (unstaged == 1) "1 Changed file" else "$unstaged Changed files"

        val staged = st.stagedCount()
        binding.tvStagedBadge.text = staged.toString()
        binding.tvStagedSubtitle.text = if (staged == 1) "1 file staged" else "$staged files staged"

        val toPush = st.unpushedCommitCount()
        binding.tvCommittedSubtitle.text = if (toPush == 1) "1 Commit to push" else "$toPush Commits to push"
        binding.tvPushedSubtitle.text = if (st.isPushed) "Published" else "Not published"

        val head = st.head
        binding.cardCommitHistory.visibility = View.VISIBLE
        binding.tvEmptyCommitHistory.visibility = View.GONE
        binding.tvCommitMsg.text = head.message
        binding.tvBranchTag.text = st.currentBranch
        binding.tvCommitTime.text = DateUtils.getRelativeTimeSpanString(
            head.time, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS
        )
        binding.tvCommitMeta.text = "${head.shortId()} · ${head.parents.size} parent(s)"

        val target = st.conflictedFiles.firstOrNull() ?: git.filesWithMarkers().firstOrNull()
        if (target == null) {
            binding.cardConflict.visibility = View.GONE
            editingFile = null
            loadedContent = null
        } else {
            binding.cardConflict.visibility = View.VISIBLE
            val current = git.readFile(target)
            if (editingFile != target || current != loadedContent) {
                editingFile = target
                loadedContent = current
                binding.tvConflictFile.text = target
                binding.etConflictEditor.setText(current)
            }
        }
    }

    private fun add(colour: String, vararg parts: String) {
        lines.add("<font color='$colour'>${parts.joinToString(" ") { TextUtils.htmlEncode(it) }}</font>")
        while (lines.size > MAX_LINES) lines.removeAt(0)
    }

    private fun render() {
        binding.tvTerminal.text = HtmlCompat.fromHtml(lines.joinToString("<br>"), HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
