package za.co.eduvos.codebridge.feature.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.data.UserIdentity
import za.co.eduvos.codebridge.databinding.FragmentDashboardBinding
import za.co.eduvos.codebridge.feature.events.EventsFragment
import za.co.eduvos.codebridge.feature.problems.ChallengesListFragment
import za.co.eduvos.codebridge.feature.problems.ProblemsFragment
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }

        // Retrieve generated Hash ID for the user (e.g., CB7F4A2D9E)
        val hashId = UserIdentity.displayId(requireContext())

        // Greet user with their unique Hash ID
        binding.tvGreeting.text = "$greeting, $hashId."
        binding.tvAvatar.text = hashId.take(2).uppercase()

        binding.btnContinue.setOnClickListener { main().open(ProblemsFragment()) }
        binding.linkExploreAll.setOnClickListener { main().open(ExploreAllFragment()) }
        binding.link0.setOnClickListener { main().open(ChallengesListFragment()) }
        binding.link1.setOnClickListener { main().open(ExploreAllFragment()) }
        binding.link2.setOnClickListener { main().open(EventsFragment()) }
        binding.linkViewAllEvents.setOnClickListener { main().open(EventsFragment()) }
    }

    private fun main() = requireActivity() as MainActivity

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
