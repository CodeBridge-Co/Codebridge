package za.co.eduvos.codebridge.core.network.dto;

import com.google.gson.annotations.SerializedName;

public class FeedbackRequest {
    @SerializedName("problem_title") private String problemTitle;
    @SerializedName("difficulty") private String difficulty;
    @SerializedName("time_taken") private String timeTaken;

    public FeedbackRequest(String problemTitle, String difficulty, String timeTaken) {
        this.problemTitle = problemTitle;
        this.difficulty = difficulty;
        this.timeTaken = timeTaken;
    }
}