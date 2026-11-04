package za.co.eduvos.codebridge.feature.company

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.MainActivity
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.dto.CompanyDto
import za.co.eduvos.codebridge.core.network.dto.ProblemDto
import za.co.eduvos.codebridge.data.Company
import za.co.eduvos.codebridge.databinding.FragmentCompanyChallengesBinding

class CompanyChallengesFragment : Fragment() {

    private var _binding: FragmentCompanyChallengesBinding? = null
    private val binding get() = _binding!!

    private var companyList = listOf<Company>()
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
            (requireActivity() as MainActivity).open(
                CompanyDetailFragment.newInstance(company.id, company.name, company.description)
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

        fetchLiveData()
    }

    private fun fetchLiveData() {
        // 1. Fetch all problems first so we can count them per company
        RetrofitClient.getInstance().apiService.getProblems().enqueue(object : Callback<List<ProblemDto>> {
            override fun onResponse(call: Call<List<ProblemDto>>, response: Response<List<ProblemDto>>) {
                val allProblems = if (response.isSuccessful) response.body() ?: emptyList() else emptyList()
                
                // 2. Fetch companies
                RetrofitClient.getInstance().apiService.getCompanies().enqueue(object : Callback<List<CompanyDto>> {
                    override fun onResponse(call: Call<List<CompanyDto>>, response: Response<List<CompanyDto>>) {
                        if (_binding == null) return
                        
                        if (response.isSuccessful && response.body() != null) {
                            companyList = response.body()!!.map { dto ->
                                // Count how many problems actually belong to this company
                                val realCount = allProblems.count { it.companyId == dto.companyId }
                                
                                Company(
                                    id = dto.companyId,
                                    name = dto.name,
                                    tags = "Tech Challenges",
                                    challengeCount = realCount, // Real count!
                                    description = "Explore challenges posted by ${dto.name}.",
                                    challenges = emptyList()
                                )
                            }
                            adapter.submit(companyList)
                            
                            if (companyList.isEmpty()) {
                                Toast.makeText(requireContext(), "No companies registered yet.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Failed to load companies", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<List<CompanyDto>>, t: Throwable) {
                        if (_binding == null) return
                        Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onFailure(call: Call<List<ProblemDto>>, t: Throwable) {
                if (_binding == null) return
                Toast.makeText(requireContext(), "Failed to load problems", Toast.LENGTH_SHORT).show()
            }
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
