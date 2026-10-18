package za.co.eduvos.codebridge.feature.speech

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.core.database.AppDatabase
import za.co.eduvos.codebridge.core.speech.KeywordAnalyzer
import za.co.eduvos.codebridge.core.speech.RoomSpeechSink
import za.co.eduvos.codebridge.core.speech.SpeechRecognizerManager
import za.co.eduvos.codebridge.core.speech.SpeechRecorder
import za.co.eduvos.codebridge.databinding.FragmentSpeechBinding
import za.co.eduvos.codebridge.feature.results.ResultsFragment
import kotlin.math.max

class SpeechFragment : Fragment() {

    private var _binding: FragmentSpeechBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var recorder: SpeechRecorder
    private lateinit var sink: RoomSpeechSink
    private var recognizer: SpeechRecognizerManager? = null
    private var recognizerAvailable = false
    private var smoothedLevel = 0f

    private val questions = listOf(
        "Explain your overall approach",
        "Why did you choose a Hash Map?",
        "What is the time and space complexity?"
    )
    private var currentQuestionIndex = 0

    companion object {
        /** Placeholder until assessment sessions are created in the app; the answers are still stored. */
        private const val SESSION_ID = 1
        private const val TICK_MS = 100L
        private const val FINAL_RESULT_GRACE_MS = 600L
        private const val HINT = "Tap the microphone and explain your answer out loud."
    }

    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (_binding == null) return@registerForActivityResult
        if (granted) {
            startListening()
        } else {
            binding.tvRecState.text = "MIC BLOCKED"
            Toast.makeText(requireContext(), "Allow the microphone to record your answer.", Toast.LENGTH_LONG).show()
        }
    }

    private val loop = object : Runnable {
        override fun run() {
            if (_binding == null) return
            smoothedLevel = max(recorder.level(), smoothedLevel * 0.8f)
            binding.waveform.push(smoothedLevel)
            updateTimer()
            showTranscript()
            if (recorder.state == SpeechRecorder.State.RECORDING) handler.postDelayed(this, TICK_MS)
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentSpeechBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ctx = requireContext()
        recorder = SpeechRecorder { System.currentTimeMillis() }
        sink = RoomSpeechSink(AppDatabase.getInstance(ctx.applicationContext).speechTranscriptDao())

        recognizerAvailable = SpeechRecognizerManager.isAvailable(ctx)
        if (recognizerAvailable) {
            recognizer = SpeechRecognizerManager(ctx, object : SpeechRecognizerManager.Listener {
                override fun onPartial(text: String) { recorder.onPartialResult(text) }
                override fun onFinal(text: String) { recorder.onFinalResult(text) }
                override fun onLevel(rmsDb: Float) { recorder.onRms(rmsDb) }
                override fun onError(message: String, fatal: Boolean) {
                    if (_binding == null) return
                    if (fatal) pauseRecording()
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }
            })
        }

        binding.stepper.setStep(2)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.btnRecord.setOnClickListener { toggle() }
        binding.btnPause.setOnClickListener { toggle() }
        binding.btnDiscard.setOnClickListener { discard() }
        binding.btnSubmitAnswer.setOnClickListener { submitAnswer() }
        binding.btnEnd.setOnClickListener {
            pauseRecording()
            (requireActivity() as MainActivity).open(ResultsFragment())
        }

        loadQuestion(0)
    }

    // ---- recording control ----

    private fun toggle() {
        if (recorder.state == SpeechRecorder.State.RECORDING) pauseRecording() else requestStart()
    }

    private fun requestStart() {
        if (!recognizerAvailable) {
            promptTypedAnswer()
            return
        }
        val granted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) startListening() else micPermission.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun startListening() {
        if (recorder.state == SpeechRecorder.State.IDLE) recorder.start() else recorder.resume()
        recognizer?.start()
        binding.tvRecState.text = "RECORDING"
        handler.removeCallbacks(loop)
        handler.post(loop)
    }

    private fun pauseRecording() {
        recorder.pause()
        recognizer?.pause()
        handler.removeCallbacks(loop)
        smoothedLevel = 0f
        if (_binding != null) {
            binding.tvRecState.text = "PAUSED"
            showTranscript()
        }
    }

    private fun discard() {
        recognizer?.pause()
        handler.removeCallbacks(loop)
        recorder.reset()
        smoothedLevel = 0f
        binding.waveform.clear()
        updateTimer()
        binding.tvRecState.text = "READY"
        binding.tvTranscript.text = HINT
    }

    /** For devices with no speech recognizer: the answer is typed instead of spoken. */
    private fun promptTypedAnswer() {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 4
            hint = "Type your explanation"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Speech recognition isn't available")
            .setMessage("This device can't transcribe speech. Type your answer instead.")
            .setView(input)
            .setPositiveButton("Use this answer") { _, _ ->
                if (_binding == null) return@setPositiveButton
                if (recorder.state == SpeechRecorder.State.IDLE) recorder.start()
                recorder.onFinalResult(input.text.toString())
                recorder.pause()
                binding.tvRecState.text = "TYPED"
                showTranscript()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---- answers ----

    private fun submitAnswer() {
        pauseRecording()
        // The recognizer delivers its last words just after pause, so give it a moment.
        handler.postDelayed({ if (_binding != null) finishAnswer() }, FINAL_RESULT_GRACE_MS)
    }

    private fun finishAnswer() {
        val transcript = recorder.transcript().trim()
        if (transcript.isEmpty()) {
            Toast.makeText(requireContext(), "Record your answer first.", Toast.LENGTH_SHORT).show()
            return
        }
        val analysis = KeywordAnalyzer.analyze(transcript)
        sink.save(SESSION_ID, transcript, analysis.density, recorder.elapsedMs(), System.currentTimeMillis())
        recorder.stop()

        val pct = Math.round(analysis.density * 100)
        val terms = analysis.occurrences.keys.take(3).joinToString(", ")
        Toast.makeText(
            requireContext(),
            "Answer ${currentQuestionIndex + 1} saved: $pct% technical terms" + if (terms.isEmpty()) "" else " ($terms)",
            Toast.LENGTH_LONG
        ).show()

        if (currentQuestionIndex < questions.size - 1) {
            loadQuestion(currentQuestionIndex + 1)
        } else {
            Toast.makeText(requireContext(), "All ${questions.size} answers recorded. Tap End to see results.", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadQuestion(index: Int) {
        currentQuestionIndex = index
        recognizer?.pause()
        handler.removeCallbacks(loop)
        recorder.reset()
        smoothedLevel = 0f
        binding.waveform.clear()
        updateTimer()
        binding.tvRecState.text = "READY"
        binding.tvQuestionNo.text = "QUESTION ${index + 1} OF ${questions.size}"
        binding.tvQuestion.text = questions[index]
        binding.tvTranscript.text = HINT
    }

    private fun showTranscript() {
        val t = recorder.transcript()
        binding.tvTranscript.text = if (t.isEmpty()) HINT else t
    }

    private fun updateTimer() {
        val seconds = recorder.elapsedMs() / 1000
        val t = "%02d:%02d".format(seconds / 60, seconds % 60)
        binding.tvTimer.text = t
        binding.tvTimerTop.text = t
    }

    override fun onPause() {
        super.onPause()
        // Never keep the microphone open while the screen is in the background.
        if (_binding != null && recorder.state == SpeechRecorder.State.RECORDING) pauseRecording()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        recognizer?.destroy()
        recognizer = null
        super.onDestroyView()
        _binding = null
    }
}
