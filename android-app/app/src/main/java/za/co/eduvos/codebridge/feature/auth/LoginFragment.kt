package za.co.eduvos.codebridge.feature.auth

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import androidx.core.content.res.ResourcesCompat
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.databinding.FragmentLoginBinding

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
        binding.tilEmail.typeface = nunito
        binding.tilPassword.typeface = nunito

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

        binding.btnGoogle.setOnClickListener {
            // TODO: Google Sign-In
            Toast.makeText(requireContext(), "Google sign-in coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.tvRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.tvForgot.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, ForgotPasswordFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        binding.tilEmail.error = if (email.isEmpty()) "Enter your email or username" else null
        binding.tilPassword.error = if (password.length < 6) "Minimum 6 characters" else null

        if (email.isNotEmpty() && password.length >= 6) {
            // TODO: replace with real authentication
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}