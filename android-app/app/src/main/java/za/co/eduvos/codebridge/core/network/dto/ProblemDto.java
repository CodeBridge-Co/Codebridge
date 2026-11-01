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

    @SerializedName("description")
    private String description;

    @SerializedName("example_input")
    private String exampleInput;

    @SerializedName("example_output")
    private String exampleOutput;

    @SerializedName("initial_code")
    private String initialCode;

    // Getters and Setters
    public int getProblemId() { return problemId; }
    public void setProblemId(int problemId) { this.problemId = problemId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public String getTestCasesJson() { return testCasesJson; }
    public void setTestCasesJson(String testCasesJson) { this.testCasesJson = testCasesJson; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExampleInput() { return exampleInput; }
    public void setExampleInput(String exampleInput) { this.exampleInput = exampleInput; }

    public String getExampleOutput() { return exampleOutput; }
    public void setExampleOutput(String exampleOutput) { this.exampleOutput = exampleOutput; }

    public String getInitialCode() { return initialCode; }
    public void setInitialCode(String initialCode) { this.initialCode = initialCode; }
}
