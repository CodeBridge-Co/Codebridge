package za.co.eduvos.codebridge.core.network.dto;
import com.google.gson.annotations.SerializedName;

public class ProblemDto {
    @SerializedName("problem_id")
    private int problemId;
    @SerializedName("title")
    private String title;
    @SerializedName("difficulty_level")
    private String difficultyLevel;
    @SerializedName("test_cases_json")
    private String testCasesJson;

    public ProblemDto() {}

    // Getters and Setters
    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public String getTestCasesJson() { return testCasesJson; }
    public void setTestCasesJson(String testCasesJson) { this.testCasesJson = testCasesJson; }
}
