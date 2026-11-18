package za.co.eduvos.codebridge.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.eduvos.codebridge.core.data.mapper.ProblemMapper
import za.co.eduvos.codebridge.core.database.AppDatabase
import za.co.eduvos.codebridge.core.network.RetrofitClient
import za.co.eduvos.codebridge.core.network.dto.ProblemDto
import za.co.eduvos.codebridge.core.sandbox.ChallengeClock
import za.co.eduvos.codebridge.core.sandbox.TestSuiteParser
import java.util.concurrent.Executors

data class ProblemDetail(
    val id: Int,
    val title: String,
    val category: String,
    val difficulty: String,
    val description: String,
    val example1Input: String,
    val example1Output: String,
    val example2Input: String,
    val example2Output: String,
    val example3Input: String,
    val example3Output: String,
    val entryPoint: String,
    val initialCode: String,
    val testCasesJson: String,
    val timeLimitMinutes: Int = ChallengeClock.DEFAULT_EASY_MINUTES,
    /** Speech questions. The problem's own, else the default ones. */
    val interviewQuestions: List<String> = ProblemDetailFactory.DEFAULT_INTERVIEW_QUESTIONS,
    val totalAttempts: Int = 0,
    val successRate: String = "0%"
)

object ProblemDetailFactory {
    private const val DEFAULT_ENTRY_POINT = "solve"

    /** Questions that fit any problem. A problem can set its own with "interviewQuestions". */
    val DEFAULT_INTERVIEW_QUESTIONS = listOf(
        "Explain your overall approach to solving this problem",
        "Why did you choose this data structure or algorithm?",
        "What is the time and space complexity of your solution?"
    )

    fun fromDto(dto: ProblemDto): ProblemDetail {
        val base = create(dto.problemId, dto.title, dto.difficultyLevel, dto.testCasesJson)
        return base.copy(
            description = if (dto.description.isNullOrBlank()) base.description else dto.description,
            example1Input = if (dto.exampleInput.isNullOrBlank()) base.example1Input else dto.exampleInput,
            example1Output = if (dto.exampleOutput.isNullOrBlank()) base.example1Output else dto.exampleOutput,
            initialCode = if (dto.initialCode.isNullOrBlank()) base.initialCode else dto.initialCode
        )
    }

    fun create(id: Int, title: String?, difficulty: String?, testCasesJson: String?): ProblemDetail {
        val json = testCasesJson.orEmpty()
        val suite = try {
            TestSuiteParser.parse(json)
        } catch (e: TestSuiteParser.ParseException) {
            null
        }
        val entryPoint = suite?.entryPoint ?: DEFAULT_ENTRY_POINT
        val shown = suite?.visibleCases().orEmpty()
        val first = shown.firstOrNull()
        val second = shown.getOrNull(1) ?: first
        val third = shown.getOrNull(2) ?: second // 🟢 Restored

        val argNames = (1..maxOf(1, countArgs(first?.input.orEmpty()))).joinToString(", ") { "arg$it" }

        return ProblemDetail(
            id = id,
            title = title.orEmpty().ifBlank { "Problem $id" },
            category = "General",
            difficulty = difficulty.orEmpty().ifBlank { "Easy" },
            description = "Write a function named $entryPoint that returns the expected output for each test case.",
            example1Input = first?.input ?: "-",
            example1Output = first?.expected ?: "-",
            example2Input = second?.input ?: "-",
            example2Output = second?.expected ?: "-",
            example3Input = third?.input ?: "-", // 🟢 Restored
            example3Output = third?.expected ?: "-", // 🟢 Restored
            entryPoint = entryPoint,
            initialCode = "function $entryPoint($argNames) {\n  // Write your solution here\n}",
            testCasesJson = json,
            timeLimitMinutes = suite?.timeLimitMinutes?.takeIf { it > 0 }
                ?: ChallengeClock.defaultMinutes(difficulty),
            interviewQuestions = suite?.interviewQuestions?.takeIf { it.isNotEmpty() }
                ?: DEFAULT_INTERVIEW_QUESTIONS
        )
    }

    internal fun countArgs(input: String): Int {
        if (input.isBlank()) return 0
        var depth = 0
        var inString = false
        var quote = ' '
        var count = 1
        var i = 0
        while (i < input.length) {
            val c = input[i]
            if (inString) {
                if (c == '\\') i++ else if (c == quote) inString = false
            } else when (c) {
                '"', '\'' -> { inString = true; quote = c }
                '[', '{', '(' -> depth++
                ']', '}', ')' -> depth--
                ',' -> if (depth == 0) count++
            }
            i++
        }
        return count
    }
}

object ProblemRepository {
    @Volatile
    var problems: List<ProblemDetail> = emptyList()
        private set

    private val io = Executors.newSingleThreadExecutor()
    private val main by lazy { Handler(Looper.getMainLooper()) }

    fun getById(id: Int): ProblemDetail? = problems.find { it.id == id }

    fun load(context: Context, onResult: (List<ProblemDetail>, Boolean, String?) -> Unit) {
        val app = context.applicationContext
        RetrofitClient.getInstance().apiService.getProblems().enqueue(object : Callback<List<ProblemDto>> {
            override fun onResponse(call: Call<List<ProblemDto>>, response: Response<List<ProblemDto>>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    val details = body.map { ProblemDetailFactory.fromDto(it) }
                    problems = details
                    io.execute {
                        AppDatabase.getInstance(app).problemDao().insertAll(body.map { ProblemMapper.toEntity(it) })
                    }
                    onResult(details, false, null)
                } else {
                    loadFromCache(app, "Server error ${response.code()}", onResult)
                }
            }

            override fun onFailure(call: Call<List<ProblemDto>>, t: Throwable) {
                loadFromCache(app, t.message ?: "Network error", onResult)
            }
        })
    }

    private fun loadFromCache(app: Context, error: String, onResult: (List<ProblemDetail>, Boolean, String?) -> Unit) {
        io.execute {
            val cached = AppDatabase.getInstance(app).problemDao().all.map {
                ProblemDetailFactory.create(it.problemId, it.title, it.difficultyLevel, it.testCasesJson)
            }
            if (cached.isNotEmpty()) problems = cached
            main.post { onResult(cached, true, error) }
        }
    }
}