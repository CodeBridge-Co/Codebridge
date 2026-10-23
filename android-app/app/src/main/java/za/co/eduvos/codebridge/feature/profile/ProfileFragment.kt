package za.co.eduvos.codebridge.feature.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.backend.ProgressRepository
import za.co.eduvos.codebridge.core.network.TokenManager
import za.co.eduvos.codebridge.databinding.FragmentProfileBinding
import za.co.eduvos.codebridge.feature.auth.AuthActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hashId = UserIdentity.displayId(requireContext())
        val completed = ProgressRepository.getCompletedCount(hashId)
        val overallPct = ProgressRepository.getOverallProgressPercent(hashId)

        binding.tvName.text = "Developer $hashId"
        binding.tvHandle.text = "@$hashId"
        binding.tvLocation.text = if (UserIdentity.isLocationPermitted(requireContext())) "Johannesburg, South Africa" else "Location not shared"
        binding.tvSolvedCount.text = completed.toString()
        binding.tvAvgScore.text = "$overallPct%"
        binding.tvRank.text = if (completed > 0) "Top ${100 - overallPct}%" else "Unranked"
        binding.tvAvatar.text = hashId.take(2).uppercase()

        // 1. Edit Profile (Placeholder)
        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Profile editing coming in V2!", Toast.LENGTH_SHORT).show()
        }

        // 2. Logout Logic
        binding.btnLogout.setOnClickListener {
            // Clear the JWT token so the app forgets the user
            TokenManager.clear()
            
            // Route back to AuthActivity (Login screen)
            val intent = Intent(requireContext(), AuthActivity::class.java)
            // Clear the backstack so they can't press "Back" to get into the app again
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            
            // Destroy the current activity
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
