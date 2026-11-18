package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class ProblemCreate {
    @SerializedName("title") private String title;
    @SerializedName("difficulty_level") private String difficultyLevel;
    @SerializedName("test_cases_json") private String testCasesJson;
    @SerializedName("description") private String description;
    @SerializedName("example_input") private String exampleInput;
    @SerializedName("example_output") private String exampleOutput;
    @SerializedName("initial_code") private String initialCode;

    public ProblemCreate(String title, String difficultyLevel, String testCasesJson, String description, String exampleInput, String exampleOutput, String initialCode) {
        this.title = title;
        this.difficultyLevel = difficultyLevel;
        this.testCasesJson = testCasesJson;
        this.description = description;
        this.exampleInput = exampleInput;
        this.exampleOutput = exampleOutput;
        this.initialCode = initialCode;
    }
}