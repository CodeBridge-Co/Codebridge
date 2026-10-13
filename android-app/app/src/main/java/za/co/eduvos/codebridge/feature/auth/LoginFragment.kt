package za.co.eduvos.codebridge.feature.auth

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.TokenManager
import za.co.eduvos.codebridge.core.network.dto.LoginRequest
import za.co.eduvos.codebridge.core.network.dto.LoginResponse
import za.co.eduvos.codebridge.core.security.HashIdManager
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.databinding.FragmentLoginBinding
import za.co.eduvos.codebridge.feature.onboarding.HashIdGeneratedActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nunito = ResourcesCompat.getFont(requireContext(), R.font.nunito_regular)
        binding.tilStudentNumber.typeface = nunito
        binding.tilInstitution.typeface = nunito

        val dark = ContextCompat.getColor(requireContext(), R.color.cb_dark_green)
        val green = ContextCompat.getColor(requireContext(), R.color.cb_green)
        binding.tvBrand.text = SpannableString("CodeBridge").apply {
            setSpan(ForegroundColorSpan(dark), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(green), 4, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnLogin.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        val studentNumber = binding.etStudentNumber.text?.toString()?.trim().orEmpty()
        val institutionId = binding.etInstitution.text?.toString()?.trim().orEmpty()

        binding.tilStudentNumber.error = if (studentNumber.isEmpty()) "Enter your student number" else null
        binding.tilInstitution.error = if (institutionId.isEmpty()) "Enter your institution ID" else null
        if (studentNumber.isEmpty() || institutionId.isEmpty()) return

        val appContext = requireContext().applicationContext
        val hashId = try {
            HashIdManager(appContext).generateHashId(studentNumber)
        } catch (e: RuntimeException) {
            Toast.makeText(requireContext(), "Couldn't create a secure ID on this device", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnLogin.isEnabled = false
        RetrofitClient.getInstance().apiService.login(LoginRequest(hashId, institutionId))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (_binding == null) return
                    binding.btnLogin.isEnabled = true
                    val token = response.body()?.token
                    if (response.isSuccessful && !token.isNullOrEmpty()) {
                        TokenManager.saveToken(token)
                        UserIdentity.save(appContext, hashId)
                        startActivity(Intent(requireContext(), HashIdGeneratedActivity::class.java))
                        requireActivity().finish()
                    } else {
                        Toast.makeText(requireContext(), "Login failed (${response.code()})", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    if (_binding == null) return
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(requireContext(), "Can't reach the server: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
