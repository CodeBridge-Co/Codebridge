package za.co.eduvos.codebridge.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.backend.HashIdManager
import za.co.eduvos.codebridge.backend.ProgressRepository
import za.co.eduvos.codebridge.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hashId = HashIdManager.getOrGenerateHashId(requireContext())
        val completed = ProgressRepository.getCompletedCount(hashId)
        val overallPct = ProgressRepository.getOverallProgressPercent(hashId)

        binding.tvName.text = "Developer $hashId"
        binding.tvHandle.text = "@$hashId"
        binding.tvLocation.text = if (HashIdManager.isLocationPermitted(requireContext())) "Johannesburg, South Africa" else "Location not shared"
        binding.tvSolvedCount.text = completed.toString()
        binding.tvAvgScore.text = "$overallPct%"
        binding.tvRank.text = if (completed > 0) "Top ${100 - overallPct}%" else "Unranked"
        binding.tvAvatar.text = hashId.take(2).uppercase()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
