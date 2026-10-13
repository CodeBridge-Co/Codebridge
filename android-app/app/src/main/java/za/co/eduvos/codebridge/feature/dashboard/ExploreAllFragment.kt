package za.co.eduvos.codebridge.feature.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.databinding.FragmentExploreAllBinding
import za.co.eduvos.codebridge.feature.events.EventsFragment
import za.co.eduvos.codebridge.feature.problems.ChallengesListFragment

class ExploreAllFragment : Fragment() {

    private var _binding: FragmentExploreAllBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreAllBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.linkTech.setOnClickListener {
            (requireActivity() as MainActivity).open(ChallengesListFragment())
        }

        binding.linkCompany.setOnClickListener {
            (requireActivity() as MainActivity).open(ChallengesListFragment())
        }

        binding.linkEvents.setOnClickListener {
            (requireActivity() as MainActivity).open(EventsFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
