package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class ProblemDto {
    @SerializedName("problem_id") private int problemId;
    @SerializedName("title") private String title;
    @SerializedName("difficulty_level") private String difficultyLevel;
    @SerializedName("test_cases_json") private String testCasesJson;
    @SerializedName("description") private String description;
    @SerializedName("example_input") private String exampleInput;
    @SerializedName("example_output") private String exampleOutput;
    @SerializedName("initial_code") private String initialCode;
    @SerializedName("company_id") private int companyId;

    // GETTERS
    public int getProblemId() { return problemId; }
    public String getTitle() { return title; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public String getTestCasesJson() { return testCasesJson; }
    public String getDescription() { return description; }
    public String getExampleInput() { return exampleInput; }
    public String getExampleOutput() { return exampleOutput; }
    public String getInitialCode() { return initialCode; }
    public int getCompanyId() { return companyId; }

    // SETTERS
    public void setProblemId(int problemId) { this.problemId = problemId; }
    public void setTitle(String title) { this.title = title; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public void setTestCasesJson(String testCasesJson) { this.testCasesJson = testCasesJson; }
    public void setDescription(String description) { this.description = description; }
    public void setExampleInput(String exampleInput) { this.exampleInput = exampleInput; }
    public void setExampleOutput(String exampleOutput) { this.exampleOutput = exampleOutput; }
    public void setInitialCode(String initialCode) { this.initialCode = initialCode; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }
}
