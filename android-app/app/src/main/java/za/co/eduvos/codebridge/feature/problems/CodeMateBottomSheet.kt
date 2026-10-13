package za.co.eduvos.codebridge.feature.problems

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.backend.HashIdManager
import za.co.eduvos.codebridge.databinding.DialogCodemateChatBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CodeMateBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogCodemateChatBinding? = null
    private val binding get() = _binding!!

    private var remainingAttempts: Int = 3
    private var onAttemptUsed: ((Int) -> Unit)? = null

    companion object {
        fun newInstance(attemptsLeft: Int, onAttemptUsed: (Int) -> Unit): CodeMateBottomSheet {
            val sheet = CodeMateBottomSheet()
            sheet.remainingAttempts = attemptsLeft
            sheet.onAttemptUsed = onAttemptUsed
            return sheet
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogCodemateChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hashId = HashIdManager.getOrGenerateHashId(requireContext())
        binding.tvInitialGreeting.text = "Hey $hashId! 👋"

        binding.tvAttemptsLeft.text = "$remainingAttempts left"
        binding.btnClose.setOnClickListener { dismiss() }

        binding.btnSend.setOnClickListener {
            val query = binding.etChatMessage.text?.toString()?.trim().orEmpty()
            if (query.isNotEmpty() && remainingAttempts > 0) {
                sendMessage(query)
            }
        }

        binding.chipHint1.setOnClickListener {
            if (remainingAttempts > 0) sendMessage("Can you show me a code example?")
        }
        binding.chipHint2.setOnClickListener {
            if (remainingAttempts > 0) sendMessage("Can you explain how HashMaps work in Java?")
        }
        binding.chipHint3.setOnClickListener {
            if (remainingAttempts > 0) sendMessage("Help me debug my solution")
        }
        binding.chipHint4.setOnClickListener {
            if (remainingAttempts > 0) sendMessage("Give me a practice problem")
        }
    }

    private fun sendMessage(userMsg: String) {
        if (remainingAttempts <= 0) return

        remainingAttempts--
        onAttemptUsed?.invoke(remainingAttempts)
        binding.tvAttemptsLeft.text = "$remainingAttempts left"

        // Add user chat bubble
        addUserBubble(userMsg)
        binding.etChatMessage.text?.clear()

        // Generate BridgeAI response
        val aiResponse = when {
            userMsg.contains("HashMap", ignoreCase = true) || userMsg.contains("explain", ignoreCase = true) ->
                "BridgeAI: Of course! Here's a simple explanation of how HashMaps work in Java:\n\nWhat is a HashMap?\nA HashMap is a data structure that stores key-value pairs using a hash function.\n\nHashMap<String, Integer> map = new HashMap<>();\nmap.put(\"apple\", 1);\nmap.get(\"apple\"); // returns 1"
            userMsg.contains("debug", ignoreCase = true) ->
                "BridgeAI: Make sure you handle edge cases such as empty inputs or missing target keys before indexing!"
            else ->
                "BridgeAI: Here is a quick code example:\n\nMap<Integer, Integer> map = new HashMap<>();\nif (map.containsKey(target - nums[i])) return new int[]{map.get(target - nums[i]), i};"
        }
        addAiBubble(aiResponse)
    }

    private fun addUserBubble(msg: String) {
        val tv = TextView(requireContext()).apply {
            text = msg
            textSize = 13f
            setPadding(20, 14, 20, 14)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_ink))
            setBackgroundResource(R.drawable.bg_chip_selected)
        }
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(40, 10, 0, 10)
            gravity = Gravity.END
        }
        binding.chatContainer.addView(tv, lp)
    }

    private fun addAiBubble(msg: String) {
        val tv = TextView(requireContext()).apply {
            text = msg
            textSize = 13f
            setPadding(20, 14, 20, 14)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.cb_ink))
            setBackgroundResource(R.drawable.bg_card_outline)
        }
        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 10, 40, 10)
        }
        binding.chatContainer.addView(tv, lp)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
