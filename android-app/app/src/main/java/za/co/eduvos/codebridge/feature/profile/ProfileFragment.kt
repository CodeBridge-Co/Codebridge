package za.co.eduvos.codebridge.feature.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.TokenManager
import za.co.eduvos.codebridge.core.network.dto.LeaderboardDto
import za.co.eduvos.codebridge.data.UserIdentity
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
        val prefs = requireContext().getSharedPreferences("codebridge_prefs", Context.MODE_PRIVATE)
        val completed = prefs.getInt("completed_$hashId", 0)
        val localXp = prefs.getInt("total_xp_$hashId", 0)

        binding.tvName.text = "Developer $hashId"
        binding.tvHandle.text = "@$hashId"
        binding.tvLocation.text = if (UserIdentity.isLocationPermitted(requireContext())) "Johannesburg, South Africa" else "Location not shared"
        binding.tvSolvedCount.text = completed.toString()
        binding.tvTotalXp.text = localXp.toString()
        binding.tvAvatar.text = hashId.take(2).uppercase()

        // Default to 0 streak until backend responds
        binding.tvStreak.text = "0"

        fetchRealStats(hashId)

        binding.btnSubmissions.setOnClickListener {
            (requireActivity() as MainActivity).selectTab(MainActivity.Tab.PROGRESS)
        }

        binding.btnLogout.setOnClickListener {
            TokenManager.clear()
            prefs.edit().remove("user_role").apply()

            val intent = Intent(requireContext(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun fetchRealStats(hashId: String) {
        RetrofitClient.getInstance().apiService.getLeaderboard().enqueue(object : Callback<List<LeaderboardDto>> {
            override fun onResponse(call: Call<List<LeaderboardDto>>, response: Response<List<LeaderboardDto>>) {
                if (_binding == null) return

                val list = response.body()
                if (response.isSuccessful && list != null) {
                    val myStats = list.find { it.hashId == hashId }
                    if (myStats != null) {
                        // Update XP with the live database value (overrides local cache)
                        binding.tvTotalXp.text = myStats.xp.toString()
                        binding.tvStreak.text = myStats.streak.toString()
                    }
                }
            }
            override fun onFailure(call: Call<List<LeaderboardDto>>, t: Throwable) {
                // Keep local cache if network fails
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}