package za.co.eduvos.codebridge.feature.speech

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
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
        "Explain your overall approach to solving this problem.",
        "What is the time and space complexity of your solution?",
        "How would you handle edge cases or empty inputs?"
    )
    private var currentQuestionIndex = 0

    companion object {
        private const val SESSION_ID = 1
        private const val TICK_MS = 100L
        private const val FINAL_RESULT_GRACE_MS = 600L
        private const val FINAL_RESULT_WAIT_MS = 3500L
        private const val POLL_MS = 200L
        private const val HINT = "Tap the microphone and explain your answer out loud."
    }

    private val micPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (_binding == null) return@registerForActivityResult
        if (granted) startListening() else {
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

        // Clear old speech data for this new session
        val prefs = requireContext().getSharedPreferences("codebridge_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("speech_answers", 0).putFloat("speech_density_sum", 0f).apply()

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
                    if (message.contains("speech model", ignoreCase = true) ||
                        message.contains("not available", ignoreCase = true) ||
                        message.contains("isn't available", ignoreCase = true)) {
                        promptTypedAnswer(message)
                    }
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

    private fun toggle() {
        if (recorder.state == SpeechRecorder.State.RECORDING) pauseRecording() else requestStart()
    }

    private fun requestStart() {
        if (!recognizerAvailable) {
            promptTypedAnswer()
            return
        }
        val granted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
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

    private fun promptTypedAnswer(reason: String? = null) {
        val input = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 4
            hint = "Type your explanation"
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Speech recognition isn't available")
            .setMessage((reason ?: "This device can't transcribe speech.") + "\n\nType your answer instead.")
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

    private fun submitAnswer() {
        pauseRecording()
        val deadline = SystemClock.uptimeMillis() + FINAL_RESULT_WAIT_MS
        fun poll() {
            if (_binding == null) return
            if (recorder.transcript().isNotBlank() || SystemClock.uptimeMillis() >= deadline) {
                finishAnswer()
            } else {
                handler.postDelayed({ poll() }, POLL_MS)
            }
        }
        handler.postDelayed({ poll() }, FINAL_RESULT_GRACE_MS)
    }

    private fun finishAnswer() {
        val transcript = recorder.transcript().trim()
        if (transcript.isEmpty()) {
            if (recorder.elapsedMs() > 2000) {
                promptTypedAnswer("No words were recognised from your recording.")
            } else {
                Toast.makeText(requireContext(), "Record your answer first.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val analysis = KeywordAnalyzer.analyze(transcript)
        sink.save(SESSION_ID, transcript, analysis.density, recorder.elapsedMs(), System.currentTimeMillis())
        recorder.stop()

        // 🟢 SAVE THE REAL SPEECH METRICS TO PREFERENCES 🟢
        val prefs = requireContext().getSharedPreferences("codebridge_prefs", Context.MODE_PRIVATE)
        val currentAnswers = prefs.getInt("speech_answers", 0) + 1
        val currentDensity = prefs.getFloat("speech_density_sum", 0f) + analysis.density.toFloat()

        prefs.edit()
            .putInt("speech_answers", currentAnswers)
            .putFloat("speech_density_sum", currentDensity)
            .putBoolean("stage3_completed", true)
            .apply()

        val pct = Math.round(analysis.density * 100)
        Toast.makeText(requireContext(), "Answer ${currentQuestionIndex + 1} saved: $pct% density", Toast.LENGTH_LONG).show()

        if (currentQuestionIndex < questions.size - 1) {
            loadQuestion(currentQuestionIndex + 1)
        } else {
            Toast.makeText(requireContext(), "All answers recorded. Tap End to see results.", Toast.LENGTH_LONG).show()
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