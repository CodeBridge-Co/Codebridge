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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.TokenManager
import za.co.eduvos.codebridge.core.network.dto.LoginRequest
import za.co.eduvos.codebridge.core.network.dto.LoginResponse
import za.co.eduvos.codebridge.core.network.dto.RegisterCompanyRequest // Ensure this DTO exists!
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

        // NEW: Click listener for the Register Company text
        binding.tvRegisterCompany.setOnClickListener {
            showCompanyRegistrationDialog()
        }
    }

    private fun attemptLogin() {
        val input1 = binding.etStudentNumber.text?.toString()?.trim().orEmpty()
        val input2 = binding.etInstitution.text?.toString()?.trim().orEmpty()

        binding.tilStudentNumber.error = if (input1.isEmpty()) "Enter your ID/Email" else null
        binding.tilInstitution.error = if (input2.isEmpty()) "Enter your institution/password" else null
        if (input1.isEmpty() || input2.isEmpty()) return

        val selectedRole = when (binding.roleGroup.checkedRadioButtonId) {
            R.id.radioCompany -> "COMPANY"
            R.id.radioAdmin -> "ADMIN"
            else -> "STUDENT"
        }

        val appContext = requireContext().applicationContext
        
        // Only hash if it's a STUDENT. Companies use raw email.
        val finalId = if (selectedRole == "STUDENT") {
            try {
                HashIdManager(appContext).generateHashId(input1)
            } catch (e: RuntimeException) {
                Toast.makeText(requireContext(), "Couldn't create a secure ID", Toast.LENGTH_LONG).show()
                return
            }
        } else {
            input1 
        }

        binding.btnLogin.isEnabled = false
        
        RetrofitClient.getInstance().apiService.login(LoginRequest(finalId, input2, selectedRole))
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (_binding == null) return
                    binding.btnLogin.isEnabled = true
                    val token = response.body()?.token
                    
                    if (response.isSuccessful && !token.isNullOrEmpty()) {
                        TokenManager.saveToken(token)
                        UserIdentity.save(appContext, finalId)
                        
                        if (selectedRole == "STUDENT") {
                            startActivity(Intent(requireContext(), HashIdGeneratedActivity::class.java))
                        } else {
                            startActivity(Intent(requireContext(), AdminCompanyPortalActivity::class.java))
                        }
                        requireActivity().finish()
                    } else {
                        Toast.makeText(requireContext(), "Login failed (${response.code()})", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    if (_binding == null) return
                    binding.btnLogin.isEnabled = true
                    Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    // NEW: Registration Dialog Function (Properly separated now)
    private fun showCompanyRegistrationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_register_company, null)
        val etCompanyName = dialogView.findViewById<TextInputEditText>(R.id.etRegCompanyName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etRegPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Register Company")
            .setView(dialogView)
            .setPositiveButton("Register") { _, _ ->
                val name = etCompanyName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val pass = etPassword.text.toString().trim()

                if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                    executeCompanyRegistration(name, email, pass)
                } else {
                    Toast.makeText(requireContext(), "All fields required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // NEW: Registration API Call Function
    private fun executeCompanyRegistration(name: String, email: String, pass: String) {
        val request = RegisterCompanyRequest(email, name, pass)
        
        RetrofitClient.getInstance().apiService.registerCompany(request)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        TokenManager.saveToken(response.body()!!.token)
                        Toast.makeText(requireContext(), "Company Registered!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireContext(), AdminCompanyPortalActivity::class.java))
                        requireActivity().finish()
                    } else {
                        Toast.makeText(requireContext(), "Registration failed: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
