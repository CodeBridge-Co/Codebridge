package za.co.eduvos.codebridge.feature.speech

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.ui.Week3Label
import za.co.eduvos.codebridge.databinding.FragmentSpeechBinding
import za.co.eduvos.codebridge.feature.results.ResultsFragment
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

class SpeechFragment : Fragment() {

    private var _binding: FragmentSpeechBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var recording = true
    private var seconds = 0
    private var tick = 0

    private val questions = listOf(
        "Explain your overall approach",
        "Why did you choose a Hash Map?",
        "What is the time and space complexity?"
    )
    private var currentQuestionIndex = 0

    private val loop = object : Runnable {
        override fun run() {
            if (!recording || _binding == null) return
            tick++
            val a = abs(sin(tick * 0.25)).toFloat() * 0.7f + Random.nextFloat() * 0.3f
            binding.waveform.push(a)
            if (tick % 10 == 0) { seconds++; updateTimer() }
            handler.postDelayed(this, 100)
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentSpeechBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Week3Label.attach(binding.root, "recording and analysis coming. Demo only")
        binding.stepper.setStep(2)
        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }

        binding.btnRecord.setOnClickListener { if (recording) pause() else resume() }
        binding.btnPause.setOnClickListener { if (recording) pause() else resume() }
        binding.btnDiscard.setOnClickListener {
            pause(); seconds = 0; tick = 0; binding.waveform.clear(); updateTimer()
            binding.tvRecState.text = "READY"
            binding.tvTranscript.text = "Start recording to see your words appear here."
        }

        binding.btnSubmitAnswer.setOnClickListener {
            pause()
            Toast.makeText(requireContext(), "Question ${currentQuestionIndex + 1} recorded & submitted!", Toast.LENGTH_SHORT).show()
            if (currentQuestionIndex < questions.size - 1) {
                currentQuestionIndex++
                loadQuestion(currentQuestionIndex)
            } else {
                Toast.makeText(requireContext(), "All 3 questions recorded! Review & view results.", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnEnd.setOnClickListener {
            pause()
            (requireActivity() as MainActivity).open(ResultsFragment())
        }

        loadQuestion(0)
    }

    private fun loadQuestion(index: Int) {
        currentQuestionIndex = index
        seconds = 0
        tick = 0
        binding.waveform.clear()
        updateTimer()

        binding.tvQuestionNo.text = "QUESTION ${index + 1} OF ${questions.size}"
        binding.tvQuestion.text = questions[index]
        binding.tvTranscript.text = when (index) {
            0 -> "I would use a hash map to store each number and its index..."
            1 -> "A hash map provides O(1) average lookup time for complements..."
            else -> "Time complexity is O(N) and space complexity is O(N)..."
        }
        resume()
    }

    private fun resume() {
        recording = true
        binding.tvRecState.text = "RECORDING"
        handler.post(loop)
    }

    private fun pause() {
        recording = false
        handler.removeCallbacks(loop)
        binding.tvRecState.text = "PAUSED"
    }

    private fun updateTimer() {
        val t = "%02d:%02d".format(seconds / 60, seconds % 60)
        binding.tvTimer.text = t
        binding.tvTimerTop.text = t
    }

    override fun onDestroyView() {
        recording = false
        handler.removeCallbacks(loop)
        super.onDestroyView()
        _binding = null
    }
}
