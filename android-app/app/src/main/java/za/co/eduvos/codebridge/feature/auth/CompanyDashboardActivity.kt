package za.co.eduvos.codebridge.feature.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.R
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.TokenManager
import za.co.eduvos.codebridge.core.network.dto.EventCreate
import za.co.eduvos.codebridge.core.network.dto.LeaderboardDto
import za.co.eduvos.codebridge.core.network.dto.ProblemCreate
import za.co.eduvos.codebridge.databinding.ActivityCompanyDashboardBinding

class CompanyDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchMetrics()

        binding.btnLogout.setOnClickListener {
            TokenManager.clear()
            val prefs = getSharedPreferences("codebridge_prefs", Context.MODE_PRIVATE)
            prefs.edit().remove("user_role").apply()

            val intent = Intent(this, AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.btnPostChallenge.setOnClickListener { showPostChallengeDialog() }
        binding.btnPostEvent.setOnClickListener { showPostEventDialog() }
    }

    private fun fetchMetrics() {
        RetrofitClient.getInstance().apiService.getLeaderboard().enqueue(object : Callback<List<LeaderboardDto>> {
            override fun onResponse(call: Call<List<LeaderboardDto>>, response: Response<List<LeaderboardDto>>) {
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    binding.tvTotalUsers.text = list.size.toString()
                    val totalXp = list.sumOf { it.xp }
                    binding.tvTotalXp.text = totalXp.toString()
                }
            }
            override fun onFailure(call: Call<List<LeaderboardDto>>, t: Throwable) {
                Toast.makeText(this@CompanyDashboardActivity, "Failed to load metrics", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showPostChallengeDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_post_challenge, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etPostTitle)
        val etDesc = dialogView.findViewById<TextInputEditText>(R.id.etPostDesc)
        val etExampleIn = dialogView.findViewById<TextInputEditText>(R.id.etPostExampleIn)
        val etExampleOut = dialogView.findViewById<TextInputEditText>(R.id.etPostExampleOut)
        val etCode = dialogView.findViewById<TextInputEditText>(R.id.etPostCode)
        val etTests = dialogView.findViewById<TextInputEditText>(R.id.etPostTests)

        val spinnerDifficulty = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerDifficulty)
        val difficulties = arrayOf("Easy", "Medium", "Hard")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, difficulties)
        spinnerDifficulty.setAdapter(adapter)

        AlertDialog.Builder(this)
            .setTitle("Post New Challenge")
            .setView(dialogView)
            .setPositiveButton("Publish") { _, _ ->
                val title = etTitle.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                val difficulty = spinnerDifficulty.text.toString().trim()
                val exampleIn = etExampleIn.text.toString().trim()
                val exampleOut = etExampleOut.text.toString().trim()
                val code = etCode.text.toString().trim()
                val tests = etTests.text.toString().trim()

                if (title.isNotEmpty() && tests.isNotEmpty() && code.isNotEmpty()) {
                    val newProblem = ProblemCreate(title, difficulty, tests, desc, exampleIn, exampleOut, code)

                    RetrofitClient.getInstance().apiService.createProblem(newProblem).enqueue(object : Callback<za.co.eduvos.codebridge.core.network.dto.ProblemDto> {
                        override fun onResponse(call: Call<za.co.eduvos.codebridge.core.network.dto.ProblemDto>, response: Response<za.co.eduvos.codebridge.core.network.dto.ProblemDto>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@CompanyDashboardActivity, "Challenge Posted!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this@CompanyDashboardActivity, "Failed to post", Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onFailure(call: Call<za.co.eduvos.codebridge.core.network.dto.ProblemDto>, t: Throwable) {
                            Toast.makeText(this@CompanyDashboardActivity, "Network Error", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "Title, Tests, and Code are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPostEventDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_post_event, null)

        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etEventTitle)
        val etDate = dialogView.findViewById<TextInputEditText>(R.id.etEventDate)

        val spinnerCategory = dialogView.findViewById<AutoCompleteTextView>(R.id.spinnerCategory)
        val categories = arrayOf("Hackathon", "Workshop", "Tech Talk")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        spinnerCategory.setAdapter(adapter)

        AlertDialog.Builder(this)
            .setTitle("Post New Event")
            .setView(dialogView)
            .setPositiveButton("Publish") { _, _ ->
                val title = etTitle.text.toString().trim()
                val date = etDate.text.toString().trim()
                val category = spinnerCategory.text.toString().trim()

                if (title.isNotEmpty() && date.isNotEmpty()) {
                    val newEvent = EventCreate(title, date, category)

                    RetrofitClient.getInstance().apiService.createEvent(newEvent).enqueue(object : Callback<za.co.eduvos.codebridge.core.network.dto.EventDto> {
                        override fun onResponse(call: Call<za.co.eduvos.codebridge.core.network.dto.EventDto>, response: Response<za.co.eduvos.codebridge.core.network.dto.EventDto>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@CompanyDashboardActivity, "Event Posted!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this@CompanyDashboardActivity, "Failed to post", Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onFailure(call: Call<za.co.eduvos.codebridge.core.network.dto.EventDto>, t: Throwable) {
                            Toast.makeText(this@CompanyDashboardActivity, "Network Error", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    Toast.makeText(this, "Title and Date are required", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}