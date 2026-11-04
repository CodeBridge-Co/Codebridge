package za.co.eduvos.codebridge.feature.problems

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.dto.AiAssistRequest
import za.co.eduvos.codebridge.core.network.dto.AiAssistResponse
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.databinding.DialogCodemateChatBinding

class CodeMateBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogCodemateChatBinding? = null
    private val binding get() = _binding!!

    private var remainingAttempts: Int = 3
    private var onAttemptUsed: ((Int) -> Unit)? = null
    
    // NEW: Variables to hold the context for the AI
    private var sessionId: String = "1"
    private var problemTitle: String = "Unknown"
    private var currentCode: String = ""

    companion object {
        fun newInstance(
            sessionId: String,
            problemTitle: String,
            currentCode: String,
            attemptsLeft: Int, 
            onAttemptUsed: (Int) -> Unit
        ): CodeMateBottomSheet {
            val sheet = CodeMateBottomSheet()
            sheet.sessionId = sessionId
            sheet.problemTitle = problemTitle
            sheet.currentCode = currentCode
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

        val hashId = UserIdentity.displayId(requireContext())
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
            if (remainingAttempts > 0) sendMessage("Can you explain how HashMaps work?")
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

        // Add user chat bubble and clear input
        addUserBubble(userMsg)
        binding.etChatMessage.text?.clear()

        // Temporarily disable UI while waiting for AI
        binding.btnSend.isEnabled = false
        binding.tvAttemptsLeft.text = "Thinking..."

        // CALL THE REAL AI BACKEND
        val request = AiAssistRequest(sessionId, problemTitle, currentCode)
        
        RetrofitClient.getInstance().apiService.askAi(request).enqueue(object : Callback<AiAssistResponse> {
            override fun onResponse(call: Call<AiAssistResponse>, response: Response<AiAssistResponse>) {
                if (_binding == null) return
                binding.btnSend.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val aiResponse = response.body()!!
                    
                    if (aiResponse.isAllowed) {
                        remainingAttempts = aiResponse.remainingUses
                        onAttemptUsed?.invoke(remainingAttempts)
                        binding.tvAttemptsLeft.text = "$remainingAttempts left"
                        
                        // Add the AI's real hint to the chat
                        addAiBubble(aiResponse.hintText)
                    } else {
                        binding.tvAttemptsLeft.text = "0 left"
                        addAiBubble("BridgeAI: You have reached your AI limit for this challenge.")
                    }
                } else {
                    binding.tvAttemptsLeft.text = "$remainingAttempts left"
                    addAiBubble("BridgeAI: I couldn't reach the server. Please try again.")
                }
            }

            override fun onFailure(call: Call<AiAssistResponse>, t: Throwable) {
                if (_binding == null) return
                binding.btnSend.isEnabled = true
                binding.tvAttemptsLeft.text = "$remainingAttempts left"
                addAiBubble("BridgeAI: Network error. Check your connection.")
            }
        })
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
