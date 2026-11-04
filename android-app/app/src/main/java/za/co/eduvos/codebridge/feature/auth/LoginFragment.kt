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
import za.co.eduvos.codebridge.core.network.dto.RegisterCompanyRequest
import za.co.eduvos.codebridge.core.network.dto.RegisterStudentRequest
import za.co.eduvos.codebridge.core.security.HashIdManager
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.databinding.FragmentLoginBinding
import za.co.eduvos.codebridge.feature.onboarding.HashIdGeneratedActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Track whether we are in Student or Company mode
    private var isCompanyMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nunito = ResourcesCompat.getFont(requireContext(), R.font.nunito_regular)
        binding.tilStudentNumber.typeface = nunito
        binding.tilInstitution.typeface = nunito
        binding.tilPassword.typeface = nunito

        val dark = ContextCompat.getColor(requireContext(), R.color.cb_dark_green)
        val green = ContextCompat.getColor(requireContext(), R.color.cb_green)
        binding.tvBrand.text = SpannableString("CodeBridge").apply {
            setSpan(ForegroundColorSpan(dark), 0, 4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(green), 4, 10, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.btnBack.setOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        binding.btnLogin.setOnClickListener { attemptLogin() }
        
        binding.tvRegisterLink.setOnClickListener { 
            if (isCompanyMode) showCompanyRegistrationDialog() else showStudentRegistrationDialog() 
        }
        
        binding.tvToggleMode.setOnClickListener { toggleMode() }
    }

    private fun toggleMode() {
        isCompanyMode = !isCompanyMode
        if (isCompanyMode) {
            binding.tvWelcomeTitle.text = "Company Login"
            binding.tvWelcomeSubtitle.text = "Enter your company credentials."
            binding.tilStudentNumber.hint = "Company Email"
            binding.tilInstitution.visibility = View.GONE
            binding.tvRegisterLink.text = "New Company? Register here"
            binding.tvToggleMode.text = "Are you a student? Click here"
        } else {
            binding.tvWelcomeTitle.text = "Student Login"
            binding.tvWelcomeSubtitle.text = "Enter your student credentials."
            binding.tilStudentNumber.hint = "Student Number"
            binding.tilInstitution.visibility = View.VISIBLE
            binding.tvRegisterLink.text = "New Student? Register here"
            binding.tvToggleMode.text = "Registering as a Company? Click here"
        }
    }

    private fun attemptLogin() {
        val idOrEmail = binding.etStudentNumber.text?.toString()?.trim().orEmpty()
        val institution = binding.etInstitution.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        binding.tilStudentNumber.error = if (idOrEmail.isEmpty()) "Required" else null
        binding.tilInstitution.error = if (!isCompanyMode && institution.isEmpty()) "Required" else null
        binding.tilPassword.error = if (password.isEmpty()) "Required" else null

        if (idOrEmail.isEmpty() || password.isEmpty() || (!isCompanyMode && institution.isEmpty())) return

        val role = if (isCompanyMode) "COMPANY" else "STUDENT"
        val appContext = requireContext().applicationContext
        
        // Only hash if it's a student
        val finalId = if (!isCompanyMode) {
            try { HashIdManager(appContext).generateHashId(idOrEmail) } catch (e: Exception) { return }
        } else { idOrEmail }

        binding.btnLogin.isEnabled = false
        
        // 🟢 FIX: Pass 4 arguments matching the new LoginRequest DTO
        val request = LoginRequest(finalId, institution, password, role)
        
        RetrofitClient.getInstance().apiService.login(request)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (_binding == null) return
                    binding.btnLogin.isEnabled = true
                    val token = response.body()?.token
                    
                    if (response.isSuccessful && !token.isNullOrEmpty()) {
                        TokenManager.saveToken(token)
                        UserIdentity.save(appContext, finalId)
                        if (!isCompanyMode) startActivity(Intent(requireContext(), HashIdGeneratedActivity::class.java))
                        else startActivity(Intent(requireContext(), AdminCompanyPortalActivity::class.java))
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

    private fun showStudentRegistrationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_register_student, null)
        val etStudentNumber = dialogView.findViewById<TextInputEditText>(R.id.etRegStudentNumber)
        val etInstitution = dialogView.findViewById<TextInputEditText>(R.id.etRegInstitution)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etRegPassword)
        val etConfirmPassword = dialogView.findViewById<TextInputEditText>(R.id.etRegConfirmPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Register Student")
            .setView(dialogView)
            .setPositiveButton("Register") { _, _ ->
                val num = etStudentNumber.text.toString().trim()
                val inst = etInstitution.text.toString().trim()
                val pass = etPassword.text.toString().trim()
                val confirmPass = etConfirmPassword.text.toString().trim()
                
                if (num.isNotEmpty() && inst.isNotEmpty() && pass.isNotEmpty()) {
                    if (pass != confirmPass) {
                        Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val hashId = HashIdManager(requireContext().applicationContext).generateHashId(num)
                    
                    // 🟢 FIX: Pass 3 arguments matching the new RegisterStudentRequest DTO
                    val request = RegisterStudentRequest(hashId, inst, pass)
                    
                    RetrofitClient.getInstance().apiService.registerStudent(request).enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            if (response.isSuccessful && response.body() != null) {
                                TokenManager.saveToken(response.body()!!.token)
                                UserIdentity.save(requireContext().applicationContext, hashId)
                                startActivity(Intent(requireContext(), HashIdGeneratedActivity::class.java))
                                requireActivity().finish()
                            } else Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun showCompanyRegistrationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_register_company, null)
        val etCompanyName = dialogView.findViewById<TextInputEditText>(R.id.etRegCompanyName)
        val etEmail = dialogView.findViewById<TextInputEditText>(R.id.etRegEmail)
        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etRegPassword)
        val etConfirmPassword = dialogView.findViewById<TextInputEditText>(R.id.etRegConfirmPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Register Company")
            .setView(dialogView)
            .setPositiveButton("Register") { _, _ ->
                val name = etCompanyName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val pass = etPassword.text.toString().trim()
                val confirmPass = etConfirmPassword.text.toString().trim()
                
                if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                    if (pass != confirmPass) {
                        Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    val request = RegisterCompanyRequest(email, name, pass)
                    RetrofitClient.getInstance().apiService.registerCompany(request).enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            if (response.isSuccessful && response.body() != null) {
                                TokenManager.saveToken(response.body()!!.token)
                                startActivity(Intent(requireContext(), AdminCompanyPortalActivity::class.java))
                                requireActivity().finish()
                            } else Toast.makeText(requireContext(), "Registration failed", Toast.LENGTH_SHORT).show()
                        }
                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            Toast.makeText(requireContext(), "Network Error", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }.setNegativeButton("Cancel", null).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
