package za.co.eduvos.codebridge.feature.company

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.data.Challenge
import za.co.eduvos.codebridge.data.Company
import za.co.eduvos.codebridge.databinding.FragmentCompanyChallengesBinding

class CompanyChallengesFragment : Fragment() {

    private var _binding: FragmentCompanyChallengesBinding? = null
    private val binding get() = _binding!!

    private val companyList = listOf(
        Company(1, "Amazon", "Frontend · Backend · System Design", 12, "Solve real-world problems used by Amazon engineers. Build your skills and get noticed!", emptyList()),
        Company(2, "Microsoft", "Full Stack · Cloud · AI", 10, "Solve cloud, AI prompt routing, and scalable backend challenges built by Microsoft teams.", emptyList()),
        Company(3, "Deloitte", "Data Analysis · Algorithms · SQL", 8, "Analyze real enterprise ledgers and audit trail data pipeline challenges.", emptyList()),
        Company(4, "Discovery", "Tech · Data · AI/ML", 6, "Build healthcare claim validation and Vitality reward algorithms.", emptyList()),
        Company(5, "FNB", "Software Engineering · APIs · Mobile", 5, "Develop secure eWallet and banking transaction stream APIs.", emptyList()),
        Company(6, "Capitec", "Backend · Java · Problem Solving", 4, "Build resilient real-time payment processing microservices in Java.", emptyList())
    )

    private lateinit var adapter: CompanyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompanyChallengesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        adapter = CompanyAdapter(companyList) { company ->
            // Open company detail screen for that specific company
            (requireActivity() as MainActivity).open(
                CompanyDetailFragment.newInstance(company.name, company.description)
            )
        }
        binding.rvCompany.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applyFilter(query: String) {
        val filtered = companyList.filter { c ->
            c.name.contains(query, ignoreCase = true) || c.tags.contains(query, ignoreCase = true)
        }
        adapter.submit(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
